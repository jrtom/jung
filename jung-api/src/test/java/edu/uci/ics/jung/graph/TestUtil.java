/*
 * Copyright (c) 2018, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.graph;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.graph.Graph;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Set;

/** Utility methods used in various edu.uci.ics.jung.graph tests. */
final class TestUtil {
  static final String ERROR_ELEMENT_NOT_IN_TREE = "not an element of this tree";
  static final String FAIL_ERROR_ELEMENT_NOT_IN_TREE =
      "Should not be allowed to pass a node that is not an element of the tree.";
  static final String ERROR_TREE_HAS_ROOT = "is already the root of this tree";
  static final String FAIL_ERROR_TREE_HAS_ROOT =
      "Should not be allowed to pass a node to the tree when it already has a root.";
  static final String ERROR_NODEV_IN_TREE = "%s is an element of this tree";
  static final String FAIL_ERROR_NODEV_IN_TREE =
      "Should not be allowed to pass a nodeV to the tree when it already has nodeV.";
  static final String ERROR_NODEU_NOT_IN_TREE = "%s is not an element of this tree";
  static final String FAIL_ERROR_NODEU_NOT_IN_TREE =
      "Should not be allowed to pass a nodeU to the tree when it does not already have nodeU.";
  private static final String NODE_STRING = "Node";
  private static final String CANNOT_ADD_NODE_STRING = "Cannot add node";
  private static final String CANNOT_ADD_EDGE_STRING = "Cannot add edge";

  private TestUtil() {}

  static void assertNodeNotInTreeErrorMessage(Throwable throwable) {
    assertThat(throwable).hasMessageThat().startsWith(NODE_STRING);
    assertThat(throwable).hasMessageThat().contains(ERROR_ELEMENT_NOT_IN_TREE);
  }

  static void assertTreeAlreadyHasRootErrorMessage(Throwable throwable) {
    assertThat(throwable).hasMessageThat().startsWith(CANNOT_ADD_NODE_STRING);
    assertThat(throwable).hasMessageThat().contains(ERROR_TREE_HAS_ROOT);
  }

  static <N> void assertNodeUNotInTreeErrorMessage(Throwable throwable, N nodeU) {
    assertThat(throwable).hasMessageThat().startsWith(CANNOT_ADD_EDGE_STRING);
    assertThat(throwable).hasMessageThat().contains(String.format(ERROR_NODEU_NOT_IN_TREE, nodeU));
  }

  static <N> void assertNodeVAlreadyElementOfTreeErrorMessage(Throwable throwable, N nodeV) {
    assertThat(throwable).hasMessageThat().startsWith(CANNOT_ADD_EDGE_STRING);
    assertThat(throwable).hasMessageThat().contains(String.format(ERROR_NODEV_IN_TREE, nodeV));
  }

  // TODO: Adapt to accept CTree<?> instances instead of Graph<?> instances, when all usages of this
  // method can themselves be adapted accordingly.
  static void assertStronglyEquivalent(Graph<?> treeA, Graph<?> treeB) {
    // Properties not covered by equals()
    assertThat(treeA.allowsSelfLoops()).isEqualTo(treeB.allowsSelfLoops());
    assertThat(treeA.nodeOrder()).isEqualTo(treeB.nodeOrder());

    assertThat(treeA).isEqualTo(treeB);
  }

  /**
   * There may be cases where tree implementations return custom sets that define their own size()
   * and contains(). Verify that these sets are consistent with the elements of their iterator.
   */
  @CanIgnoreReturnValue
  static <T> Set<T> sanityCheckSet(Set<T> set) {
    assertThat(set).hasSize(Iterators.size(set.iterator()));
    for (Object element : set) {
      assertThat(set).contains(element);
    }
    assertThat(set).doesNotContain(new Object());
    assertThat(set).isEqualTo(ImmutableSet.copyOf(set));
    return set;
  }
}
