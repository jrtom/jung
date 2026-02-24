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
package edu.uci.ics.jung.graph.util

import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.ImmutableSet
import com.google.common.graph.Graph
import com.google.common.graph.Network
import edu.uci.ics.jung.graph.CTreeNetwork
import edu.uci.ics.jung.graph.MutableCTreeNetwork
import edu.uci.ics.jung.graph.TreeNetworkBuilder
import com.google.common.graph.Graphs as GuavaGraphs

/** Contains static methods for operating on instances of `Tree`. */
// TODO: add tests
object TreeUtils {

  @JvmStatic
  fun <N> roots(graph: Graph<N>): ImmutableSet<N> {
    checkNotNull(graph, "graph")
    return graph.nodes().stream()
      .filter { node -> graph.predecessors(node).isEmpty() }
      .collect(ImmutableSet.toImmutableSet())
  }

  /**
   * A graph is "forest-shaped" if it is directed, acyclic, and each node has at most one
   * predecessor.
   */
  @JvmStatic
  fun <N> isForestShaped(graph: Graph<N>): Boolean {
    checkNotNull(graph, "graph")
    return graph.isDirected
      && !GuavaGraphs.hasCycle(graph)
      && graph.nodes().stream().allMatch { node -> graph.predecessors(node).size <= 1 }
  }

  /**
   * A graph is "forest-shaped" if it is directed, acyclic, and each node has at most one
   * predecessor.
   */
  @JvmStatic
  fun <N> isForestShaped(graph: Network<N, *>): Boolean {
    checkNotNull(graph, "graph")
    return graph.isDirected
      && !GuavaGraphs.hasCycle(graph)
      && graph.nodes().stream().allMatch { node -> graph.predecessors(node).size <= 1 }
  }

  /**
   * Returns a copy of the subtree of `tree` which is rooted at `root`.
   *
   * @param N the node type
   * @param E the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param root the root of the subtree to be extracted
   */
  @JvmStatic
  fun <N : Any, E : Any> getSubTree(tree: CTreeNetwork<N, E>, root: N): MutableCTreeNetwork<N, E> {
    checkNotNull(tree, "tree")
    checkNotNull(root, "root")
    checkArgument(
      tree.nodes().contains(root), "Input tree does not contain the input subtree root"
    )
    val subtree = TreeNetworkBuilder.from(tree).withRoot(root).build<N, E>()
    growSubTree(tree, subtree, root)
    return subtree
  }

  /**
   * Populates `subtree` with the subtree of `tree` which is rooted at `root`.
   *
   * @param N the node type
   * @param E the edge type
   * @param tree the tree whose subtree is to be extracted
   * @param subTree the tree instance which is to be populated with the subtree of `tree`
   * @param root the root of the subtree to be extracted
   */
  // does this need to be a public method?  (or even separate?)
  @JvmStatic
  fun <N : Any, E : Any> growSubTree(
    tree: CTreeNetwork<N, E>,
    subTree: MutableCTreeNetwork<N, E>,
    root: N
  ) {
    checkNotNull(tree, "tree")
    checkNotNull(subTree, "subTree")
    checkNotNull(root, "root")
    for (kid in tree.successors(root)) {
      val edge = tree.edgesConnecting(root, kid).iterator().next() // guaranteed to be only one edge
      subTree.addEdge(root, kid, edge)
      growSubTree(tree, subTree, kid)
    }
  }

  /**
   * Connects [subTree] to [tree] by attaching it as a child of [subTreeParent]
   * with edge [connectingEdge].
   *
   * @param N the node type
   * @param E the edge type
   * @param tree the tree to which [subTree] is to be added
   * @param subTree the tree which is to be grafted on to [tree]
   * @param subTreeParent the parent of the root of [subTree] in its new position in [tree]
   * @param connectingEdge the edge used to connect [subTreeParent] to [subTree]'s root
   */
  @JvmStatic
  fun <N : Any, E : Any> addSubTree(
    tree: MutableCTreeNetwork<N, E>,
    subTree: CTreeNetwork<N, E>,
    subTreeParent: N,
    connectingEdge: E
  ) {
    checkNotNull(tree, "tree")
    checkNotNull(subTree, "subTree")
    checkNotNull(subTreeParent, "subTreeParent")
    checkNotNull(connectingEdge, "connectingEdge")
    checkArgument(tree.nodes().contains(subTreeParent), "'tree' does not contain 'subTreeParent'")

    if (!subTree.root().isPresent) {
      // empty subtree; nothing to do
      return
    }

    val subTreeRoot = subTree.root().get()
    tree.addEdge(subTreeParent, subTreeRoot, connectingEdge)
    addFromSubTree(tree, subTree, subTreeRoot)
  }

  private fun <N : Any, E : Any> addFromSubTree(
    tree: MutableCTreeNetwork<N, E>,
    subTree: CTreeNetwork<N, E>,
    subTreeRoot: N
  ) {
    checkNotNull(tree, "tree")
    checkNotNull(subTree, "subTree")
    checkNotNull(subTreeRoot, "subTreeRoot")
    for (edge in subTree.outEdges(subTreeRoot)) {
      val child = subTree.incidentNodes(edge).target()
      tree.addEdge(subTreeRoot, child, edge)
      addFromSubTree(tree, subTree, child)
    }
  }
}
