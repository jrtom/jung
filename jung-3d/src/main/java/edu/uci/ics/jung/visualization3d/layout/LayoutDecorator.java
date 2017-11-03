/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 23, 2005
 */

package edu.uci.ics.jung.visualization3d.layout;

import edu.uci.ics.jung.algorithms.layout3d.Layout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import java.util.Collection;
import java.util.function.Function;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3f;

/**
 * a pure decorator for the Layout interface. Intended to be overridden to provide specific behavior
 * decoration
 *
 * @author Tom Nelson
 */
public abstract class LayoutDecorator<N, E> implements Layout<N, E>, IterativeContext {

  protected Layout<N, E> delegate;

  public LayoutDecorator(Layout<N, E> delegate) {
    this.delegate = delegate;
  }

  /**
   * getter for the delegate
   *
   * @return the delegate
   */
  public Layout getDelegate() {
    return delegate;
  }

  /**
   * setter for the delegate
   *
   * @param delegate the new delegate
   */
  public void setDelegate(Layout<N, E> delegate) {
    this.delegate = delegate;
  }

  /** */
  public void step() {
    if (delegate instanceof IterativeContext) {
      ((IterativeContext) delegate).step();
    }
  }

  /** @see edu.uci.ics.jung.algorithms.layout.Layout#initialize() */
  public void initialize() {
    delegate.initialize();
  }

  /** @param initializer */
  public void setInitializer(Function<N, Point3f> initializer) {
    delegate.setInitializer(initializer);
  }

  /**
   * @param v
   * @param location
   */
  public void setLocation(N v, Point3f location) {
    delegate.setLocation(v, location);
  }

  /** */
  public BoundingSphere getSize() {
    return delegate.getSize();
  }

  /** */
  public Point3f apply(N v) {
    return delegate.apply(v);
  }

  /** */
  public boolean done() {
    if (delegate instanceof IterativeContext) {
      return ((IterativeContext) delegate).done();
    }
    return true;
  }

  /** */
  public void lock(N v, boolean state) {
    delegate.lock(v, state);
  }

  /** */
  public boolean isLocked(N v) {
    return delegate.isLocked(v);
  }

  /** */
  public void setSize(BoundingSphere d) {
    delegate.setSize(d);
  }

  /** */
  public void reset() {
    delegate.reset();
  }

  public Collection<N> nodes() {
    return delegate.nodes();
  }
}
