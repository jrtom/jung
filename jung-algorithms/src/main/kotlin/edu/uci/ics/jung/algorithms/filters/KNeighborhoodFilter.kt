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
 * Created on Dec 26, 2001
 *
 */
package edu.uci.ics.jung.algorithms.filters

import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import java.util.ArrayDeque

/**
 * A filter used to extract the k-neighborhood around a set of root nodes. The k-neighborhood is
 * defined as the subgraph induced by the set of nodes that are k or fewer hops away from the root
 * node.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
object KNeighborhoodFilter {

    // TODO: create ValueGraph/Network versions
    @JvmStatic
    fun <N : Any> filterGraph(graph: Graph<N>, rootNodes: Set<N>, radius: Int): MutableGraph<N> {
        checkNotNull(graph)
        checkNotNull(rootNodes)
        checkArgument(graph.nodes().containsAll(rootNodes), "graph must contain all of rootNodes")
        checkArgument(radius > 0, "radius must be > 0")

        val filtered: MutableGraph<N> = GraphBuilder.from(graph).build()
        for (root in rootNodes) {
            filtered.addNode(root)
        }
        var currentNodes = ArrayDeque(rootNodes)
        var nextNodes = ArrayDeque<N>()

        var depth = 1
        while (depth <= radius && currentNodes.isNotEmpty()) {
            while (currentNodes.isNotEmpty()) {
                val currentNode = currentNodes.remove()
                for (nextNode in graph.successors(currentNode)) {
                    // the addNode needs to happen before putEdge() because we need to know whether
                    // the node was present in the graph
                    // (and putEdge() will always add the node if not present)
                    if (filtered.addNode(nextNode)) {
                        nextNodes.add(nextNode)
                    }
                    filtered.putEdge(currentNode, nextNode)
                }
            }
            val emptyQueue = currentNodes
            currentNodes = nextNodes
            nextNodes = emptyQueue
            depth++
        }

        // put in in-edges from nodes in the filtered graph
        for (node in filtered.nodes()) {
            for (predecessor in graph.predecessors(node)) {
                if (filtered.nodes().contains(predecessor)) {
                    filtered.putEdge(predecessor, node)
                }
            }
        }

        return filtered
    }
}
