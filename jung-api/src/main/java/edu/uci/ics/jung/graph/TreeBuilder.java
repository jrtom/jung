/*
 * Created on Feb 12, 2017
 *
 * Copyright (c) 2017, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.Optional;

public class TreeBuilder<N> {
  ElementOrder<N> nodeOrder = ElementOrder.insertion();
  Optional<Integer> expectedNodeCount = Optional.empty();
  Optional<N> root = Optional.empty();

  /** Returns a {@link TreeBuilder} instance. */
  public static TreeBuilder<Object> builder() {
    return new TreeBuilder<Object>();
  }

  /**
   * Returns a {@link TreeBuilder} initialized with all properties queryable from {@code tree}.
   *
   * <p>The "queryable" properties are those that are exposed through the {@link CTree} interface,
   * such as {@link CTree#nodeOrder()}. Other properties, such as {@link #expectedNodeCount(int)},
   * are not set in the new builder.
   */
  public static <N> TreeBuilder<N> from(CTree<N> tree) {
    return new TreeBuilder<Object>().nodeOrder(tree.nodeOrder());
  }

  /** Specifies the root of this graph. */
  public <N1 extends N> TreeBuilder<N1> withRoot(N1 root) {
    checkNotNull(root);
    TreeBuilder<N1> newBuilder = cast();
    newBuilder.root = Optional.of(root);
    return newBuilder;
  }

  /**
   * Specifies the expected number of nodes in the graph.
   *
   * @throws IllegalArgumentException if {@code expectedNodeCount} is negative
   */
  public TreeBuilder<N> expectedNodeCount(int expectedNodeCount) {
    checkArgument(expectedNodeCount >= 0);
    this.expectedNodeCount = Optional.of(expectedNodeCount);
    return this;
  }

  /**
   * Specifies the order of iteration for the elements of {@link
   * com.google.common.graph.Graph#nodes()}.
   */
  public <N1 extends N> TreeBuilder<N1> nodeOrder(ElementOrder<N1> nodeOrder) {
    TreeBuilder<N1> newBuilder = cast();
    newBuilder.nodeOrder = checkNotNull(nodeOrder);
    return newBuilder;
  }

  /** Returns an empty {@link MutableCTree} with the properties of this {@link TreeBuilder}. */
  // TODO(jrtom): decide how we're going to handle different implementations.
  // For the graph stuff, we don't really need different implementations, but
  // for trees, maybe we do; at least for binary trees vs. trees with no restrictions on outgoing
  // edges...
  public <N1 extends N> MutableCTree<N1> build() {
    GraphBuilder<Object> graphBuilder = GraphBuilder.directed().allowsSelfLoops(false);
    if (expectedNodeCount.isPresent()) {
      graphBuilder = graphBuilder.expectedNodeCount(expectedNodeCount.get());
    }
    MutableGraph<N1> delegate = graphBuilder.nodeOrder(nodeOrder).build();
    @SuppressWarnings("unchecked")
    Optional<N1> rootCast = (Optional<N1>) root;
    return new DelegateCTree<N1>(delegate, rootCast);
  }

  @SuppressWarnings("unchecked")
  private <N1 extends N> TreeBuilder<N1> cast() {
    return (TreeBuilder<N1>) this;
  }
}
