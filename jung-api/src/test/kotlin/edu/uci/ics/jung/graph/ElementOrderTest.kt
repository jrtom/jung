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

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.Ordering
import com.google.common.graph.ElementOrder
import com.google.common.graph.ElementOrder.insertion
import com.google.common.graph.ElementOrder.unordered
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Comparator

class ElementOrderTest {
  // Node order tests

  @Test
  fun nodeOrder_none() {
    val tree: MutableCTree<Int> = TreeBuilder.builder().nodeOrder(unordered<Int>()).build()

    assertThat(tree.nodeOrder()).isEqualTo(unordered<Int>())
  }

  @Test
  fun nodeOrder_insertion() {
    val tree: MutableCTree<Int> = TreeBuilder.builder().nodeOrder(insertion<Int>()).build()

    addNodes(tree)

    assertThat(tree.nodeOrder()).isEqualTo(insertion<Int>())
    assertThat(tree.nodes()).containsExactly(3, 1, 4).inOrder()
  }

  // The default ordering is INSERTION unless otherwise specified.
  @Test
  fun nodeOrder_default() {
    val tree: MutableCTree<Int> = TreeBuilder.builder().build()

    addNodes(tree)

    assertThat(tree.nodeOrder()).isEqualTo(insertion<Int>())
    assertThat(tree.nodes()).containsExactly(3, 1, 4).inOrder()
  }

  @Test
  fun nodeOrder_natural() {
    val tree: MutableCTree<Int> =
      TreeBuilder.builder().nodeOrder(ElementOrder.natural<Int>()).build()

    addNodes(tree)

    assertThat(tree.nodeOrder()).isEqualTo(ElementOrder.sorted(Ordering.natural<Int>()))
    assertThat(tree.nodes()).containsExactly(1, 3, 4).inOrder()
  }

  @Test
  fun nodeOrder_sorted() {
    val tree: MutableCTree<Int> =
      TreeBuilder.builder()
        .nodeOrder(ElementOrder.sorted(Ordering.natural<Int>().reverse()))
        .build()

    addNodes(tree)

    assertThat(tree.nodeOrder())
      .isEqualTo(ElementOrder.sorted(Ordering.natural<Int>().reverse()))
    assertThat(tree.nodes()).containsExactly(4, 3, 1).inOrder()
  }

  // Sorting of user-defined classes

  @Test
  fun customComparator() {
    val comparator: Comparator<NonComparableSuperClass> = Comparator.comparing { it.value }

    val tree: MutableCTree<NonComparableSuperClass> =
      TreeBuilder.builder().nodeOrder(ElementOrder.sorted(comparator)).build()

    val node1 = NonComparableSuperClass(1)
    val node3 = NonComparableSuperClass(3)
    val node5 = NonComparableSuperClass(5)
    val node7 = NonComparableSuperClass(7)

    tree.putEdge(node1, node7)
    tree.putEdge(node1, node5)
    tree.putEdge(node5, node3)

    assertThat(tree.nodeOrder().comparator()).isEqualTo(comparator)
    assertThat(tree.nodes()).containsExactly(node1, node3, node5, node7).inOrder()
  }

  @Test
  fun customComparable() {
    val tree: MutableCTree<ComparableSubClass> =
      TreeBuilder.builder().nodeOrder(ElementOrder.natural<ComparableSubClass>()).build()

    val node2 = ComparableSubClass(2)
    val node4 = ComparableSubClass(4)
    val node6 = ComparableSubClass(6)
    val node8 = ComparableSubClass(8)

    tree.putEdge(node2, node8)
    tree.putEdge(node2, node6)
    tree.putEdge(node6, node4)

    assertThat(tree.nodeOrder().comparator()).isEqualTo(Ordering.natural<Comparable<Any>>())
    assertThat(tree.nodes()).containsExactly(node2, node4, node6, node8).inOrder()
  }

  companion object {
    private fun addNodes(tree: MutableCTree<Int>) {
      tree.putEdge(3, 1)
      tree.putEdge(1, 4)
    }
  }

  private open class NonComparableSuperClass(val value: Int) {
    init {
      checkNotNull(value)
    }

    override fun toString(): String = "value=$value"
  }

  private class ComparableSubClass(value: Int) : NonComparableSuperClass(value),
    Comparable<NonComparableSuperClass> {

    override fun compareTo(other: NonComparableSuperClass): Int {
      return this.value.compareTo(other.value)
    }
  }
}
