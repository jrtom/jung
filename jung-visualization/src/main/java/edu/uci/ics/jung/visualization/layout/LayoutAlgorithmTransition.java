package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.layout.util.Relaxer;
import edu.uci.ics.jung.visualization.util.Animator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * unused for now
 *
 * @param <N>
 * @param <E>
 * @param <P>
 */
public class LayoutAlgorithmTransition<N, E, P> implements IterativeContext {

  private static Logger log = LoggerFactory.getLogger(LayoutAlgorithmTransition.class);

  protected LayoutAlgorithm<N, P> endLayoutAlgorithm;
  protected LayoutModel<N, P> transitionLayoutModel;
  protected LayoutModel<N, P> initialLayoutModel;
  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;
  protected LayoutModel<N, P> layoutModel;
  protected VisualizationModel<N, E, P> visualizationModel;
  protected DomainModel<P> domainModel;

  public static <N, E, P> void animate(
      VisualizationModel<N, E, P> visualizationModel, LayoutAlgorithm<N, P> endLayoutAlgorithm) {
    new LayoutAlgorithmTransition(visualizationModel, endLayoutAlgorithm);
  }

  public static <N, E, P> void apply(
      VisualizationModel<N, E, P> visualizationModel, LayoutAlgorithm<N, P> endLayoutAlgorithm) {
    visualizationModel.setLayoutAlgorithm(endLayoutAlgorithm);
  }

  private LayoutAlgorithmTransition(
      VisualizationModel<N, E, P> visualizationModel, LayoutAlgorithm<N, P> endLayoutAlgorithm) {
    if (log.isTraceEnabled()) {
      log.trace(
          "transition from {} to {}", visualizationModel.getLayoutAlgorithm(), endLayoutAlgorithm);
    }
    this.visualizationModel = visualizationModel;
    this.layoutModel = visualizationModel.getLayoutModel();
    // stop any relaxing that is going on now
    Relaxer relaxer = this.layoutModel.getRelaxer();
    if (relaxer != null) {
      relaxer.stop();
    }
    this.domainModel = layoutModel.getDomainModel();
    LayoutAlgorithm<N, P> transitionLayoutAlgorithm = new StaticLayoutAlgorithm(domainModel);
    visualizationModel.setLayoutAlgorithm(transitionLayoutAlgorithm);

    this.initialLayoutModel =
        new LoadingCacheLayoutModel<N, P>(
            visualizationModel.getNetwork().asGraph(),
            layoutModel.getDomainModel(),
            layoutModel.getWidth(),
            layoutModel.getHeight());
    initialLayoutModel.setInitializer(layoutModel);

    // the layout model still has locations from its previous algor
    this.transitionLayoutModel =
        new LoadingCacheLayoutModel<N, P>(
            visualizationModel.getNetwork().asGraph(),
            layoutModel.getDomainModel(),
            layoutModel.getWidth(),
            layoutModel.getHeight());

    transitionLayoutModel.accept(endLayoutAlgorithm);
    this.endLayoutAlgorithm = endLayoutAlgorithm;
    Animator animator = new Animator(this);
    animator.start();
  }

  public boolean done() {
    return done;
  }

  public void step() {
    for (N v : visualizationModel.getNetwork().nodes()) {
      P tp = initialLayoutModel.apply(v);
      P fp = transitionLayoutModel.apply(v);
      double dx = (domainModel.getX(fp) - domainModel.getX(tp)) / (count - counter);
      double dy = (domainModel.getY(fp) - domainModel.getY(tp)) / (count - counter);
      layoutModel.set(v, domainModel.getX(tp) + dx, domainModel.getY(tp) + dy);
    }
    counter++;
    if (counter >= count) {
      done = true;
      this.transitionLayoutModel.stopRelaxer();
      this.visualizationModel.setLayoutAlgorithm(endLayoutAlgorithm);
    }
  }
}
