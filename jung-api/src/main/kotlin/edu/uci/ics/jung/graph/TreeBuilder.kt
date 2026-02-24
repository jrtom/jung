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
import com.google.common.graph.GraphBuilder
import java.util.Optional

class TreeBuilder<N : Any> {
  var nodeOrder: ElementOrder<N> = ElementOrder.insertion()
    internal set
  var expectedNodeCount: Optional<Int> = Optional.empty()
    internal set
  var root: Optional<N> = Optional.empty()
    internal set

  companion object {
    /** Returns a [TreeBuilder] instance. */
    @JvmStatic
    fun builder(): TreeBuilder<Any> = TreeBuilder()

    /**
     * Returns a [TreeBuilder] initialized with all properties queryable from [tree].
     *
     * The "queryable" properties are those that are exposed through the [CTree] interface,
     * such as [CTree.nodeOrder]. Other properties, such as [expectedNodeCount],
     * are not set in the new builder.
     */
    @JvmStatic
    fun <N : Any> from(tree: CTree<N>): TreeBuilder<N> {
      return TreeBuilder<Any>().nodeOrder(tree.nodeOrder())
    }
  }

  /** Specifies the root of this graph. */
  fun <N1 : N> withRoot(root: N1): TreeBuilder<N1> {
    checkNotNull(root)
    val newBuilder = cast<N1>()
    newBuilder.root = Optional.of(root)
    return newBuilder
  }

  /**
   * Specifies the expected number of nodes in the graph.
   *
   * @throws IllegalArgumentException if [expectedNodeCount] is negative
   */
  fun expectedNodeCount(expectedNodeCount: Int): TreeBuilder<N> {
    checkArgument(expectedNodeCount >= 0)
    this.expectedNodeCount = Optional.of(expectedNodeCount)
    return this
  }

  /**
   * Specifies the order of iteration for the elements of
   * [com.google.common.graph.Graph.nodes].
   */
  fun <N1 : N> nodeOrder(nodeOrder: ElementOrder<N1>): TreeBuilder<N1> {
    val newBuilder = cast<N1>()
    newBuilder.nodeOrder = checkNotNull(nodeOrder)
    return newBuilder
  }

  /** Returns an empty [MutableCTree] with the properties of this [TreeBuilder]. */
  // TODO(jrtom): decide how we're going to handle different implementations.
  // For the graph stuff, we don't really need different implementations, but
  // for trees, maybe we do; at least for binary trees vs. trees with no restrictions on outgoing
  // edges...
  fun <N1 : N> build(): MutableCTree<N1> {
    var graphBuilder = GraphBuilder.directed().allowsSelfLoops(false)
    if (expectedNodeCount.isPresent) {
      graphBuilder = graphBuilder.expectedNodeCount(expectedNodeCount.get())
    }
    val delegate = graphBuilder.nodeOrder(nodeOrder).build<N1>()
    @Suppress("UNCHECKED_CAST")
    val rootCast = root as Optional<N1>
    return DelegateCTree(delegate, rootCast)
  }

  @Suppress("UNCHECKED_CAST")
  private fun <N1 : N> cast(): TreeBuilder<N1> = this as TreeBuilder<N1>
}
