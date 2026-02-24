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
package edu.uci.ics.jung.algorithms.metrics

import com.google.common.collect.Sets.difference
import com.google.common.graph.Graph
import java.util.function.BiFunction

/**
 * Calculates some of the measures from Burt's text "Structural Holes: The Social Structure of
 * Competition".
 *
 * **Notes**:
 *
 * * Each of these measures assumes that each edge has an associated non-null weight whose value
 *   is accessed through the specified `Function` instance.
 * * Nonexistent edges are treated as edges with weight 0 for purposes of edge weight
 *   calculations.
 *
 * Based on code donated by Jasper Voskuilen and Diederik van Liere of the Department of
 * Information and Decision Sciences at Erasmus University.
 *
 * @author Joshua O'Madadhain
 * @author Jasper Voskuilen
 * @see "Ronald Burt, Structural Holes: The Social Structure of Competition"
 * @author Tom Nelson - converted to jung2
 */
open class StructuralHoles<N : Any>(
    protected val g: Graph<N>,
    protected val edgeWeights: BiFunction<N, N, out Number>,
) {
    /**
     * Burt's measure of the effective size of a node's network. Essentially, the number of neighbors
     * minus the average degree of those in `v`'s neighbor set, not counting ties to `v`.
     * Formally:
     *
     * ```
     * effectiveSize(v) = v.degree() - (sum_{u in N(v)} sum_{w in N(u), w !=u,v} p(v,w)*m(u,w))
     * ```
     *
     * where
     *
     * * `N(a) = a.adjacentNodes()`
     * * `p(v,w)` = normalized mutual edge weight of `v` and `w`
     * * `m(u,w)` = maximum-scaled mutual edge weight of `u` and `w`
     *
     * @param v the node whose properties are being measured
     * @return the effective size of the node's network
     * @see normalizedMutualEdgeWeight
     * @see maxScaledMutualEdgeWeight
     */
    fun effectiveSize(v: N): Double {
        var result = g.degree(v).toDouble()
        for (u in g.adjacentNodes(v)) {
            for (w in difference(g.adjacentNodes(u), setOf(u, v))) {
                result -= normalizedMutualEdgeWeight(v, w) * maxScaledMutualEdgeWeight(u, w)
            }
        }
        return result
    }

    /**
     * Returns the effective size of `v` divided by the number of alters in `v`'s
     * network. (In other words, `effectiveSize(v) / v.degree()`.) If `v.degree() == 0`,
     * returns 0.
     *
     * @param v the node whose properties are being measured
     * @return the effective size of the node divided by its degree
     */
    fun efficiency(v: N): Double {
        val degree = g.degree(v).toDouble()
        return if (degree == 0.0) 0.0 else effectiveSize(v) / degree
    }

    /**
     * Burt's constraint measure (equation 2.4, page 55 of Burt, 1992). Essentially a measure of the
     * extent to which `v` is invested in people who are invested in other of `v`'s alters
     * (neighbors). The "constraint" is characterized by a lack of primary holes around each neighbor.
     * Formally:
     *
     * ```
     * constraint(v) = sum_{w in MP(v), w != v} localConstraint(v,w)
     * ```
     *
     * where MP(v) is the subset of v's neighbors that are both predecessors and successors of v.
     *
     * @see localConstraint
     * @param v the node whose properties are being measured
     * @return the constraint of the node
     */
    fun constraint(v: N): Double {
        var result = 0.0
        for (w in difference(g.successors(v), setOf(v))) {
            if (g.predecessors(v).contains(w)) {
                result += localConstraint(v, w)
            }
        }
        return result
    }

    /**
     * Calculates the hierarchy value for a given node. Returns `NaN` when `v`'s degree is
     * 0, and 1 when `v`'s degree is 1. Formally:
     *
     * ```
     * hierarchy(v) = (sum_{v in N(v), w != v} s(v,w) * log(s(v,w))}) / (v.degree() * Math.log(v.degree())
     * ```
     *
     * where
     *
     * * `N(v) = v.adjacentNodes()`
     * * `s(v,w) = localConstraint(v,w) / (aggregateConstraint(v) / v.degree())`
     *
     * @see localConstraint
     * @see aggregateConstraint
     * @param v the node whose properties are being measured
     * @return the hierarchy value for a given node
     */
    fun hierarchy(v: N): Double {
        val vDegree = g.degree(v).toDouble()

        if (vDegree == 0.0) {
            return Double.NaN
        }
        if (vDegree == 1.0) {
            return 1.0
        }

        val vConstraint = aggregateConstraint(v)

        var numerator = 0.0
        for (w in difference(g.adjacentNodes(v), setOf(v))) {
            val slConstraint = localConstraint(v, w) / (vConstraint / vDegree)
            numerator += slConstraint * Math.log(slConstraint)
        }

        return numerator / (vDegree * Math.log(vDegree))
    }

    /**
     * Returns the local constraint on `v1` from a lack of primary holes around its neighbor
     * `v2`. Based on Burt's equation 2.4. Formally:
     *
     * ```
     * localConstraint(v1, v2) = ( p(v1,v2) + ( sum_{w in N(v)} p(v1,w) * p(w, v2) ) )^2
     * ```
     *
     * where
     *
     * * `N(v) = v.adjacentNodes()`
     * * `p(v,w) =` normalized mutual edge weight of v and w
     *
     * @param v1 the first node whose local constraint is desired
     * @param v2 the second node whose local constraint is desired
     * @return the local constraint on (v1, v2)
     * @see normalizedMutualEdgeWeight
     */
    fun localConstraint(v1: N, v2: N): Double {
        val nmewVw = normalizedMutualEdgeWeight(v1, v2)
        var innerResult = 0.0
        for (w in g.adjacentNodes(v1)) {
            innerResult += normalizedMutualEdgeWeight(v1, w) * normalizedMutualEdgeWeight(w, v2)
        }
        return (nmewVw + innerResult) * (nmewVw + innerResult)
    }

    /**
     * The aggregate constraint on `v`. Based on Burt's equation 2.7. Formally:
     *
     * ```
     * aggregateConstraint(v) = sum_{w in N(v)} localConstraint(v,w) * O(w)
     * ```
     *
     * where
     *
     * * `N(v) = v.adjacentNodes()`
     * * `O(w) = organizationalMeasure(w)`
     *
     * @param v the node whose properties are being measured
     * @return the aggregate constraint on v
     */
    fun aggregateConstraint(v: N): Double {
        var result = 0.0
        for (w in g.adjacentNodes(v)) {
            result += localConstraint(v, w) * organizationalMeasure(g, w)
        }
        return result
    }

    /**
     * A measure of the organization of individuals within the subgraph centered on `v`. Burt's
     * text suggests that this is in some sense a measure of how "replaceable" `v` is by some
     * other element of this subgraph. Should be a number in the closed interval [0,1].
     *
     * This implementation returns 1. Users may wish to override this method in order to define
     * their own behavior.
     *
     * @param g the subgraph centered on v
     * @param v the node whose properties are being measured
     * @return 1.0 (in this implementation)
     */
    protected open fun organizationalMeasure(g: Graph<N>, v: N): Double = 1.0

    /**
     * Returns the proportion of `v1`'s network time and energy invested in the relationship
     * with `v2`. Formally:
     *
     * ```
     * normalizedMutualEdgeWeight(a,b) = mutual_weight(a,b) / (sum_c mutual_weight(a,c))
     * ```
     *
     * Returns 0 if either numerator or denominator = 0, or if `v1.equals(v2)`.
     *
     * @see mutualWeight
     * @param v1 the first node of the pair whose property is being measured
     * @param v2 the second node of the pair whose property is being measured
     * @return the normalized mutual edge weight between v1 and v2
     */
    protected open fun normalizedMutualEdgeWeight(v1: N, v2: N): Double {
        if (v1 === v2) {
            return 0.0
        }

        val numerator = mutualWeight(v1, v2)

        if (numerator == 0.0) {
            return 0.0
        }

        var denominator = 0.0
        for (v in g.adjacentNodes(v1)) {
            denominator += mutualWeight(v1, v)
        }
        if (denominator == 0.0) {
            return 0.0
        }

        return numerator / denominator
    }

    /**
     * Returns the weight of the edge from `v1` to `v2` plus the weight of the edge from
     * `v2` to `v1`; if either edge does not exist, it is treated as an edge with weight
     * 0. Undirected edges are treated as two antiparallel directed edges (that is, if there is one
     * undirected edge with weight `w` connecting `v1` to `v2`, the value returned
     * is 2`w`). If parallel edges are present, chooses one arbitrarily.
     *
     * @param v1 the first node of the pair whose property is being measured
     * @param v2 the second node of the pair whose property is being measured
     * @return the summed weights of the edges `<v1, v2>` and `<v2, v1>`
     * @throws NullPointerException if either edge is not assigned a weight by the
     *     constructor-specified `Function`.
     */
    protected open fun mutualWeight(v1: N, v2: N): Double {
        var weight = 0.0
        if (g.successors(v1).contains(v2)) {
            weight += edgeWeights.apply(v1, v2).toDouble()
        }
        if (g.successors(v2).contains(v1)) {
            weight += edgeWeights.apply(v2, v1).toDouble()
        }
        return weight
    }

    /**
     * The marginal strength of v1's relation with contact v2. Formally:
     *
     * ```
     * normalized_mutual_weight = mutual_weight(a,b) / (max_c mutual_weight(a,c))
     * ```
     *
     * Returns 0 if either numerator or denominator is 0, or if `v1 == v2`.
     *
     * @param v1 the first node of the pair whose property is being measured
     * @param v2 the second node of the pair whose property is being measured
     * @return the marginal strength of v1's relation with v2
     * @see mutualWeight
     */
    protected open fun maxScaledMutualEdgeWeight(v1: N, v2: N): Double {
        if (v1 === v2) {
            return 0.0
        }

        val numerator = mutualWeight(v1, v2)

        if (numerator == 0.0) {
            return 0.0
        }

        var denominator = 0.0
        for (w in difference(g.adjacentNodes(v1), setOf(v2))) {
            denominator = Math.max(numerator, mutualWeight(v1, w))
        }

        if (denominator == 0.0) {
            return 0.0
        }

        return numerator / denominator
    }
}
