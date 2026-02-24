/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.subLayout

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import org.slf4j.LoggerFactory

@Suppress("UNCHECKED_CAST")
open class GraphCollapser(private val originalGraph: Network<*, *>) {

    private val graphBuilder: NetworkBuilder<Any, Any> =
        NetworkBuilder.from(originalGraph) as NetworkBuilder<Any, Any>

    fun collapse(inGraph: Network<*, *>, clusterGraph: Network<*, *>): Network<*, *> {
        if (clusterGraph.nodes().size < 2) {
            return inGraph
        }

        val graph: MutableNetwork<Any, Any> = graphBuilder.build()
        val cluster = clusterGraph.nodes()

        // add all nodes in the delegate, unless the node is in the
        // cluster.
        for (v in inGraph.nodes()) {
            if (!cluster.contains(v)) {
                graph.addNode(v)
            }
        }
        // add the clusterGraph as a node
        graph.addNode(clusterGraph)

        // add all edges from the inGraph, unless both endpoints of
        // the edge are in the cluster
        for (e in inGraph.edges()) {
            val endpoints = (inGraph as Network<Any, Any>).incidentNodes(e)
            val u = endpoints.nodeU()
            val v = endpoints.nodeV()
            // only add edges whose endpoints are not both in the cluster
            if (cluster.contains(u) && cluster.contains(v)) {
                continue
            }

            if (cluster.contains(u)) {
                graph.addEdge(clusterGraph, v, e)
            } else if (cluster.contains(v)) {
                graph.addEdge(u, clusterGraph, e)
            } else {
                graph.addEdge(u, v, e)
            }
        }
        return graph
    }

    fun expand(
        originalNetwork: Network<*, *>, inGraph: Network<*, *>, clusterGraphNode: Network<*, *>
    ): Network<*, *> {
        val networkBuilder: NetworkBuilder<Any, Any> =
            NetworkBuilder.from(originalNetwork) as NetworkBuilder<Any, Any>
        // build a new empty network
        val newGraph: MutableNetwork<Any, Any> = networkBuilder.build()
        // add all the nodes from inGraph that are not clusterGraphNode and are not in clusterGraphNode
        for (node in inGraph.nodes()) {
            if (node != clusterGraphNode && !this.contains(clusterGraphNode, node)) {
                newGraph.addNode(node)
            }
        }

        // add all edges that don't have an endpoint that either is clusterGraphNode or is in
        // clusterGraphNode
        for (edge in inGraph.edges()) {
            val endpoints = (inGraph as Network<Any, Any>).incidentNodes(edge)
            var dontWantThis = false
            for (endpoint in listOf(endpoints.nodeU(), endpoints.nodeV())) {
                dontWantThis = dontWantThis ||
                    endpoint == clusterGraphNode || this.contains(clusterGraphNode, endpoint)
            }
            if (!dontWantThis) {
                newGraph.addEdge(endpoints.nodeU(), endpoints.nodeV(), edge)
            }
        }

        // add all the nodes from the clusterGraphNode
        for (node in clusterGraphNode.nodes()) {
            newGraph.addNode(node)
        }

        // add all the edges that are in the clusterGraphNode
        for (edge in clusterGraphNode.edges()) {
            val endpoints = (clusterGraphNode as Network<Any, Any>).incidentNodes(edge)
            newGraph.addEdge(endpoints.nodeU(), endpoints.nodeV(), edge)
        }

        // add edges from ingraph where one endpoint is the clusterGraphNode
        // it will now be connected to the node that was expanded from clusterGraphNode
        for (edge in inGraph.edges()) {
            val endpointsFromCollapsedGraph =
                Sets.newHashSet((inGraph as Network<Any, Any>).incidentNodes(edge))
            for (endpoint in inGraph.incidentNodes(edge)) {
                if (endpoint == clusterGraphNode) {
                    // get the endpoints for this edge from the original graph
                    val endpointsFromOriginalGraph =
                        Sets.newHashSet((originalNetwork as Network<Any, Any>).incidentNodes(edge))
                    // remove the endpoint that is the cluster i am expanding
                    endpointsFromCollapsedGraph.remove(endpoint)
                    // put in the one that is in the collapsedGraphNode i am expanding
                    for (originalEndpoint in endpointsFromOriginalGraph) {
                        if (this.contains(clusterGraphNode, originalEndpoint)) {
                            endpointsFromCollapsedGraph.add(originalEndpoint)
                            break
                        }
                    }
                    val list = Lists.newArrayList(endpointsFromCollapsedGraph)
                    newGraph.addEdge(list[0], list[1], edge)
                }
            }
        }
        return newGraph
    }

    fun findNode(inGraph: Network<*, *>, inNode: Any): Any? {
        /** if the inNode is in the inGraph, return the inNode */
        if (inGraph.nodes().contains(inNode)) {
            return inNode
        }

        /**
         * if the inNode is part of a node that is a Network, return the Network that contains inNode
         */
        for (node in inGraph.nodes()) {
            if (node is Network<*, *> && contains(node, inNode)) {
                return node
            }
        }
        return null
    }

    internal fun findOriginalNode(
        inGraph: Network<*, *>, inNode: Any, clusterGraph: Network<*, *>
    ): Any? {
        if (inGraph.nodes().contains(inNode)) {
            return inNode
        }

        for (node in inGraph.nodes()) {
            if (node is Network<*, *> && node != clusterGraph) {
                return node
            }
            if (node is Network<*, *> && contains(node, inNode)) {
                return node
            }
        }
        return null
    }

    fun contains(inGraph: Network<*, *>, inNode: Any): Boolean {
        if (inGraph.nodes().contains(inNode)) {
            return true
        }

        var contained = false
        for (node in inGraph.nodes()) {
            contained = contained || (node is Network<*, *> && contains(node, inNode))
        }
        return contained
    }

    fun getClusterGraph(inGraph: Network<*, *>, picked: Collection<*>): Network<*, *> {
        val clusterGraph: MutableNetwork<Any, Any> = graphBuilder.build()
        for (node in picked) {
            clusterGraph.addNode(node!!)
            val edges = (inGraph as Network<Any, Any>).incidentEdges(node)
            for (edge in edges) {
                val u = inGraph.incidentNodes(edge).nodeU()
                val v = inGraph.incidentNodes(edge).nodeV()
                if (picked.contains(u) && picked.contains(v)) {
                    clusterGraph.addEdge(u, v, edge)
                }
            }
        }
        return clusterGraph
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GraphCollapser::class.java)
    }
}
