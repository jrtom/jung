/*
 * Created on Jul 10, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring

import com.google.common.collect.Maps
import com.google.common.graph.Graph
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance
import edu.uci.ics.jung.algorithms.shortestpath.Distance
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath
import java.util.HashMap
import java.util.function.Function

/**
 * Assigns scores to nodes based on their distances to each other node in the graph.
 *
 * This class optionally normalizes its results based on the value of its 'averaging' constructor
 * parameter. If it is `true`, then the value returned for node v is 1 / (_average_
 * distance from v to all other nodes); this is sometimes called *closeness centrality*. If it
 * is `false`, then the value returned is 1 / (_total_ distance from v to all other
 * nodes); this is sometimes referred to as *barycenter centrality*. (If the average/total
 * distance is 0, the value returned is [Double.POSITIVE_INFINITY].)
 *
 * @see BarycenterScorer
 * @see ClosenessCentrality
 */
open class DistanceCentralityScorer<N : Any, E : Any> : NodeScorer<N, Double> {
  /** The graph on which the node scores are to be calculated. */
  protected val graph: Graph<N>

  /** The metric to use for specifying the distance between pairs of nodes. */
  protected val distance: Distance<N>

  /**
   * The cache for the output results. Null encodes "not yet calculated", < 0 encodes "no such
   * distance exists".
   */
  protected val output: MutableMap<N, Double>

  /**
   * Specifies whether the values returned are the sum of the v-distances or the mean v-distance.
   */
  protected val averaging: Boolean

  /**
   * Specifies whether, for a node `v` with missing (null) distances, `v`'s
   * score should ignore the missing values or be set to 'null'. Defaults to 'true'.
   */
  protected val ignore_missing: Boolean

  /**
   * Specifies whether the values returned should ignore self-distances (distances from `v`
   * to itself). Defaults to 'true'.
   */
  protected val ignore_self_distances: Boolean

  // TODO: consider using a builder pattern rather than a bunch of parameters

  /**
   * Creates an instance with the specified graph, distance metric, and averaging behavior.
   *
   * @param graph The graph on which the node scores are to be calculated.
   * @param distance The metric to use for specifying the distance between pairs of nodes.
   * @param averaging Specifies whether the values returned is the sum of all v-distances or the
   *     mean v-distance.
   * @param ignore_missing Specifies whether scores for missing distances are to ignore missing
   *     distances or be set to null.
   * @param ignore_self_distances Specifies whether distances from a node to itself should be
   *     included in its score.
   */
  constructor(
    graph: Network<N, E>,
    distance: Distance<N>,
    averaging: Boolean,
    ignore_missing: Boolean,
    ignore_self_distances: Boolean
  ) {
    this.graph = graph.asGraph()
    this.distance = distance
    this.averaging = averaging
    this.ignore_missing = ignore_missing
    this.ignore_self_distances = ignore_self_distances
    this.output = HashMap()
  }

  /**
   * Creates an instance with the specified graph, distance metric, and averaging behavior.
   *
   * @param graph The graph on which the node scores are to be calculated.
   * @param distance The metric to use for specifying the distance between pairs of nodes.
   * @param averaging Specifies whether the values returned is the sum of all v-distances or the
   *     mean v-distance.
   * @param ignore_missing Specifies whether scores for missing distances are to ignore missing
   *     distances or be set to null.
   * @param ignore_self_distances Specifies whether distances from a node to itself should be
   *     included in its score.
   */
  constructor(
    graph: Graph<N>,
    distance: Distance<N>,
    averaging: Boolean,
    ignore_missing: Boolean,
    ignore_self_distances: Boolean
  ) {
    this.graph = graph
    this.distance = distance
    this.averaging = averaging
    this.ignore_missing = ignore_missing
    this.ignore_self_distances = ignore_self_distances
    this.output = HashMap()
  }

  /**
   * Equivalent to `this(graph, distance, averaging, true, true)`.
   *
   * @param graph The graph on which the node scores are to be calculated.
   * @param distance The metric to use for specifying the distance between pairs of nodes.
   * @param averaging Specifies whether the values returned is the sum of all v-distances or the
   *     mean v-distance.
   */
  constructor(graph: Network<N, E>, distance: Distance<N>, averaging: Boolean)
      : this(graph, distance, averaging, true, true)

