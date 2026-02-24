/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.cluster

import com.google.common.base.Preconditions
import com.google.common.graph.Graphs
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality
import java.util.LinkedHashSet
import java.util.function.Function

/**
 * An algorithm for computing clusters (community structure) in graphs based on edge betweenness.
 * The betweenness of an edge is defined as the extent to which that edge lies along shortest paths
 * between all pairs of nodes.
 *
 * This algorithm works by iteratively following the 2 step process:
 *
 * * Compute edge betweenness for all edges in current graph
 * * Remove edge with highest betweenness
 *
 * Running time is: O(kmn) where k is the number of edges to remove, m is the total number of
 * edges, and n is the total number of nodes. For very sparse graphs the running time is closer to
 * O(kn^2) and for graphs with strong community structure, the complexity is even lower.
 *
 * This algorithm is a slight modification of the algorithm discussed below in that the number of
 * edges to be removed is parameterized.
 *
 * @author Scott White
 * @author Tom Nelson (converted to jung2)
 * @author Joshua O'Madadhain (converted to common.graph)
 * @see "Community structure in social and biological networks by Michelle Girvan and Mark Newman"
 */
class EdgeBetweennessClusterer<N : Any, E : Any>(
    private val numEdgesToRemove: Int,
) : Function<Network<N, E>, Set<Set<N>>> {

    private var edgesRemoved: LinkedHashSet<E>

    init {
        Preconditions.checkArgument(
            numEdgesToRemove >= 0,
            "Number of edges to remove must be positive",
        )
        edgesRemoved = LinkedHashSet(numEdgesToRemove)
    }

    /**
     * Finds the set of clusters which have the strongest "community structure". The more edges
     * removed the smaller and more cohesive the clusters.
     *
     * @param graph the graph
     */
    override fun apply(graph: Network<N, E>): Set<Set<N>> {
        Preconditions.checkArgument(
            numEdgesToRemove <= graph.edges().size,
            "Number of edges to remove must be <= the number of edges in the graph",
        )
        // TODO(jrtom): is there something smarter that we can do if we're removing
        // (almost) all the edges in the graph?
        val filtered = Graphs.copyOf(graph)
        edgesRemoved.clear()

        for (k in 0 until numEdgesToRemove) {
            val bc = BetweennessCentrality<N, E>(filtered)
            var toRemove: E? = null
            var score = 0.0
            for (e in filtered.edges()) {
                if (bc.getEdgeScore(e) > score) {
                    toRemove = e
                    score = bc.getEdgeScore(e)
                }
            }
            edgesRemoved.add(toRemove!!)
            filtered.removeEdge(toRemove)
        }

        val wcSearch = WeakComponentClusterer<N>()
        return wcSearch.apply(filtered.asGraph())
    }

    /**
     * Retrieves the set of all edges that were removed. The edges returned are stored in order in
     * which they were removed.
     *
     * @return the edges removed from the original graph
     */
    fun getEdgesRemoved(): Set<E> = edgesRemoved
}
