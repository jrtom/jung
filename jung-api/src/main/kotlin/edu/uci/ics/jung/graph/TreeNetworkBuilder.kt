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
package edu.uci.ics.jung.graph

import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Preconditions.checkNotNull
import com.google.common.graph.ElementOrder
import com.google.common.graph.NetworkBuilder
import java.util.Optional

class TreeNetworkBuilder<N : Any, E : Any> {
  var nodeOrder: ElementOrder<N> = ElementOrder.insertion()
    internal set
  var edgeOrder: ElementOrder<E> = ElementOrder.insertion()
    internal set
  var expectedNodeCount: Optional<Int> = Optional.empty()
    internal set
  var root: Optional<N> = Optional.empty()
    internal set

  companion object {
    /** Returns a [TreeNetworkBuilder] instance. */
    @JvmStatic
    fun builder(): TreeNetworkBuilder<Any, Any> = TreeNetworkBuilder()

    /**
     * Returns a [TreeNetworkBuilder] initialized with all properties queryable from [tree].
     *
     * The "queryable" properties are those that are exposed through the [CTree] interface,
     * such as [CTree.nodeOrder]. Other properties, such as [expectedNodeCount],
     * are not set in the new builder.
     */
    @JvmStatic
    fun <N : Any, E : Any> from(tree: CTreeNetwork<N, E>): TreeNetworkBuilder<N, E> {
      return TreeNetworkBuilder<N, E>().nodeOrder(tree.nodeOrder())
    }
  }

  /** Specifies the root of this graph. */
  fun <N1 : N> withRoot(root: N1): TreeNetworkBuilder<N1, E> {
    checkNotNull(root)
    val newBuilder = cast<N1, E>()
    newBuilder.root = Optional.of(root)
    return newBuilder
  }

  /**
   * Specifies the expected number of nodes in the graph.
   *
   * @throws IllegalArgumentException if [expectedNodeCount] is negative
   */
  fun expectedNodeCount(expectedNodeCount: Int): TreeNetworkBuilder<N, E> {
    checkArgument(expectedNodeCount >= 0)
    this.expectedNodeCount = Optional.of(expectedNodeCount)
    return this
  }

  /** Specifies the order of iteration for the elements of [com.google.common.graph.Graph.nodes]. */
  fun <N1 : N> nodeOrder(nodeOrder: ElementOrder<N1>): TreeNetworkBuilder<N1, E> {
    val newBuilder = cast<N1, E>()
    newBuilder.nodeOrder = checkNotNull(nodeOrder)
    return newBuilder
  }

  /** Specifies the order of iteration for the elements of [com.google.common.graph.Graph.nodes]. */
  fun <E1 : E> edgeOrder(edgeOrder: ElementOrder<E1>): TreeNetworkBuilder<N, E1> {
    val newBuilder = cast<N, E1>()
    newBuilder.edgeOrder = checkNotNull(edgeOrder)
    return newBuilder
  }

  /**
   * Returns an empty [MutableCTreeNetwork] with the properties of this [TreeNetworkBuilder].
   */
  // TODO(jrtom): decide how we're going to handle different implementations.
  // For the graph stuff, we don't really need different implementations, but
  // for trees, maybe we do; at least for binary trees vs. trees with no restrictions on outgoing
  // edges...
  fun <N1 : N, E1 : E> build(): MutableCTreeNetwork<N1, E1> {
    var graphBuilder = NetworkBuilder.directed().allowsSelfLoops(false).allowsParallelEdges(false)
    if (expectedNodeCount.isPresent) {
      graphBuilder = graphBuilder.expectedNodeCount(expectedNodeCount.get())
    }
    val delegate = graphBuilder.nodeOrder(nodeOrder).edgeOrder(edgeOrder).build<N1, E1>()
    @Suppress("UNCHECKED_CAST")
    val rootCast = root as Optional<N1>
    return DelegateCTreeNetwork(delegate, rootCast)
  }

  @Suppress("UNCHECKED_CAST")
  private fun <N1 : N, E1 : E> cast(): TreeNetworkBuilder<N1, E1> =
    this as TreeNetworkBuilder<N1, E1>
}
