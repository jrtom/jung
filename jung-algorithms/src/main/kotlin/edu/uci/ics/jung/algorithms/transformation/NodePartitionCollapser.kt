/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.transformation

import com.google.common.graph.ValueGraph
import com.google.common.graph.ValueGraphBuilder
import edu.uci.ics.jung.algorithms.blockmodel.NodePartition

/**
 * This class transforms a graph with a known node partitioning into a graph whose nodes correspond
 * to the input graph's partitions.
 *
 * Concept based on Danyel Fisher's `GraphCollapser` in JUNG 1.x.
 */
// TODO: add tests
object NodePartitionCollapser {

    /**
     * Creates a new graph whose nodes correspond to the partitions of the supplied graph. Two nodes u
     * and v in the collapsed graph will be connected if there is an edge between any of the nodes in
     * u and any of the nodes in v, and u and v are distinct. The value of the edge represents the
     * number of such edges.
     *
     * @param partitioning a node partition of a graph
     * @return the collapsed graph
     */
    @JvmStatic
    fun <N : Any> collapseNodePartitions(
        partitioning: NodePartition<N>,
    ): ValueGraph<Set<N>, Int> {
        val original = partitioning.graph
        val builder =
            if (original.isDirected) ValueGraphBuilder.directed() else ValueGraphBuilder.undirected()
        val collapsed = builder.build<Set<N>, Int>()

        // create nodes in new graph corresponding to equivalence sets in the original graph
        for (set in partitioning.getNodePartitions()) {
            collapsed.addNode(set)
        }

        // for each pair of endpoints in the original graph, connect the corresponding nodes
        // (representing partitions) in the collapsed graph if the partitions are different
        val nodeToPartition = partitioning.getNodeToPartitionMap()
        for (endpoints in original.edges()) {
            val nodeU = endpoints.nodeU()
            val nodeV = endpoints.nodeV()
            val partitionU = nodeToPartition[nodeU]
            val partitionV = nodeToPartition[nodeV]
            if (nodeU == nodeV || partitionU == partitionV) {
                // we only connect partitions if the partitions are different;
                // check the nodes first as an optimization
                continue
            }

            val edgeCount = collapsed.edgeValueOrDefault(partitionU, partitionV, 0)!!
            collapsed.putEdgeValue(partitionU, partitionV, edgeCount + 1)
        }
        return collapsed
    }
}
