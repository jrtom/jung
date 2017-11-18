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

import com.google.common.graph.MutableGraph;

/**
 * A subtype of CTree<N> that permits
 *
 * @author joshua
 * @param <N>
 */
public interface MutableCTree<N> extends CTree<N>, MutableGraph<N> {

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
   * <p>Adds {@code nodeV} as a new successor of {@code nodeU}. Requirements:
   *
   * <ul>
   *   <li>{@code nodeU} must be already in the tree, or the tree must be empty
   *   <li>{@code nodeV} must either not be in the tree, or there must be an existing edge
   *       connecting {@code nodeU} to {@code nodeV} (in which case this method is a no-op).
   * </ul>
   */
  @Override
  boolean putEdge(N nodeU, N nodeV);

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
   * <p>Removes all nodes from the subtree rooted at {@code nodeV}, so that this graph continues to
   * be a rooted tree.
   */
  @Override
  boolean removeEdge(N nodeU, N nodeV);
}
