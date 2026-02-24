/*
 * Created on Jul 9, 2005
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

import com.google.common.base.Preconditions
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.util.MapBinaryHeap
import java.util.AbstractMap.SimpleImmutableEntry
import java.util.Comparator.comparingDouble
import java.util.LinkedHashMap
import java.util.function.Function

/**
 * Calculates distances in a specified graph, using Dijkstra's single-source-shortest-path
 * algorithm. All edge weights in the graph must be nonnegative; if any edge with negative weight is
 * found in the course of calculating distances, an `IllegalArgumentException` will be
 * thrown. (Note: this exception will only be thrown when such an edge would be used to update a
 * given tentative distance; the algorithm does not check for negative-weight edges "up front".)
 *
 * Distances and partial results are optionally cached (by this instance) for later reference.
 * Thus, if the 10 closest nodes to a specified source node are known, calculating the 20 closest
 * nodes does not require starting Dijkstra's algorithm over from scratch.
 *
 * Distances are stored as double-precision values. If a node is not reachable from the specified
 * source node, no distance is stored. **This is new behavior with version 1.4**; the previous
 * behavior was to store a value of `Double.POSITIVE_INFINITY`. This change gives the
 * algorithm an approximate complexity of O(kD log k), where k is either the number of requested
 * targets or the number of reachable nodes (whichever is smaller), and D is the average degree of a
 * node.
 *
 * The elements in the maps returned by `getDistanceMap` are ordered (that is,
 * returned by the iterator) by nondecreasing distance from `source`.
 *
 * Users are cautioned that distances calculated should be assumed to be invalidated by changes
 * to the graph, and should invoke `reset()` when appropriate so that the distances can
 * be recalculated.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson converted to jung2
 */
