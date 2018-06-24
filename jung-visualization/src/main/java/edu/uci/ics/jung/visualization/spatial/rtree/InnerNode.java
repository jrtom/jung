package edu.uci.ics.jung.visualization.spatial.rtree;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a non-leaf node of the R-Tree. Contains a list of non leaf or leaf node children
 *
 * @author Tom Nelson
 */
public class InnerNode<T> extends RTreeNode<T> implements Node<T> {

  private static final Logger log = LoggerFactory.getLogger(InnerNode.class);

  private Optional<Rectangle2D> bounds = Optional.empty();

  /** child nodes of this InnerNode */
  private List<Node<T>> children;

  /** true if the child nodes are LeafNodes. falise otherwise */
  private final boolean leafChildren;

  /**
   * create a new InnerNode with one child
   *
   * @param node the first child for the created Node
   * @param <T> the type of the node and children
   * @return the newly created InnerNode
   */
  public static <T> InnerNode<T> create(Node<T> node) {
    return new InnerNode(node);
  }

  /**
   * create a new InnerNode with one child
   *
   * @param node the first child of the created node
   * @param <T> the type of the Node
   * @return the newly created InnerNode
   */
  public static <T> InnerNode<T> create(InnerNode<T> node) {
    return new InnerNode(node);
  }

  /**
   * create a new InnerNode with the passed nodes as children
   *
   * @param nodes the children for the new InnerNode
   * @param <T> the type of the Node
   * @return the newly created InnerNode
   */
  public static <T> InnerNode<T> create(Collection<Node<T>> nodes) {
    return new InnerNode(nodes);
  }

  /**
   * create an InnerNode with the passed Node as the first child
   *
   * @param node the first child for the InnerNode
   */
  InnerNode(Node<T> node) {
    node.setParent(this);
    updateBounds(node.getBounds());
    leafChildren = node instanceof LeafNode;
    children = Lists.newArrayList(node);
  }

  /**
   * create an InnerNOde with the passed nodes as children
   *
   * @param nodes the children for the new InnerNode
   */
  InnerNode(Collection<Node<T>> nodes) {
    children = Lists.newArrayList();
    Node<T> sample = null;
    for (Node<T> node : nodes) {
      sample = node;
      node.setParent(this);
      updateBounds(node.getBounds());
      children.add(node);
    }
    leafChildren = sample instanceof LeafNode; // ugh
  }

  /**
   * true if the children are LeafNodes
   *
   * @return
   */
  @Override
  public boolean isLeafChildren() {
    return leafChildren;
  }

  /**
   * return the ith child node
   *
   * @param i the index of the child to return
   * @return the ith child
   */
  public Node<T> get(int i) {
    return children.get(i);
  }

  /** @return an immutable collection of the child nodes */
  public List<Node<T>> getChildren() {
    return Collections.unmodifiableList(children);
  }

  /**
   * @return the bounding box of this InnerNode. A zero sized Rectangle is returned if this
   *     InnerNode is empty
   */
  @Override
  public Rectangle2D getBounds() {
    return bounds.orElse(new Rectangle2D.Double());
  }

  /**
   * recompute the bounding box for this InnerNode, then the recompute for parent node Climbs the
   * tree to the root as it recalcultes. This i required when a leaf node is removed.
   *
   * @return
   */
  @Override
  public Node<T> recalculateBounds() {
    bounds = Optional.empty();
    int size = children.size();
    for (int i = 0; i < size; i++) {
      updateBounds(children.get(i).getBounds());
    }
    if (parent.isPresent()) {
      return parent.get().recalculateBounds();
    }
    return this;
  }

  /**
   * @param p the point to search
   * @return the element in the Leaf node that is contained by p
   */
  @Override
  public T getPickedObject(Point2D p) {
    T picked = null;
    if (getBounds().contains(p)) {
      int size = children.size();
      for (int i = 0; i < size; i++) {
        return children.get(i).getPickedObject(p);
      }
    }
    return picked;
  }

