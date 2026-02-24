/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath

import com.google.common.graph.Graph

/**
 * Computes the shortest path distances for graphs whose edges are not weighted (using BFS).
 *
 * @author Scott White
 */
// TODO: refactor to make this (much!) more efficient
class UnweightedShortestPath<N : Any>(private val mGraph: Graph<N>) : Distance<N> {

  private val mDistanceMap = HashMap<N, Map<N, Int>>()
  private val mPredecessorMap = HashMap<N, Map<N, N>>()
  private var distances = HashMap<N, Int>()

  /**
   * @see Distance.getDistance
   */
  override fun getDistance(source: N, target: N): Int? {
    val sourceSPMap = getDistanceMap(source)
    return sourceSPMap[target]
  }

  /**
   * @see Distance.getDistanceMap
   */
  override fun getDistanceMap(source: N): Map<N, Int> {
    var sourceSPMap = mDistanceMap[source]
    if (sourceSPMap == null) {
      computeShortestPathsFromSource(source)
      sourceSPMap = mDistanceMap[source]
    }
    return sourceSPMap!!
  }

  /**
   * @see ShortestPath.getIncomingEdgeMap
   */
  fun getIncomingEdgeMap(source: N): Map<N, N> {
    var sourceIEMap = mPredecessorMap[source]
    if (sourceIEMap == null) {
      computeShortestPathsFromSource(source)
      sourceIEMap = mPredecessorMap[source]
    }
    return sourceIEMap!!
  }

  /**
   * Computes the shortest path distances from a given node to all other nodes.
   *
   * @param source the source node
   */
  private fun computeShortestPathsFromSource(source: N) {
    val labeler = BFSDistanceLabeler<N>()
    labeler.labelDistances(mGraph, source)
    distances = HashMap(labeler.getDistanceDecorator())
    val currentSourceSPMap = HashMap<N, Int>()
    val currentSourcePredMap = HashMap<N, N>()

    for (node in mGraph.nodes()) {
      val distanceVal = distances[node]
      // BFSDistanceLabeler uses -1 to indicate unreachable nodes;
      // don't bother to store unreachable nodes
      if (distanceVal != null && distanceVal >= 0) {
        currentSourceSPMap[node] = distanceVal
        var minDistance: Int = distanceVal
        for (predecessor in mGraph.predecessors(node)) {
          if (predecessor == node) {
            continue
          }

          val predDistance: Int = distances[predecessor] ?: continue
          if (predDistance < minDistance && predDistance >= 0) {
            minDistance = predDistance
            currentSourcePredMap[node] = predecessor
          }
        }
      }
    }
    mDistanceMap[source] = currentSourceSPMap
    mPredecessorMap[source] = currentSourcePredMap
  }

  /**
   * Clears all stored distances for this instance. Should be called whenever the graph is modified
   * (edge weights changed or edges added/removed). If the user knows that some currently calculated
   * distances are unaffected by a change, `reset(V)` may be appropriate instead.
   *
   * @see .reset
   */
  fun reset() {
    mDistanceMap.clear()
    mPredecessorMap.clear()
  }

  /**
   * Clears all stored distances for the specified source node `source`. Should be called
   * whenever the stored distances from this node are invalidated by changes to the graph.
   *
   * @see .reset
   * @param v the node for which distances should be cleared
   */
  fun reset(v: N) {
    mDistanceMap.remove(v)
    mPredecessorMap.remove(v)
  }
}
