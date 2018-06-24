/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 *
 */
package edu.uci.ics.jung.visualization.layout;

import com.google.common.collect.Maps;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that combines multiple other layouts so that they may be
 * manipulated as one layout. The relaxer thread will step each layout in sequence.
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public class AggregateLayoutModel<N> implements LayoutModel<N> {

  private static final Logger log = LoggerFactory.getLogger(AggregateLayoutModel.class);
  protected final LayoutModel<N> delegate;
  protected Map<LayoutModel<N>, Point> layouts = Maps.newHashMap();

  /**
   * Creates an instance backed by the specified {@code delegate}.
   *
   * @param delegate the layout to which this instance is delegating
   */
  public AggregateLayoutModel(LayoutModel<N> delegate) {
    this.delegate = delegate;
  }

  /**
   * Adds the passed layout as a sublayout, and specifies the center of where this sublayout should
   * appear.
   *
   * @param layoutModel the layout model to use as a sublayout
   * @param center the center of the coordinates for the sublayout model
   */
  public void put(LayoutModel<N> layoutModel, Point center) {
    if (log.isTraceEnabled()) {
      log.trace("put layout: {} at {}", layoutModel, center);
    }
    layouts.put(layoutModel, center);
    connectListeners(layoutModel);
  }

  private void connectListeners(LayoutModel<N> newLayoutModel) {
    for (LayoutStateChangeListener layoutStateChangeListener :
        delegate.getLayoutStateChangeSupport().getLayoutStateChangeListeners()) {
      newLayoutModel
          .getLayoutStateChangeSupport()
          .addLayoutStateChangeListener(layoutStateChangeListener);
    }

    for (LayoutModel.ChangeListener changeListener :
        delegate.getChangeSupport().getChangeListeners()) {
      newLayoutModel.getChangeSupport().addChangeListener(changeListener);
    }
  }

  private void disconnectListeners(LayoutModel<N> newLayoutModel) {
    newLayoutModel.getLayoutStateChangeSupport().getLayoutStateChangeListeners().clear();
    newLayoutModel.getChangeSupport().getChangeListeners().clear();
  }

  /**
   * @param layout the layout whose center is to be returned
   * @return the center of the passed layout
   */
  public Point get(LayoutModel<N> layout) {
    return layouts.get(layout);
  }

  @Override
  public void accept(LayoutAlgorithm<N> layoutAlgorithm) {
    delegate.accept(layoutAlgorithm);
  }

  @Override
  public Map<N, Point> getLocations() {
    return delegate.getLocations();
  }

  @Override
  public void setSize(int width, int height) {
    delegate.setSize(width, height);
  }

  @Override
  public void stopRelaxer() {
    delegate.stopRelaxer();
    for (LayoutModel<N> childLayoutModel : layouts.keySet()) {
      childLayoutModel.stopRelaxer();
    }
  }

  @Override
  public void setRelaxing(boolean relaxing) {
    delegate.setRelaxing(relaxing);
  }

  @Override
  public boolean isRelaxing() {
    return delegate.isRelaxing();
  }

  @Override
  public CompletableFuture getTheFuture() {
    return delegate.getTheFuture();
  }

  @Override
  public void set(N node, Point location) {
    delegate.set(node, location);
  }

  @Override
  public void set(N node, double x, double y) {
    delegate.set(node, x, y);
  }

  @Override
  public Point get(N node) {
    return delegate.get(node);
  }

  @Override
  public Graph<N> getGraph() {
    return delegate.getGraph();
  }

  @Override
  public void setGraph(Graph<N> graph) {
    delegate.setGraph(graph);
  }

  /**
   * Removes {@code layout} from this instance.
   *
   * @param layout the layout to remove
   */
  public void remove(LayoutModel<N> layout) {
    layouts.remove(layout);
  }

  /** Removes all layouts from this instance. */
  public void removeAll() {
    layouts.clear();
  }

  @Override
  public int getWidth() {
    return delegate.getWidth();
  }

  @Override
  public int getHeight() {
    return delegate.getHeight();
  }

  /**
   * @param node the node whose locked state is to be returned
   * @return true if v is locked in any of the layouts, and false otherwise
   */
  public boolean isLocked(N node) {
    for (LayoutModel<N> layoutModel : layouts.keySet()) {
      if (layoutModel.isLocked(node)) {
        return true;
      }
    }
    return delegate.isLocked(node);
  }

  /**
   * Locks this node in the main layout and in any sublayouts whose graph contains this node.
   *
   * @param node the node whose locked state is to be set
   * @param state {@code true} if the node is to be locked, and {@code false} if unlocked
   */
  public void lock(N node, boolean state) {
    for (LayoutModel<N> layoutModel : layouts.keySet()) {
      if (layoutModel.getGraph().nodes().contains(node)) {
        layoutModel.lock(node, state);
      }
    }
    delegate.lock(node, state);
  }

  @Override
  public void lock(boolean locked) {
    delegate.lock(locked);
    for (LayoutModel model : layouts.keySet()) {
      model.lock(locked);
    }
  }

  @Override
  public boolean isLocked() {
    return delegate.isLocked();
  }

  public void setInitializer(Function<N, Point> initializer) {
    delegate.setInitializer(initializer);
  }

  @Override
  public LayoutStateChangeSupport getLayoutStateChangeSupport() {
    return delegate.getLayoutStateChangeSupport();
  }

  @Override
  public ChangeSupport getChangeSupport() {
    return delegate.getChangeSupport();
  }

  /**
   * Returns the location of the node. The location is specified first by the sublayouts, and then
   * by the base layout if no sublayouts operate on this node.
   *
   * @return the location of the node
   */
  public Point apply(N node) {
    for (LayoutModel<N> layoutModel : layouts.keySet()) {
      if (layoutModel.getGraph().nodes().contains(node)) {
        Point center = layouts.get(layoutModel);
        // transform by the layout itself, but offset to the
        // center of the sublayout
        int width = layoutModel.getWidth();
        int height = layoutModel.getHeight();
        AffineTransform at =
            AffineTransform.getTranslateInstance(center.x - width / 2, center.y - height / 2);
        Point nodeCenter = layoutModel.apply(node);
        log.trace("sublayout center is {}", nodeCenter);
        double[] srcPoints = new double[] {nodeCenter.x, nodeCenter.y};
        double[] destPoints = new double[2];
        at.transform(srcPoints, 0, destPoints, 0, 1);
        return Point.of(destPoints[0], destPoints[1]);
      }
    }
    return delegate.apply(node);
  }
}