  /** @return the number of child nodes */
  @Override
  public int size() {
    return children.size();
  }

  private Node<T> findElement(T o) {
    Node<T> found = null;
    int size = children.size();
    for (int i = 0; i < size; i++) {
      Node<T> kid = children.get(i);
      if (kid instanceof LeafNode) {
        return kid;
      } else {
        found = ((InnerNode<T>) kid).findElement(o);
      }
    }
    return found;
  }

  /**
   * @param element the element to look for
   * @return the LeafNode that contains the element
   */
  @Override
  public LeafNode<T> getContainingLeaf(T element) {
    LeafNode<T> containingLeaf = null;
    int size = children.size();
    for (int i = 0; i < size; i++) {
      containingLeaf = children.get(i).getContainingLeaf(element);
      if (containingLeaf != null) {
        break;
      }
    }
    return containingLeaf;
  }

  /**
   * @param element the element to look for
   * @return the LeafNode that contains the element
   */
  LeafNode<T> getContainingLeaf(T element, Rectangle2D bounds) {
    LeafNode<T> containingLeaf = null;
    int size = children.size();
    for (int i = 0; i < size; i++) {
      Node<T> node = children.get(i);
      if (node.getBounds().intersects(bounds)) {
        containingLeaf = node.getContainingLeaf(element);
        if (containingLeaf != null) {
          break;
        }
      }
    }
    return containingLeaf;
  }

  /**
   * @param p the point to look for
   * @return Collection of the LeafNodes that would contain the passed point
   */
  @Override
  public Set<LeafNode<T>> getContainingLeafs(Set<LeafNode<T>> containingLeafs, Point2D p) {
    return getContainingLeafs(containingLeafs, p.getX(), p.getY());
  }

  /**
   * @param x coordinate of a point to look for
   * @param y coordinate of a point to look for
   * @return Collection of the LeafNodes that would contain the passed coordinates
   */
  @Override
  public Set<LeafNode<T>> getContainingLeafs(Set<LeafNode<T>> containingLeafs, double x, double y) {
    if (getBounds().contains(x, y)) {
      int size = children.size();
      for (int i = 0; i < size; i++) {
        Node<T> node = children.get(i);
        node.getContainingLeafs(containingLeafs, x, y);
      }
    }
    return containingLeafs;
  }

  /**
   * gather the RTree Node rectangles into a Collection
   *
   * @param list
   * @return
   */
  public Collection<Shape> collectGrids(Collection<Shape> list) {
    list.add(getBounds());
    int size = children.size();
    for (int i = 0; i < size; i++) {
      children.get(i).collectGrids(list);
    }
    log.trace(
        "in nonleaf {}, added {} so list size now {}",
        this.hashCode(),
        children.size(),
        list.size());
    return list;
  }

  /**
   * add Nodes directly to the children list
   *
   * @param collection
   */
  private void add(Collection<? extends Node<T>> collection) {
    children.addAll(collection);
  }

  private void updateBounds(Rectangle2D r) {
    if (bounds.isPresent()) {
      bounds = Optional.of(bounds.get().createUnion(r));
    } else {
      bounds = Optional.of(r);
    }
    Rectangle2D b = bounds.get();
  }

  /**
   * @param splitterContext rules for splitting nodes
   * @param element the element to add
   * @param bounds the bounds of the element to add
   * @return the returned node or its parent
   */
  @Override
  public Node<T> add(SplitterContext<T> splitterContext, T element, Rectangle2D bounds) {
    // update bounds with the new element's bounds
    updateBounds(bounds);
    Optional<Node<T>> pathToFollow = splitterContext.splitter.chooseSubtree(this, element, bounds);
    if (pathToFollow.isPresent()) {
      Node<T> node = pathToFollow.get().add(splitterContext, element, bounds);
      return node.getParent().orElse(node);
    }
    return null;
  }

