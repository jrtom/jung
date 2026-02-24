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

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Iterators
import com.google.common.graph.Graph
import com.google.common.truth.Truth.assertThat

/** Utility methods used in various edu.uci.ics.jung.graph tests. */
object TestUtil {
  const val ERROR_ELEMENT_NOT_IN_TREE = "not an element of this tree"
  const val FAIL_ERROR_ELEMENT_NOT_IN_TREE =
    "Should not be allowed to pass a node that is not an element of the tree."
  const val ERROR_TREE_HAS_ROOT = "is already the root of this tree"
  const val FAIL_ERROR_TREE_HAS_ROOT =
    "Should not be allowed to pass a node to the tree when it already has a root."
  const val ERROR_NODEV_IN_TREE = "%s is an element of this tree"
  const val FAIL_ERROR_NODEV_IN_TREE =
    "Should not be allowed to pass a nodeV to the tree when it already has nodeV."
  const val ERROR_NODEU_NOT_IN_TREE = "%s is not an element of this tree"
  const val FAIL_ERROR_NODEU_NOT_IN_TREE =
    "Should not be allowed to pass a nodeU to the tree when it does not already have nodeU."
  private const val NODE_STRING = "Node"
  private const val CANNOT_ADD_NODE_STRING = "Cannot add node"
  private const val CANNOT_ADD_EDGE_STRING = "Cannot add edge"

  @JvmStatic
  fun assertNodeNotInTreeErrorMessage(throwable: Throwable) {
    assertThat(throwable).hasMessageThat().startsWith(NODE_STRING)
    assertThat(throwable).hasMessageThat().contains(ERROR_ELEMENT_NOT_IN_TREE)
  }

  @JvmStatic
  fun assertTreeAlreadyHasRootErrorMessage(throwable: Throwable) {
    assertThat(throwable).hasMessageThat().startsWith(CANNOT_ADD_NODE_STRING)
    assertThat(throwable).hasMessageThat().contains(ERROR_TREE_HAS_ROOT)
  }

  @JvmStatic
  fun <N : Any> assertNodeUNotInTreeErrorMessage(throwable: Throwable, nodeU: N) {
    assertThat(throwable).hasMessageThat().startsWith(CANNOT_ADD_EDGE_STRING)
    assertThat(throwable).hasMessageThat().contains(String.format(ERROR_NODEU_NOT_IN_TREE, nodeU))
  }

  @JvmStatic
  fun <N : Any> assertNodeVAlreadyElementOfTreeErrorMessage(throwable: Throwable, nodeV: N) {
    assertThat(throwable).hasMessageThat().startsWith(CANNOT_ADD_EDGE_STRING)
    assertThat(throwable).hasMessageThat().contains(String.format(ERROR_NODEV_IN_TREE, nodeV))
  }

  // TODO: Adapt to accept CTree<?> instances instead of Graph<?> instances, when all usages of this
  // method can themselves be adapted accordingly.
  @JvmStatic
  fun assertStronglyEquivalent(treeA: Graph<*>, treeB: Graph<*>) {
    // Properties not covered by equals()
    assertThat(treeA.allowsSelfLoops()).isEqualTo(treeB.allowsSelfLoops())
    assertThat(treeA.nodeOrder()).isEqualTo(treeB.nodeOrder())

    assertThat(treeA).isEqualTo(treeB)
  }

  /**
   * There may be cases where tree implementations return custom sets that define their own size()
   * and contains(). Verify that these sets are consistent with the elements of their iterator.
   */
  @JvmStatic
  fun <T> sanityCheckSet(set: Set<T>): Set<T> {
    assertThat(set).hasSize(Iterators.size(set.iterator()))
    for (element in set) {
      assertThat(set).contains(element)
    }
    assertThat(set).doesNotContain(Any())
    assertThat(set).isEqualTo(ImmutableSet.copyOf(set))
    return set
  }
}
