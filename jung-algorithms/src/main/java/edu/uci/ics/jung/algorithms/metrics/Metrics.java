/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * <p>All rights reserved.
 *
 * <p>This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jun 7, 2008
 */
package edu.uci.ics.jung.algorithms.metrics;

import static com.google.common.collect.Sets.difference;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.Graph;
import java.util.Set;
import java.util.function.BiFunction;

/** A class consisting of static methods for calculating graph metrics. */
public class Metrics {
  /**
   * Returns a {@code Map} of nodes to their clustering coefficients. The clustering coefficient of
   * a node {@code v} is defined as the number of {@code v}'s neighbors that are also neighbors of
   * each other, divided by the maximum possible number of such mutual neighbors. Formally, given
   * {@code N(v)} as the neighbors of {@code v} and |N(v)| as the size of {@code N(v)} (also known
   * as the degree of {@code v}):
   *
   * <ul>
   *   <li>{@code degree(v) < 2} 0 or 1: 0
   *   <li>{@code degree(v) >= 2}: (sum_{w in N(v)} sum_{x in N(w)} {@code
   *       graph.hasEdgeConnecting(w,x) ? 1 : 0}) / ((|N(v)| * (|N(v)| - 1) / 2).
   * </ul>
   *
   * <p><b>Note</b>: This algorithm treats its argument as an undirected graph; edge direction is
   * ignored.
   *
   * @param graph the graph whose clustering coefficients are to be calculated
   * @param <N> the node type
   * @return the clustering coefficient for each node
   * @see "The structure and function of complex networks, M.E.J. Newman,
   *     aps.arxiv.org/abs/cond-mat/0303516"
   */
  public static <N> ImmutableMap<N, Double> clusteringCoefficients(Graph<N> graph) {
    ImmutableMap.Builder<N, Double> coefficients = ImmutableMap.builder();

    for (N v : graph.nodes()) {
      int n = graph.degree(v);
      if (n < 2) {
        coefficients.put(v, Double.valueOf(0));
      } else {
        int edgeCount = 0;
        for (N w : difference(graph.adjacentNodes(v), Set.of(v))) {
          for (N x : difference(graph.adjacentNodes(v), Set.of(w))) {
            if (graph.adjacentNodes(x).contains(w)) {
              edgeCount++;
            }
          }
        }
        double possible_edges = (n * (n - 1)) / 2.0;
        coefficients.put(v, Double.valueOf(edgeCount / possible_edges));
      }
    }

    return coefficients.build();
  }

  /**
   * Returns an instance of {@code StructuralHoles} with the specified edge weight {@code
   * BiFunction}.
   */
  @SuppressWarnings("deprecation")
  public static <N> StructuralHoles<N> structuralHoles(
      Graph<N> graph, BiFunction<N, N, ? extends Number> edgeWeights) {
    // TODO(jrtom): consider adding an overload that takes a Function<EndpointPair<N>, Number>
    // instead
    return new StructuralHoles<N>(graph, edgeWeights);
  }

  /**
   * Returns an array whose ith element (for i in [1,16]) is the number of occurrences of the
   * corresponding triad type in {@code graph}. (The 0th element is not meaningful; this array is
   * effectively 1-based.)
   *
   * <p>See {@link TriadicCensus} for more information.
   *
   * @param graph the graph whose properties are being measured
   * @param <N> the node type
   * @return an array encoding the number of occurrences of each triad type
   */
  public static <N> long[] triadicCensus(Graph<N> graph) {
    return TriadicCensus.getCounts(graph);
  }
}
