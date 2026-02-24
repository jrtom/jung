package edu.uci.ics.jung.graph

import com.google.common.graph.Network
import java.util.Optional

/**
 * A subtype of Network<N, E> that is a directed rooted tree.
 *
 * @author Joshua O'Madadhain
 * @param <N> the node type
 * @param <E> the edge type
 */
interface CTreeNetwork<N : Any, E : Any> : BaseTree<N>, Network<N, E> {
  /** Returns {@code true}; trees are always directed (away from the root). */
  override fun isDirected(): Boolean

  /** Returns {@code false}; trees may never have self-loops. */
  override fun allowsSelfLoops(): Boolean

  /** Returns {@code false}; trees may never have parallel edges. */
  override fun allowsParallelEdges(): Boolean

  /**
   * Returns the incoming edge of {@code node} in this tree, or {@code Optional#empty()} if {@code
   * node} is this tree's root.
   *
   * @param node whose incoming edge is being requested
   * @throws IllegalArgumentException if {@code node} is not an element of this tree.
   */
  fun inEdge(node: N): Optional<E>
}
