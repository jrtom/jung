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
import edu.uci.ics.jung.algorithms.layout.DomainModel;
import edu.uci.ics.jung.algorithms.layout.LayoutAlgorithm;
import edu.uci.ics.jung.algorithms.layout.LayoutModel;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.algorithms.util.Relaxer;
import java.awt.geom.AffineTransform;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code Layout} implementation that combines multiple other layouts so that they may be
 * manipulated as one layout. The relaxer thread will step each layout in sequence.
 *
 * @author Tom Nelson
 * @param <N> the node type
 * @param <P> the domain point type
 */
public class AggregateLayoutModel<N, P> implements LayoutModel<N, P> {

  private static final Logger log = LoggerFactory.getLogger(AggregateLayoutModel.class);
  protected final LayoutModel<N, P> delegate;
  protected Map<LayoutModel<N, P>, P> layouts = Maps.newHashMap();

  /**
   * Creates an instance backed by the specified {@code delegate}.
   *
   * @param delegate the layout to which this instance is delegating
   */
  public AggregateLayoutModel(LayoutModel<N, P> delegate) {
    this.delegate = delegate;
  }

  /**
   * Adds the passed layout as a sublayout, and specifies the center of where this sublayout should
   * appear.
   *
   * @param layout the layout algorithm to use as a sublayout
   * @param center the center of the coordinates for the sublayout
   */
  public void put(LayoutModel<N, P> layout, P center) {
    if (log.isTraceEnabled()) {
      log.trace("put layout: {} at {}", layout, center);
    }
    layouts.put(layout, center);
  }

  /**
   * @param layout the layout whose center is to be returned
   * @return the center of the passed layout
   */
  public P get(LayoutModel<N, P> layout) {
    return layouts.get(layout);
  }

  @Override
  public void accept(LayoutAlgorithm<N, P> layoutAlgorithm) {
    delegate.accept(layoutAlgorithm);
  }

  @Override
  public DomainModel<P> getDomainModel() {
    return delegate.getDomainModel();
  }

  @Override
  public void setSize(int width, int height) {
    delegate.setSize(width, height);
  }

  @Override
  public Relaxer getRelaxer() {
    return delegate.getRelaxer();
  }

  @Override
  public void stopRelaxer() {
    delegate.stopRelaxer();
    for (LayoutModel<N, P> childLayoutModel : layouts.keySet()) {
      childLayoutModel.stopRelaxer();
    }
  }

  @Override
  public void set(N node, P location) {
    delegate.set(node, location);
  }

  @Override
  public void set(N node, double x, double y) {
    delegate.set(node, x, y);
  }

  @Override
  public void set(N node, P location, boolean forceUpdate) {
    delegate.set(node, location, forceUpdate);
  }

  @Override
  public void set(N node, double x, double y, boolean forceUpdate) {
    delegate.set(node, x, y, forceUpdate);
  }

  @Override
  public P get(N node) {
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
  public void remove(LayoutModel<N, P> layout) {
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
    for (LayoutModel<N, P> layoutModel : layouts.keySet()) {
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
    for (LayoutModel<N, P> layoutModel : layouts.keySet()) {
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

  public void setInitializer(Function<N, P> initializer) {
    delegate.setInitializer(initializer);
  }

  /**
   * Returns the location of the node. The location is specified first by the sublayouts, and then
   * by the base layout if no sublayouts operate on this node.
   *
   * @return the location of the node
   */
  public P apply(N node) {
    DomainModel<P> domainModel = delegate.getDomainModel();
    boolean wasInSublayout = false;
    for (LayoutModel<N, P> layoutModel : layouts.keySet()) {
      if (layoutModel.getGraph().nodes().contains(node)) {
        wasInSublayout = true;
        P center = layouts.get(layoutModel);
        // transform by the layout itself, but offset to the
        // center of the sublayout
        int width = layoutModel.getWidth();
        int height = layoutModel.getHeight();
        AffineTransform at =
            AffineTransform.getTranslateInstance(
                domainModel.getX(center) - width / 2, domainModel.getY(center) - height / 2);
        P nodeCenter = layoutModel.apply(node);
        log.trace("sublayout center is {}", nodeCenter);
        double[] srcPoints =
            new double[] {domainModel.getX(nodeCenter), domainModel.getY(nodeCenter)};
        double[] destPoints = new double[2];
        at.transform(srcPoints, 0, destPoints, 0, 1);
        return domainModel.newPoint(destPoints[0], destPoints[1]);
      }
    }
    if (wasInSublayout == false) {
      return delegate.apply(node);
    }
    return null;
  }

  /** @return {@code true} iff the delegate layout and all sublayouts are done */
  public boolean done() {
    for (LayoutModel<N, P> layoutModel : layouts.keySet()) {
      if (layoutModel instanceof IterativeContext) {
        if (!((IterativeContext) layoutModel).done()) {
          return false;
        }
      }
    }
    if (delegate instanceof IterativeContext) {
      return ((IterativeContext) delegate).done();
    }
    return true;
  }
}
