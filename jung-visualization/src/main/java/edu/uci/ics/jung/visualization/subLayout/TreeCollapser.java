/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.subLayout;

import com.google.common.collect.Iterables;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Traverser;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.util.TreeUtils;
import java.util.Optional;

public class TreeCollapser {

  /**
   * Replaces the subtree of {@code tree} rooted at {@code subRoot} with a node representing that
   * subtree.
   *
   * @param tree the tree whose subtree is to be collapsed
   * @param subRoot the root of the subtree to be collapsed
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static MutableCTreeNetwork collapse(MutableCTreeNetwork tree, Object subRoot) {
    System.out.format("collapse: (1): tree: %s, subRoot: %s%n", tree, subRoot);
    // get the subtree rooted at subRoot
    MutableCTreeNetwork subTree = TreeUtils.getSubTree(tree, subRoot);
    Optional parent = tree.predecessor(subRoot);
    if (parent.isPresent()) {
      // subRoot has a parent, so attach its parent to subTree in its place
      Object parentEdge = tree.inEdges(subRoot).iterator().next(); // THERE CAN BE ONLY ONE
      tree.removeNode(subRoot);
      tree.addEdge(parent.get(), subTree, parentEdge);
    } else {
      // subRoot is the root of tree
      tree.removeNode(subRoot);
      tree.addNode(subTree);
    }
    System.out.format("collapse: (2): return: %s%n", subTree);
    return subTree;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static CTreeNetwork expand(MutableCTreeNetwork tree, CTreeNetwork subTree) {
    System.out.format("expand: (1): tree: %s, subTree: %s%n", tree, subTree);
    Optional parent = tree.predecessor(subTree);
    Object parentEdge =
        parent.isPresent()
            ? Iterables.getOnlyElement(tree.inEdges(subTree)) // THERE CAN BE ONLY ONE
            : null;
    tree.removeNode(subTree);
    if (!subTree.root().isPresent()) {
      // then the subTree is empty, so just return the tree itself
      System.out.format("expand: (2): return: %s%n", tree);
      return tree;
    }
    if (parent.isPresent()) {
      TreeUtils.addSubTree(tree, subTree, parent.get(), parentEdge);
      System.out.format("expand: (3): return: %s%n", tree);
      return tree;
    } else {
      // then the tree is empty, so just return the subTree itself
      System.out.format("expand: (2): return: %s%n", subTree);
      return subTree;
    }
  }

  // currently unused
  private static <N, E> void copyInto(MutableCTreeNetwork<N, E> tree, CTreeNetwork<N, E> subTree) {
    N root = subTree.root().get(); // safe to use .get() as .root() is already known to be present
    tree.addNode(root);
    // Connect each successively deeper node to its parent
    Iterable<N> insertionOrder =
        Iterables.skip(
            Traverser.forTree(subTree).depthFirstPreOrder(root), 1); // skip the root itself
    for (N current : insertionOrder) {
      E inEdge = Iterables.getOnlyElement(subTree.inEdges(current));
      EndpointPair<N> endpointPair = subTree.incidentNodes(inEdge);
      tree.addEdge(endpointPair.nodeU(), endpointPair.nodeV(), inEdge);
    }
  }
}
