/*
 * Created on Jul 6, 2007
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

import com.google.common.collect.Maps;
import com.google.common.graph.Network;
import java.util.Map;

/**
 * Assigns a score to each node equal to its degree.
 *
 * @param <N> the node type
 */
public class DegreeScorer<N> implements NodeScorer<N, Integer> {
  // TODO: Graph and Network degree are different, so we either need to
  // provide separate constructors or separate classes
  /** The graph for which scores are to be generated. */
  protected Network<N, ?> graph;

  /**
   * Creates an instance for the specified graph.
   *
   * @param graph the input graph
   */
  public DegreeScorer(Network<N, ?> graph) {
    this.graph = graph;
  }

  /**
   * Returns the degree of the node.
   *
   * @return the degree of the node
   */
  public Integer getNodeScore(N node) {
    return graph.degree(node);
  }

  public Map<N, Integer> nodeScores() {
    return Maps.asMap(graph.nodes(), node -> graph.degree(node));
  }
}
