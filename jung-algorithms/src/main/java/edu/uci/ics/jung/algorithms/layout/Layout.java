/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.function.Function;

/**
 * A generalized interface for mechanisms that associate (x,y) coordinates with nodes.
 *
 * <p>
 *
 * @author danyelf
 * @author tom nelson
 */
public interface Layout<N> extends Function<N, Point2D> {
  /**
   * Initializes fields in the node that may not have been set during the constructor. Must be
   * called before the iterations begin.
   */
  void initialize();

  /** @param initializer a function that specifies initial locations for all nodes */
  void setInitializer(Function<N, Point2D> initializer);

  /** @return the set of nodes for which this Layout assigns positions */
  Set<N> nodes();

  void reset();

  /** @param d the space to use to lay out this graph */
  void setSize(Dimension d);

  /** @return the current size of the visualization's space */
  Dimension getSize();

  /**
   * Locks or unlocks the specified node. Locking the node fixes it at its current position, so that
   * it will not be affected by the layout algorithm. Unlocking it allows the layout algorithm to
   * change the node's position.
   *
   * @param v the node to lock/unlock
   * @param state {@code true} to lock the node, {@code false} to unlock it
   */
  void lock(N n, boolean state);

  /**
   * @param v the node whose locked state is being queried
   * @return <code>true</code> if the position of node <code>v</code> is locked
   */
  boolean isLocked(N n);

  /**
   * Changes the layout coordinates of {@code node} to {@code location}.
   *
   * @param v the node whose location is to be specified
   * @param location the coordinates of the specified location
   */
  void setLocation(N n, Point2D location);
}
