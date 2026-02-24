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

import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import java.util.Random
import java.util.function.Supplier

/**
 * Generates a random graph using the Erdos-Renyi binomial model (each pair of nodes is connected
 * with probability p).
 *
 * @author William Giordano, Scott White, Joshua O'Madadhain
 */
class ErdosRenyiGenerator<N : Any>(
    private val nodeSupplier: Supplier<N>,
    private val nodeCount: Int,
    private val edgeConnectionProbability: Double,
) {
    private var random: Random = Random()

    init {
        checkNotNull(nodeSupplier)
        checkArgument(nodeCount > 0, "Number of nodes must be positive")
        checkArgument(
            edgeConnectionProbability in 0.0..1.0,
            "Probability of connection must be in [0, 1]",
        )
    }

    /**
     * Returns a graph in which each pair of nodes is connected by an undirected edge with the
     * probability specified by the constructor.
     */
    fun get(): Graph<N> {
        val graph: MutableGraph<N> = GraphBuilder.undirected().expectedNodeCount(nodeCount).build()
        for (i in 0 until nodeCount) {
            graph.addNode(nodeSupplier.get())
        }
        val list = ArrayList(graph.nodes())

        for (i in 0 until nodeCount - 1) {
            val vI = list[i]
            for (j in i + 1 until nodeCount) {
                val vJ = list[j]
                if (random.nextDouble() < edgeConnectionProbability) {
                    graph.putEdge(vI, vJ)
                }
            }
        }
        return graph
    }

    fun setRandom(random: Random) {
        this.random = random
    }
}
