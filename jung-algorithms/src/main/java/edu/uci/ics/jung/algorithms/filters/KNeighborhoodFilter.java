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
 * Created on Dec 26, 2001
 *
 */
package edu.uci.ics.jung.algorithms.filters;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

/**
 * A filter used to extract the k-neighborhood around a set of root nodes. The k-neighborhood is
 * defined as the subgraph induced by the set of nodes that are k or fewer hops away from the root
 * node.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
public class KNeighborhoodFilter {

  // TODO: create ValueGraph/Network versions
  public static <N> MutableGraph<N> filterGraph(Graph<N> graph, Set<N> rootNodes, int radius) {
    checkNotNull(graph);
    checkNotNull(rootNodes);
    checkArgument(graph.nodes().containsAll(rootNodes), "graph must contain all of rootNodes");
    checkArgument(radius > 0, "radius must be > 0");

    MutableGraph<N> filtered = GraphBuilder.from(graph).build();
    for (N root : rootNodes) {
      filtered.addNode(root);
    }
    Queue<N> currentNodes = new ArrayDeque<>(rootNodes);
    Queue<N> nextNodes = new ArrayDeque<>();

    for (int depth = 1; depth <= radius && !currentNodes.isEmpty(); depth++) {
      while (!currentNodes.isEmpty()) {
        N currentNode = currentNodes.remove();
        for (N nextNode : graph.successors(currentNode)) {
          // the addNode needs to happen before putEdge() because we need to know whether
          // the node was present in the graph
          // (and putEdge() will always add the node if not present)
          if (filtered.addNode(nextNode)) {
            nextNodes.add(nextNode);
          }
          filtered.putEdge(currentNode, nextNode);
        }
      }
      Queue<N> emptyQueue = currentNodes;
      currentNodes = nextNodes;
      nextNodes = emptyQueue;
    }

    // put in in-edges from nodes in the filtered graph
    for (N node : filtered.nodes()) {
      for (N predecessor : graph.predecessors(node)) {
        if (filtered.nodes().contains(predecessor)) {
          filtered.putEdge(predecessor, node);
        }
      }
    }

    return filtered;
  }
}
