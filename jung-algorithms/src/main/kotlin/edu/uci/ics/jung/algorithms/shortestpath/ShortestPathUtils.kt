/*
 * Created on Jul 10, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath

import com.google.common.graph.Network
import java.util.LinkedList

/** Utilities relating to the shortest paths in a graph. */
object ShortestPathUtils {
  /**
   * Returns a `List` of the edges on the shortest path from `source` to
   * `target`, in order of their occurrence on this path.
   *
   * @param graph the graph for which the shortest path is defined
   * @param sp holder of the shortest path information
   * @param source the node from which the shortest path is measured
   * @param target the node to which the shortest path is measured
   * @param N the node type
   * @param E the edge type
   * @return the edges on the shortest path from [source] to [target], in the order
   *     traversed
   */
  @JvmStatic
  fun <N : Any, E : Any> getPath(
    graph: Network<N, E>, sp: ShortestPath<N, E>, source: N, target: N
  ): List<E> {
    val path = LinkedList<E>()

    val incomingEdges = sp.getIncomingEdgeMap(source)

    if (incomingEdges.isEmpty() || incomingEdges[target] == null) {
      return path
    }
    var current = target
    while (current != source) {
      val incoming = incomingEdges[current]!!
      path.addFirst(incoming)
      current = graph.incidentNodes(incoming).adjacentNode(current)
    }
    return path
  }
}
