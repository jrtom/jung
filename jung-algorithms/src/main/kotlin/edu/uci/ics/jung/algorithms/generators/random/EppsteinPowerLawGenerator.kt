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

import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import java.util.Random
import java.util.function.Supplier

/**
 * Graph generator that generates undirected graphs with power-law degree distributions.
 *
 * @author Joshua O'Madadhain
 * @author Scott White
 * @see "A Steady State Model for Graph Power Law by David Eppstein and Joseph Wang"
 */
class EppsteinPowerLawGenerator<N : Any>(
    private val nodeFactory: Supplier<N>,
    private val nodeCount: Int,
    private val edgeCount: Int,
    private val numIterations: Int,
) {
    private var maxDegree: Double = 0.0
    private var random: Random = Random()
    private lateinit var nodes: List<N>

    protected fun initializeGraph(): MutableGraph<N> {
        val graph: MutableGraph<N> = GraphBuilder.undirected().build()
        nodes = ArrayList(nodeCount)
        for (i in 0 until nodeCount) {
            val node = nodeFactory.get()
            graph.addNode(node)
            (nodes as MutableList).add(node)
        }
        while (graph.edges().size < edgeCount) {
            val u = nodes[(random.nextDouble() * nodeCount).toInt()]
            val v = nodes[(random.nextDouble() * nodeCount).toInt()]
            if (u != v) { // no self-loops
                graph.putEdge(u, v)
            }
        }

        var currentMaxDegree = 0.0
        for (v in graph.nodes()) {
            currentMaxDegree = Math.max(graph.degree(v).toDouble(), currentMaxDegree)
        }
        maxDegree = currentMaxDegree

        return graph
    }

    /**
     * Generates a graph whose degree distribution approximates a power-law.
     *
     * @return the generated graph
     */
    fun get(): Graph<N> {
        val graph = initializeGraph()

        for (rIdx in 0 until numIterations) {
            var v: N
            do {
                v = nodes[(random.nextDouble() * nodeCount).toInt()]
            } while (graph.degree(v) == 0)

            val neighbors = graph.adjacentNodes(v)
            val neighborIndex = (random.nextDouble() * neighbors.size).toInt()
            var i = 0
            var w: N? = null
            for (neighbor in graph.adjacentNodes(v)) {
                if (i++ == neighborIndex) {
                    w = neighbor
                    break
                }
            }

            // FIXME: use WeightedChoice (see BarabasiAlbert) for a more efficient impl
            // for finding an edge
            val x = nodes[(random.nextDouble() * nodeCount).toInt()]
            var y: N
            do {
                y = nodes[(random.nextDouble() * nodeCount).toInt()]
            } while (random.nextDouble() > (graph.degree(y) + 1) / maxDegree)

            // TODO: figure out why we sometimes have insufficient edges in the graph
            // if we make the two clauses below part of the while condition above
            if (x != y && !graph.successors(x).contains(y)) {
                graph.removeEdge(v, w!!)
                graph.putEdge(x, y)
            }
        }

        return graph
    }

    fun setRandom(random: Random) {
        this.random = random
    }
}
