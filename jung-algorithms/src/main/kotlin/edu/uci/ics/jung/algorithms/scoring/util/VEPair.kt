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
package edu.uci.ics.jung.algorithms.scoring.util

import com.google.common.base.Preconditions

/**
 * Convenience class for associating a node and an edge. Used, for example, in contexts in which it
 * is necessary to know the origin for an edge traversal (that is, the direction in which an
 * (undirected) edge is being traversed).
 *
 * @param N the node type
 * @param E the edge type
 */
class VEPair<N : Any, E : Any>(
  /** @return the node of this pair */
  val v: N,
  /** @return the edge of this pair */
  val e: E
) {
  init {
    Preconditions.checkNotNull(v)
    Preconditions.checkNotNull(e)
  }
}
