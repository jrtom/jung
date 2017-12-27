/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import com.google.common.graph.Graph;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality;
import edu.uci.ics.jung.algorithms.scoring.util.NodeScoreTransformer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Statistics relating to node-node distances in a graph.
 *
 * <p>Formerly known as <code>GraphStatistics</code> in JUNG 1.x.
 *
 * @author Scott White
 * @author Joshua O'Madadhain
 */
public class DistanceStatistics {
  /**
   * For each node <code>v</code> in <code>graph</code>, calculates the average shortest path length
   * from <code>v</code> to all other nodes in <code>graph</code> using the metric specified by
   * <code>d</code>, and returns the results in a <code>Map</code> from nodes to <code>Double</code>
   * values. If there exists an ordered pair <code>&lt;u,v&gt;</code> for which <code>
   * d.getDistance(u,v)</code> returns <code>null</code>, then the average distance value for <code>
   * u</code> will be stored as <code>Double.POSITIVE_INFINITY</code>).
   *
   * <p>Does not include self-distances (path lengths from <code>v</code> to <code>v</code>).
   *
   * <p>To calculate the average distances, ignoring edge weights if any:
   *
   * <pre>
   * Map distances = DistanceStatistics.averageDistances(g, new UnweightedShortestPath(g));
   * </pre>
   *
   * To calculate the average distances respecting edge weights:
   *
   * <pre>
   * DijkstraShortestPath dsp = new DijkstraShortestPath(g, nev);
   * Map distances = DistanceStatistics.averageDistances(g, dsp);
   * </pre>
   *
   * where <code>nev</code> is an instance of <code>Transformer</code> that is used to fetch the
   * weight for each edge.
   *
   * @see edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath
   * @see edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance
   * @param graph the graph for which distances are to be calculated
   * @param d the distance metric to use for the calculation
   * @param <N> the node type
   * @param <E> the edge type
   * @return a map from each node to the mean distance to each other (reachable) node
   */
  public static <N, E> Function<N, Double> averageDistances(Network<N, E> graph, Distance<N> d) {
    final ClosenessCentrality<N, E> cc = new ClosenessCentrality<N, E>(graph, d);
    return new NodeScoreTransformer<N, Double>(cc);
  }

  /**
   * For each node <code>v</code> in <code>g</code>, calculates the average shortest path length
   * from <code>v</code> to all other nodes in <code>g</code>, ignoring edge weights.
   *
   * @see #diameter(Hypergraph)
   * @see edu.uci.ics.jung.algorithms.scoring.ClosenessCentrality
   * @param g the graph for which distances are to be calculated
   * @param <N> the node type
   * @param <E> the edge type
   * @return a map from each node to the mean distance to each other (reachable) node
   */
  public static <N, E> Function<N, Double> averageDistances(Network<N, E> g) {
    final ClosenessCentrality<N, E> cc =
        new ClosenessCentrality<N, E>(g, new UnweightedShortestPath<N>(g.asGraph()));
    return new NodeScoreTransformer<N, Double>(cc);
  }

  /**
   * Returns the diameter of <code>g</code> using the metric specified by <code>d</code>. The
   * diameter is defined to be the maximum, over all pairs of nodes <code>u,v</code>, of the length
   * of the shortest path from <code>u</code> to <code>v</code>. If the graph is disconnected (that
   * is, not all pairs of nodes are reachable from one another), the value returned will depend on
   * <code>use_max</code>: if <code>use_max == true</code>, the value returned will be the the
   * maximum shortest path length over all pairs of <b>connected</b> nodes; otherwise it will be
   * <code>Double.POSITIVE_INFINITY</code>.
   *
   * @param g the graph for which distances are to be calculated
   * @param d the distance metric to use for the calculation
   * @param use_max if {@code true}, return the maximum shortest path length for all graphs;
   *     otherwise, return {@code Double.POSITIVE_INFINITY} for disconnected graphs
   * @param <N> the node type
   * @return the longest distance from any node to any other
   */
  public static <N> double diameter(
      Graph<N> g, BiFunction<N, N, ? extends Number> d, boolean use_max) {
    double diameter = 0;
    // TODO: provide an undirected version
    for (N v : g.nodes()) {
      for (N w : g.nodes()) {
        if (v.equals(w)) {
          continue; // don't include self-distances
        }
        Number dist = d.apply(v, w);
        if (dist == null) {
          if (!use_max) {
            return Double.POSITIVE_INFINITY;
          }
        } else {
          diameter = Math.max(diameter, dist.doubleValue());
        }
      }
    }
    return diameter;
  }

  /**
   * Returns the diameter of <code>g</code> using the metric specified by <code>d</code>. The
   * diameter is defined to be the maximum, over all pairs of nodes <code>u,v</code>, of the length
   * of the shortest path from <code>u</code> to <code>v</code>, or <code>
   * Double.POSITIVE_INFINITY</code> if any of these distances do not exist.
   *
   * @see #diameter(Graph, BiFunction, boolean)
   * @param g the graph for which distances are to be calculated
   * @param distance the distance metric to use for the calculation
   * @param <N> the node type
   * @return the longest distance from any node to any other
   */
  public static <N> double diameter(Graph<N> g, BiFunction<N, N, ? extends Number> distance) {
    return diameter(g, distance, false);
  }

  /**
   * Returns the diameter of <code>g</code>, ignoring edge weights.
   *
   * @see #diameter(Graph, BiFunction, boolean)
   * @param g the graph for which distances are to be calculated
   * @param <N> the node type
   * @return the longest distance from any node to any other
   */
  public static <N> double diameter(Graph<N> g) {
    return diameter(g, new UnweightedShortestPath<N>(g)::getDistance);
  }
}
