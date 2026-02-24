package edu.uci.ics.jung.graph

import java.util.Optional

/**
 * Interface defining tree operations common to all tree types.
 *
 * @author Joshua O'Madadhain
 * @param <N> the node type
 */
interface BaseTree<N : Any> {
  /**
   * Returns the root of this tree (the single node in this tree with no predecessor), or {@code
   * Optional#empty()} if the tree is empty.
   */
  fun root(): Optional<N>

  /**
   * Returns the predecessor of {@code node} in this tree, or {@code Optional#empty()} if {@code
   * node} is this tree's root.
   *
   * @throws IllegalArgumentException if {@code node} is not an element of this tree.
   */
  fun predecessor(node: N): Optional<N>

  /**
   * Returns the number of edges that one must traverse from the {@code root} of this tree in order
   * to reach {@code node}.
   *
   * @param node the node whose depth is being requested
   * @throws IllegalArgumentException if {@code node} is not an element of this tree.
   */
  fun depth(node: N): Int

  /**
   * Returns the maximum depth of all nodes in this tree. If the tree is empty, returns {@code
   * Optional#empty()}.
   */
  fun height(): Optional<Int>
}
