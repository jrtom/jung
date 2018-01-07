package edu.uci.ics.jung.visualization.spatial.rtree;

import java.util.Optional;

/**
 * contains the parent for Node implementations
 *
 * @param <T>
 */
public abstract class RTreeNode<T> implements Node<T> {

  protected Optional<Node<T>> parent = Optional.empty();

  public void setParent(Node<T> node) {
    parent = Optional.of(node);
  }

  public Optional<Node<T>> getParent() {
    return parent;
  }
}
