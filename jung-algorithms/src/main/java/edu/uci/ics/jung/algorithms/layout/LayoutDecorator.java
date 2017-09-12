/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */

package edu.uci.ics.jung.algorithms.layout;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.function.Function;

/**
 * a pure decorator for the Layout interface. Intended to be overridden to provide specific behavior
 * decoration
 *
 * @author Tom Nelson
 */
public abstract class LayoutDecorator<N> implements Layout<N>, IterativeContext {

  protected Layout<N> delegate;

  /**
   * Creates an instance backed by the specified {@code delegate}.
   *
   * @param delegate the layout to which this instance is delegating
   */
  public LayoutDecorator(Layout<N> delegate) {
    this.delegate = delegate;
  }

  /** @return the backing (delegate) layout. */
  public Layout<N> getDelegate() {
    return delegate;
  }

  public void setDelegate(Layout<N> delegate) {
    this.delegate = delegate;
  }

  public void step() {
    if (delegate instanceof IterativeContext) {
      ((IterativeContext) delegate).step();
    }
  }

  public void initialize() {
    delegate.initialize();
  }

  public void setInitializer(Function<N, Point2D> initializer) {
    delegate.setInitializer(initializer);
  }

  public void setLocation(N node, Point2D location) {
    delegate.setLocation(node, location);
  }

  public Dimension getSize() {
    return delegate.getSize();
  }

  public Point2D transform(N node) {
    return delegate.apply(node);
  }

  public boolean done() {
    if (delegate instanceof IterativeContext) {
      return ((IterativeContext) delegate).done();
    }
    return true;
  }

  public void lock(N node, boolean state) {
    delegate.lock(node, state);
  }

  public boolean isLocked(N node) {
    return delegate.isLocked(node);
  }

  public void setSize(Dimension d) {
    delegate.setSize(d);
  }

  public void reset() {
    delegate.reset();
  }
}
