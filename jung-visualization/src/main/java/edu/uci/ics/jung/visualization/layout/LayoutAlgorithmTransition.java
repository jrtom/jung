package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.layout.util.VisRunner;
import java.awt.*;
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
  //  protected LayoutAlgorithm<N, P> startLayoutAlgorithm;
  protected LayoutAlgorithm<N, P> endLayoutAlgorithm;
  //  protected LayoutAlgorithm<N, P> transitionLayoutAlgorithm;
  protected LayoutModel<N, P> transitionLayoutModel;
  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;
  protected LayoutModel<N, P> layoutModel;
  protected VisualizationModel<N, E, P> visualizationModel;
  protected DomainModel<P> domainModel;

  public LayoutAlgorithmTransition(
      VisualizationModel<N, E, P> visualizationModel, LayoutAlgorithm<N, P> endLayoutAlgorithm) {
    log.info("transition to " + endLayoutAlgorithm);
    this.visualizationModel = visualizationModel;
    this.layoutModel = visualizationModel.getLayoutModel();
    log.info("current LayoutAlgorithm is " + visualizationModel.getLayoutAlgorithm());
    this.domainModel = layoutModel.getDomainModel();
    LayoutModel<N, P> currentLayoutModel = visualizationModel.getLayoutModel();
    LayoutAlgorithm<N, P> transitionLayoutAlgorithm = new StaticLayoutAlgorithm(domainModel);
    this.transitionLayoutModel =
        new LoadingCacheLayoutModel<N, P>(
            visualizationModel.getNetwork().asGraph(),
            layoutModel.getDomainModel(),
            layoutModel.getWidth(),
            layoutModel.getHeight());
    transitionLayoutModel.setInitializer(currentLayoutModel);

    transitionLayoutModel.accept(transitionLayoutAlgorithm);
    this.endLayoutAlgorithm = endLayoutAlgorithm;
    VisRunner visRunner = new VisRunner(this);
    visRunner.setSleepTime(1000);
    visRunner.relax();
  }

  public boolean done() {
    return done;
  }

  public void step() {
    for (N v : visualizationModel.getNetwork().nodes()) {
      P tp = transitionLayoutModel.apply(v);
      P fp = layoutModel.apply(v);
      double dx = (domainModel.getX(fp) - domainModel.getX(tp)) / (count - counter);
      double dy = (domainModel.getY(fp) - domainModel.getY(tp)) / (count - counter);
      transitionLayoutModel.set(v, domainModel.getX(tp) + dx, domainModel.getY(tp) + dy);
    }
    counter++;
    if (counter >= count) {
      done = true;
      this.visualizationModel.setLayoutAlgorithm(endLayoutAlgorithm);
    }
    //    vv.repaint();
  }
}
