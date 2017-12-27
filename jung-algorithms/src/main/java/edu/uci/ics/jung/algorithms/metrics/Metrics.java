/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * <p>All rights reserved.
 *
 * <p>This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jun 7, 2008
 */
package edu.uci.ics.jung.algorithms.metrics;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.Graph;

/** A class consisting of static methods for calculating graph metrics. */
public class Metrics {
  /**
   * Returns a <code>Map</code> of nodes to their clustering coefficients. The clustering
   * coefficient cc(v) of a node v is defined as follows:
   *
   * <ul>
   *   <li><code>degree(v) == {0,1}</code>: 0
   *   <li><code>degree(v) == n, n &gt;= 2</code>: given S, the set of neighbors of <code>v</code>:
   *       cc(v) = (the sum over all w in S of the number of other elements of w that are neighbors
   *       of w) / ((|S| * (|S| - 1) / 2). Less formally, the fraction of <code>v</code>'s neighbors
   *       that are also neighbors of each other.
   * </ul>
   *
   * <p><b>Note</b>: This algorithm treats its argument as an undirected graph; edge direction is
   * ignored.
   *
   * @param graph the graph whose clustering coefficients are to be calculated
   * @param <N> the node type
   * @param <E> the edge type
   * @return the clustering coefficient for each node
   * @see "The structure and function of complex networks, M.E.J. Newman,
   *     aps.arxiv.org/abs/cond-mat/0303516"
   */
  public static <N> ImmutableMap<N, Double> clusteringCoefficients(Graph<N> graph) {
    ImmutableMap.Builder<N, Double> coefficients = ImmutableMap.builder();

    for (N v : graph.nodes()) {
      int n = graph.degree(v);
      if (n < 2) {
        coefficients.put(v, new Double(0));
      } else {
        int edgeCount = 0;
        for (N w : graph.adjacentNodes(v)) {
          if (!w.equals(v)) {
            for (N x : graph.adjacentNodes(v)) {
              // TODO: replace with hasEdge() once it's ready
              if (!w.equals(x) && graph.adjacentNodes(w).contains(x)) {
                edgeCount++;
              }
            }
          }
        }
        double possible_edges = (n * (n - 1)) / 2.0;
        coefficients.put(v, new Double(edgeCount / possible_edges));
      }
    }

    return coefficients.build();
  }
}
