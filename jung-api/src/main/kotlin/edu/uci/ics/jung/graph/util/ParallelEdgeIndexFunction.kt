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
package edu.uci.ics.jung.graph.util

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.graph.Network

/**
 * A class which creates and maintains indices for parallel edges. Parallel edges are defined here
 * to be the collection of edges that are returned by `graph.edgesConnecting(v, w)` for
 * some `v` and `w`.
 *
 * At this time, users are responsible for resetting the indices (by calling `reset()`)
 * if changes to the graph make it appropriate.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 */
class ParallelEdgeIndexFunction<N : Any, E : Any>(
  private val graph: Network<N, E>
) : EdgeIndexFunction<E> {

  private val edgeIndex: MutableMap<E, Int> = HashMap()

  init {
    checkNotNull(graph, "graph")
  }

  /**
   * @param graph the graph for which this index function is defined
   */

  override fun getIndex(edge: E): Int {
    checkNotNull(edge, "edge")
    val index = edgeIndex[edge]
    if (index == null) {
      val endpoints = graph.incidentNodes(edge)
      val u = endpoints.nodeU()
      val v = endpoints.nodeV()
      var count = 0
      for (connectingEdge in graph.edgesConnecting(u, v)) {
        edgeIndex[connectingEdge] = count++
      }
      return edgeIndex[edge]!!
    }
    return index
  }

  override fun reset(edge: E) {
    edgeIndex.remove(checkNotNull(edge, "edge"))
  }

  override fun reset() {
    edgeIndex.clear()
  }
}
