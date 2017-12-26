package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.algorithms.AbstractLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class AnimationLayoutAlgorithm<N> extends AbstractLayoutAlgorithm<N, Point2D>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(AnimationLayoutAlgorithm.class);

  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;

  LayoutModel<N, Point2D> transitionLayoutModel;
  VisualizationServer<N, ?> visualizationServer;
  LayoutAlgorithm<N, Point2D> endLayoutAlgorithm;
  LayoutModel<N, Point2D> layoutModel;

  public AnimationLayoutAlgorithm(
      VisualizationServer<N, ?> visualizationServer,
      LayoutAlgorithm<N, Point2D> endLayoutAlgorithm) {
    this.visualizationServer = visualizationServer;
    this.endLayoutAlgorithm = endLayoutAlgorithm;
  }

  public void visit(LayoutModel<N, Point2D> layoutModel) {
    super.visit(layoutModel);
    // save off the existing layoutModel
    this.layoutModel = layoutModel;
    // create a LayoutModel to hold points for the transition
    this.transitionLayoutModel =
        LoadingCacheLayoutModel.<N, Point2D>builder()
            .setGraph(visualizationServer.getModel().getNetwork().asGraph())
            .setLayoutModel(layoutModel)
            .setInitializer(layoutModel)
            .build();
    // start off the transitionLayoutModel with the endLayoutAlgorithm
    transitionLayoutModel.accept(endLayoutAlgorithm);
  }

  /**
   * each step of the animation moves every pouit 1/count of the distance from its old location to
   * its new location
   */
  public void step() {
    for (N v : layoutModel.getGraph().nodes()) {
      Point2D tp = layoutModel.apply(v);
      Point2D fp = transitionLayoutModel.apply(v);
      double dx = (fp.getX() - tp.getX()) / (count - counter);
      double dy = (fp.getY() - tp.getY()) / (count - counter);
      log.trace("dx:{},dy:{}", dx, dy);
      layoutModel.set(v, tp.getX() + dx, tp.getY() + dy);
    }
    counter++;
    if (counter >= count) {
      done = true;
      this.transitionLayoutModel.stopRelaxer();
      this.visualizationServer.getModel().setLayoutAlgorithm(endLayoutAlgorithm);
    }
  }

  public boolean done() {
    return done;
  }
}
