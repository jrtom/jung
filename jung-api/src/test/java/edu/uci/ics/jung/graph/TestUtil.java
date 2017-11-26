/*
 * Copyright (C) 2016 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.jung.graph;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Set;

/** Utility methods used in various edu.uci.ics.jung.graph tests. */
final class TestUtil {
  static final String ERROR_ELEMENT_NOT_IN_TREE = "not an element of this tree";
  static final String ERROR_NODE_NOT_IN_TREE =
      "Should not be allowed to pass a node that is not an element of the tree.";
  private static final String NODE_STRING = "Node";

  private TestUtil() {}

  static void assertNodeNotInTreeErrorMessage(Throwable throwable) {
    assertThat(throwable).hasMessageThat().startsWith(NODE_STRING);
    assertThat(throwable).hasMessageThat().contains(ERROR_ELEMENT_NOT_IN_TREE);
  }

  static void assertStronglyEquivalent(CTree<?> treeA, CTree<?> treeB) {
    // Properties not covered by equals()
    assertThat(treeA.allowsSelfLoops()).isEqualTo(treeB.allowsSelfLoops());
    assertThat(treeA.nodeOrder()).isEqualTo(treeB.nodeOrder());

    assertThat(treeA).isEqualTo(treeB);
  }

  /**
   * In some cases our graph implementations return custom sets that define their own size() and
   * contains(). Verify that these sets are consistent with the elements of their iterator.
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
