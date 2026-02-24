/*
 * Created on Apr 2, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath

/**
 * An interface for classes which calculate the distance between one node and another.
 *
 * @author Joshua O'Madadhain
 */
interface Distance<N : Any> {
  /**
   * Returns the distance from the `source` node to the `target` node. If
   * `target` is not reachable from `source`, returns null.
   *
   * @param source the node from which distance is to be measured
   * @param target the node to which distance is to be measured
   * @return the distance from [source] to [target]
   */
  fun getDistance(source: N, target: N): Number?

  /**
   * Returns a `Map` which maps each node in the graph (including the `source`
   * node) to its distance (represented as a Number) from `source`. If any node
   * is not reachable from `source`, no distance is stored for that node.
   *
   * @param source the node from which distances are to be measured
   * @return a [Map] of the distances from [source] to other nodes in the graph
   */
  fun getDistanceMap(source: N): Map<N, out Number>
}