  /**
   * Creates an instance with the specified graph and averaging behavior whose node distances are
   * calculated based on the specified edge weights.
   *
   * @param graph The graph on which the node scores are to be calculated.
   * @param edge_weights The edge weights to use for specifying the distance between pairs of nodes.
   * @param averaging Specifies whether the values returned is the sum of all v-distances or the
   *     mean v-distance.
   * @param ignore_missing Specifies whether scores for missing distances are to ignore missing
   *     distances or be set to null.
   * @param ignore_self_distances Specifies whether distances from a node to itself should be
   *     included in its score.
   */
  constructor(
    graph: Network<N, E>,
    edge_weights: Function<E, out Number>,
    averaging: Boolean,
    ignore_missing: Boolean,
    ignore_self_distances: Boolean
  ) : this(
    graph,
    DijkstraDistance<N, E>(graph, edge_weights),
    averaging,
    ignore_missing,
    ignore_self_distances
  )

  /**
   * Equivalent to `this(graph, edge_weights, averaging, true, true)`.
   *
   * @param graph The graph on which the node scores are to be calculated.
   * @param edge_weights The edge weights to use for specifying the distance between pairs of nodes.
   * @param averaging Specifies whether the values returned is the sum of all v-distances or the
   *     mean v-distance.
   */
  constructor(graph: Network<N, E>, edge_weights: Function<E, out Number>, averaging: Boolean)
      : this(graph, DijkstraDistance<N, E>(graph, edge_weights), averaging, true, true)

  /**
   * Creates an instance with the specified graph and averaging behavior whose node distances are
   * calculated on the unweighted graph.
   *
   * @param graph The graph on which the node scores are to be calculated.
   * @param averaging Specifies whether the values returned is the sum of all v-distances or the
   *     mean v-distance.
   * @param ignore_missing Specifies whether scores for missing distances are to ignore missing
   *     distances or be set to null.
   * @param ignore_self_distances Specifies whether distances from a node to itself should be
   *     included in its score.
   */
  constructor(
    graph: Graph<N>,
    averaging: Boolean,
    ignore_missing: Boolean,
    ignore_self_distances: Boolean
  ) : this(
    graph,
    UnweightedShortestPath<N>(graph),
    averaging,
    ignore_missing,
    ignore_self_distances
  )

  /**
   * Equivalent to `this(graph, averaging, true, true)`.
   *
   * @param graph The graph on which the node scores are to be calculated.
   * @param averaging Specifies whether the values returned is the sum of all v-distances or the
   *     mean v-distance.
   */
  constructor(graph: Graph<N>, averaging: Boolean)
      : this(graph, UnweightedShortestPath<N>(graph), averaging, true, true)

  /**
   * Calculates the score for the specified node. Returns `null` if there are missing
   * distances and such are not ignored by this instance.
   */
  override fun getNodeScore(v: N): Double {
    var value = output[v]
    if (value != null) {
      if (value < 0) {
        return Double.POSITIVE_INFINITY
      }
      return value
    }

    val v_distances = HashMap<N, Number>(distance.getDistanceMap(v))
    if (ignore_self_distances) {
      v_distances.remove(v)
    }

    // if we don't ignore missing distances and there aren't enough
    // distances, return infinity (shortcut)
    if (!ignore_missing) {
      val num_dests = graph.nodes().size - if (ignore_self_distances) 1 else 0
      if (v_distances.size != num_dests) {
        output[v] = -1.0
        return Double.POSITIVE_INFINITY
      }
    }

    var sum = 0.0
    for (w in graph.nodes()) {
      if (w == v && ignore_self_distances) {
        continue
      }
      val w_distance = v_distances[w]
      if (w_distance == null) {
        if (ignore_missing) {
          continue
        } else {
          output[v] = -1.0
          return Double.POSITIVE_INFINITY
        }
      } else {
        sum += w_distance.toDouble()
      }
    }
    value = sum
    if (averaging) {
      value /= v_distances.size
    }

    val score = if (value == 0.0) Double.POSITIVE_INFINITY else 1.0 / value
    output[v] = score

    return score
  }

  override fun nodeScores(): Map<N, Double> =
    Maps.asMap(graph.nodes()) { node -> getNodeScore(node!!) }
}
