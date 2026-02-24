/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Apr 21, 2004
 */
package edu.uci.ics.jung.algorithms.transformation

import com.google.common.base.Preconditions
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import com.google.common.graph.MutableValueGraph
import com.google.common.graph.ValueGraphBuilder
import java.util.LinkedHashSet

/**
 * Methods for creating a "folded" graph based on an input graph.
 *
 * A "folded" graph is derived from an input graph by identifying a subset of nodes which will
 * become the nodes of the new graph, copying these nodes into the new graph, and then connecting
 * those nodes that were connected indirectly in the input graph through nodes not in that subset.
 * This subset is conventionally (but not necessarily) a partition of a k-partite graph.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
// TODO: consider creating hypergraph versions if we add a hypergraph type;
// see JUNG 2.1 source for this file for concepts:
// * nodes -> nodes, replace hyperedges by k-cliques on incident nodes
// * hyperedges -> nodes, (a,b) exists in new graph if a and b share a node
// TODO: consider adding
// (a) variants that define the input nodes via a Predicate
// (b) a utility method that identifies partitions of a k-partite graph
object FoldingTransformer {

    /**
     * Converts `graph` into a graph *T* by "folding" `graph` "around" the node set
     * `nodes`. *T*'s nodes will be `nodes`; for any two nodes `a` and
     * `b` in `nodes`, *T* will contain the edge `(a, b)` if, and only if,
     * `graph` contains a node `c` such that
     *
     * * `a` is not equal to `b` (thus, *T* will contain no self-loops)
     * * `c` is **not** in `nodes`
     * * `graph` contains edges `(a,c)` and `(c,b)`
     *
     * The properties of *T* (such as directedness) are taken from the properties of the input
     * graph (see [com.google.common.graph.GraphBuilder.from]).
     *
     * @param N node type
     * @param graph input graph
     * @param nodes input node set
     * @throws IllegalArgumentException if `graph` does not contain all of `nodes`
     */
    // TODO: consider renaming this
    @JvmStatic
    fun <N : Any> foldToGraph(graph: Graph<N>, nodes: Set<N>): MutableGraph<N> {
        Preconditions.checkArgument(
            graph.nodes().containsAll(nodes),
            "Input graph must contain all specified nodes",
        )
        val newGraph: MutableGraph<N> =
            GraphBuilder.from(graph).expectedNodeCount(nodes.size).build()

        for (node in nodes) {
            for (s in graph.successors(node)) {
                for (t in graph.successors(s)) {
                    if (!nodes.contains(t) || t == node) {
                        continue
                    }
                    newGraph.putEdge(node, t)
                }
            }
        }
        return newGraph
    }

    /**
     * Converts `graph` into a graph *T* by "folding" `graph` "around" the node set
     * `nodes`. *T*'s nodes will be `nodes`; for any two nodes `a` and
     * `b` in `nodes`, *T* will contain the edge `(a, b)` if, and only if,
     * `graph` contains a node `c` such that
     *
     * * `a` is not equal to `b` (thus, *T* will contain no self-loops)
     * * `c` is **not** in `nodes`
     * * `graph` contains edges `(a,c)` and `(c,b)`
     *
     * The properties of *T* (such as directedness) are taken from the properties of the input
     * graph (see [com.google.common.graph.GraphBuilder.from]).
     *
     * *T*'s edge values are the sets of nodes that connected the edge's endpoints in
     * `graph`.
     *
     * @param N node type
     * @param g input graph
     * @param nodes input node set
     * @throws IllegalArgumentException if `graph` does not contain all of `nodes`
     */
    // TODO: consider renaming this
    @JvmStatic
    fun <N : Any> foldToValueGraph(g: Graph<N>, nodes: Set<N>): MutableValueGraph<N, Set<N>> {
        Preconditions.checkArgument(
            g.nodes().containsAll(nodes),
            "Input graph must contain all specified nodes",
        )
        val builder =
            if (g.isDirected) ValueGraphBuilder.directed() else ValueGraphBuilder.undirected()
        val newGraph: MutableValueGraph<N, Set<N>> =
            builder.expectedNodeCount(nodes.size).nodeOrder(g.nodeOrder()).build()

        for (node in nodes) {
            for (s in g.successors(node)) {
                for (t in g.successors(s)) {
                    if (!nodes.contains(t) || t == node) {
                        continue
                    }
                    @Suppress("UNCHECKED_CAST")
                    val intermediateNodes =
                        newGraph.edgeValueOrDefault(node, t, LinkedHashSet<N>())!! as MutableSet<N>
                    if (intermediateNodes.isEmpty()) {
                        newGraph.putEdgeValue(node, t, intermediateNodes)
                    }
                    intermediateNodes.add(s)
                }
            }
        }
        return newGraph
    }
}
