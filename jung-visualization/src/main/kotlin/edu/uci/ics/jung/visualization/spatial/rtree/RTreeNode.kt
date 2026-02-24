package edu.uci.ics.jung.visualization.spatial.rtree

import java.util.Optional

/**
 * contains the _parent for Node implementations
 *
 * @param T
 */
abstract class RTreeNode<T> : Node<T> {

  protected var _parent: Optional<Node<T>> = Optional.empty()

  override fun setParent(node: Node<T>) {
    _parent = Optional.of(node)
  }

  override fun getParent(): Optional<Node<T>> = _parent
}
