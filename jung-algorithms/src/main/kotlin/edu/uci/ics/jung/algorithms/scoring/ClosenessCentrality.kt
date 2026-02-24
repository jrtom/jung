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

/**
 * Assigns scores to each node based on the mean distance to each other node.
 *
 * @author Joshua O'Madadhain
 */
class ClosenessCentrality<N : Any, E : Any> : DistanceCentralityScorer<N, E> {

  /**
   * Creates an instance using the specified node/node distance metric.
   *
   * @param graph the input
   * @param distance the node/node distance metric.
   */
  constructor(graph: Network<N, E>, distance: Distance<N>) : super(graph, distance, true)

  /**
   * Creates an instance which measures distance using the specified edge weights.
   *
   * @param graph the input graph
   * @param edge_weights the edge weights to be used to determine node/node distances
   */
  constructor(graph: Network<N, E>, edge_weights: Function<E, out Number>)
      : super(graph, edge_weights, true)

  /**
   * Creates an instance which measures distance on the graph without edge weights.
   *
   * @param graph the graph whose nodes' centrality scores will be calculated
   */
  constructor(graph: Graph<N>) : super(graph, true)
}
