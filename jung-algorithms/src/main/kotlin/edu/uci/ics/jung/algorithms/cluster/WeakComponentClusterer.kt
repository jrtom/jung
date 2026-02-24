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

import com.google.common.graph.Graph
import java.util.LinkedList
import java.util.function.Function

/**
 * Finds all weak components in a graph as sets of node sets. A weak component is defined as a
 * maximal subgraph in which all pairs of nodes in the subgraph are reachable from one another in
 * the underlying undirected subgraph.
 *
 * This implementation identifies components as sets of node sets. To create the induced graphs
 * from any or all of these node sets, see `algorithms.filters.FilterUtils`.
 *
 * Running time: O(|V| + |E|) where |V| is the number of nodes and |E| is the number of edges.
 *
 * @author Scott White
 */
class WeakComponentClusterer<N : Any> : Function<Graph<N>, Set<Set<N>>> {

    /**
     * Extracts the weak components from a graph.
     *
     * @param graph the graph whose weak components are to be extracted
     * @return the list of weak components
     */
    override fun apply(graph: Graph<N>): Set<Set<N>> {
        val clusterSet = HashSet<Set<N>>()

        val unvisitedNodes = HashSet(graph.nodes())

        while (unvisitedNodes.isNotEmpty()) {
            val cluster = HashSet<N>()
            val root = unvisitedNodes.iterator().next()
            unvisitedNodes.remove(root)
            cluster.add(root)

            val queue = LinkedList<N>()
            queue.add(root)

            while (queue.isNotEmpty()) {
                val currentNode = queue.remove()
                val neighbors = graph.adjacentNodes(currentNode)

                for (neighbor in neighbors) {
                    if (unvisitedNodes.contains(neighbor)) {
                        queue.add(neighbor)
                        unvisitedNodes.remove(neighbor)
                        cluster.add(neighbor)
                    }
                }
            }
            clusterSet.add(cluster)
        }
        return clusterSet
    }
}
