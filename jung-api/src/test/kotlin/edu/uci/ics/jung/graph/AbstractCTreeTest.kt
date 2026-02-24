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

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graphs
import com.google.common.graph.ImmutableGraph
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import edu.uci.ics.jung.graph.TestUtil.FAIL_ERROR_ELEMENT_NOT_IN_TREE
import edu.uci.ics.jung.graph.TestUtil.FAIL_ERROR_NODEU_NOT_IN_TREE
import edu.uci.ics.jung.graph.TestUtil.FAIL_ERROR_NODEV_IN_TREE
import edu.uci.ics.jung.graph.TestUtil.FAIL_ERROR_TREE_HAS_ROOT
import edu.uci.ics.jung.graph.TestUtil.assertNodeNotInTreeErrorMessage
import edu.uci.ics.jung.graph.TestUtil.assertNodeUNotInTreeErrorMessage
import edu.uci.ics.jung.graph.TestUtil.assertNodeVAlreadyElementOfTreeErrorMessage
import edu.uci.ics.jung.graph.TestUtil.assertStronglyEquivalent
import edu.uci.ics.jung.graph.TestUtil.assertTreeAlreadyHasRootErrorMessage
import edu.uci.ics.jung.graph.TestUtil.sanityCheckSet
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.HashSet

abstract class AbstractCTreeTest {
  lateinit var tree: MutableCTree<Int>

  companion object {
    const val N1 = 1
    const val N2 = 2
    const val N3 = 3
    const val N4 = 4
    const val N5 = 5
    const val N6 = 6
    private const val NODE_NOT_IN_TREE = 1000

    const val ERROR_MODIFIABLE_SET = "Set returned is unexpectedly modifiable"
    const val ERROR_SELF_LOOP = "self-loops are not allowed"
    const val ERROR_ADDED_SELF_LOOP = "Should not be allowed to add a self-loop edge."

    @JvmStatic
    fun <N : Any> validateTree(tree: CTree<N>) {
      // TODO: Replace Graphs#copyOf with a tree-specific copyOf method if it ever turns up
      assertStronglyEquivalent(tree, Graphs.copyOf(tree))
      assertStronglyEquivalent(tree, ImmutableGraph.copyOf(tree))

      val treeString = tree.toString()
      assertThat(treeString).contains("isDirected: true")
      assertThat(treeString).contains("allowsSelfLoops: false")

      val nodeStart = treeString.indexOf("nodes:")
      val edgeStart = treeString.indexOf("edges:")
      val nodeString = treeString.substring(nodeStart, edgeStart)

      val allEndpointPairs = HashSet<EndpointPair<N>>()

      for (node in sanityCheckSet(tree.nodes())) {
        assertThat(nodeString).contains(node.toString())

        assertThat(tree.degree(node)).isEqualTo(tree.inDegree(node) + tree.outDegree(node))
        assertThat(tree.predecessors(node)).hasSize(tree.inDegree(node))
        assertThat(tree.successors(node)).hasSize(tree.outDegree(node))

        for (adjacentNode in sanityCheckSet(tree.adjacentNodes(node))) {
          assertThat(
            tree.predecessors(node).contains(adjacentNode)
                || tree.successors(node).contains(adjacentNode)
          ).isTrue()
        }

        for (predecessor in sanityCheckSet(tree.predecessors(node))) {
          assertThat(tree.successors(predecessor)).contains(node)
          assertThat(tree.hasEdgeConnecting(predecessor, node)).isTrue()
          assertThat(tree.depth(predecessor)).isLessThan(tree.depth(node))
        }

        for (successor in sanityCheckSet(tree.successors(node))) {
          allEndpointPairs.add(EndpointPair.ordered(node, successor))
          assertThat(tree.predecessors(successor)).contains(node)
          assertThat(tree.hasEdgeConnecting(node, successor)).isTrue()
          assertThat(tree.depth(successor)).isGreaterThan(tree.depth(node))
        }
      }

      sanityCheckSet(tree.edges())
      assertThat(tree.edges()).doesNotContain(EndpointPair.ordered(Any(), Any()))
      assertThat(tree.edges()).isEqualTo(allEndpointPairs)
    }
  }

