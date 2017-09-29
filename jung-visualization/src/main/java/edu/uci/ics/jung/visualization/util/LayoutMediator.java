package edu.uci.ics.jung.visualization.util;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.function.Function;

/** Created by Tom Nelson */
public class LayoutMediator implements Layout<Object> {

  private final Network network;

  private final Layout<Object> layout;

  public LayoutMediator(Network network, Layout layout) {
    this.network = network;
    this.layout = layout;
  }

  public Network getNetwork() {
    return network;
  }

  public Layout<Object> getLayout() {
    return layout;
  }

  @Override
  public String toString() {
    return "LayoutMediator{" + "networkNodes=" + network.nodes() + ", layout=" + layout + '}';
  }

  @Override
  public void initialize() {
    layout.initialize();
  }

  @Override
  public void setInitializer(Function<Object, Point2D> initializer) {
    this.layout.setInitializer(initializer);
  }

  @Override
  public Set nodes() {
    return this.layout.nodes();
  }

  @Override
  public void reset() {
    this.layout.reset();
  }

  @Override
  public void setSize(Dimension d) {
    this.layout.setSize(d);
  }

  @Override
  public Dimension getSize() {
    return this.layout.getSize();
  }

  @Override
  public void lock(Object n, boolean state) {
    this.layout.lock(n, state);
  }

  @Override
  public boolean isLocked(Object n) {
    return this.layout.isLocked(n);
  }

  @Override
  public void setLocation(Object n, Point2D location) {
    this.layout.setLocation(n, location);
  }

  @Override
  public Point2D apply(Object n) {
    return this.layout.apply(n);
  }

  public boolean isIterative() {
    return this.layout instanceof IterativeContext;
  }
}
