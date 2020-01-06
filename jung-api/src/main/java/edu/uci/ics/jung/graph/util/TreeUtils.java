/*
 * Created on Mar 3, 2007
 *
 * Copyright (c) 2007, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;

/** Contains static methods for operating on instances of <code>Tree</code>. */
// TODO: add tests
public class TreeUtils {
  public static <N> ImmutableSet<N> roots(Graph<N> graph) {
    checkNotNull(graph, "graph");
    return graph.nodes().stream()
        .filter(node -> graph.predecessors(node).isEmpty())
        .collect(toImmutableSet());
  }

  /**
   * A graph is "forest-shaped" if it is directed, acyclic, and each node has at most one
   * predecessor.
   */
  public static <N> boolean isForestShaped(Graph<N> graph) {
    checkNotNull(graph, "graph");
    return graph.isDirected()
        && !Graphs.hasCycle(graph)
        && graph.nodes().stream().allMatch(node -> graph.predecessors(node).size() <= 1);
  }

  /**
   * A graph is "forest-shaped" if it is directed, acyclic, and each node has at most one
   * predecessor.
   */
  public static <N> boolean isForestShaped(Network<N, ?> graph) {
    checkNotNull(graph, "graph");
    return graph.isDirected()
        && !Graphs.hasCycle(graph)
        && graph.nodes().stream().allMatch(node -> graph.predecessors(node).size() <= 1);
  }

  /**
   * Returns a copy of the subtree of <code>tree</code> which is rooted at <code>root</code>.
   *
   * @param <N> the node type
   * @param <E> the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param root the root of the subtree to be extracted
   */
  public static <N, E> MutableCTreeNetwork<N, E> getSubTree(CTreeNetwork<N, E> tree, N root) {
    checkNotNull(tree, "tree");
    checkNotNull(root, "root");
    checkArgument(
        tree.nodes().contains(root), "Input tree does not contain the input subtree root");
    MutableCTreeNetwork<N, E> subtree = TreeNetworkBuilder.from(tree).withRoot(root).build();
    growSubTree(tree, subtree, root);

    return subtree;
  }

  /**
   * Populates <code>subtree</code> with the subtree of <code>tree</code> which is rooted at <code>
   * root</code>.
   *
   * @param <N> the node type
   * @param <E> the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param subTree the tree instance which is to be populated with the subtree of <code>tree</code>
   * @param root the root of the subtree to be extracted
   */
  // does this need to be a public method?  (or even separate?)
  public static <N, E> void growSubTree(
      CTreeNetwork<N, E> tree, MutableCTreeNetwork<N, E> subTree, N root) {
    checkNotNull(tree, "tree");
    checkNotNull(subTree, "subTree");
    checkNotNull(root, "root");
    for (N kid : tree.successors(root)) {
      E edge = tree.edgesConnecting(root, kid).iterator().next(); // guaranteed to be only one edge
      subTree.addEdge(root, kid, edge);
      growSubTree(tree, subTree, kid);
    }
  }

  /**
   * Connects {@code subTree} to {@code tree} by attaching it as a child of {@code subTreeParent}
   * with edge {@code connectingEdge}.
   *
   * @param <N> the node type
   * @param <E> the edge type
   * @param tree the tree to which {@code subTree} is to be added
   * @param subTree the tree which is to be grafted on to {@code tree}
   * @param subTreeParent the parent of the root of {@code subTree} in its new position in {@code
   *     tree}
   * @param connectingEdge the edge used to connect {@code subTreeParent} to {@code subtree}'s root
   */
  public static <N, E> void addSubTree(
      MutableCTreeNetwork<N, E> tree,
      CTreeNetwork<N, E> subTree,
      N subTreeParent,
      E connectingEdge) {
    checkNotNull(tree, "tree");
    checkNotNull(subTree, "subTree");
    checkNotNull(subTreeParent, "subTreeParent");
    checkNotNull(connectingEdge, "connectingEdge");
    checkArgument(tree.nodes().contains(subTreeParent), "'tree' does not contain 'subTreeParent'");

    if (!subTree.root().isPresent()) {
      // empty subtree; nothing to do
      return;
    }

    N subTreeRoot = subTree.root().get();
    tree.addEdge(subTreeParent, subTreeRoot, connectingEdge);
    addFromSubTree(tree, subTree, subTreeRoot);
  }

  private static <N, E> void addFromSubTree(
      MutableCTreeNetwork<N, E> tree, CTreeNetwork<N, E> subTree, N subTreeRoot) {
    checkNotNull(tree, "tree");
    checkNotNull(subTree, "subTree");
    checkNotNull(subTreeRoot, "subTreeRoot");
    for (E edge : subTree.outEdges(subTreeRoot)) {
      N child = subTree.incidentNodes(edge).target();
      tree.addEdge(subTreeRoot, child, edge);
      addFromSubTree(tree, subTree, child);
    }
  }
}
