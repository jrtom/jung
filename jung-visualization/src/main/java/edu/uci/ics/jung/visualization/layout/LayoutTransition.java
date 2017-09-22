package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.geom.Point2D;

public class LayoutTransition<V, E> implements IterativeContext {

  protected Layout<V> startLayout;
  protected Layout<V> endLayout;
  protected Layout<V> transitionLayout;
  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;
  protected VisualizationViewer<V, E> vv;

  public LayoutTransition(
      VisualizationViewer<V, E> vv, Layout<V> startLayout, Layout<V> endLayout) {
    this.vv = vv;
    this.startLayout = startLayout;
    this.endLayout = endLayout;
    if (endLayout instanceof IterativeContext) {
      Relaxer relaxer = new VisRunner((IterativeContext) endLayout);
      relaxer.prerelax();
    }
    this.transitionLayout =
        new StaticLayout<V>(vv.getModel().getLayoutMediator().getNetwork().asGraph(), startLayout);
    vv.setGraphLayout(transitionLayout);
  }

  public boolean done() {
    return done;
  }

  public void step() {
    for (V v : vv.getModel().getLayoutMediator().getNetwork().nodes()) {
      Point2D tp = transitionLayout.apply(v);
      Point2D fp = endLayout.apply(v);
      double dx = (fp.getX() - tp.getX()) / (count - counter);
      double dy = (fp.getY() - tp.getY()) / (count - counter);
      transitionLayout.setLocation(v, new Point2D.Double(tp.getX() + dx, tp.getY() + dy));
    }
    counter++;
    if (counter >= count) {
      done = true;
      vv.setGraphLayout(endLayout);
    }
    vv.repaint();
  }
}
