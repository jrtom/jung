/*
 * Copyright (c) 2009, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators

import com.google.common.base.Preconditions
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.algorithms.shortestpath.Distance
import java.util.function.Supplier
import java.util.stream.Collectors.toSet

/**
 * Simple generator of graphs in the shape of an m x n lattice where each node is adjacent to each
 * of its neighbors (to the left, right, up, and down). May be toroidal, in which case the nodes on
 * the boundaries are connected to their counterparts on the opposite boundaries as well.
 *
 * @author Joshua O'Madadhain
 */
class Lattice2DGenerator<N : Any, E : Any>(
    private val rowCount: Int,
    private val colCount: Int,
    private val toroidal: Boolean,
) {
    // TODO: consider using a Builder here as well

    init {
        // TODO: relax the row/col count restrictions to be >= 3 once we get the random selection
        // mechanism
        // in KleinbergSmallWorld to behave better
        Preconditions.checkArgument(rowCount >= 4, "row count must be >= 4")
        Preconditions.checkArgument(colCount >= 4, "column count must be >= 4")
    }

    /**
     * Creates a lattice-shaped `Network` with the specified node and edge suppliers, and
     * direction.
     *
     * @param directed
     * @param nodeFactory
     * @param edgeFactory
     * @return
     */
    fun generateNetwork(
        directed: Boolean,
        nodeFactory: Supplier<N>,
        edgeFactory: Supplier<E>,
    ): MutableNetwork<N, E> {
        Preconditions.checkNotNull(nodeFactory)
        Preconditions.checkNotNull(edgeFactory)

        val nodeCount = rowCount * colCount

        val boundaryAdjustment = if (toroidal) 0 else 1
        var edgeCount =
            colCount * (rowCount - boundaryAdjustment) + // vertical edges
                rowCount * (colCount - boundaryAdjustment) // horizontal edges
        if (directed) {
            edgeCount *= 2
        }

        val builder: NetworkBuilder<Any, Any> =
            if (directed) NetworkBuilder.directed() else NetworkBuilder.undirected()
        val graph: MutableNetwork<N, E> =
            builder.expectedNodeCount(nodeCount).expectedEdgeCount(edgeCount).build()

        for (i in 0 until nodeCount) {
            val v = nodeFactory.get()
            graph.addNode(v)
        }
        val elements = ArrayList(graph.nodes())

        val endRow = if (toroidal) rowCount else rowCount - 1
        val endCol = if (toroidal) colCount else colCount - 1

        // fill in edges
        // down
        for (i in 0 until endRow) {
            for (j in 0 until colCount) {
                graph.addEdge(
                    elements[getIndex(i, j)],
                    elements[getIndex(i + 1, j)],
                    edgeFactory.get(),
                )
            }
        }
        // right
        for (i in 0 until rowCount) {
            for (j in 0 until endCol) {
                graph.addEdge(
                    elements[getIndex(i, j)],
                    elements[getIndex(i, j + 1)],
                    edgeFactory.get(),
                )
            }
        }

        // if the graph is directed, fill in the edges going the other directions
        if (graph.isDirected) {
            val endpointPairs: Set<EndpointPair<N>> =
                graph.edges().stream().map { e -> graph.incidentNodes(e) }.collect(toSet())

            for (endpoints in endpointPairs) {
                graph.addEdge(endpoints.target(), endpoints.source(), edgeFactory.get())
            }
        }
        return graph
    }

    // TODO: this way of getting a Distance is kind of messed up: it shouldn't be possible to
    // get a Distance for a graph other than the one provided, but it is because of how the API
    // works.
    // Fix this.

    /**
     * Returns a `Distance` implementation that assumes that `graph` is lattice-shaped.
     *
     * @param graph
     * @return
     */
    fun distance(graph: Graph<N>): Distance<N> = LatticeDistance(graph)

    private inner class LatticeDistance(graph: Graph<N>) : Distance<N> {
        private val nodeIndices = HashMap<N, Int>()
        private val distances: LoadingCache<N, LoadingCache<N, Number>> =
            CacheBuilder.newBuilder()
                .build(
                    object : CacheLoader<N, LoadingCache<N, Number>>() {
                        override fun load(source: N): LoadingCache<N, Number> =
                            CacheBuilder.newBuilder()
                                .build(
                                    object : CacheLoader<N, Number>() {
                                        override fun load(target: N): Number =
                                            getDistance(source, target)
                                    },
                                )
                    },
                )

        init {
            Preconditions.checkNotNull(graph)
            var index = 0
            for (node in graph.nodes()) {
                nodeIndices[node] = index++
            }
        }

        override fun getDistance(source: N, target: N): Number {
            val sourceIndex = nodeIndices[source]!!
            val targetIndex = nodeIndices[target]!!
            val sourceRow = getRow(sourceIndex)
            val sourceCol = getCol(sourceIndex)
            val targetRow = getRow(targetIndex)
            val targetCol = getCol(targetIndex)

            var vDist = Math.abs(sourceRow - targetRow)
            var hDist = Math.abs(sourceCol - targetCol)
            if (toroidal) {
                vDist = Math.min(vDist, Math.abs(rowCount - vDist) + 1)
                hDist = Math.min(hDist, Math.abs(colCount - hDist) + 1)
            }
            return vDist + hDist
        }

        override fun getDistanceMap(source: N): Map<N, out Number> =
            distances.getUnchecked(source).asMap()

        /**
         * @param i index of the node whose row we want
         * @return the row in which the node with index `i` is found
         */
        private fun getRow(i: Int): Int = i / colCount

        /**
         * @param i index of the node whose column we want
         * @return the column in which the node with index `i` is found
         */
        private fun getCol(i: Int): Int = i % colCount
    }

    internal fun getIndex(i: Int, j: Int): Int =
        (mod(i, rowCount) * colCount) + mod(j, colCount)

    private fun mod(i: Int, modulus: Int): Int {
        val iMod = i % modulus
        return if (iMod >= 0) iMod else iMod + modulus
    }
}
