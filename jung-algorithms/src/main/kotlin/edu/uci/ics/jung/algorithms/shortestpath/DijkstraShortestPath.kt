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

import com.google.common.base.Preconditions
import com.google.common.graph.Network
import java.util.LinkedHashMap
import java.util.LinkedList
import java.util.function.Function

/**
 * Calculates distances and shortest paths using Dijkstra's single-source-shortest-path algorithm.
 * This is a lightweight extension of `DijkstraDistance` that also stores path
 * information, so that the shortest paths can be reconstructed.
 *
 * The elements in the maps returned by `getIncomingEdgeMap` are ordered (that is,
 * returned by the iterator) by nondecreasing distance from `source`.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson converted to jung2
 * @see DijkstraDistance
 */
// TODO: refactor the heck out of this and of DijkstraDistance
class DijkstraShortestPath<N : Any, E : Any> : DijkstraDistance<N, E>, ShortestPath<N, E> {

  /**
   * Creates an instance of `DijkstraShortestPath` for the specified graph and the
   * specified method of extracting weights from edges, which caches results locally if and only if
   * `cached` is `true`.
   *
   * @param g the graph on which distances will be calculated
   * @param nev the class responsible for returning weights for edges
   * @param cached specifies whether the results are to be cached
   */
  constructor(g: Network<N, E>, nev: Function<E, out Number>, cached: Boolean) : super(g, nev, cached)

  /**
   * Creates an instance of `DijkstraShortestPath` for the specified graph and the
   * specified method of extracting weights from edges, which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param nev the class responsible for returning weights for edges
   */
  constructor(g: Network<N, E>, nev: Function<E, out Number>) : super(g, nev)

  /**
   * Creates an instance of `DijkstraShortestPath` for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   */
  constructor(g: Network<N, E>) : super(g)

  /**
   * Creates an instance of `DijkstraShortestPath` for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param cached specifies whether the results are to be cached
   */
  constructor(g: Network<N, E>, cached: Boolean) : super(g, cached)

  override fun getSourceData(source: N): SourceData {
    var sd = sourceMap[source]
    if (sd == null) {
      sd = SourcePathData(source)
    }
    return sd
  }

  /**
   * Returns the last edge on a shortest path from `source` to `target`, or
   * null if `target` is not reachable from `source`.
   *
   * If either node is not in the graph for which this instance was created, throws
   * `IllegalArgumentException`.
   *
   * @param source the node where the shortest path starts
   * @param target the node where the shortest path ends
   * @return the last edge on a shortest path from [source] to [target] or null if
   *     [target] is not reachable from [source]
   */
  fun getIncomingEdge(source: N, target: N): E? {
    Preconditions.checkArgument(
      g.nodes().contains(target), "Specified target node %s  is not part of graph %s", target, g
    )
    Preconditions.checkArgument(
      g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g
    )

    val targets = HashSet<N>()
    targets.add(target)
    singleSourceShortestPath(source, targets, g.nodes().size)
    val incomingEdgeMap = (sourceMap[source] as SourcePathData).incomingEdges
    val incomingEdge = incomingEdgeMap[target]

    if (!cached) {
      reset(source)
    }

    return incomingEdge
  }

  /**
   * Returns a `LinkedHashMap` which maps each node in the graph (including the
   * `source` node) to the last edge on the shortest path from the `source` node.
   * The map's iterator will return the elements in order of increasing distance from
   * `source`.
   *
   * @see DijkstraDistance.getDistanceMap
   * @see DijkstraDistance.getDistance
   * @param source the node from which distances are measured
   */
  override fun getIncomingEdgeMap(source: N): Map<N, E> =
    getIncomingEdgeMap(source, g.nodes().size)

  /**
   * Returns a `List` of the edges on the shortest path from `source` to
   * `target`, in order of their occurrence on this path. If either node is not in the
   * graph for which this instance was created, throws `IllegalArgumentException`.
   *
   * @param source the starting node for the path to generate
   * @param target the ending node for the path to generate
   * @return the edges on the shortest path from [source] to [target], in order of their
   *     occurrence
   */
  fun getPath(source: N, target: N): List<E> {
    Preconditions.checkArgument(
      g.nodes().contains(target), "Specified target node %s  is not part of graph %s", target, g
    )
    Preconditions.checkArgument(
      g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g
    )

    // we use a LinkedList here because we're always appending to the front
    val path = LinkedList<E>()

    // collect path data; must use internal method rather than
    // calling getIncomingEdge() because getIncomingEdge() may
    // wipe out results if results are not cached
    val targets = HashSet<N>()
    targets.add(target)
    singleSourceShortestPath(source, targets, g.nodes().size)
    val incomingEdges = (sourceMap[source] as SourcePathData).incomingEdges

    if (incomingEdges.isEmpty() || incomingEdges[target] == null) {
      return path
    }
    var current = target
    while (current != source) {
      val incoming = incomingEdges[current]!!
      path.addFirst(incoming)
      current = g.incidentNodes(incoming).adjacentNode(current)
    }

    if (!cached) {
      reset(source)
    }

    return ArrayList(path)
  }

  /**
   * Returns a `LinkedHashMap` which maps each of the closest `numDests` nodes
   * to the `source` node in the graph (including the `source` node) to the
   * incoming edge along the path from that node. Throws an
   * `IllegalArgumentException` if `source` is not in this instance's graph, or if
   * `numDests` is either less than 1 or greater than the number of nodes in the graph.
   *
   * @see .getIncomingEdgeMap
   * @see .getPath
   * @param source the node from which distances are measured
   * @param numDests the number of nodes for which to measure distances
   * @return a map from each of the closest [numDests] nodes to the last edge on the shortest
   *     path to that node starting from [source]
   */
  fun getIncomingEdgeMap(source: N, numDests: Int): LinkedHashMap<N, E> {
    Preconditions.checkArgument(
      g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g
    )
    Preconditions.checkArgument(
      numDests in 1..g.nodes().size,
      "number of destinations must be in [1, %d]",
      g.nodes().size
    )

    singleSourceShortestPath(source, null, numDests)

    val incomingEdgeMap = (sourceMap[source] as SourcePathData).incomingEdges

    if (!cached) {
      reset(source)
    }

    return incomingEdgeMap
  }

  /**
   * For a given source node, holds the estimated and final distances, tentative and final
   * assignments of incoming edges on the shortest path from the source node, and a priority queue
   * (ordered by estimated distance) of the nodes for which distances are unknown.
   *
   * @author Joshua O'Madadhain
   */
  protected inner class SourcePathData(source: N) : SourceData(source) {
    val tentativeIncomingEdges: MutableMap<N, E> = HashMap()
    val incomingEdges: LinkedHashMap<N, E> = LinkedHashMap()

    override fun update(dest: N, tentativeEdge: E, newDist: Double) {
      super.update(dest, tentativeEdge, newDist)
      tentativeIncomingEdges[dest] = tentativeEdge
    }

    override fun getNextNode(): Map.Entry<N, Number> {
      val p = super.getNextNode()
      val v = p.key
      val incoming = tentativeIncomingEdges.remove(v)
      if (incoming != null) {
        incomingEdges[v] = incoming
      }
      return p
    }

    override fun restoreNode(v: N, dist: Double) {
      super.restoreNode(v, dist)
      val incoming = incomingEdges[v]
      if (incoming != null) {
        tentativeIncomingEdges[v] = incoming
      }
    }

    override fun createRecord(w: N, e: E, newDist: Double) {
      super.createRecord(w, e, newDist)
      tentativeIncomingEdges[w] = e
    }
  }
}
