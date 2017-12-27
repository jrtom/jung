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
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.graph.Network;
import java.util.function.Function;

/**
 * Calculates eigenvector centrality for each node in the graph. The 'eigenvector centrality' for a
 * node is defined as the fraction of time that a random walk(er) will spend at that node over an
 * infinite time horizon. Assumes that the graph is strongly connected.
 */
public class EigenvectorCentrality<N, E> extends PageRank<N, E> {
  /**
   * Creates an instance with the specified graph and edge weights. The outgoing edge weights for
   * each edge must sum to 1. (See <code>UniformDegreeWeight</code> for one way to handle this for
   * undirected graphs.)
   *
   * @param graph the graph for which the centrality is to be calculated
   * @param edge_weights the edge weights
   */
  public EigenvectorCentrality(Network<N, E> graph, Function<E, ? extends Number> edge_weights) {
    super(graph, edge_weights, 0);
    acceptDisconnectedGraph(false);
  }

  /**
   * Creates an instance with the specified graph and default edge weights. (Default edge weights:
   * <code>UniformDegreeWeight</code>.)
   *
   * @param graph the graph for which the centrality is to be calculated.
   */
  public EigenvectorCentrality(Network<N, E> graph) {
    super(graph, 0);
    acceptDisconnectedGraph(false);
  }
}
