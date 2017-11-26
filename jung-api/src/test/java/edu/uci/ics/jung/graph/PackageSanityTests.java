/*
 * Copyright (C) 2015 The Guava Authors
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

import static com.google.common.truth.Truth.assertWithMessage;
import static edu.uci.ics.jung.graph.TestUtil.ERROR_ELEMENT_NOT_IN_TREE;

import com.google.common.graph.ElementOrder;
import com.google.common.testing.AbstractPackageSanityTests;
import junit.framework.AssertionFailedError;

public class PackageSanityTests extends AbstractPackageSanityTests {

  private static final TreeBuilder<?> TREE_BUILDER_A = TreeBuilder.builder().expectedNodeCount(10);
  private static final TreeBuilder<?> TREE_BUILDER_B =
      TreeBuilder.builder().nodeOrder(ElementOrder.unordered()).expectedNodeCount(16);

  private static final CTree TREE_A = TreeBuilder.builder().withRoot("A").build();
  private static final CTree TREE_B = TreeBuilder.builder().withRoot("B").build();

  public PackageSanityTests() {
    setDistinctValues(TreeBuilder.class, TREE_BUILDER_A, TREE_BUILDER_B);
    setDistinctValues(CTree.class, TREE_A, TREE_B);
  }

  @Override
  public void testNulls() throws Exception {
    try {
      super.testNulls();
    } catch (AssertionFailedError e) {
      assertWithMessage("Method did not throw null pointer OR element not in tree exception.")
          .that(e.getCause().getMessage())
          .contains(ERROR_ELEMENT_NOT_IN_TREE);
    }
  }
}
