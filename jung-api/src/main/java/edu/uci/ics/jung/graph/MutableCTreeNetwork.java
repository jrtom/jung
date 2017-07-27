package edu.uci.ics.jung.graph;

import com.google.common.graph.MutableNetwork;

public interface MutableCTreeNetwork<N, E> extends MutableNetwork<N, E>, CTreeNetwork<N, E> {
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
  @Override
  boolean addNode(N node);

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
  @Override
  boolean addEdge(N nodeU, N nodeV, E edge);

  /**
   * {@inheritDoc}
   *
   * <p>Removes all nodes from the subtree rooted at {@code node}, so that this graph continues to
   * be a rooted tree.
   */
  @Override
  boolean removeNode(N node);

  /**
   * {@inheritDoc}
   *
   * <p>Removes all nodes from the subtree rooted at the source of {@code edge}, so that this graph
   * continues to be a rooted tree.
   */
  @Override
  boolean removeEdge(E edge);
}
