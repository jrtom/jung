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

import com.google.common.graph.ElementOrder;
import com.google.common.graph.MutableGraph;
import org.junit.Test;

public final class CTreeEquivalenceTest {
  private static final Integer N1 = 1;
  private static final Integer N2 = 2;
  private static final Integer N3 = 3;

  @Test
  public void equivalent_nodeSetsDiffer() {
    MutableCTree<Object> t1 = TreeBuilder.builder().build();
    t1.addNode(N1);

    MutableCTree<Integer> t2 = TreeBuilder.builder().build();
    t2.addNode(N2);

    assertThat(t1).isNotEqualTo(t2);
  }

  // Node/edge sets are the same; one is constructed with an explicit root, whereas the other isn't.
  @Test
  public void equivalent_explicitlyRootedVsImplicitlyRooted() {
    MutableCTree<Integer> t1 = TreeBuilder.builder().withRoot(N1).build();

    MutableCTree<Integer> t2 = TreeBuilder.builder().build();
    t2.addNode(N1);

    assertThat(t1).isEqualTo(t2);
  }

  // Node/edge sets and node/edge connections are the same, but tree properties differ.
  // In this case the trees are considered equivalent; the property differences are irrelevant.
  @Test
  public void equivalent_propertiesDiffer() {
    MutableCTree<Integer> t1 = TreeBuilder.builder().nodeOrder(ElementOrder.insertion()).build();
    t1.putEdge(N1, N2);

    MutableCTree<Integer> t2 = TreeBuilder.from(t1).nodeOrder(ElementOrder.unordered()).build();
    t2.putEdge(N1, N2);

    assertThat(t1).isEqualTo(t2);
  }

  // Node/edge sets and node/edge connections are the same, but edge order differs.
  // In this case the graphs are considered equivalent; the edge add orderings are irrelevant.
  @Test
  public void equivalent_edgeAddOrdersDiffer() {
    TreeBuilder<Object> builder = TreeBuilder.builder();
    MutableGraph<Integer> t1 = builder.build();
    MutableGraph<Integer> t2 = builder.build();

    // for t1, add 1->2 first, then 1->3
    t1.putEdge(N1, N2);
    t1.putEdge(N1, N3);

    // for t2, add 1->3 first, then 1->2
    t2.putEdge(N1, N3);
    t2.putEdge(N1, N2);

    assertThat(t1).isEqualTo(t2);
  }
}
