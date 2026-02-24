package edu.uci.ics.jung.graph

import com.google.common.graph.MutableNetwork

interface MutableCTreeNetwork<N : Any, E : Any> : MutableNetwork<N, E>, CTreeNetwork<N, E> {
  /**
   * {@inheritDoc}
   *
   * <p>May only be called if one of the following conditions holds:
   *
   * <ul>
   *   <li>the tree is empty (in this case {@code node} will become the tree's root)
   *   <li>{@code node} is already the root of this tree
   * </ul>
   *
   * @throws IllegalArgumentException if neither of the above conditions holds
   */
  override fun addNode(node: N): Boolean

  /**
   * {@inheritDoc}
   *
   * <p>Adds {@code nodeV} as a new successor of {@code nodeU}, and {@code edge} as a new outgoing
   * edge of {@code nodeU}. Requirements:
   *
   * <ul>
   *   <li>{@code nodeU} must be already in the tree, or the tree must be empty
   *   <li>{@code nodeV} must either not be in the tree, or {@code edge} must already connect {@code
   *       nodeU} to {@code nodeV} (in which case this method is a no-op).
   * </ul>
   */
  override fun addEdge(nodeU: N, nodeV: N, edge: E): Boolean

  /**
   * {@inheritDoc}
   *
   * <p>Removes all nodes from the subtree rooted at {@code node}, so that this graph continues to
   * be a rooted tree.
   */
  override fun removeNode(node: N): Boolean

  /**
   * {@inheritDoc}
   *
   * <p>Removes all nodes from the subtree rooted at the source of {@code edge}, so that this graph
   * continues to be a rooted tree.
   */
  override fun removeEdge(edge: E): Boolean
}
