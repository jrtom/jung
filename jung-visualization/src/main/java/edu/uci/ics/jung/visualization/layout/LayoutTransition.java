package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.awt.geom.Point2D;

public class LayoutTransition<V, E> implements IterativeContext {

  protected Layout<V> startLayout;
  protected LayoutMediator<V, E> endLayoutMediator;
  protected LayoutMediator<V, E> transitionLayoutMediator;
  protected boolean done = false;
  protected int count = 20;
  protected int counter = 0;
  protected VisualizationViewer<V, E> vv;

  public LayoutTransition(
      VisualizationViewer<V, E> vv, Layout<V> startLayout, LayoutMediator<V, E> endLayoutMediator) {
    this.vv = vv;
    this.startLayout = startLayout;
    Network<V, E> network = endLayoutMediator.getNetwork();
    Layout<V> transitionLayout = new StaticLayout(network.asGraph(), startLayout);
    this.endLayoutMediator = endLayoutMediator;
    if (endLayoutMediator.getLayout() instanceof IterativeContext) {
      Relaxer relaxer = new VisRunner((IterativeContext) endLayoutMediator.getLayout());
      relaxer.prerelax();
    }
    this.transitionLayoutMediator = new LayoutMediator<V, E>(network, transitionLayout);
    vv.setLayoutMediator(transitionLayoutMediator);
  }

  public boolean done() {
    return done;
  }

  public void step() {
    for (V v : vv.getModel().getLayoutMediator().getNetwork().nodes()) {
      Point2D tp = transitionLayoutMediator.getLayout().apply(v);
      Point2D fp = endLayoutMediator.getLayout().apply(v);
      double dx = (fp.getX() - tp.getX()) / (count - counter);
      double dy = (fp.getY() - tp.getY()) / (count - counter);
      transitionLayoutMediator
          .getLayout()
          .setLocation(v, new Point2D.Double(tp.getX() + dx, tp.getY() + dy));
    }
    counter++;
    if (counter >= count) {
      done = true;
      vv.setLayoutMediator(endLayoutMediator);
    }
    vv.repaint();
  }
}
