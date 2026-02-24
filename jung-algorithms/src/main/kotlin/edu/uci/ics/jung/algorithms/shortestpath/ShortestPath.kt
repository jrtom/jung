/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Feb 12, 2004
 */
package edu.uci.ics.jung.algorithms.shortestpath

/** An interface for algorithms that calculate shortest paths. */
interface ShortestPath<N : Any, E : Any> {
  /**
   * Returns a map from nodes to the last edge on the shortest path to that node starting from
   * [source].
   *
   * @param source the starting point for the shortest paths
   * @return a map from nodes to the last edge on the shortest path to that node starting from
   *     [source]
   */
  fun getIncomingEdgeMap(source: N): Map<N, E>
}
