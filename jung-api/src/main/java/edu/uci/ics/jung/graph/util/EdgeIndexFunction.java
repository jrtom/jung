/*
 * Created on Sep 24, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph.util;

/**
 * An interface for a service to access the index of a given edge (in a given {@code Network}) into
 * the set formed by the given edge and all the other edges it is parallel to.
 *
 * <p>This index is assumed to be an integer value in the interval [0,n-1], where n-1 is the number
 * of edges parallel to <code>e</code>.
 *
 * @author Tom Nelson
 */
public interface EdgeIndexFunction<E> {

  /**
   * The index of <code>e</code> is defined as its position in some consistent ordering of <code>e
   * </code> and all edges parallel to <code>e</code>.
   *
   * @param edge the edge whose index is to be queried
   * @return {@code edge}'s index in this instance's <code>Network</code>.
   */
  int getIndex(E edge);

  /**
   * Resets the indices for <code>edge</code> and its parallel edges. Should be invoked when an edge
   * parallel to <code>edge</code> has been added or removed.
   *
   * @param edge the edge whose index is to be reset
   */
  void reset(E edge);

  /** Clears all edge indices for all edges. Does not recalculate the indices. */
  void reset();
}
