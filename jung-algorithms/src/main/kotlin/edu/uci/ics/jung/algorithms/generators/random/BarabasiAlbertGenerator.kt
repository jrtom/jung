/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random

import com.google.common.base.Preconditions
import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.ImmutableSet
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.algorithms.util.WeightedChoice
import java.util.Random
import java.util.function.Supplier

/**
 * Simple evolving scale-free random graph generator. At each time step, a new node is created and
 * is connected to existing nodes according to the principle of "preferential attachment", whereby
 * nodes with higher degree have a higher probability of being selected for attachment.
 *
 * At a given timestep, the probability `p` of creating an edge between an existing
 * node `v` and the newly added node is
 *
 * ```
 * p = (degree(v) + 1) / (|E| + |V|);
 * ```
 *
 * where `|E|` and `|V|` are, respectively, the number of edges and nodes
 * currently in the network (counting neither the new node nor the other edges that are being
 * attached to it).
 *
 * Note that the formula specified in the original paper (cited below) was
 *
 * ```
 * p = degree(v) / |E|
 * ```
 *
 * However, this would have meant that the probability of attachment for any existing isolated
 * node would be 0. This version uses Lagrangian smoothing to give each existing node a positive
 * attachment probability.
 *
 * The graph created may be either directed or undirected (controlled by a constructor
 * parameter); the default is undirected. If the graph is specified to be directed, then the edges
 * added will be directed from the newly added node u to the existing node v, with probability
 * proportional to the indegree of v (number of edges directed towards v). If the graph is specified
 * to be undirected, then the (undirected) edges added will connect u to v, with probability
 * proportional to the degree of v.
 *
 * The `parallel` constructor parameter specifies whether parallel edges may be
 * created.
 *
 * @see "A.-L. Barabasi and R. Albert, Emergence of scaling in random networks, Science 286, 1999."
 * @author Scott White
 * @author Joshua O'Madadhain
 * @author Tom Nelson - adapted to jung2
 * @author James Marchant
 */
// TODO(jrtom): decide whether EvolvingGraphGenerator is actually necessary for this to extend
class BarabasiAlbertGenerator<N : Any, E : Any> {

    private val numEdgesToAttachPerStep: Int
    private var elapsedTimeSteps: Int = 0
    private var random: Random
    protected var nodeFactory: Supplier<N>
    protected val edgeFactory: Supplier<E>
    var seedNodes: ImmutableSet<N>
        private set
    private val graph: MutableNetwork<N, E>

    /**
     * Constructs a new instance of the generator.
     *
     * @param graphBuilder builder for graph instances
     * @param nodeFactory factory for nodes of the appropriate type
     * @param edgeFactory factory for edges of the appropriate type
     * @param initNodes number of unconnected 'seed' nodes that the graph should start with
     * @param numEdgesToAttach the number of edges that should be attached from the new node to
     *     pre-existing nodes at each time step
     * @param seed random number seed
     */
    // TODO(jrtom): consider using a Builder pattern here
    constructor(
        graphBuilder: NetworkBuilder<Any, Any>,
        nodeFactory: Supplier<N>,
        edgeFactory: Supplier<E>,
        initNodes: Int,
        numEdgesToAttach: Int,
        seed: Int,
    ) {
        this.nodeFactory = checkNotNull(nodeFactory)
        this.edgeFactory = checkNotNull(edgeFactory)
        checkArgument(initNodes > 0, "Number of initial unconnected 'seed' nodes must be positive")
        checkArgument(
            numEdgesToAttach > 0,
            "Number of edges to attach at each time step must be positive",
        )
        checkArgument(
            numEdgesToAttach <= initNodes,
            "Number of edges to attach at each time step must be <= the number of initial nodes",
        )
        this.graph = graphBuilder.build()
        this.numEdgesToAttachPerStep = numEdgesToAttach
        this.random = Random(seed.toLong())
        this.nodeFactory = nodeFactory
        this.seedNodes = ImmutableSet.of()
        initialize(initNodes)
    }

    /**
     * Constructs a new instance of the generator, whose output will be an undirected graph, and which
     * will use the current time as a seed for the random number generation.
     *
     * @param nodeFactory factory for nodes of the appropriate type
     * @param edgeFactory factory for edges of the appropriate type
     * @param initNodes number of nodes that the graph should start with
     * @param numEdgesToAttach the number of edges that should be attached from the new node to
     *     pre-existing nodes at each time step
     */
    constructor(
        graphBuilder: NetworkBuilder<Any, Any>,
        nodeFactory: Supplier<N>,
        edgeFactory: Supplier<E>,
        initNodes: Int,
        numEdgesToAttach: Int,
    ) : this(
        graphBuilder,
        nodeFactory,
        edgeFactory,
        initNodes,
        numEdgesToAttach,
        System.currentTimeMillis().toInt(),
    )

    private fun initialize(initNodes: Int) {
        val seedBuilder = ImmutableSet.builder<N>()

        for (i in 0 until initNodes) {
            val v = nodeFactory.get()
            seedBuilder.add(v)
            graph.addNode(v)
        }

        seedNodes = seedBuilder.build()
        elapsedTimeSteps = 0
    }

    private fun buildNodeProbabilities(): WeightedChoice<N> {
        val itemWeights = HashMap<N, Double>()
        for (v in graph.nodes()) {
            val degree: Double
            val denominator: Double

            // Attachment probability is dependent on whether the graph is
            // directed or undirected.
            if (graph.isDirected) {
                degree = graph.inDegree(v).toDouble()
                denominator = (graph.edges().size + graph.nodes().size).toDouble()
            } else {
                degree = graph.degree(v).toDouble()
                denominator = (2 * graph.edges().size + graph.nodes().size).toDouble()
            }

            val prob = (degree + 1) / denominator
            itemWeights[v] = prob
        }
        return WeightedChoice(itemWeights, random)
    }

    private fun generateAdjacentNodes(edgesToAdd: Int): List<N> {
        Preconditions.checkArgument(edgesToAdd >= 1)
        val nodeChooser = buildNodeProbabilities()
        val adjacentNodes = ArrayList<N>(edgesToAdd)
        while (adjacentNodes.size < edgesToAdd) {
            val attachPoint = nodeChooser.nextItem() ?: continue

            // if parallel edges are not allowed, skip this node if already present
            if (!graph.allowsParallelEdges() && adjacentNodes.contains(attachPoint)) {
                continue
            }

            adjacentNodes.add(attachPoint)
        }
        return adjacentNodes
    }

    fun evolveGraph(numTimeSteps: Int) {
        for (i in 0 until numTimeSteps) {
            val newNode = nodeFactory.get()

            // determine the nodes to connect to newNode before connecting anything, because
            // we don't want to bias the degree calculations
            // note: because we don't add newNode to the graph until after identifying the
            // adjacent nodes, we don't need to worry about creating a self-loop
            val adjacentNodes = generateAdjacentNodes(numEdgesToAttachPerStep)
            graph.addNode(newNode)

            for (node in adjacentNodes) {
                graph.addEdge(newNode, node, edgeFactory.get())
            }

            elapsedTimeSteps++
        }
    }

    fun numIterations(): Int = elapsedTimeSteps

    fun get(): MutableNetwork<N, E> = graph
}
