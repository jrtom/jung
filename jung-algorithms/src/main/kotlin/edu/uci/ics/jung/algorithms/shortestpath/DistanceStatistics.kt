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
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality
import edu.uci.ics.jung.algorithms.scoring.util.NodeScoreTransformer
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Statistics relating to node-node distances in a graph.
 *
 * Formerly known as `GraphStatistics` in JUNG 1.x.
 *
 * @author Scott White
 * @author Joshua O'Madadhain
 */
object DistanceStatistics {
  /**
   * For each node `v` in `graph`, calculates the average shortest path length
   * from `v` to all other nodes in `graph` using the metric specified by
   * `d`, and returns the results in a `Map` from nodes to `Double`
   * values. If there exists an ordered pair `<u,v>` for which
   * `d.getDistance(u,v)` returns `null`, then the average distance value for
   * `u` will be stored as `Double.POSITIVE_INFINITY`).
   *
   * Does not include self-distances (path lengths from `v` to `v`).
   *
   * To calculate the average distances, ignoring edge weights if any:
   *
   * ```
   * val distances = DistanceStatistics.averageDistances(g, UnweightedShortestPath(g))
   * ```
   *
   * To calculate the average distances respecting edge weights:
   *
   * ```
   * val dsp = DijkstraShortestPath(g, nev)
   * val distances = DistanceStatistics.averageDistances(g, dsp)
   * ```
   *
   * where `nev` is an instance of `Function` that is used to fetch the
   * weight for each edge.
   *
   * @see UnweightedShortestPath
   * @see DijkstraDistance
   * @param graph the graph for which distances are to be calculated
   * @param d the distance metric to use for the calculation
   * @param N the node type
   * @param E the edge type
   * @return a map from each node to the mean distance to each other (reachable) node
   */
  @JvmStatic
  fun <N : Any, E : Any> averageDistances(graph: Network<N, E>, d: Distance<N>): Function<N, Double> {
    val cc = ClosenessCentrality<N, E>(graph, d)
    return NodeScoreTransformer<N, Double>(cc)
  }

  /**
   * For each node `v` in `g`, calculates the average shortest path length
   * from `v` to all other nodes in `g`, ignoring edge weights.
   *
   * @see .diameter
   * @see ClosenessCentrality
   * @param g the graph for which distances are to be calculated
   * @param N the node type
   * @return a map from each node to the mean distance to each other (reachable) node
   */
  @JvmStatic
  fun <N : Any> averageDistances(g: Graph<N>): Function<N, Double> {
    val cc = ClosenessCentrality<N, Any>(g)
    return NodeScoreTransformer<N, Double>(cc)
  }

  /**
   * Returns the diameter of `g` using the metric specified by `d`. The
   * diameter is defined to be the maximum, over all pairs of nodes `u,v`, of the length
   * of the shortest path from `u` to `v`. If the graph is disconnected (that
   * is, not all pairs of nodes are reachable from one another), the value returned will depend on
   * `use_max`: if `use_max == true`, the value returned will be the the
   * maximum shortest path length over all pairs of **connected** nodes; otherwise it will be
   * `Double.POSITIVE_INFINITY`.
   *
   * @param g the graph for which distances are to be calculated
   * @param d the distance metric to use for the calculation
   * @param use_max if `true`, return the maximum shortest path length for all graphs;
   *     otherwise, return `Double.POSITIVE_INFINITY` for disconnected graphs
   * @param N the node type
   * @return the longest distance from any node to any other
   */
  @JvmStatic
  fun <N : Any> diameter(
    g: Graph<N>, d: BiFunction<N, N, out Number?>, use_max: Boolean
  ): Double {
    var diameter = 0.0
    // TODO: provide an undirected version
    for (v in g.nodes()) {
      for (w in g.nodes()) {
        if (v == w) {
          continue // don't include self-distances
        }
        val dist = d.apply(v, w)
        if (dist == null) {
          if (!use_max) {
            return Double.POSITIVE_INFINITY
          }
        } else {
          diameter = maxOf(diameter, dist.toDouble())
        }
      }
    }
    return diameter
  }

  /**
   * Returns the diameter of `g` using the metric specified by `d`. The
   * diameter is defined to be the maximum, over all pairs of nodes `u,v`, of the length
   * of the shortest path from `u` to `v`, or
   * `Double.POSITIVE_INFINITY` if any of these distances do not exist.
   *
   * @see .diameter
   * @param g the graph for which distances are to be calculated
   * @param distance the distance metric to use for the calculation
   * @param N the node type
   * @return the longest distance from any node to any other
   */
  @JvmStatic
  fun <N : Any> diameter(g: Graph<N>, distance: BiFunction<N, N, out Number?>): Double =
    diameter(g, distance, false)

  /**
   * Returns the diameter of `g`, ignoring edge weights.
   *
   * @see .diameter
   * @param g the graph for which distances are to be calculated
   * @param N the node type
   * @return the longest distance from any node to any other
   */
  @JvmStatic
  fun <N : Any> diameter(g: Graph<N>): Double =
    diameter(g, BiFunction { v, w -> UnweightedShortestPath(g).getDistance(v, w) })
}
