/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Jun 7, 2008
 */
package edu.uci.ics.jung.algorithms.metrics

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Sets.difference
import com.google.common.graph.Graph
import java.util.function.BiFunction

/** A class consisting of static methods for calculating graph metrics. */
object Metrics {

    /**
     * Returns a `Map` of nodes to their clustering coefficients. The clustering coefficient of
     * a node `v` is defined as the number of `v`'s neighbors that are also neighbors of
     * each other, divided by the maximum possible number of such mutual neighbors. Formally, given
     * `N(v)` as the neighbors of `v` and |N(v)| as the size of `N(v)` (also known
     * as the degree of `v`):
     *
     * * `degree(v) < 2` 0 or 1: 0
     * * `degree(v) >= 2`: (sum_{w in N(v)} sum_{x in N(w)} `graph.hasEdgeConnecting(w,x) ? 1 : 0`) / ((|N(v)| * (|N(v)| - 1) / 2).
     *
     * **Note**: This algorithm treats its argument as an undirected graph; edge direction is
     * ignored.
     *
     * @param graph the graph whose clustering coefficients are to be calculated
     * @param N the node type
     * @return the clustering coefficient for each node
     * @see "The structure and function of complex networks, M.E.J. Newman,
     *     aps.arxiv.org/abs/cond-mat/0303516"
     */
    @JvmStatic
    fun <N : Any> clusteringCoefficients(graph: Graph<N>): ImmutableMap<N, Double> {
        val coefficients = ImmutableMap.builder<N, Double>()

        for (v in graph.nodes()) {
            val n = graph.degree(v)
            if (n < 2) {
                coefficients.put(v, 0.0)
            } else {
                var edgeCount = 0
                for (w in difference(graph.adjacentNodes(v), setOf(v))) {
                    for (x in difference(graph.adjacentNodes(v), setOf(w))) {
                        if (graph.adjacentNodes(x).contains(w)) {
                            edgeCount++
                        }
                    }
                }
                val possibleEdges = (n * (n - 1)) / 2.0
                coefficients.put(v, edgeCount / possibleEdges)
            }
        }

        return coefficients.build()
    }

    /**
     * Returns an instance of `StructuralHoles` with the specified edge weight
     * `BiFunction`.
     */
    @JvmStatic
    fun <N : Any> structuralHoles(
        graph: Graph<N>,
        edgeWeights: BiFunction<N, N, out Number>,
    ): StructuralHoles<N> {
        // TODO(jrtom): consider adding an overload that takes a Function<EndpointPair<N>, Number>
        // instead
        return StructuralHoles(graph, edgeWeights)
    }

    /**
     * Returns an array whose ith element (for i in [1,16]) is the number of occurrences of the
     * corresponding triad type in `graph`. (The 0th element is not meaningful; this array is
     * effectively 1-based.)
     *
     * See [TriadicCensus] for more information.
     *
     * @param graph the graph whose properties are being measured
     * @param N the node type
     * @return an array encoding the number of occurrences of each triad type
     */
    @JvmStatic
    fun <N : Any> triadicCensus(graph: Graph<N>): LongArray = TriadicCensus.getCounts(graph)
}
