package edu.uci.ics.jung.layout.util;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LayoutModelAware;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the transition to a new LayoutAlgorithm. The transition can me animated or immediate.
 *
 * @param <N>
 * @param <E>
 * @param <P>
 */
public class LayoutAlgorithmTransition<N, E, P> implements IterativeContext {

  private static Logger log = LoggerFactory.getLogger(LayoutAlgorithmTransition.class);

  protected LayoutAlgorithm<N, P> endLayoutAlgorithm;
  protected LayoutModel<N, P> transitionLayoutModel;
  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;
  protected LayoutModel<N, P> layoutModel;
  protected LayoutModelAware<N, E, P> layoutModelAware;
  protected PointModel<P> pointModel;
  protected Animator animator;

  public static <N, E, P> void animate(
      LayoutModelAware<N, E, P> visualizationModel, LayoutAlgorithm<N, P> endLayoutAlgorithm) {
    new LayoutAlgorithmTransition(visualizationModel, endLayoutAlgorithm);
  }

  public static <N, E, P> void apply(
      LayoutModelAware<N, E, P> visualizationModel, LayoutAlgorithm<N, P> endLayoutAlgorithm) {
    visualizationModel.setLayoutAlgorithm(endLayoutAlgorithm);
  }

  private LayoutAlgorithmTransition(
      LayoutModelAware<N, E, P> visualizationModel, LayoutAlgorithm<N, P> endLayoutAlgorithm) {
    if (log.isTraceEnabled()) {
      log.trace(
          "transition from {} to {}", visualizationModel.getLayoutAlgorithm(), endLayoutAlgorithm);
    }
    this.layoutModelAware = visualizationModel;
    this.layoutModel = visualizationModel.getLayoutModel();
    // stop any relaxing that is going on now
    layoutModel.stopRelaxer();
    this.pointModel = layoutModel.getPointModel();
    LayoutAlgorithm<N, P> transitionLayoutAlgorithm = new StaticLayoutAlgorithm();
    visualizationModel.setLayoutAlgorithm(transitionLayoutAlgorithm);

    // the layout model still has locations from its previous algorithm
    this.transitionLayoutModel =
        LoadingCacheLayoutModel.<N, P>builder()
            .setGraph(visualizationModel.getNetwork().asGraph())
            .setLayoutModel(layoutModel)
            .build();

    transitionLayoutModel.accept(endLayoutAlgorithm);
    this.endLayoutAlgorithm = endLayoutAlgorithm;
    if (animator != null) {
      animator.stop();
    }
    animator = new Animator(this);
    animator.start();
  }

  public boolean done() {
    return done;
  }

  public void step() {
    for (N v : layoutModelAware.getNetwork().nodes()) {
      P tp = layoutModel.apply(v);
      P fp = transitionLayoutModel.apply(v);
      double dx = (pointModel.getX(fp) - pointModel.getX(tp)) / (count - counter);
      double dy = (pointModel.getY(fp) - pointModel.getY(tp)) / (count - counter);
      double dz = (pointModel.getZ(fp) - pointModel.getZ(tp)) / (count - counter);
      layoutModel.set(
          v, pointModel.getX(tp) + dx, pointModel.getY(tp) + dy, pointModel.getZ(tp) + dz);
    }
    counter++;
    if (counter >= count) {
      done = true;
      this.transitionLayoutModel.stopRelaxer();
      this.layoutModelAware.setLayoutAlgorithm(endLayoutAlgorithm);
    }
  }
}
