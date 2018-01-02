/*
 * Created on Jul 8, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring.util;

import com.google.common.base.Preconditions;

/**
 * Convenience class for associating a node and an edge. Used, for example, in contexts in which it
 * is necessary to know the origin for an edge traversal (that is, the direction in which an
 * (undirected) edge is being traversed).
 *
 * @param <N> the node type
 * @param <E> the edge type
 */
public class VEPair<N, E> {
  private N v;
  private E e;

  /**
   * Creates an instance with the specified node and edge
   *
   * @param v the node to add
   * @param e the edge to add
   */
  public VEPair(N v, E e) {
    Preconditions.checkNotNull(v);
    Preconditions.checkNotNull(e);

    this.v = v;
    this.e = e;
  }

  /** @return the node of this pair */
  public N getV() {
    return v;
  }

  /** @return the edge of this pair */
  public E getE() {
    return e;
  }
}
