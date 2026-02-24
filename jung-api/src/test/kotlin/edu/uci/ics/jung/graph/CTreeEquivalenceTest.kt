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
import com.google.common.graph.MutableGraph
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CTreeEquivalenceTest {
  companion object {
    private const val N1 = 1
    private const val N2 = 2
    private const val N3 = 3
  }

  @Test
  fun equivalent_nodeSetsDiffer() {
    val t1: MutableCTree<Any> = TreeBuilder.builder().build()
    t1.addNode(N1)

    val t2: MutableCTree<Int> = TreeBuilder.builder().build()
    t2.addNode(N2)

    assertThat(t1).isNotEqualTo(t2)
  }

  // Node/edge sets are the same; one is constructed with an explicit root, whereas the other isn't.
  @Test
  fun equivalent_explicitlyRootedVsImplicitlyRooted() {
    val t1: MutableCTree<Int> = TreeBuilder.builder().withRoot(N1).build()

    val t2: MutableCTree<Int> = TreeBuilder.builder().build()
    t2.addNode(N1)

    assertThat(t1).isEqualTo(t2)
  }

  // Node/edge sets and node/edge connections are the same, but tree properties differ.
  // In this case the trees are considered equivalent; the property differences are irrelevant.
  @Test
  fun equivalent_propertiesDiffer() {
    val t1: MutableCTree<Int> = TreeBuilder.builder().nodeOrder(ElementOrder.insertion()).build()
    t1.putEdge(N1, N2)

    val t2: MutableCTree<Int> = TreeBuilder.from(t1).nodeOrder(ElementOrder.unordered()).build()
    t2.putEdge(N1, N2)

    assertThat(t1).isEqualTo(t2)
  }

  // Node/edge sets and node/edge connections are the same, but edge order differs.
  // In this case the graphs are considered equivalent; the edge add orderings are irrelevant.
  @Test
  fun equivalent_edgeAddOrdersDiffer() {
    val builder: TreeBuilder<Any> = TreeBuilder.builder()
    val t1: MutableGraph<Int> = builder.build()
    val t2: MutableGraph<Int> = builder.build()

    // for t1, add 1->2 first, then 1->3
    t1.putEdge(N1, N2)
    t1.putEdge(N1, N3)

    // for t2, add 1->3 first, then 1->2
    t2.putEdge(N1, N3)
    t2.putEdge(N1, N2)

    assertThat(t1).isEqualTo(t2)
  }
}
