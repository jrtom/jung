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
 * Created on Apr 21, 2004
 */
package edu.uci.ics.jung.algorithms.transformation;

import com.google.common.base.Preconditions;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Methods for creating a "folded" graph based on an input graph.
 *
 * <p>A "folded" graph is derived from an input graph by identifying a subset of nodes which will
 * become the nodes of the new graph, copying these nodes into the new graph, and then connecting
 * those nodes that were connected indirectly in the input graph through nodes not in that subset.
 * This subset is conventionally (but not necessarily) a partition of a k-partite graph.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
// TODO: consider creating hypergraph versions if we add a hypergraph type;
// see JUNG 2.1 source for this file for concepts:
// * nodes -> nodes, replace hyperedges by k-cliques on incident nodes
// * hyperedges -> nodes, (a,b) exists in new graph if a and b share a node
// TODO: consider adding
// (a) variants that define the input nodes via a Predicate
// (b) a utility method that identifies partitions of a k-partite graph
public class FoldingTransformer<N, E> {
  /**
   * Converts {@code graph} into a graph <i>T</i> by "folding" {@code graph} "around" the node set
   * {@code nodes}. <i>T</i>'s nodes will be {@code nodes}; for any two nodes {@code a} and {@code
   * b} in {@code nodes}, <i>T</i> will contain the edge {@code (a, b)} if, and only if, {@code
   * graph} contains a node {@code c} such that
   *
   * <ul>
   *   <li>{@code a} is not equal to {@code b} (thus, <i>T</i> will contain no self-loops)
   *   <li>{@code c} is <b>not</b> in {@code nodes}
   *   <li>{@code graph} contains edges {@code (a,c)} and {@code (c,b)}
   * </ul>
   *
   * <p>The properties of <i>T</i> (such as directedness) are taken from the properties of the input
   * graph (see {@link com.google.common.graph.GraphBuilder#from(com.google.common.graph.Graph)}).
   *
   * @param <N> node type
   * @param graph input graph
   * @param nodes input node set
   * @throws IllegalArgumentException if {@code graph} does not contain all of {@code nodes}
   */
  // TODO: consider renaming this
  public static <N> MutableGraph<N> foldToGraph(Graph<N> graph, Set<N> nodes) {
    Preconditions.checkArgument(
        graph.nodes().containsAll(nodes), "Input graph must contain all specified nodes");
    MutableGraph<N> newGraph = GraphBuilder.from(graph).expectedNodeCount(nodes.size()).build();

    for (N node : nodes) {
      for (N s : graph.successors(node)) {
        for (N t : graph.successors(s)) {
          if (!nodes.contains(t) || t.equals(node)) {
            continue;
          }
          newGraph.putEdge(node, t);
        }
      }
    }
    return newGraph;
  }

  /**
   * Converts {@code graph} into a graph <i>T</i> by "folding" {@code graph} "around" the node set
   * {@code nodes}. <i>T</i>'s nodes will be {@code nodes}; for any two nodes {@code a} and {@code
   * b} in {@code nodes}, <i>T</i> will contain the edge {@code (a, b)} if, and only if, {@code
   * graph} contains a node {@code c} such that
   *
   * <ul>
   *   <li>{@code a} is not equal to {@code b} (thus, <i>T</i> will contain no self-loops)
   *   <li>{@code c} is <b>not</b> in {@code nodes}
   *   <li>{@code graph} contains edges {@code (a,c)} and {@code (c,b)}
   * </ul>
   *
   * <p>The properties of <i>T</i> (such as directedness) are taken from the properties of the input
   * graph (see {@link com.google.common.graph.GraphBuilder#from(com.google.common.graph.Graph)}).
   *
   * <p><i>T</i>'s edge values are the sets of nodes that connected the edge's endpoints in {@code
   * graph}.
   *
   * @param <N> node type
   * @param graph input graph
   * @param nodes input node set
   * @throws IllegalArgumentException if {@code graph} does not contain all of {@code nodes}
   */
  // TODO: consider renaming this
  public static <N> MutableValueGraph<N, Set<N>> foldToValueGraph(Graph<N> g, Set<N> nodes) {
    Preconditions.checkArgument(
        g.nodes().containsAll(nodes), "Input graph must contain all specified nodes");
    ValueGraphBuilder<Object, Object> builder =
        g.isDirected() ? ValueGraphBuilder.directed() : ValueGraphBuilder.undirected();
    MutableValueGraph<N, Set<N>> newGraph =
        builder.expectedNodeCount(nodes.size()).nodeOrder(g.nodeOrder()).build();

    for (N node : nodes) {
      for (N s : g.successors(node)) {
        for (N t : g.successors(s)) {
          if (!nodes.contains(t) || t.equals(node)) {
            continue;
          }
          Set<N> intermediateNodes = newGraph.edgeValueOrDefault(node, t, new LinkedHashSet<N>());
          if (intermediateNodes.isEmpty()) {
            newGraph.putEdgeValue(node, t, intermediateNodes);
          }
          intermediateNodes.add(s);
        }
      }
    }
    return newGraph;
  }
}
