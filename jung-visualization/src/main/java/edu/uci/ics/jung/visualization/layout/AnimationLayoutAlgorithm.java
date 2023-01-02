package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.algorithms.AbstractIterativeLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.visualization.VisualizationServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Nelson
 */
public class AnimationLayoutAlgorithm<N> extends AbstractIterativeLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(AnimationLayoutAlgorithm.class);

  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;

  LayoutModel<N> transitionLayoutModel;
  VisualizationServer<N, ?> visualizationServer;
  LayoutAlgorithm<N> endLayoutAlgorithm;
  LayoutModel<N> layoutModel;

  public AnimationLayoutAlgorithm(
      VisualizationServer<N, ?> visualizationServer, LayoutAlgorithm<N> endLayoutAlgorithm) {
    this.visualizationServer = visualizationServer;
    this.endLayoutAlgorithm = endLayoutAlgorithm;
    this.shouldPreRelax = false;
  }

  public void visit(LayoutModel<N> layoutModel) {
    // save off the existing layoutModel
    this.layoutModel = layoutModel;
    // create a LayoutModel to hold points for the transition
    this.transitionLayoutModel =
        LoadingCacheLayoutModel.<N>builder()
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
      Point tp = layoutModel.apply(v);
      Point fp = transitionLayoutModel.apply(v);
      double dx = (fp.x - tp.x) / (count - counter);
      double dy = (fp.y - tp.y) / (count - counter);
      log.trace("dx:{},dy:{}", dx, dy);
      layoutModel.set(v, tp.x + dx, tp.y + dy);
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
