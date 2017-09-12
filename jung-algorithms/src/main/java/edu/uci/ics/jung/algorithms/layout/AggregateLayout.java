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
package edu.uci.ics.jung.algorithms.layout;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A {@code Layout} implementation that combines multiple other layouts so that they may be
 * manipulated as one layout. The relaxer thread will step each layout in sequence.
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 * @param <N> the node type
 * @param <E> the edge type
 */
public class AggregateLayout<N> implements Layout<N>, IterativeContext {

  protected Layout<N> delegate;
  protected Map<Layout<N>, Point2D> layouts = new HashMap<Layout<N>, Point2D>();

  /**
   * Creates an instance backed by the specified {@code delegate}.
   *
   * @param delegate the layout to which this instance is delegating
   */
  public AggregateLayout(Layout<N> delegate) {
    this.delegate = delegate;
  }

  /** @return the delegate */
  public Layout<N> getDelegate() {
    return delegate;
  }

  /** @param delegate the delegate to set */
  public void setDelegate(Layout<N> delegate) {
    this.delegate = delegate;
  }

  /**
   * Adds the passed layout as a sublayout, and specifies the center of where this sublayout should
   * appear.
   *
   * @param layout the layout algorithm to use as a sublayout
   * @param center the center of the coordinates for the sublayout
   */
  public void put(Layout<N> layout, Point2D center) {
    layouts.put(layout, center);
  }

  /**
   * @param layout the layout whose center is to be returned
   * @return the center of the passed layout
   */
  public Point2D get(Layout<N> layout) {
    return layouts.get(layout);
  }

  /**
   * Removes {@code layout} from this instance.
   *
   * @param layout the layout to remove
   */
  public void remove(Layout<N> layout) {
    layouts.remove(layout);
  }

  /** Removes all layouts from this instance. */
  public void removeAll() {
    layouts.clear();
  }

  public Set<N> nodes() {
    return delegate.nodes();
  }

  public Dimension getSize() {
    return delegate.getSize();
  }

  public void initialize() {
    delegate.initialize();
    for (Layout<N> layout : layouts.keySet()) {
      layout.initialize();
    }
  }

  /**
   * @param v the node whose locked state is to be returned
   * @return true if v is locked in any of the layouts, and false otherwise
   */
  public boolean isLocked(N node) {
    for (Layout<N> layout : layouts.keySet()) {
      if (layout.isLocked(node)) {
        return true;
      }
    }
    return delegate.isLocked(node);
  }

  /**
   * Locks this node in the main layout and in any sublayouts whose graph contains this node.
   *
   * @param v the node whose locked state is to be set
   * @param state {@code true} if the node is to be locked, and {@code false} if unlocked
   */
  public void lock(N node, boolean state) {
    for (Layout<N> layout : layouts.keySet()) {
      if (layout.nodes().contains(node)) {
        layout.lock(node, state);
      }
    }
    delegate.lock(node, state);
  }

  public void reset() {
    for (Layout<N> layout : layouts.keySet()) {
      layout.reset();
    }
    delegate.reset();
  }

  public void setInitializer(Function<N, Point2D> initializer) {
    delegate.setInitializer(initializer);
  }

  public void setLocation(N node, Point2D location) {
    boolean wasInSublayout = false;
    for (Layout<N> layout : layouts.keySet()) {
      if (layout.nodes().contains(node)) {
        Point2D center = layouts.get(layout);
        // transform by the layout itself, but offset to the
        // center of the sublayout
        Dimension d = layout.getSize();

        AffineTransform at =
            AffineTransform.getTranslateInstance(
                -center.getX() + d.width / 2, -center.getY() + d.height / 2);
        Point2D localLocation = at.transform(location, null);
        layout.setLocation(node, localLocation);
        wasInSublayout = true;
      }
    }
    if (wasInSublayout == false && nodes().contains(node)) {
      delegate.setLocation(node, location);
    }
  }

  public void setSize(Dimension d) {
    delegate.setSize(d);
  }

  /** @return a map from each {@code Layout} instance to its center point. */
  public Map<Layout<N>, Point2D> getLayouts() {
    return layouts;
  }

  /**
   * Returns the location of the node. The location is specified first by the sublayouts, and then
   * by the base layout if no sublayouts operate on this node.
   *
   * @return the location of the node
   */
  public Point2D apply(N node) {
    boolean wasInSublayout = false;
    for (Layout<N> layout : layouts.keySet()) {
      if (layout.nodes().contains(node)) {
        wasInSublayout = true;
        Point2D center = layouts.get(layout);
        // transform by the layout itself, but offset to the
        // center of the sublayout
        Dimension d = layout.getSize();
        AffineTransform at =
            AffineTransform.getTranslateInstance(
                center.getX() - d.width / 2, center.getY() - d.height / 2);
        return at.transform(layout.apply(node), null);
      }
    }
    if (wasInSublayout == false) {
      return delegate.apply(node);
    }
    return null;
  }

  /** @return {@code true} iff the delegate layout and all sublayouts are done */
  public boolean done() {
    for (Layout<N> layout : layouts.keySet()) {
      if (layout instanceof IterativeContext) {
        if (!((IterativeContext) layout).done()) {
          return false;
        }
      }
    }
    if (delegate instanceof IterativeContext) {
      return ((IterativeContext) delegate).done();
    }
    return true;
  }

  /** Call step on any sublayout that is also an IterativeContext and is not done */
  public void step() {
    for (Layout<N> layout : layouts.keySet()) {
      if (layout instanceof IterativeContext) {
        IterativeContext context = (IterativeContext) layout;
        if (context.done() == false) {
          context.step();
        }
      }
    }
    if (delegate instanceof IterativeContext) {
      IterativeContext context = (IterativeContext) delegate;
      if (context.done() == false) {
        context.step();
      }
    }
  }
}
