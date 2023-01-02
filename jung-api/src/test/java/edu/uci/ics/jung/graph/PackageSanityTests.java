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

import static com.google.common.truth.Truth.assertWithMessage;
import static edu.uci.ics.jung.graph.TestUtil.ERROR_ELEMENT_NOT_IN_TREE;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.testing.AbstractPackageSanityTests;
import java.util.Objects;
import java.util.Optional;

public class PackageSanityTests extends AbstractPackageSanityTests {

  private static final TreeBuilder<?> TREE_BUILDER_A = TreeBuilder.builder().expectedNodeCount(10);
  private static final TreeBuilder<?> TREE_BUILDER_B =
      TreeBuilder.builder().nodeOrder(ElementOrder.unordered()).expectedNodeCount(16);

  private static final CTree TREE_A = TreeBuilder.builder().withRoot("A").build();
  private static final CTree TREE_B = TreeBuilder.builder().withRoot("B").build();

  public PackageSanityTests() {
    setDistinctValues(TreeBuilder.class, TREE_BUILDER_A, TREE_BUILDER_B);
    setDistinctValues(CTree.class, TREE_A, TREE_B);
    setDefault(Optional.class, Optional.empty());
    ignoreClasses(clazz -> Objects.equals(clazz, TestUtil.class));
    setDefault(EndpointPair.class, EndpointPair.ordered("A", "B"));
  }

  @Override
  public void testNulls() throws Exception {
    try {
      super.testNulls();
    } catch (AssertionError e) {
      assertWithMessage("Method did not throw null pointer OR element not in tree exception.")
          .that(e.getCause().getMessage())
          .contains(ERROR_ELEMENT_NOT_IN_TREE);
    }
  }

  @Override
  public void testEquals() throws Exception {
    super.testEquals();
  }
}
