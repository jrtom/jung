/*
 * Created on Sep 19, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.metrics;

import static com.google.common.collect.Sets.difference;

import com.google.common.graph.Graph;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Calculates some of the measures from Burt's text "Structural Holes: The Social Structure of
 * Competition".
 *
 * <p><b>Notes</b>:
 *
 * <ul>
 *   <li>Each of these measures assumes that each edge has an associated non-null weight whose value
 *       is accessed through the specified {@code Function} instance.
 *   <li>Nonexistent edges are treated as edges with weight 0 for purposes of edge weight
 *       calculations.
 * </ul>
 *
 * <p>Based on code donated by Jasper Voskuilen and Diederik van Liere of the Department of
 * Information and Decision Sciences at Erasmus University.
 *
 * @author Joshua O'Madadhain
 * @author Jasper Voskuilen
 * @see "Ronald Burt, Structural Holes: The Social Structure of Competition"
 * @author Tom Nelson - converted to jung2
 */
public class StructuralHoles<N> {

  protected BiFunction<N, N, ? extends Number> edgeWeights;
  protected Graph<N> g;

  /**
   * @param graph the graph for which the metrics are to be calculated
   * @param nev the edge weights
   * @deprecated use {@code Metrics.structuralHoles(graph, edgeWeights)}
   */
  public StructuralHoles(Graph<N> graph, BiFunction<N, N, ? extends Number> edgeWeights) {
    this.g = graph;
    this.edgeWeights = edgeWeights;
  }

  /**
   * Burt's measure of the effective size of a node's network. Essentially, the number of neighbors
   * minus the average degree of those in {@code v}'s neighbor set, not counting ties to {@code v}.
   * Formally:
   *
   * <pre>
   * effectiveSize(v) = v.degree() - (sum_{u in N(v)} sum_{w in N(u), w !=u,v} p(v,w)*m(u,w))
   * </pre>
   *
   * where
   *
   * <ul>
   *   <li>{@code N(a) = a.adjacentNodes()}
   *   <li>{@code p(v,w)} = normalized mutual edge weight of {@code v} and {@code w}
   *   <li>{@code m(u,w)} = maximum-scaled mutual edge weight of {@code u} and {@code w}
   * </ul>
   *
   * @param v the node whose properties are being measured
   * @return the effective size of the node's network
   * @see #normalizedMutualEdgeWeight(Object, Object)
   * @see #maxScaledMutualEdgeWeight(Object, Object)
   */
  public double effectiveSize(N v) {
    double result = g.degree(v);
    for (N u : g.adjacentNodes(v)) {
      for (N w : difference(g.adjacentNodes(u), Set.of(u, v))) {
        result -= normalizedMutualEdgeWeight(v, w) * maxScaledMutualEdgeWeight(u, w);
      }
    }
    return result;
  }

  /**
   * Returns the effective size of {@code v} divided by the number of alters in {@code v} 's
   * network. (In other words, {@code effectiveSize(v) / v.degree()}.) If {@code v.degree() == 0},
   * returns 0.
   *
   * @param v the node whose properties are being measured
   * @return the effective size of the node divided by its degree
   */
  public double efficiency(N v) {
    double degree = g.degree(v);

    if (degree == 0) {
      return 0;
    } else {
      return effectiveSize(v) / degree;
    }
  }

  /**
   * Burt's constraint measure (equation 2.4, page 55 of Burt, 1992). Essentially a measure of the
   * extent to which {@code v} is invested in people who are invested in other of {@code v}'s alters
   * (neighbors). The "constraint" is characterized by a lack of primary holes around each neighbor.
   * Formally:
   *
   * <pre>
   * constraint(v) = sum_{w in MP(v), w != v} localConstraint(v,w)
   * </pre>
   *
   * where MP(v) is the subset of v's neighbors that are both predecessors and successors of v.
   *
   * @see #localConstraint(Object, Object)
   * @param v the node whose properties are being measured
   * @return the constraint of the node
   */
  public double constraint(N v) {
    double result = 0;
    for (N w : difference(g.successors(v), Set.of(v))) {
      if (g.predecessors(v).contains(w)) {
        result += localConstraint(v, w);
      }
    }

    return result;
  }

  /**
   * Calculates the hierarchy value for a given node. Returns {@code NaN} when {@code v}'s degree is
   * 0, and 1 when {@code v}'s degree is 1. Formally:
   *
   * <pre>
   * hierarchy(v) = (sum_{v in N(v), w != v} s(v,w) * log(s(v,w))}) / (v.degree() * Math.log(v.degree())
   * </pre>
   *
   * where
   *
   * <ul>
   *   <li>{@code N(v) = v.adjacentNodes()}
   *   <li>{@code s(v,w) = localConstraint(v,w) / (aggregateConstraint(v) / v.degree())}
   * </ul>
   *
   * @see #localConstraint(Object, Object)
   * @see #aggregateConstraint(Object)
   * @param v the node whose properties are being measured
   * @return the hierarchy value for a given node
   */
  public double hierarchy(N v) {
    double v_degree = g.degree(v);

    if (v_degree == 0) {
      return Double.NaN;
    }
    if (v_degree == 1) {
      return 1;
    }

    double v_constraint = aggregateConstraint(v);

    double numerator = 0;
    for (N w : difference(g.adjacentNodes(v), Set.of(v))) {
      double sl_constraint = localConstraint(v, w) / (v_constraint / v_degree);
      numerator += sl_constraint * Math.log(sl_constraint);
    }

    return numerator / (v_degree * Math.log(v_degree));
  }

