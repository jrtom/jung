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
 * Methods for creating a "folded" graph based on an input graph.graph or a hypergraph.
 *
 * <p>A "folded" graph is derived from a k-partite graph by identifying a partition of nodes which
 * will become the nodes of the new graph, copying these nodes into the new graph, and then
 * connecting those nodes whose original analogues were connected indirectly through elements of
 * other partitions.
 *
 * <p>A "folded" graph is derived from a hypergraph by creating nodes based on either the nodes or
 * the hyperedges of the original graph, and connecting nodes in the new graph if their
 * corresponding nodes/hyperedges share a connection with a common hyperedge/node.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 */
// TODO: consider creating hypergraph versions once we have a hypergraph type;
// see JUNG 2.1 source for this file for concepts:
// * nodes -> nodes, replace hyperedges by k-cliques on incident nodes
// * hyperedges -> nodes, (a,b) exists in new graph if a and b share a node
public class FoldingTransformer<N, E> {

  /**
   * Converts <code>g</code> into a unipartite graph whose node set is the nodes of <code>g
   * </code>'s partition <code>p</code>. For nodes <code>a</code> and <code>b</code> in this
   * partition, the resultant graph will include the edge <code>(a,b)</code> if the original graph
   * contains edges <code>(a,c)</code> and <code>(c,b)</code> for at least one node <code>c</code> .
   *
   * <p>The nodes of the new graph are the same as the nodes of the appropriate partition in the old
   * graph.
   *
   * <p>This function will not create self-loops.
   *
   * @param <N> node type
   * @param <E> input edge type
   * @param g input graph
   * @param nodes input node set
   */
  // TODO: consider providing ValueGraph/Network versions of this
  // TODO: consider renaming this
  public static <N> MutableGraph<N> foldToGraph(Graph<N> g, Set<N> nodes) {
    Preconditions.checkArgument(
        g.nodes().containsAll(nodes), "Input graph must contain all specified nodes");
    MutableGraph<N> newGraph = GraphBuilder.from(g).expectedNodeCount(nodes.size()).build();

    for (N node : nodes) {
      for (N s : g.successors(node)) {
        for (N t : g.successors(s)) {
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
   * Converts <code>g</code> into a unipartite graph whose node set is the nodes of <code>g
   * </code>'s partition <code>p</code>. For nodes <code>a</code> and <code>b</code> in this
   * partition, the resultant graph will include the edge <code>(a,b)</code> if the original graph
   * contains edges <code>(a,c)</code> and <code>(c,b)</code> for at least one node <code>c</code> .
   *
   * <p>The nodes of the new graph are the same as the nodes of the appropriate partition in the
   * input graph. The edge values are the sets of nodes that connected the edge's endpoints in the
   * input graph.
   *
   * <p>This function will not create self-loops.
   *
   * @param <N> node type
   * @param <E> input edge type
   * @param g input graph
   * @param nodes input node set
   */
  // TODO: consider providing ValueGraph/Network versions of this
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
          // TODO: consider having the type of Set depend on
          // the input graph's node order
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
