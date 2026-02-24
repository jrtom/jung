/*
 * Created on Jul 12, 2007
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

import com.google.common.graph.Graph
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.shortestpath.Distance
import java.util.function.Function

/** Assigns scores to each node according to the sum of its distances to all other nodes. */
class BarycenterScorer<N : Any, E : Any> : DistanceCentralityScorer<N, E> {
  /**
   * Creates an instance with the specified graph and distance metric.
   *
   * @param graph the input graph
   * @param distance the distance metric to use
   */
  constructor(graph: Network<N, E>, distance: Distance<N>) : super(graph, distance, false)

  /**
   * Creates an instance with the specified graph and edge weights. Will generate a `Distance`
   * metric internally based on the edge weights.
   *
   * @param graph the input graph
   * @param edge_weights the edge weights to use to calculate node/node distances
   */
  constructor(graph: Network<N, E>, edge_weights: Function<E, out Number>)
      : super(graph, edge_weights, false)

  /**
   * Creates an instance with the specified graph. Will generate a `Distance` metric
   * internally assuming that the graph is unweighted.
   *
   * @param graph the input graph
   */
  constructor(graph: Graph<N>) : super(graph, false)
}