  /** Creates and returns an instance of the tree to be tested. */
  abstract fun createTree(): MutableCTree<Int>

  /**
   * A proxy method that adds the node [n] to the tree being tested.
   *
   * @return `true` iff the tree was modified as a result of this call
   */
  protected open fun addNode(n: Int): Boolean {
    return tree.addNode(n)
  }

  /**
   * A proxy method that adds the edge to the tree being tested.
   *
   * @return `true` iff the tree was modified as a result of this call
   */
  protected open fun putEdge(n1: Int, n2: Int): Boolean {
    return tree.putEdge(n1, n2)
  }

  @Before
  fun init() {
    tree = createTree()
  }

  @After
  fun validateTreeState() {
    validateTree(tree)
  }

  /**
   * Verifies that the `Set` returned by `nodes` has the expected mutability property
   * (see Guava's `Graph` documentation for more information).
   */
  @Test
  abstract fun nodes_checkReturnedSetMutability()

  /**
   * Verifies that the `Set` returned by `adjacentNodes` has the expected mutability
   * property (see Guava's `Graph` documentation for more information).
   */
  @Test
  abstract fun adjacentNodes_checkReturnedSetMutability()

  /**
   * Verifies that the `Set` returned by `predecessors` has the expected mutability
   * property (see Guava's `Graph` documentation for more information).
   */
  @Test
  abstract fun predecessors_checkReturnedSetMutability()

  /**
   * Verifies that the `Set` returned by `successors` has the expected mutability
   * property (see Guava's `Graph` documentation for more information).
   */
  @Test
  abstract fun successors_checkReturnedSetMutability()

  @Test
  fun nodes_oneNode() {
    addNode(N1)
    assertThat(tree.nodes()).containsExactly(N1)
  }

  @Test
  fun nodes_noNodes() {
    assertThat(tree.nodes()).isEmpty()
  }

