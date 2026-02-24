/*
 * Created on Sep 24, 2005
 *
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.util

import com.google.common.graph.Network

/**
 * A class which creates and maintains indices for parallel edges. Parallel edges are defined here
 * to be the collection of edges that are returned by `graph.edgesConnecting(v, w)` for
 * some `v` and `w`.
 *
 * At this time, users are responsible for resetting the indices (by calling `reset()`)
 * if changes to the graph make it appropriate.
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 */
open class ParallelEdgeIndexFunction<N, E> : EdgeIndexFunction<N, E> {

    protected val edge_index: MutableMap<E, Int> = HashMap()

    override fun getIndex(context: Context<Network<N, E>, E>): Int {
        val network = context.graph
        val edge = context.element
        var index = edge_index[edge]
        if (index == null) {
            val endpoints = network.incidentNodes(edge)
            val u = endpoints.nodeU()
            val v = endpoints.nodeV()
            var count = 0
            for (connectingEdge in network.edgesConnecting(u, v)) {
                edge_index[connectingEdge] = count++
            }
            return edge_index[edge]!!
        }
        return index
    }

    override fun reset(context: Context<Network<N, E>, E>) {
        edge_index.remove(context.element)
    }

    override fun reset() {
        edge_index.clear()
    }
}
