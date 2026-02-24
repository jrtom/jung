/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.flows

import com.google.common.base.Preconditions
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graphs
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import edu.uci.ics.jung.algorithms.util.IterativeProcess
import java.util.HashMap
import java.util.LinkedList
import java.util.function.Function
import java.util.function.Supplier

/**
 * Implements the Edmonds-Karp maximum flow algorithm for solving the maximum flow problem. After
 * the algorithm is executed, the input `Map` is populated with a `Integer` for each
 * edge that indicates the flow along that edge.
 *
 * An example of using this algorithm is as follows:
 *
 * ```
 * EdmondsKarpMaxFlow ek = new EdmondsKarpMaxFlow(graph, source, sink, edge_capacities, edge_flows,
 * edge_factory);
 * ek.evaluate(); // This instructs the class to compute the max flow
 * ```
 *
 * @see "Introduction to Algorithms by Cormen, Leiserson, Rivest, and Stein."
 * @see "Network Flows by Ahuja, Magnanti, and Orlin."
 * @see "Theoretical improvements in algorithmic efficiency for network flow problems by Edmonds and
 *     Karp, 1972."
 * @author Scott White, adapted to jung2 by Tom Nelson
 */
// TODO: this should work for input ValueGraphs also
// TODO: use a ValueGraph for the flow graph (take max of available parallel edge capacities)
// TODO: this currently works on Integers; can probably be generalized at least somewhat
// TODO: does this algorithm in fact actually fail for undirected graphs?
// TODO: no reason why the user should have to supply the edgeFlowMap
class EdmondsKarpMaxFlow<N : Any, E : Any>(
    private val network: Network<N, E>,
    private val source: N,
    private val target: N,
    private val edgeCapacityTransformer: Function<E, Int?>,
    private val edgeFlowMap: MutableMap<E, Int>,
    private val edgeFactory: Supplier<E>,
) : IterativeProcess() {

    private var flowNetwork: MutableNetwork<N, E> = Graphs.copyOf(network)

    /** @return the value of the maximum flow from the source to the sink. */
    var maxFlow: Int = 0
        private set

    private var sourcePartitionNodes: MutableSet<N> = HashSet()
    private var sinkPartitionNodes: MutableSet<N> = HashSet()
    private var minCutEdges: MutableSet<E> = HashSet()

    private val residualCapacityMap = HashMap<E, Int>()
    private val parentMap = HashMap<N, N>()
    private val parentCapacityMap = HashMap<N, Int>()

    init {
        Preconditions.checkArgument(network.isDirected, "input graph must be directed")
        Preconditions.checkArgument(
            network.nodes().contains(source),
            "input graph must contain source node",
        )
        Preconditions.checkArgument(
            network.nodes().contains(target),
            "input graph must contain sink node",
        )
        Preconditions.checkArgument(source != target, "source and sink nodes must be distinct")
    }

    private fun clearParentValues() {
        parentMap.clear()
        parentCapacityMap.clear()
        parentCapacityMap[source] = Int.MAX_VALUE
        parentMap[source] = source
    }

    protected fun hasAugmentingPath(): Boolean {
        sinkPartitionNodes.clear()
        sourcePartitionNodes.clear()
        sinkPartitionNodes.addAll(flowNetwork.nodes())

        val visitedEdgesMap = HashSet<E>()
        val queue: java.util.Queue<N> = LinkedList()
        queue.add(source)

        while (queue.isNotEmpty()) {
            val currentNode = queue.remove()
            sinkPartitionNodes.remove(currentNode)
            sourcePartitionNodes.add(currentNode)
            val currentCapacity = parentCapacityMap[currentNode]!!

            for (neighboringEdge in flowNetwork.outEdges(currentNode)) {
                val neighboringNode = flowNetwork.incidentNodes(neighboringEdge).target()

                val residualCapacity = residualCapacityMap[neighboringEdge]!!
                if (residualCapacity <= 0 || visitedEdgesMap.contains(neighboringEdge)) {
                    continue
                }

                val neighborsParent = parentMap[neighboringNode]
                val neighborCapacity = parentCapacityMap[neighboringNode]
                val newCapacity = Math.min(residualCapacity, currentCapacity)

                if (neighborsParent == null || newCapacity > neighborCapacity!!) {
                    parentMap[neighboringNode] = currentNode
                    parentCapacityMap[neighboringNode] = newCapacity
                    visitedEdgesMap.add(neighboringEdge)
                    if (neighboringNode != target) {
                        queue.add(neighboringNode)
                    }
                }
            }
        }

        var hasAugmentingPath = false
        val targetsParentCapacity = parentCapacityMap[target]
        if (targetsParentCapacity != null && targetsParentCapacity > 0) {
            updateResidualCapacities()
            hasAugmentingPath = true
        }
        clearParentValues()
        return hasAugmentingPath
    }

    override fun step() {
        while (hasAugmentingPath()) {
            // keep finding augmenting paths
        }
        computeMinCut()
    }

    private fun computeMinCut() {
        for (e in network.edges()) {
            val endpoints = network.incidentNodes(e)
            val src = endpoints.source()
            val destination = endpoints.target()
            if (sinkPartitionNodes.contains(src) && sinkPartitionNodes.contains(destination)) {
                continue
            }
            if (sourcePartitionNodes.contains(src) && sourcePartitionNodes.contains(destination)) {
                continue
            }
            if (sinkPartitionNodes.contains(src) && sourcePartitionNodes.contains(destination)) {
                continue
            }
            minCutEdges.add(e)
        }
    }

    /**
     * @return the nodes which share the same partition (as defined by the min-cut edges) as the sink
     *     node.
     */
    fun getNodesInSinkPartition(): Set<N> = sinkPartitionNodes

    /**
     * @return the nodes which share the same partition (as defined by the min-cut edges) as the
     *     source node.
     */
    fun getNodesInSourcePartition(): Set<N> = sourcePartitionNodes

    /** @return the edges in the minimum cut. */
    fun getMinCutEdges(): Set<E> = minCutEdges

    /** @return the graph for which the maximum flow is calculated. */
    fun getFlowGraph(): Network<N, E> = flowNetwork

    override fun initializeIterations() {
        parentCapacityMap[source] = Int.MAX_VALUE
        parentMap[source] = source

        val backEdges = HashSet<EndpointPair<N>>()
        for (edge in flowNetwork.edges()) {
            val capacity = edgeCapacityTransformer.apply(edge)
            Preconditions.checkNotNull(capacity, "Edge capacities must exist for all edges")

            residualCapacityMap[edge] = capacity!!
            val endpoints = flowNetwork.incidentNodes(edge)
            val src = endpoints.source()
            val destination = endpoints.target()

            if (!flowNetwork.successors(destination).contains(src)) {
                backEdges.add(EndpointPair.ordered(destination, src))
            }
        }

        for (endpoints in backEdges) {
            val backEdge = edgeFactory.get()
            flowNetwork.addEdge(endpoints.source(), endpoints.target(), backEdge)
            residualCapacityMap[backEdge] = 0
        }
    }

    override fun finalizeIterations() {
        for (currentEdge in flowNetwork.edges()) {
            val capacity = edgeCapacityTransformer.apply(currentEdge)

            val residualCapacity = residualCapacityMap[currentEdge]!!
            if (capacity != null) {
                val flowValue = capacity - residualCapacity
                edgeFlowMap[currentEdge] = flowValue
            }
        }

        val backEdges = HashSet<E>()
        for (currentEdge in flowNetwork.edges()) {
            if (edgeCapacityTransformer.apply(currentEdge) == null) {
                backEdges.add(currentEdge)
            } else {
                residualCapacityMap.remove(currentEdge)
            }
        }
        for (e in backEdges) {
            flowNetwork.removeEdge(e)
        }
    }

    private fun updateResidualCapacities() {
        val augmentingPathCapacity = parentCapacityMap[target]!!
        maxFlow += augmentingPathCapacity
        var currentNode = target
        var parentNode: N
        while (true) {
            parentNode = parentMap[currentNode]!!
            if (parentNode == currentNode) break
            // TODO: change this to edgeConnecting() once we are using Guava 22.0+
            val currentEdge = flowNetwork.edgesConnecting(parentNode, currentNode).iterator().next()

            var residualCapacity = residualCapacityMap[currentEdge]!!
            residualCapacity -= augmentingPathCapacity
            residualCapacityMap[currentEdge] = residualCapacity

            val backEdge = flowNetwork.edgesConnecting(currentNode, parentNode).iterator().next()
            var backResidualCapacity = residualCapacityMap[backEdge]!!
            backResidualCapacity += augmentingPathCapacity
            residualCapacityMap[backEdge] = backResidualCapacity
            currentNode = parentNode
        }
    }
}
