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
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.Optional;

public class TreeNetworkBuilder<N, E> {
  ElementOrder<N> nodeOrder = ElementOrder.insertion();
  ElementOrder<E> edgeOrder = ElementOrder.insertion();
  Optional<Integer> expectedNodeCount = Optional.empty();
  Optional<N> root = Optional.empty();

  /** Returns a {@link TreeNetworkBuilder} instance. */
  public static TreeNetworkBuilder<Object, Object> builder() {
    return new TreeNetworkBuilder<Object, Object>();
  }

  /**
   * Returns a {@link TreeNetworkBuilder} initialized with all properties queryable from {@code
   * tree}.
   *
   * <p>The "queryable" properties are those that are exposed through the {@link CTree} interface,
   * such as {@link CTree#nodeOrder()}. Other properties, such as {@link #expectedNodeCount(int)},
   * are not set in the new builder.
   */
  public static <N, E> TreeNetworkBuilder<N, E> from(CTreeNetwork<N, E> tree) {
    return new TreeNetworkBuilder<N, E>().nodeOrder(tree.nodeOrder());
  }

  /** Specifies the root of this graph. */
  public <N1 extends N> TreeNetworkBuilder<N1, E> withRoot(N1 root) {
    checkNotNull(root);
    TreeNetworkBuilder<N1, E> newBuilder = cast();
    newBuilder.root = Optional.of(root);
    return newBuilder;
  }

  /**
   * Specifies the expected number of nodes in the graph.
   *
   * @throws IllegalArgumentException if {@code expectedNodeCount} is negative
   */
  public TreeNetworkBuilder<N, E> expectedNodeCount(int expectedNodeCount) {
    checkArgument(expectedNodeCount >= 0);
    this.expectedNodeCount = Optional.of(expectedNodeCount);
    return this;
  }

  /** Specifies the order of iteration for the elements of {@link Graph#nodes()}. */
  public <N1 extends N> TreeNetworkBuilder<N1, E> nodeOrder(ElementOrder<N1> nodeOrder) {
    TreeNetworkBuilder<N1, E> newBuilder = cast();
    newBuilder.nodeOrder = checkNotNull(nodeOrder);
    return newBuilder;
  }

  /** Specifies the order of iteration for the elements of {@link Graph#nodes()}. */
  public <E1 extends E> TreeNetworkBuilder<N, E1> edgeOrder(ElementOrder<E1> edgeOrder) {
    TreeNetworkBuilder<N, E1> newBuilder = cast();
    newBuilder.edgeOrder = checkNotNull(edgeOrder);
    return newBuilder;
  }

  /**
   * Returns an empty {@link MutableCTree} with the properties of this {@link TreeNetworkBuilder}.
   */
  // TODO(jrtom): decide how we're going to handle different implementations.
  // For the graph stuff, we don't really need different implementations, but
  // for trees, maybe we do; at least for binary trees vs. trees with no restrictions on outgoing
  // edges...
  public <N1 extends N, E1 extends E> MutableCTreeNetwork<N1, E1> build() {
    NetworkBuilder<Object, Object> graphBuilder =
        NetworkBuilder.directed().allowsSelfLoops(false).allowsParallelEdges(false);
    if (expectedNodeCount.isPresent()) {
      graphBuilder = graphBuilder.expectedNodeCount(expectedNodeCount.get());
    }
    MutableNetwork<N1, E1> delegate =
        graphBuilder.nodeOrder(nodeOrder).edgeOrder(edgeOrder).build();
    @SuppressWarnings("unchecked")
    Optional<N1> rootCast = (Optional<N1>) root;
    return new DelegateCTreeNetwork<N1, E1>(delegate, rootCast);
  }

  @SuppressWarnings("unchecked")
  private <N1 extends N, E1 extends E> TreeNetworkBuilder<N1, E1> cast() {
    return (TreeNetworkBuilder<N1, E1>) this;
  }
}
