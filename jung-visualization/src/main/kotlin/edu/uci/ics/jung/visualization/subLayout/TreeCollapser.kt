/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.subLayout

import com.google.common.collect.Iterables
import edu.uci.ics.jung.graph.MutableCTreeNetwork
import edu.uci.ics.jung.graph.util.TreeUtils

object TreeCollapser {

    /**
     * Replaces the subtree of `tree` rooted at `subRoot` with a node representing that
     * subtree.
     *
     * @param tree the tree whose subtree is to be collapsed
     * @param subRoot the root of the subtree to be collapsed
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun collapse(tree: MutableCTreeNetwork<Any, Any>, subRoot: Any): MutableCTreeNetwork<*, *> {
        // get the subtree rooted at subRoot
        val subTree = TreeUtils.getSubTree(tree, subRoot) as MutableCTreeNetwork<Any, Any>
        val parent = tree.predecessor(subRoot)
        if (parent.isPresent) {
            // subRoot has a parent, so attach its parent to subTree in its place
            val parentEdge = Iterables.getOnlyElement(tree.inEdges(subRoot)) // THERE CAN BE ONLY ONE
            tree.removeNode(subRoot)
            tree.addEdge(parent.get(), subTree, parentEdge)
        } else {
            // subRoot is the root of tree
            tree.removeNode(subRoot)
            tree.addNode(subTree)
        }
        return subTree
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun expand(
        tree: MutableCTreeNetwork<Any, Any>, subTree: MutableCTreeNetwork<Any, Any>
    ): MutableCTreeNetwork<*, *> {
        val parent = tree.predecessor(subTree)
        val parentEdge: Any? = if (parent.isPresent) {
            Iterables.getOnlyElement(tree.inEdges(subTree)) // THERE CAN BE ONLY ONE
        } else {
            null
        }
        if (!subTree.root().isPresent) {
            // then the subTree is empty, so just return the tree itself
            return tree
        }
        tree.removeNode(subTree)
        if (parent.isPresent) {
            TreeUtils.addSubTree(tree, subTree, parent.get(), parentEdge!!)
            return tree
        } else {
            // then the tree is empty, so just return the subTree itself
            return subTree
        }
    }
}
