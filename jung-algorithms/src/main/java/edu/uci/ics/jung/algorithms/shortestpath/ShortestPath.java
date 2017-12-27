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
package edu.uci.ics.jung.algorithms.shortestpath;

import java.util.Map;

/** An interface for algorithms that calculate shortest paths. */
public interface ShortestPath<N, E> {
  /**
   * Returns a map from nodes to the last edge on the shortest path to that node starting from
   * {@code source}.
   *
   * @param source the starting point for the shortest paths
   * @return a map from nodes to the last edge on the shortest path to that node starting from
   *     {@code source}
   */
  Map<N, E> getIncomingEdgeMap(N source);
}
