package edu.uci.ics.jung.graph;

import com.google.common.graph.Network;

/**
 * A subtype of Network<N, E> that is a directed rooted tree.
 *
 * @author Joshua O'Madadhain
 * @param <N> the node type
 * @param <E> the edge type
 */
public interface CTreeNetwork<N, E> extends BaseTree<N>, Network<N, E> {
  /** Returns {@code true}; trees are always directed (away from the root). */
  @Override
  public boolean isDirected();

  /** Returns {@code false}; trees may never have self-loops. */
  @Override
  public boolean allowsSelfLoops();

  /** Returns {@code false}; trees may never have parallel edges. */
  @Override
  public boolean allowsParallelEdges();
}
