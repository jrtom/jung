/*
 * Copyright (c) 2018, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.graph

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.fail
import org.junit.Test

// TODO: Consider merging AbstractCTreeTest into CTreeTest
class CTreeTest : AbstractCTreeTest() {

  override fun createTree(): MutableCTree<Int> {
    return TreeBuilder.builder().build()
  }

  @Test
  override fun nodes_checkReturnedSetMutability() {
    val nodes = tree.nodes()
    try {
      nodes.add(N2)
      fail(ERROR_MODIFIABLE_SET)
    } catch (e: UnsupportedOperationException) {
      addNode(N1)
      assertThat(tree.nodes()).containsExactlyElementsIn(nodes)
    }
  }

  @Test
  override fun adjacentNodes_checkReturnedSetMutability() {
    addNode(N1)
    val adjacentNodes = tree.adjacentNodes(N1)
    try {
      adjacentNodes.add(N2)
      fail(ERROR_MODIFIABLE_SET)
    } catch (e: UnsupportedOperationException) {
      putEdge(N1, N2)
      assertThat(tree.adjacentNodes(N1)).containsExactlyElementsIn(adjacentNodes)
    }
  }

  @Test
  override fun predecessors_checkReturnedSetMutability() {
    addNode(N2)
    val predecessors = tree.predecessors(N2)
    try {
      predecessors.add(N1)
      fail(ERROR_MODIFIABLE_SET)
    } catch (e: UnsupportedOperationException) {
      // putting an edge like `putEdge(N1, N2)`, which would have been used to check that N2's
      // predecessors gets updated, cannot be done because CTree#putEdge cannot be used to
      // implicitly update the root of a tree.
    }
  }

  @Test
  override fun successors_checkReturnedSetMutability() {
    addNode(N1)
    val successors = tree.successors(N1)
    try {
      successors.add(N2)
      fail(ERROR_MODIFIABLE_SET)
    } catch (e: UnsupportedOperationException) {
      putEdge(N1, N2)
      assertThat(successors).containsExactlyElementsIn(tree.successors(N1))
    }
  }
}