  /**
   * Returns the local constraint on {@code v1} from a lack of primary holes around its neighbor
   * {@code v2}. Based on Burt's equation 2.4. Formally:
   *
   * <pre>
   * localConstraint(v1, v2) = ( p(v1,v2) + ( sum_{w in N(v)} p(v1,w) * p(w, v2) ) )^2
   * </pre>
   *
   * where
   *
   * <ul>
   *   <li>{@code N(v) = v.adjacentNodes()}
   *   <li>{@code p(v,w) =} normalized mutual edge weight of v and w
   * </ul>
   *
   * @param v1 the first node whose local constraint is desired
   * @param v2 the second node whose local constraint is desired
   * @return the local constraint on (v1, v2)
   * @see #normalizedMutualEdgeWeight(Object, Object)
   */
  public double localConstraint(N v1, N v2) {
    double nmew_vw = normalizedMutualEdgeWeight(v1, v2);
    double inner_result = 0;
    for (N w : g.adjacentNodes(v1)) {
      inner_result += normalizedMutualEdgeWeight(v1, w) * normalizedMutualEdgeWeight(w, v2);
    }
    return (nmew_vw + inner_result) * (nmew_vw + inner_result);
  }

  /**
   * The aggregate constraint on {@code v}. Based on Burt's equation 2.7. Formally:
   *
   * <pre>
   * aggregateConstraint(v) = sum_{w in N(v)} localConstraint(v,w) * O(w)
   * </pre>
   *
   * where
   *
   * <ul>
   *   <li>{@code N(v) = v.adjacentNodes()}
   *   <li>{@code O(w) = organizationalMeasure(w)}
   * </ul>
   *
   * @param v the node whose properties are being measured
   * @return the aggregate constraint on v
   */
  public double aggregateConstraint(N v) {
    double result = 0;
    for (N w : g.adjacentNodes(v)) {
      result += localConstraint(v, w) * organizationalMeasure(g, w);
    }
    return result;
  }

  /**
   * A measure of the organization of individuals within the subgraph centered on {@code v}. Burt's
   * text suggests that this is in some sense a measure of how "replaceable" {@code v} is by some
   * other element of this subgraph. Should be a number in the closed interval [0,1].
   *
   * <p>This implementation returns 1. Users may wish to override this method in order to define
   * their own behavior.
   *
   * @param g the subgraph centered on v
   * @param v the node whose properties are being measured
   * @return 1.0 (in this implementation)
   */
  protected double organizationalMeasure(Graph<N> g, N v) {
    return 1.0;
  }

  /**
   * Returns the proportion of {@code v1}'s network time and energy invested in the relationship
   * with {@code v2}. Formally:
   *
   * <pre>
   * normalizedMutualEdgeWeight(a,b) = mutual_weight(a,b) / (sum_c mutual_weight(a,c))
   * </pre>
   *
   * Returns 0 if either numerator or denominator = 0, or if {@code v1.equals(v2)}.
   *
   * @see #mutualWeight(Object, Object)
   * @param v1 the first node of the pair whose property is being measured
   * @param v2 the second node of the pair whose property is being measured
   * @return the normalized mutual edge weight between v1 and v2
   */
  protected double normalizedMutualEdgeWeight(N v1, N v2) {
    if (v1 == v2) {
      return 0;
    }

    double numerator = mutualWeight(v1, v2);

    if (numerator == 0) {
      return 0;
    }

    double denominator = 0;
    for (N v : g.adjacentNodes(v1)) {
      denominator += mutualWeight(v1, v);
    }
    if (denominator == 0) {
      return 0;
    }

    return numerator / denominator;
  }

  /**
   * Returns the weight of the edge from {@code v1} to {@code v2} plus the weight of the edge from
   * {@code v2} to {@code v1}; if either edge does not exist, it is treated as an edge with weight
   * 0. Undirected edges are treated as two antiparallel directed edges (that is, if there is one
   * undirected edge with weight {@code w} connecting {@code v1} to {@code v2}, the value returned
   * is 2{@code w}). If parallel edges are present, chooses one arbitrarily.
   *
   * @param v1 the first node of the pair whose property is being measured
   * @param v2 the second node of the pair whose property is being measured
   * @return the summed weights of the edges {@code<v1, v2>} and {@code <v2, v1>}
   * @throws NullPointerException if either edge is not assigned a weight by the
   *     constructor-specified {@code Function}.
   */
  protected double mutualWeight(N v1, N v2) {
    double weight = 0;
    if (g.successors(v1).contains(v2)) {
      weight += edgeWeights.apply(v1, v2).doubleValue();
    }
    if (g.successors(v2).contains(v1)) {
      weight += edgeWeights.apply(v2, v1).doubleValue();
    }

    return weight;
  }

  /**
   * The marginal strength of v1's relation with contact v2. Formally:
   *
   * <pre>
   * normalized_mutual_weight = mutual_weight(a,b) / (max_c mutual_weight(a,c))
   * </pre>
   *
   * Returns 0 if either numerator or denominator is 0, or if {@code v1 == v2}.
   *
   * @param v1 the first node of the pair whose property is being measured
   * @param v2 the second node of the pair whose property is being measured
   * @return the marginal strength of v1's relation with v2
   * @see #mutualWeight(Object, Object)
   */
  protected double maxScaledMutualEdgeWeight(N v1, N v2) {
    if (v1 == v2) {
      return 0;
    }

    double numerator = mutualWeight(v1, v2);

    if (numerator == 0) {
      return 0;
    }

    double denominator = 0;
    for (N w : difference(g.adjacentNodes(v1), Set.of(v2))) {
      denominator = Math.max(numerator, mutualWeight(v1, w));
    }

    if (denominator == 0) {
      return 0;
    }

    return numerator / denominator;
  }
}
