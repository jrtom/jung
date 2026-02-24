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

import com.google.common.graph.Network
import java.util.function.Function

/**
 * Calculates eigenvector centrality for each node in the graph. The 'eigenvector centrality' for a
 * node is defined as the fraction of time that a random walk(er) will spend at that node over an
 * infinite time horizon. Assumes that the graph is strongly connected.
 */
class EigenvectorCentrality<N : Any, E : Any> : PageRank<N, E> {
  /**
   * Creates an instance with the specified graph and edge weights. The outgoing edge weights for
   * each edge must sum to 1. (See `UniformDegreeWeight` for one way to handle this for
   * undirected graphs.)
   *
   * @param graph the graph for which the centrality is to be calculated
   * @param edge_weights the edge weights
   */
  constructor(graph: Network<N, E>, edge_weights: Function<E, out Number>) : super(graph, edge_weights, 0.0) {
    acceptDisconnectedGraph(false)
  }

  /**
   * Creates an instance with the specified graph and default edge weights. (Default edge weights:
   * `UniformDegreeWeight`.)
   *
   * @param graph the graph for which the centrality is to be calculated.
   */
  constructor(graph: Network<N, E>) : super(graph, 0.0) {
    acceptDisconnectedGraph(false)
  }
}
