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

import com.google.common.graph.ElementOrder
import com.google.common.graph.EndpointPair
import com.google.common.testing.AbstractPackageSanityTests
import com.google.common.truth.Truth.assertWithMessage
import edu.uci.ics.jung.graph.TestUtil.ERROR_ELEMENT_NOT_IN_TREE
import java.util.Objects
import java.util.Optional

class PackageSanityTests : AbstractPackageSanityTests() {

  companion object {
    private val TREE_BUILDER_A: TreeBuilder<*> = TreeBuilder.builder().expectedNodeCount(10)
    private val TREE_BUILDER_B: TreeBuilder<*> =
      TreeBuilder.builder().nodeOrder(ElementOrder.unordered<Any>()).expectedNodeCount(16)

    private val TREE_A: CTree<*> = TreeBuilder.builder().withRoot("A").build<String>()
    private val TREE_B: CTree<*> = TreeBuilder.builder().withRoot("B").build<String>()
  }

  init {
    setDistinctValues(TreeBuilder::class.java, TREE_BUILDER_A, TREE_BUILDER_B)
    setDistinctValues(CTree::class.java, TREE_A, TREE_B)
    setDefault(Optional::class.java, Optional.empty<Any>())
    ignoreClasses { clazz -> Objects.equals(clazz, TestUtil::class.java) }
    setDefault(EndpointPair::class.java, EndpointPair.ordered("A", "B"))
  }

  @Throws(Exception::class)
  override fun testNulls() {
    try {
      super.testNulls()
    } catch (e: AssertionError) {
      assertWithMessage("Method did not throw null pointer OR element not in tree exception.")
        .that(e.cause!!.message)
        .contains(ERROR_ELEMENT_NOT_IN_TREE)
    }
  }

  @Throws(Exception::class)
  override fun testEquals() {
    super.testEquals()
  }
}
