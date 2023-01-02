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
package edu.uci.ics.jung.algorithms.blockmodel;

import com.google.common.graph.Graph;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maintains information about a node partition of a graph. This can be built from a map from nodes
 * to node sets or from a collection of (disjoint) node sets, such as those created by various
 * clustering methods.
 */
public class NodePartition<N> {
  private Map<N, Set<N>> node_partition_map;
  private Collection<Set<N>> node_sets;
  private Graph<N> graph;

  /**
   * Creates an instance based on the specified graph and mapping from nodes to node sets, and
   * generates a set of partitions based on this mapping.
   *
   * @param g the graph over which the node partition is defined
   * @param partition_map the mapping from nodes to node sets (partitions)
   */
  public NodePartition(Graph<N> g, Map<N, Set<N>> partition_map) {
    this.node_partition_map = Collections.unmodifiableMap(partition_map);
    this.graph = g;
  }

  /**
   * Creates an instance based on the specified graph, node-set mapping, and set of disjoint node
   * sets. The node-set mapping and node partitions must be consistent; that is, the mapping must
   * reflect the division of nodes into partitions, and each node must appear in exactly one
   * partition.
   *
   * @param g the graph over which the node partition is defined
   * @param partition_map the mapping from nodes to node sets (partitions)
   * @param node_sets the set of disjoint node sets
   */
  public NodePartition(Graph<N> g, Map<N, Set<N>> partition_map, Collection<Set<N>> node_sets) {
    this.node_partition_map = Collections.unmodifiableMap(partition_map);
    this.node_sets = node_sets;
    this.graph = g;
  }

  /**
   * Creates an instance based on the specified graph and set of disjoint node sets, and generates a
   * node-to-partition map based on these sets.
   *
   * @param g the graph over which the node partition is defined
   * @param node_sets the set of disjoint node sets
   */
  public NodePartition(Graph<N> g, Collection<Set<N>> node_sets) {
    this.node_sets = node_sets;
    this.graph = g;
  }

  /**
   * Returns the graph on which the partition is defined.
   *
   * @return the graph on which the partition is defined
   */
  public Graph<N> getGraph() {
    return graph;
  }

  /**
   * Returns a map from each node in the input graph to its partition. This map is generated if it
   * does not already exist.
   *
   * @return a map from each node in the input graph to a node set
   */
  public Map<N, Set<N>> getNodeToPartitionMap() {
    if (node_partition_map == null) {
      this.node_partition_map = new HashMap<N, Set<N>>();
      for (Set<N> set : this.node_sets) {
        for (N v : set) {
          this.node_partition_map.put(v, set);
        }
      }
    }
    return node_partition_map;
  }

  /**
   * Returns a collection of node sets, where each node in the input graph is in exactly one set.
   * This collection is generated based on the node-to-partition map if it does not already exist.
   *
   * @return a collection of node sets such that each node in the instance's graph is in exactly one
   *     set
   */
  public Collection<Set<N>> getNodePartitions() {
    if (node_sets == null) {
      this.node_sets = new HashSet<Set<N>>();
      this.node_sets.addAll(node_partition_map.values());
    }
    return node_sets;
  }

  /**
   * @return the number of partitions.
   */
  public int numPartitions() {
    return node_sets.size();
  }

  @Override
  public String toString() {
    return "Partitions: " + node_partition_map;
  }
}
