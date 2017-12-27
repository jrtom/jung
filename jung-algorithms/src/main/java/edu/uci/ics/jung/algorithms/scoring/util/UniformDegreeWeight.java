/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * <p>All rights reserved.
 *
 * <p>This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jul 14, 2008
 */
package edu.uci.ics.jung.algorithms.scoring.util;

import com.google.common.graph.Network;
import java.util.function.Function;

/**
 * An edge weight function that assigns weights as uniform transition probabilities:
 *
 * <ul>
 *   <li>for undirected edges, returns 1/degree(v) (where 'v' is the node in the VEPair)
 *   <li>for directed edges, returns 1/outdegree(source(e)) (where 'e' is the edge in the VEPair)
 */
public class UniformDegreeWeight<N, E> implements Function<VEPair<N, E>, Double> {
  private Network<N, E> graph;

  /** @param graph the graph for which an instance is being created */
  public UniformDegreeWeight(Network<N, E> graph) {
    this.graph = graph;
  }

  public Double apply(VEPair<N, E> ve_pair) {
    E e = ve_pair.getE();
    N v = ve_pair.getV();
    return graph.isDirected()
        ? 1.0 / graph.outDegree(graph.incidentNodes(e).source())
        : 1.0 / graph.degree(v);
  }
}