open class DijkstraDistance<N : Any, E : Any>(
  protected val g: Network<N, E>,
  protected val nev: Function<in E, out Number>,
  protected var cached: Boolean
) : Distance<N> {

  protected var sourceMap: MutableMap<N, SourceData?> = HashMap()
  protected var maxDistance: Double = Double.POSITIVE_INFINITY
    set(value) {
      field = value
      for (v in sourceMap.keys) {
        val sd = sourceMap[v] ?: continue
        sd.reachedMax = (this.maxDistance <= sd.distanceReached) || (sd.distances.size >= maxTargets)
      }
    }
  protected var maxTargets: Int = Int.MAX_VALUE
    set(value) {
      field = value
      for (v in sourceMap.keys) {
        val sd = sourceMap[v] ?: continue
        sd.reachedMax = (this.maxDistance <= sd.distanceReached) || (sd.distances.size >= maxTargets)
      }
    }

  /**
   * Creates an instance of `DijkstraShortestPath` for the specified graph and the
   * specified method of extracting weights from edges, which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param nev the class responsible for returning weights for edges
   */
  constructor(g: Network<N, E>, nev: Function<in E, out Number>) : this(g, nev, true)

  /**
   * Creates an instance of `DijkstraShortestPath` for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   */
  constructor(g: Network<N, E>) : this(g, Function { 1 }, true)

  /**
   * Creates an instance of `DijkstraShortestPath` for the specified unweighted graph
   * (that is, all weights 1) which caches results locally.
   *
   * @param g the graph on which distances will be calculated
   * @param cached specifies whether the results are to be cached
   */
  constructor(g: Network<N, E>, cached: Boolean) : this(g, Function { 1 }, cached)

  /**
   * Implements Dijkstra's single-source shortest-path algorithm for weighted graphs. Uses a
   * `MapBinaryHeap` as the priority queue, which gives this algorithm a time complexity of O(m
   * lg n) (m = # of edges, n = # of nodes). This algorithm will terminate when any of the
   * following have occurred (in order of priority):
   *
   *  * the distance to the specified target (if any) has been found
   *  * no more nodes are reachable
   *  * the specified # of distances have been found, or the maximum distance desired has been
   *    exceeded
   *  * all distances have been found
   *
   * @param source the node from which distances are to be measured
   * @param numDistances the number of distances to measure
   * @param targets the set of nodes to which distances are to be measured
   * @return a mapping from node to the shortest distance from the source to each target
   */
  protected open fun singleSourceShortestPath(
    source: N, targets: Collection<N>?, numDistances: Int
  ): LinkedHashMap<N, Number> {
    val sd = getSourceData(source)

    val toGet = HashSet<N>()
    if (targets != null) {
      toGet.addAll(targets)
      val existingDistances = sd.distances.keys
      for (o in targets) {
        if (existingDistances.contains(o)) {
          toGet.remove(o)
        }
      }
    }

    // if we've exceeded the max distance or max # of distances we're willing to calculate, or
    // if we already have all the distances we need,
    // terminate
    if (sd.reachedMax
      || (targets != null && toGet.isEmpty())
      || (sd.distances.size >= numDistances)
    ) {
      return sd.distances
    }

    while (sd.unknownNodes.isNotEmpty() && (sd.distances.size < numDistances || toGet.isNotEmpty())) {
      val p = sd.getNextNode()
      val v = p.key
      val vDist = p.value.toDouble()
      toGet.remove(v)
      if (vDist > this.maxDistance) {
        // we're done; put this node back in so that we're not including
        // a distance beyond what we specified
        sd.restoreNode(v, vDist)
        sd.reachedMax = true
        break
      }
      sd.distanceReached = vDist

      if (sd.distances.size >= this.maxTargets) {
        sd.reachedMax = true
        break
      }

      for (w in g.successors(v)) {
        for (e in g.edgesConnecting(v, w)) {
          if (!sd.distances.containsKey(w)) {
            val edgeWeight = nev.apply(e).toDouble()
            Preconditions.checkArgument(
              edgeWeight >= 0,
              "encountered negative edge weight %s for edge %s",
              nev.apply(e),
              e
            )
            val newDist = vDist + edgeWeight
            if (!sd.estimatedDistances.containsKey(w)) {
              sd.createRecord(w, e, newDist)
            } else {
              val wDist = sd.estimatedDistances[w] as Double
              if (newDist < wDist) { // update tentative distance & path for w
                sd.update(w, e, newDist)
              }
            }
          }
        }
      }
    }
    return sd.distances
  }

  protected open fun getSourceData(source: N): SourceData {
    var sd = sourceMap[source]
    if (sd == null) {
      sd = SourceData(source)
    }
    return sd
  }

  /**
   * Returns the length of a shortest path from the source to the target node, or null if the target
   * is not reachable from the source. If either node is not in the graph for which this instance
   * was created, throws `IllegalArgumentException`.
   *
   * @param source the node from which the distance to [target] is to be measured
   * @param target the node to which the distance from [source] is to be measured
   * @return the distance between [source] and [target]
   * @see .getDistanceMap
   */
  override fun getDistance(source: N, target: N): Number? {
    Preconditions.checkArgument(
      g.nodes().contains(target), "Specified target node %s  is not part of graph %s", target, g
    )
    Preconditions.checkArgument(
      g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g
    )

    val targets = HashSet<N>()
    targets.add(target)
    val distanceMap = getDistanceMap(source, targets)
    return distanceMap[target]
  }

  /**
   * Returns a `Map` from each element `t` of `targets` to the shortest-path
   * distance from `source` to `t`.
   *
   * @param source the node from which the distance to each target is to be measured
   * @param targets the nodes to which the distance from the source is to be measured
   * @return [Map] from each element of [targets] to its distance from [source]
   */
  fun getDistanceMap(source: N, targets: Collection<N>): Map<N, Number> {
    Preconditions.checkArgument(
      g.nodes().contains(source), "Specified source node %s  is not part of graph %s", source, g
    )
    Preconditions.checkArgument(
      targets.size <= maxTargets,
      "size of target set %d exceeds maximum number of targets allowed: %d",
      targets.size,
      this.maxTargets
    )

    val distanceMap = singleSourceShortestPath(source, targets, minOf(g.nodes().size, maxTargets))
    if (!cached) {
      reset(source)
    }

    return distanceMap
  }

  /**
   * Returns a `LinkedHashMap` which maps each node in the graph (including the
   * `source` node) to its distance from the `source` node. The map's iterator will
   * return the elements in order of increasing distance from `source`.
   *
   * The size of the map returned will be the number of nodes reachable from `source`.
   *
   * @see .getDistanceMap
   * @see .getDistance
   * @param source the node from which distances are measured
   * @return a mapping from each node in the graph to its distance from [source]
   */
  override fun getDistanceMap(source: N): Map<N, Number> =
    getDistanceMap(source, minOf(g.nodes().size, maxTargets))

  /**
   * Returns a `LinkedHashMap` which maps each of the closest `numDist` nodes
   * to the `source` node in the graph (including the `source` node) to its
   * distance from the `source` node. Throws an `IllegalArgumentException` if
   * `source` is not in this instance's graph, or if `numDests` is either less than 1
   * or greater than the number of nodes in the graph.
   *
   * The size of the map returned will be the smaller of `numDests` and the number of
   * nodes reachable from `source`.
   *
   * @see .getDistanceMap
   * @see .getDistance
   * @param source the node from which distances are measured
   * @param numDests the number of nodes for which to measure distances
   * @return a mapping from the [numDests] nodes in the graph closest to [source], to
   *     their distance from [source]
   */
  fun getDistanceMap(source: N, numDests: Int): LinkedHashMap<N, Number> {
    Preconditions.checkArgument(
      g.nodes().contains(source), "Specified source node %s is not part of graph %s", source, g
    )
    Preconditions.checkArgument(
      numDests in 1..g.nodes().size,
      "number of destinations must be in [1, %d]",
      g.nodes().size
    )
    Preconditions.checkArgument(
      numDests <= maxTargets,
      "size of target set %d exceeds maximum number of targets allowed: %d",
      numDests,
      this.maxTargets
    )

    val distanceMap = singleSourceShortestPath(source, null, numDests)

    if (!cached) {
      reset(source)
    }

    return distanceMap
  }

  /**
   * Clears all stored distances for this instance. Should be called whenever the graph is modified
   * (edge weights changed or edges added/removed). If the user knows that some currently calculated
   * distances are unaffected by a change, `reset(V)` may be appropriate instead.
   *
   * @see .reset
   */
  fun reset() {
    sourceMap = HashMap()
  }

  /**
   * Specifies whether or not this instance of `DijkstraShortestPath` should cache its
   * results (final and partial) for future reference.
   *
   * @param enable `true` if the results are to be cached, and `false` otherwise
   */
  fun enableCaching(enable: Boolean) {
    this.cached = enable
  }

  /**
   * Clears all stored distances for the specified source node `source`. Should be called
   * whenever the stored distances from this node are invalidated by changes to the graph.
   *
   * @param source the node for which stored distances should be cleared
   * @see .reset
   */
  fun reset(source: N) {
    sourceMap[source] = null
  }

  /**
   * For a given source node, holds the estimated and final distances, tentative and final
   * assignments of incoming edges on the shortest path from the source node, and a priority queue
   * (ordered by estimated distance) of the nodes for which distances are unknown.
   *
   * @author Joshua O'Madadhain
   */
  protected open inner class SourceData(source: N) {
    val distances: LinkedHashMap<N, Number> = LinkedHashMap()
    val estimatedDistances: MutableMap<N, Number> = HashMap()
    val unknownNodes: MapBinaryHeap<N> =
      MapBinaryHeap(comparingDouble { n: N -> estimatedDistances[n]!!.toDouble() })
    var reachedMax: Boolean = false
    var distanceReached: Double = 0.0

    init {
      sourceMap[source] = this

      // initialize priority queue
      estimatedDistances[source] = 0.0 // distance from source to itself is 0
      unknownNodes.add(source)
    }

    open fun getNextNode(): Map.Entry<N, Number> {
      val v = unknownNodes.remove()
      val dist = estimatedDistances.remove(v)!!
      distances[v] = dist
      return SimpleImmutableEntry(v, dist)
    }

    open fun update(dest: N, tentativeEdge: E, newDist: Double) {
      estimatedDistances[dest] = newDist
      unknownNodes.update(dest)
    }

    open fun createRecord(w: N, e: E, newDist: Double) {
      estimatedDistances[w] = newDist
      unknownNodes.add(w)
    }

    open fun restoreNode(v: N, dist: Double) {
      estimatedDistances[v] = dist
      unknownNodes.add(v)
      distances.remove(v)
    }
  }
}
