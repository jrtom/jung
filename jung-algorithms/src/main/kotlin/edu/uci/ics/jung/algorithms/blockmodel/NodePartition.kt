/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Feb 3, 2004
 */
package edu.uci.ics.jung.algorithms.blockmodel

import com.google.common.graph.Graph
import java.util.Collections

/**
 * Maintains information about a node partition of a graph. This can be built from a map from nodes
 * to node sets or from a collection of (disjoint) node sets, such as those created by various
 * clustering methods.
 */
class NodePartition<N : Any> {
    private var nodePartitionMap: Map<N, Set<N>>?
    private var nodeSets: MutableCollection<Set<N>>?

    /** Returns the graph on which the partition is defined. */
    val graph: Graph<N>

    /**
     * Creates an instance based on the specified graph and mapping from nodes to node sets, and
     * generates a set of partitions based on this mapping.
     *
     * @param g the graph over which the node partition is defined
     * @param partitionMap the mapping from nodes to node sets (partitions)
     */
    constructor(g: Graph<N>, partitionMap: Map<N, Set<N>>) {
        this.nodePartitionMap = Collections.unmodifiableMap(partitionMap)
        this.nodeSets = null
        this.graph = g
    }

    /**
     * Creates an instance based on the specified graph, node-set mapping, and set of disjoint node
     * sets. The node-set mapping and node partitions must be consistent; that is, the mapping must
     * reflect the division of nodes into partitions, and each node must appear in exactly one
     * partition.
     *
     * @param g the graph over which the node partition is defined
     * @param partitionMap the mapping from nodes to node sets (partitions)
     * @param nodeSets the set of disjoint node sets
     */
    constructor(g: Graph<N>, partitionMap: Map<N, Set<N>>, nodeSets: Collection<Set<N>>) {
        this.nodePartitionMap = Collections.unmodifiableMap(partitionMap)
        this.nodeSets = nodeSets.toMutableList()
        this.graph = g
    }

    /**
     * Creates an instance based on the specified graph and set of disjoint node sets, and generates a
     * node-to-partition map based on these sets.
     *
     * @param g the graph over which the node partition is defined
     * @param nodeSets the set of disjoint node sets
     */
    constructor(g: Graph<N>, nodeSets: Collection<Set<N>>) {
        this.nodeSets = nodeSets.toMutableList()
        this.nodePartitionMap = null
        this.graph = g
    }

    /**
     * Returns a map from each node in the input graph to its partition. This map is generated if it
     * does not already exist.
     *
     * @return a map from each node in the input graph to a node set
     */
    fun getNodeToPartitionMap(): Map<N, Set<N>> {
        if (nodePartitionMap == null) {
            val map = HashMap<N, Set<N>>()
            for (set in nodeSets!!) {
                for (v in set) {
                    map[v] = set
                }
            }
            nodePartitionMap = map
        }
        return nodePartitionMap!!
    }

    /**
     * Returns a collection of node sets, where each node in the input graph is in exactly one set.
     * This collection is generated based on the node-to-partition map if it does not already exist.
     *
     * @return a collection of node sets such that each node in the instance's graph is in exactly one
     *     set
     */
    fun getNodePartitions(): Collection<Set<N>> {
        if (nodeSets == null) {
            val sets = HashSet<Set<N>>()
            sets.addAll(nodePartitionMap!!.values)
            nodeSets = sets
        }
        return nodeSets!!
    }

    /** @return the number of partitions. */
    fun numPartitions(): Int = nodeSets!!.size

    override fun toString(): String = "Partitions: $nodePartitionMap"
}
