/*
 * Copyright (c) 2018, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.graph.util

import edu.uci.ics.jung.graph.MutableCTreeNetwork
import edu.uci.ics.jung.graph.TreeNetworkBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author Tom Nelson
 */
class TreeUtilsTest {

  @Test
  fun testSubTree() {
    val tree: MutableCTreeNetwork<String, Int> =
      TreeNetworkBuilder.builder().expectedNodeCount(7).build()

    tree.addNode("root")

    tree.addEdge("root", "child1", 1) // two kids
    tree.addEdge("root", "child2", 2)

    tree.addEdge("child1", "grandchild11", 11)
    tree.addEdge("child1", "grandchild12", 12)

    tree.addEdge("child2", "grandchild21", 21)
    tree.addEdge("child2", "grandchild22", 22)

    val subTree: MutableCTreeNetwork<String, Int> = TreeUtils.getSubTree(tree, "root")

    // better be the same
    assertEquals(tree.asGraph(), subTree.asGraph())

    val childOneSubTree: MutableCTreeNetwork<String, Int> = TreeUtils.getSubTree(tree, "child1")

    val expectedSubTree: MutableCTreeNetwork<String, Int> =
      TreeNetworkBuilder.builder().expectedNodeCount(3).build()
    expectedSubTree.addNode("child1")
    expectedSubTree.addEdge("child1", "grandchild11", 11)
    expectedSubTree.addEdge("child1", "grandchild12", 12)

    assertEquals(childOneSubTree.asGraph(), expectedSubTree.asGraph())
  }
}