  @Test
  fun adjacentNodes_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.adjacentNodes(N1)).containsExactly(N2)
    assertThat(tree.adjacentNodes(N2)).containsExactly(N1)
  }

  @Test
  fun adjacentNodes_noAdjacentNodes() {
    addNode(N1)
    assertThat(tree.adjacentNodes(N1)).isEmpty()
  }

  @Test
  fun adjacentNodes_nodeNotInTree() {
    try {
      tree.adjacentNodes(NODE_NOT_IN_TREE)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun predecessors_noPredecessors() {
    addNode(N1)
    assertThat(tree.predecessors(N1)).isEmpty()
  }

  @Test
  fun predecessors_nodeNotInTree() {
    try {
      tree.predecessors(NODE_NOT_IN_TREE)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun successors_noSuccessors() {
    addNode(N1)
    assertThat(tree.successors(N1)).isEmpty()
  }

  @Test
  fun successors_nodeNotInTree() {
    try {
      tree.successors(NODE_NOT_IN_TREE)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun degree_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.degree(N1)).isEqualTo(1)
    assertThat(tree.degree(N2)).isEqualTo(1)
  }

  @Test
  fun degree_isolatedNode() {
    addNode(N1)
    assertThat(tree.degree(N1)).isEqualTo(0)
  }

  @Test
  fun degree_nodeNotInTree() {
    try {
      tree.degree(NODE_NOT_IN_TREE)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun inDegree_isolatedNode() {
    addNode(N1)
    assertThat(tree.inDegree(N1)).isEqualTo(0)
  }

  @Test
  fun inDegree_nodeNotInTree() {
    try {
      tree.inDegree(NODE_NOT_IN_TREE)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun outDegree_isolatedNode() {
    addNode(N1)
    assertThat(tree.outDegree(N1)).isEqualTo(0)
  }

  @Test
  fun outDegree_nodeNotInTree() {
    try {
      tree.outDegree(NODE_NOT_IN_TREE)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun predecessors_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.predecessors(N2)).containsExactly(N1)
    // Edge direction handled correctly
    assertThat(tree.predecessors(N1)).isEmpty()
  }

  @Test
  fun successors_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.successors(N1)).containsExactly(N2)
    // Edge direction handled correctly
    assertThat(tree.successors(N2)).isEmpty()
  }

  @Test
  fun inDegree_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.inDegree(N2)).isEqualTo(1)
    // Edge direction handled correctly
    assertThat(tree.inDegree(N1)).isEqualTo(0)
  }

  @Test
  fun outDegree_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.outDegree(N1)).isEqualTo(1)
    // Edge direction handled correctly
    assertThat(tree.outDegree(N2)).isEqualTo(0)
  }

  @Test
  fun edges_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.edges()).containsExactly(EndpointPair.ordered(N1, N2))
  }

  @Test
  fun edges_noEdges() {
    assertThat(tree.edges()).isEmpty()
  }

  @Test
  fun incidentEdges_oneOutgoingEdge() {
    putEdge(N1, N2)
    assertThat(tree.incidentEdges(N1)).containsExactly(EndpointPair.ordered(N1, N2))
  }

  @Test
  fun incidentEdges_oneIncomingEdge() {
    putEdge(N1, N2)
    assertThat(tree.incidentEdges(N2)).containsExactly(EndpointPair.ordered(N1, N2))
  }

  @Test
  fun incidentEdges_noEdges() {
    try {
      tree.incidentEdges(NODE_NOT_IN_TREE)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun incidentEdges_outgoingAndIncomingEdges() {
    putEdge(N1, N2)
    putEdge(N2, N3)
    putEdge(N2, N4)
    putEdge(N3, N5)
    assertThat(tree.incidentEdges(N1)).containsExactly(EndpointPair.ordered(N1, N2))
    assertThat(tree.incidentEdges(N2))
      .containsExactly(
        EndpointPair.ordered(N1, N2),
        EndpointPair.ordered(N2, N3),
        EndpointPair.ordered(N2, N4)
      )
    assertThat(tree.incidentEdges(N3))
      .containsExactly(EndpointPair.ordered(N2, N3), EndpointPair.ordered(N3, N5))
    assertThat(tree.incidentEdges(N4)).containsExactly(EndpointPair.ordered(N2, N4))
    assertThat(tree.incidentEdges(N5)).containsExactly(EndpointPair.ordered(N3, N5))
  }

  @Test
  fun hasEdgeConnecting_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.hasEdgeConnecting(N1, N2)).isTrue()
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N2, N1)).isFalse()
  }

  @Test
  fun hasEdgeConnecting_noEdges() {
    val allPossibleNodes = ImmutableSet.of(N1, N2, N3, N4, N5, N6)
    val allPairs = Sets.cartesianProduct(ImmutableList.of(allPossibleNodes, allPossibleNodes))
    for (pair in allPairs) {
      try {
        tree.hasEdgeConnecting(pair[0], pair[1])
      } catch (e: IllegalArgumentException) {
        assertNodeNotInTreeErrorMessage(e)
      }
    }
  }

  @Test
  fun hasEdgeConnecting_multipleEdges() {
    putEdge(N1, N2)
    putEdge(N2, N3)
    putEdge(N3, N4)
    putEdge(N3, N5)

    assertThat(tree.hasEdgeConnecting(N1, N2)).isTrue()
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N2, N1)).isFalse()

    assertThat(tree.hasEdgeConnecting(N2, N3)).isTrue()
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N3, N2)).isFalse()

    assertThat(tree.hasEdgeConnecting(N3, N4)).isTrue()
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N4, N3)).isFalse()
    assertThat(tree.hasEdgeConnecting(N3, N5)).isTrue()
    // Edge direction handled correctly
    assertThat(tree.hasEdgeConnecting(N5, N3)).isFalse()

    // Sanity check
    assertThat(tree.hasEdgeConnecting(N5, N1)).isFalse()
  }

  @Test
  fun root_oneNode() {
    addNode(N1)
    assertThat(tree.root()).hasValue(N1)
  }

  @Test
  fun root_noNodes() {
    assertThat(tree.root()).isEmpty()
  }

  @Test
  fun root_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.root()).hasValue(N1)
  }

  @Test
  fun root_multipleEdges() {
    buildTreeWithMultipleEdges()
    assertThat(tree.root()).hasValue(N1)
  }

  @Test
  fun depth_oneNode() {
    addNode(N1)
    assertThat(tree.depth(N1)).isEqualTo(0)
    try {
      tree.depth(N2)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun depth_noNodes() {
    try {
      tree.depth(N1)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun depth_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.depth(N1)).isEqualTo(0)
    assertThat(tree.depth(N2)).isEqualTo(1)
    try {
      tree.depth(N3)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun depth_multipleEdges() {
    buildTreeWithMultipleEdges()
    assertWithMessage("tree.depth(%s)", N1).that(tree.depth(N1)).isEqualTo(0)
    assertWithMessage("tree.depth(%s)", N2).that(tree.depth(N2)).isEqualTo(1)
    assertWithMessage("tree.depth(%s)", N3).that(tree.depth(N3)).isEqualTo(1)
    assertWithMessage("tree.depth(%s)", N4).that(tree.depth(N4)).isEqualTo(2)
    assertWithMessage("tree.depth(%s)", N5).that(tree.depth(N5)).isEqualTo(2)
    assertWithMessage("tree.depth(%s)", N6).that(tree.depth(N6)).isEqualTo(3)
  }

  @Test
  fun height_oneNode() {
    addNode(N1)
    assertThat(tree.height()).hasValue(0)
  }

  @Test
  fun height_noNodes() {
    assertThat(tree.height()).isEmpty()
  }

  @Test
  fun height_oneEdge() {
    putEdge(N1, N2)
    assertThat(tree.height()).hasValue(1)
  }

  @Test
  fun height_multipleEdges() {
    buildTreeWithMultipleEdges()
    assertThat(tree.height()).hasValue(3)
  }

  private fun buildTreeWithMultipleEdges() {
    putEdge(N1, N2)
    putEdge(N1, N3)
    putEdge(N3, N4)
    putEdge(N2, N5)
    putEdge(N5, N6)
  }

  // Element Mutation

  @Test
  fun addNode_newNode() {
    assertThat(addNode(N1)).isTrue()
    assertThat(tree.nodes()).contains(N1)
  }

  @Test
  fun addNode_existingNode() {
    addNode(N1)
    val nodes = ImmutableSet.copyOf(tree.nodes())
    assertThat(addNode(N1)).isFalse()
    assertThat(tree.nodes()).containsExactlyElementsIn(nodes)
  }

  @Test
  fun addNode_twoNodes() {
    addNode(N1)
    try {
      addNode(N2)
      fail(FAIL_ERROR_TREE_HAS_ROOT)
    } catch (e: IllegalArgumentException) {
      assertTreeAlreadyHasRootErrorMessage(e)
    }
  }

  @Test
  fun addNode_existingEdge() {
    putEdge(N1, N2)
    try {
      addNode(N3)
      fail(FAIL_ERROR_TREE_HAS_ROOT)
    } catch (e: IllegalArgumentException) {
      assertTreeAlreadyHasRootErrorMessage(e)
    }
  }

  @Test
  fun removeNode_existingNode() {
    putEdge(N1, N2)
    putEdge(N1, N4)
    assertThat(tree.removeNode(N2)).isTrue()
    assertThat(tree.removeNode(N2)).isFalse()
    assertThat(tree.nodes()).containsExactly(N1, N4)
    assertThat(tree.successors(N1)).containsExactly(N4)
    assertThat(tree.predecessors(N4)).containsExactly(N1)
  }

  @Test
  fun removeNode_nodeNotPresent() {
    addNode(N1)
    val nodes = ImmutableSet.copyOf(tree.nodes())
    assertThat(tree.removeNode(NODE_NOT_IN_TREE)).isFalse()
    assertThat(tree.nodes()).containsExactlyElementsIn(nodes)
  }

  @Test
  fun removeNode_queryAfterRemoval() {
    addNode(N1)
    @Suppress("UNUSED_VARIABLE")
    val unused = tree.adjacentNodes(N1) // ensure cache (if any) is populated
    assertThat(tree.removeNode(N1)).isTrue()
    try {
      tree.adjacentNodes(N1)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun addEdge_existingRoot() {
    addNode(N1)
    assertThat(putEdge(N1, N2)).isTrue()
  }

  @Test
  fun addEdge_noExistingRoot() {
    assertThat(putEdge(N1, N2)).isTrue()
  }

  @Test
  fun addEdge_existingEdgeBetweenSameNodes() {
    putEdge(N1, N2)
    assertThat(putEdge(N1, N2)).isFalse()
  }

  @Test
  fun addEdge_selfLoop() {
    try {
      putEdge(N1, N1)
      fail(ERROR_ADDED_SELF_LOOP)
    } catch (e: IllegalArgumentException) {
      assertThat(e).hasMessageThat().contains(ERROR_SELF_LOOP)
    }
  }

  @Test
  fun addEdge_antiparallelEdges() {
    putEdge(N1, N2)
    try {
      putEdge(N2, N1)
      fail(FAIL_ERROR_NODEV_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeVAlreadyElementOfTreeErrorMessage(e, N1)
    }
  }

  @Test
  fun addEdge_disconnectedEdges() {
    putEdge(N1, N2)
    try {
      putEdge(N3, N4)
      fail(FAIL_ERROR_NODEU_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeUNotInTreeErrorMessage(e, N3)
    }
  }

  @Test
  fun addEdge_nodesNotInTree() {
    addNode(N1)

    assertThat(putEdge(N1, N5)).isTrue()
    assertThat(putEdge(N1, N4)).isTrue()
    assertThat(putEdge(N1, N2)).isTrue()
    assertThat(putEdge(N4, N3)).isTrue()
    assertThat(putEdge(N3, N6)).isTrue()

    assertThat(tree.nodes()).containsExactly(N1, N5, N4, N2, N3, N6).inOrder()
    assertThat(tree.successors(N1)).containsExactly(N2, N4, N5)
    assertThat(tree.successors(N2)).isEmpty()
    assertThat(tree.successors(N3)).containsExactly(N6)
    assertThat(tree.successors(N4)).containsExactly(N3)
    assertThat(tree.successors(N5)).isEmpty()
    assertThat(tree.successors(N6)).isEmpty()

    assertThat(tree.predecessors(N1)).isEmpty()
    assertThat(tree.predecessors(N2)).containsExactly(N1)
    assertThat(tree.predecessors(N3)).containsExactly(N4)
    assertThat(tree.predecessors(N4)).containsExactly(N1)
    assertThat(tree.predecessors(N5)).containsExactly(N1)
    assertThat(tree.predecessors(N6)).containsExactly(N3)
  }

  @Test
  fun removeEdge_existingEdge() {
    putEdge(N1, N2)
    assertThat(tree.successors(N1)).containsExactly(N2)
    assertThat(tree.predecessors(N2)).containsExactly(N1)
    assertThat(tree.removeEdge(N1, N2)).isTrue()
    assertThat(tree.removeEdge(N1, N2)).isFalse()
    assertThat(tree.successors(N1)).isEmpty()
    try {
      tree.predecessors(N2)
      fail(FAIL_ERROR_ELEMENT_NOT_IN_TREE)
    } catch (e: IllegalArgumentException) {
      assertNodeNotInTreeErrorMessage(e)
    }
  }

  @Test
  fun removeEdge_oneOfMany() {
    putEdge(N1, N2)
    putEdge(N1, N3)
    putEdge(N1, N4)
    assertThat(tree.removeEdge(N1, N3)).isTrue()
    assertThat(tree.adjacentNodes(N1)).containsExactly(N2, N4)
  }

  @Test
  fun removeEdge_nodeNotPresent() {
    putEdge(N1, N2)
    assertThat(tree.removeEdge(N1, NODE_NOT_IN_TREE)).isFalse()
    assertThat(tree.successors(N1)).contains(N2)
  }
}
