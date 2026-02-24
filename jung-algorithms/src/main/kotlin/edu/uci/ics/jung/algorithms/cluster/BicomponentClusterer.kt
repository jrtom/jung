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

import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import java.util.ArrayDeque
import java.util.Deque
import java.util.function.Function

/**
 * Finds all biconnected components (bicomponents) of a graph, **ignoring edge direction**. A
 * graph is a biconnected component if at least 2 nodes must be removed in order to disconnect the
 * graph. (Graphs consisting of one node, or of two connected nodes, are also biconnected.)
 * Biconnected components of three or more nodes have the property that every pair of nodes in the
 * component are connected by two or more node-disjoint paths.
 *
 * Running time: O(|V| + |E|) where |V| is the number of nodes and |E| is the number of edges
 *
 * @see "Depth first search and linear graph algorithms by R. E. Tarjan (1972), SIAM J. Comp."
 * @author Joshua O'Madadhain
 */
class BicomponentClusterer<N : Any, E : Any> : Function<Graph<N>, Set<Set<N>>> {

    protected lateinit var dfsNum: MutableMap<N, Number>
    protected lateinit var high: MutableMap<N, Number>
    protected lateinit var parents: MutableMap<N, N>
    protected lateinit var stack: Deque<EndpointPair<N>>
    protected var converseDepth: Int = 0

    /**
     * Extracts the bicomponents from the graph.
     *
     * @param graph the graph whose bicomponents are to be extracted
     * @return the `ClusterSet` of bicomponents
     */
    override fun apply(graph: Graph<N>): Set<Set<N>> {
        val bicomponents = LinkedHashSet<Set<N>>()

        if (graph.nodes().isEmpty()) {
            return bicomponents
        }

        // initialize DFS number for each node to 0
        dfsNum = HashMap()
        for (v in graph.nodes()) {
            dfsNum[v] = 0
        }

        for (v in graph.nodes()) {
            if (dfsNum[v]!!.toInt() == 0) { // if we haven't hit this node yet...
                high = HashMap()
                stack = ArrayDeque()
                parents = HashMap()
                converseDepth = graph.nodes().size
                // find the biconnected components for this subgraph, starting from v
                findBiconnectedComponents(graph, v, bicomponents)

                // if we only visited one node, this method won't have
                // ID'd it as a biconnected component, so mark it as one
                if (graph.nodes().size - converseDepth == 1) {
                    val s = HashSet<N>()
                    s.add(v)
                    bicomponents.add(s)
                }
            }
        }

        return bicomponents
    }

    /**
     * Stores, in `bicomponents`, all the biconnected components that are reachable from
     * `v`.
     *
     * The algorithm basically proceeds as follows: do a depth-first traversal starting from
     * `v`, marking each node with a value that indicates the order in which it was encountered
     * (dfs_num), and with a value that indicates the highest point in the DFS tree that is known to
     * be reachable from this node using non-DFS edges (high). (Since it is measured on non-DFS edges,
     * "high" tells you how far back in the DFS tree you can reach by two distinct paths, hence
     * biconnectivity.) Each time a new node w is encountered, push the edge just traversed on a
     * stack, and call this method recursively. If w.high is no greater than v.dfs_num, then the
     * contents of the stack down to (v,w) is a biconnected component (and v is an articulation point,
     * that is, a component boundary). In either case, set v.high to max(v.high, w.high), and
     * continue. If w has already been encountered but is not v's parent, set v.high max(v.high,
     * w.dfs_num) and continue.
     *
     * (In case anyone cares, the version of this algorithm on p. 224 of Udi Manber's "Introduction
     * to Algorithms: A Creative Approach" seems to be wrong: the stack should be initialized outside
     * this method, (v,w) should only be put on the stack if w hasn't been seen already, and there's
     * no real benefit to putting v on the stack separately: just check for (v,w) on the stack rather
     * than v. Had I known this, I could have saved myself a few days. JRTOM)
     *
     * @param g the graph to check for biconnected components
     * @param v the starting place for searching for biconnected components
     * @param bicomponents storage for the biconnected components found by this algorithm
     */
    protected fun findBiconnectedComponents(g: Graph<N>, v: N, bicomponents: MutableSet<Set<N>>) {
        val vDfsNum = converseDepth
        dfsNum[v] = vDfsNum
        converseDepth--
        high[v] = vDfsNum

        for (w in g.adjacentNodes(v)) {
            val wDfsNum = dfsNum[w]!!.toInt()
            val vw = EndpointPair.unordered(v, w)
            if (wDfsNum == 0) { // w hasn't yet been visited
                parents[w] = v // v is w's parent in the DFS tree
                stack.push(vw)
                findBiconnectedComponents(g, w, bicomponents)
                val wHigh = high[w]!!.toInt()
                if (wHigh <= vDfsNum) {
                    // v disconnects w from the rest of the graph,
                    // i.e., v is an articulation point
                    // thus, everything between the top of the stack and
                    // v is part of a single biconnected component
                    val bicomponent = HashSet<N>()
                    var endpoints: EndpointPair<N>
                    do {
                        endpoints = stack.pop()
                        bicomponent.add(endpoints.nodeU())
                        bicomponent.add(endpoints.nodeV())
                    } while (endpoints != vw)
                    bicomponents.add(bicomponent)
                }
                high[v] = Math.max(wHigh, high[v]!!.toInt())
            } else if (w != parents[v]) { // (v,w) is a back or a forward edge
                high[v] = Math.max(wDfsNum, high[v]!!.toInt())
            }
        }
    }
}
