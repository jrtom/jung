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
    // get the subtree rooted at subRoot
    MutableCTreeNetwork subTree = TreeUtils.getSubTree(tree, subRoot);
    Optional parent = tree.predecessor(subRoot);
    if (parent.isPresent()) {
      // subRoot has a parent, so attach its parent to subTree in its place
      Object parentEdge = Iterables.getOnlyElement(tree.inEdges(subRoot)); // THERE CAN BE ONLY ONE
      tree.removeNode(subRoot);
      tree.addEdge(parent.get(), subTree, parentEdge);
    } else {
      // subRoot is the root of tree
      tree.removeNode(subRoot);
      tree.addNode(subTree);
    }
    return subTree;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static MutableCTreeNetwork expand(MutableCTreeNetwork tree, MutableCTreeNetwork subTree) {
    Optional parent = tree.predecessor(subTree);
    Object parentEdge =
        parent.isPresent()
            ? Iterables.getOnlyElement(tree.inEdges(subTree)) // THERE CAN BE ONLY ONE
            : null;
    if (!subTree.root().isPresent()) {
      // then the subTree is empty, so just return the tree itself
      return tree;
    }
    tree.removeNode(subTree);
    if (parent.isPresent()) {
      TreeUtils.addSubTree(tree, subTree, parent.get(), parentEdge);
      return tree;
    } else {
      // then the tree is empty, so just return the subTree itself
      return subTree;
    }
  }
}
