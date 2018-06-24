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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.graph.ElementOrder.insertion;
import static com.google.common.graph.ElementOrder.unordered;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Comparator.comparing;

import com.google.common.collect.Ordering;
import com.google.common.graph.ElementOrder;
import java.util.Comparator;
import org.junit.Test;

public final class ElementOrderTest {
  // Node order tests

  @Test
  public void nodeOrder_none() {
    MutableCTree<Integer> tree = TreeBuilder.builder().nodeOrder(unordered()).build();

    assertThat(tree.nodeOrder()).isEqualTo(unordered());
  }

  @Test
  public void nodeOrder_insertion() {
    MutableCTree<Integer> tree = TreeBuilder.builder().nodeOrder(insertion()).build();

    addNodes(tree);

    assertThat(tree.nodeOrder()).isEqualTo(insertion());
    assertThat(tree.nodes()).containsExactly(3, 1, 4).inOrder();
  }

  // The default ordering is INSERTION unless otherwise specified.
  @Test
  public void nodeOrder_default() {
    MutableCTree<Integer> tree = TreeBuilder.builder().build();

    addNodes(tree);

    assertThat(tree.nodeOrder()).isEqualTo(insertion());
    assertThat(tree.nodes()).containsExactly(3, 1, 4).inOrder();
  }

  @Test
  public void nodeOrder_natural() {
    MutableCTree<Integer> tree =
        TreeBuilder.builder().nodeOrder(ElementOrder.<Integer>natural()).build();

    addNodes(tree);

    assertThat(tree.nodeOrder()).isEqualTo(ElementOrder.sorted(Ordering.<Integer>natural()));
    assertThat(tree.nodes()).containsExactly(1, 3, 4).inOrder();
  }

  @Test
  public void nodeOrder_sorted() {
    MutableCTree<Integer> tree =
        TreeBuilder.builder()
            .nodeOrder(ElementOrder.sorted(Ordering.<Integer>natural().reverse()))
            .build();

    addNodes(tree);

    assertThat(tree.nodeOrder())
        .isEqualTo(ElementOrder.sorted(Ordering.<Integer>natural().reverse()));
    assertThat(tree.nodes()).containsExactly(4, 3, 1).inOrder();
  }

  // Sorting of user-defined classes

  @Test
  public void customComparator() {
    Comparator<NonComparableSuperClass> comparator = comparing(left -> left.value);

    MutableCTree<NonComparableSuperClass> tree =
        TreeBuilder.builder().nodeOrder(ElementOrder.sorted(comparator)).build();

    NonComparableSuperClass node1 = new NonComparableSuperClass(1);
    NonComparableSuperClass node3 = new NonComparableSuperClass(3);
    NonComparableSuperClass node5 = new NonComparableSuperClass(5);
    NonComparableSuperClass node7 = new NonComparableSuperClass(7);

    tree.putEdge(node1, node7);
    tree.putEdge(node1, node5);
    tree.putEdge(node5, node3);

    assertThat(tree.nodeOrder().comparator()).isEqualTo(comparator);
    assertThat(tree.nodes()).containsExactly(node1, node3, node5, node7).inOrder();
  }

  @Test
  public void customComparable() {
    MutableCTree<ComparableSubClass> tree =
        TreeBuilder.builder().nodeOrder(ElementOrder.<ComparableSubClass>natural()).build();

    ComparableSubClass node2 = new ComparableSubClass(2);
    ComparableSubClass node4 = new ComparableSubClass(4);
    ComparableSubClass node6 = new ComparableSubClass(6);
    ComparableSubClass node8 = new ComparableSubClass(8);

    tree.putEdge(node2, node8);
    tree.putEdge(node2, node6);
    tree.putEdge(node6, node4);

    assertThat(tree.nodeOrder().comparator()).isEqualTo(Ordering.natural());
    assertThat(tree.nodes()).containsExactly(node2, node4, node6, node8).inOrder();
  }

  private static void addNodes(MutableCTree<Integer> tree) {
    tree.putEdge(3, 1);
    tree.putEdge(1, 4);
  }

  private static class NonComparableSuperClass {
    final Integer value;

    NonComparableSuperClass(Integer value) {
      this.value = checkNotNull(value);
    }

    @Override
    public String toString() {
      return "value=" + value;
    }
  }

  private static class ComparableSubClass extends NonComparableSuperClass
      implements Comparable<NonComparableSuperClass> {

    ComparableSubClass(Integer value) {
      super(value);
    }

    @Override
    public int compareTo(NonComparableSuperClass other) {
      return value.compareTo(other.value);
    }
  }
}