  /**
   * remove the passed element. Find the LeafNode that contains the element, remove the element from
   * the LeafNode map
   *
   * @param element the element to remove
   * @return the parent node or this node
   */
  @Override
  public Node<T> remove(T element) {
    LeafNode<T> containingLeaf = getContainingLeaf(element);
    if (containingLeaf == null) {
      log.warn("{} is not in the tree! ", element);
      return this;
    }
    return containingLeaf.remove(element);
  }

  /**
   * diectly add a child node to this node.
   *
   * @param node
   */
  void addNode(Node<T> node) {
    Preconditions.checkArgument(node != this, "Attempt to add self as child");
    Preconditions.checkArgument(!children.contains(node), "Attempt to add duplicate child");
    node.setParent(this);
    updateBounds(node.getBounds());
    children.add(node);
  }

  /**
   * directly remove a shild node from this node
   *
   * @param node
   */
  void removeNode(Node<T> node) {
    children.remove(node);
  }

  InnerNode<T> add(SplitterContext<T> splitterContext, Node<T>... nodes) {
    InnerNode<T> top = this;
    for (Node<T> node : nodes) {
      top = add(splitterContext, node);
    }
    if (top.getParent().isPresent()) {
      return (InnerNode<T>) top.getParent().get();
    }
    return top;
  }
  /**
   * adding either a LeafNode or an InnerNode
   *
   * @param node
   * @return the parent, if exists, or this
   */
  private InnerNode<T> add(SplitterContext<T> splitterContext, Node<T> node) {
    Preconditions.checkArgument(node != this, "Attempt to add self as child");

    updateBounds(node.getBounds());

    if (size() > M) {
      log.trace("splitting InnerNode {}", this);
      Pair<InnerNode<T>> pair = splitterContext.splitter.split(children, node);

      if (parent.isPresent()) {
        InnerNode<T> innerNodeParent = (InnerNode<T>) parent.get();
        // sanity check
        Preconditions.checkArgument(
            this != pair.left && this != pair.right,
            "Pair left {} or right {} the same as this {}",
            pair.left,
            pair.right,
            this);
        innerNodeParent.removeNode(this);
        return innerNodeParent.add(splitterContext, pair.left, pair.right);
      } else {
        // create a new parent
        InnerNode<T> innerNodeParent = InnerNode.create(pair.left);
        return innerNodeParent.add(splitterContext, pair.right);
      }

    } else {
      // no split required
      addNode(node);
      return (InnerNode<T>) parent.orElse(this);
    }
  }

  /**
   * @param shape the shape to filter the visible elements
   * @return a collection of all elements that intersect with the passed shape
   */
  @Override
  public Set<T> getVisibleElements(Set<T> visibleElements, Shape shape) {
    if (shape.intersects(getBounds())) {
      int size = children.size();
      for (int i = 0; i < size; i++) {
        children.get(i).getVisibleElements(visibleElements, shape);
      }
    }
    log.trace("visibleElements of InnerNode inside {} are {}", shape, visibleElements);
    return visibleElements;
  }

  /**
   * descend into the tree and count all children
   *
   * @return
   */
  public int count() {
    int count = 0;
    int size = children.size();
    for (int i = 0; i < size; i++) {
      count += children.get(i).count();
    }
    return count;
  }
  // to string methods:

  private String asString() {
    return asString("");
  }

  @Override
  public String toString() {
    return this.asString();
  }

  public String asString(String margin) {
    StringBuilder s = new StringBuilder();
    s.append(margin);
    s.append("InnerNode:parent:").append(parent.isPresent() ? "yes" : "none");
    s.append(" bounds=");
    s.append(Node.asString(this.getBounds()));
    s.append('\n');
    for (Node<T> child : this.children) {
      s.append(child.asString(margin + marginIncrement));
    }
    return s.toString();
  }
}
