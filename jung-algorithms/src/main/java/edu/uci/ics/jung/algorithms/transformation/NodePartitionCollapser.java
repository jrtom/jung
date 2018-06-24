/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.transformation;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import edu.uci.ics.jung.algorithms.blockmodel.NodePartition;
import java.util.Map;
import java.util.Set;

/**
 * This class transforms a graph with a known node partitioning into a graph whose nodes correspond
 * to the input graph's partitions
 *
 * <p>Concept based on Danyel Fisher's <code>GraphCollapser</code> in JUNG 1.x.
 */
// TODO: add tests
public class NodePartitionCollapser {
  /**
   * Creates a new graph whose nodes correspond to the partitions of the supplied graph. Two nodes u
   * and v in the collapsed graph will be connected if there is an edge between any of the nodes in
   * u and any of the nodes in v, and u and v are distinct. The value of the edge represents the
   * number of such edges.
   *
   * @param partitioning a node partition of a graph
   * @return the collapsed graph
   */
  public static <N> ValueGraph<Set<N>, Integer> collapseNodePartitions(
      NodePartition<N> partitioning) {
    Graph<N> original = partitioning.getGraph();
    ValueGraphBuilder<Object, Object> builder =
        original.isDirected() ? ValueGraphBuilder.directed() : ValueGraphBuilder.undirected();
    MutableValueGraph<Set<N>, Integer> collapsed = builder.build();

    // create nodes in new graph corresponding to equivalence sets in the original graph
    for (Set<N> set : partitioning.getNodePartitions()) {
      collapsed.addNode(set);
    }

    // for each pair of endpoints in the original graph, connect the corresponding nodes
    // (representing partitions) in the collapsed graph if the partitions are different
    Map<N, Set<N>> nodeToPartition = partitioning.getNodeToPartitionMap();
    for (EndpointPair<N> endpoints : original.edges()) {
      N nodeU = endpoints.nodeU();
      N nodeV = endpoints.nodeV();
      Set<N> partitionU = nodeToPartition.get(nodeU);
      Set<N> partitionV = nodeToPartition.get(nodeV);
      if (nodeU.equals(nodeV) || partitionU.equals(partitionV)) {
        // we only connect partitions if the partitions are different;
        // check the nodes first as an optimization
        continue;
      }

      int edgeCount = collapsed.edgeValueOrDefault(partitionU, partitionV, 0);
      collapsed.putEdgeValue(partitionU, partitionV, edgeCount + 1);
    }
    return collapsed;
  }
}
