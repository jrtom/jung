package edu.uci.ics.jung.visualization.spatial.rtree;

import edu.uci.ics.jung.visualization.spatial.TreeNode;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a leaf node of an R-Tree. Contains a map of elements to their 2d bounds
 *
 * @author Tom Nelson
 */
public class LeafNode<T> extends RTreeNode<T> implements Node<T> {

  private static final Logger log = LoggerFactory.getLogger(LeafNode.class);

  /** a map of child elements for this LeafNode */
  final NodeMap<T> map = new NodeMap<T>();

  /**
   * @param entry the child for the newly created LeafNode
   * @param <T> the type of the element
   * @return a newly created LeafNode that contains the passed entry as its only element
   */
  public static <T> LeafNode<T> create(Map.Entry<T, Rectangle2D> entry) {
    return new LeafNode(entry);
  }

  /**
   * @param element the element child for the newly created LeafNode
   * @param bounds the bounds for the child of the newly created LeafNode
   * @param <T> the type of the element
   * @return the newly created LeafNode with one child element
   */
  public static <T> LeafNode<T> create(T element, Rectangle2D bounds) {
    return new LeafNode(element, bounds);
  }

  /**
   * @param entries the elements for the newly created LeafNode
   * @param <T> the type of the elements for this LeafNode
   * @return the newly created LeadNode with the entries as children
   */
  public static <T> LeafNode<T> create(Collection<Map.Entry<T, Rectangle2D>> entries) {
    return new LeafNode(entries);
  }

  /**
   * @param entries child elements for the new LeafNode
   */
  LeafNode(Collection<Map.Entry<T, Rectangle2D>> entries) {
    for (Map.Entry<T, Rectangle2D> entry : entries) {
      map.put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * @param entry one child element for the new LeafNode
   */
  LeafNode(Map.Entry<T, Rectangle2D> entry) {
    map.put(entry.getKey(), entry.getValue());
  }

  /**
   * @param element one child element for the new LeafNode
   * @param bounds bounds for the child element
   */
  LeafNode(T element, Rectangle2D bounds) {
    map.put(element, bounds);
  }

  /** a LeafNode with no children */
  public static LeafNode EMPTY = new LeafNode();

  /** create an empty LeafNode; */
  private LeafNode() {}

  /**
   * @param splitterContext how to split on overflow (R-Tree or R*-Tree)
   * @param entries add to this LeafNode
   * @return the last node added to
   */
  public Node<T> add(SplitterContext<T> splitterContext, Map.Entry<T, Rectangle2D>... entries) {
    Node<T> top = this;
    for (Map.Entry<T, Rectangle2D> entry : entries) {
      top = add(splitterContext, entry.getKey(), entry.getValue());
    }
    return top;
  }

  /**
   * @param splitterContext how to split on overflow (R-Tree or R*-Tree)
   * @param element the element to add
   * @param bounds the bounding box of the element
   * @return the highest node available after adding (this or parent)
   */
  public Node<T> add(SplitterContext<T> splitterContext, T element, Rectangle2D bounds) {

    if (size() > M) {
      // overflow. Split this node into 2
      Pair<LeafNode<T>> pair =
          splitterContext.leafSplitter.split(
              map.entrySet(), new AbstractMap.SimpleEntry<>(element, bounds));

      if (parent.isPresent()) {
        // if there is a parent node, remove this node from it
        // and add the pair from the split
        InnerNode<T> innerNodeParent = (InnerNode<T>) parent.get();
        innerNodeParent.removeNode(this);
        return innerNodeParent.add(splitterContext, pair.left, pair.right);
      } else {
        // if there is no parent, create one then add the pair from the split
        InnerNode<T> newParent = InnerNode.create(pair.left);
        return newParent.add(splitterContext, pair.right);
      }

    } else {
      // no split required
      // just add this element to the map
      map.put(element, bounds);
      return parent.orElse(this);
    }
  }

  /**
   * always false. children are elements, not LeafNodes
   *
   * @return
   */
  @Override
  public boolean isLeafChildren() {
    return false;
  }

  /**
   * remove passed element from the map if it exists call recalculateBounds to update all parent
   * node bounds after removal of the element
   *
   * @param element the element to remove
   * @return the parent node, recurses to the top
   */
  @Override
  public Node<T> remove(T element) {
    log.trace("LeafNode wants to remove {}", element);
    if (map.containsKey(element)) {
      map.remove(element);
      if (parent.isPresent()) {
        InnerNode<T> parentNode = (InnerNode<T>) parent.get();
        if (map.size() == 0) {
          parentNode.removeNode(this);
        }
        return parentNode.recalculateBounds();
      } else {
        log.trace("no parent? return this {}", this);
        return this;
      }
    } else {
      return null;
    }
  }

  /**
   * called after a removal. climb the tree to the root recalculating the bounds
   *
   * @return this LeafNode
   */
  public Node<T> recalculateBounds() {
    if (parent.isPresent()) {
      return parent.get().recalculateBounds();
    }
    return this;
  }

  public Rectangle2D getBoundsFor(T element) {
    return map.get(element);
  }

  /**
   * add the passed Entry to the map
   *
   * @param splitterContext how to split on overflow
   * @param entry the entry to add to the map
   * @return the parent or this
   */
  public Node<T> add(SplitterContext<T> splitterContext, Map.Entry<T, Rectangle2D> entry) {
    return add(splitterContext, entry.getKey(), entry.getValue());
  }

  /**
   * @param key the element key to search for
   * @return true if the key exists in the map. false otherwise
   */
  public boolean contains(Object key) {
    return map.containsKey(key);
  }

  /**
   * the number of children of this node
   *
   * @return
   */
  public int size() {
    return map.size();
  }

  /**
   * returns the elements from the child map
   *
   * @return
   */
  Collection<T> getKeys() {
    return map.keySet();
  }

  /**
   * @param element the element to find
   * @return the LeafNode that contains the passed element
   */
  @Override
  public LeafNode<T> getContainingLeaf(T element) {
    if (map.containsKey(element)) {
      return this;
    }
    return null;
  }

  /**
   * @param p the point to search for
   * @return a Collection of LeafNodes that would contain the point
   */
  @Override
  public Set<LeafNode<T>> getContainingLeafs(Set<LeafNode<T>> containingLeafs, Point2D p) {
    return getContainingLeafs(containingLeafs, p.getX(), p.getY());
  }

  /**
   * @param x coordinate of the point to search for
   * @param y coordinate of the point to search for
   * @return a Collection of LeafNodes that would contain the passed coordinates
   */
  @Override
  public Set<LeafNode<T>> getContainingLeafs(Set<LeafNode<T>> containingLeafs, double x, double y) {
    if (getBounds().contains(x, y)) {
      containingLeafs.add(this);
      return containingLeafs;
    }
    return Collections.emptySet();
  }

  /**
   * gather the bounds of the node children of this node
   *
   * @param list a list to populate with child bounds rectangles
   * @return the list of child bounds rectangles
   */
  public Collection<Shape> collectGrids(Collection<Shape> list) {
    list.add(getBounds());
    for (Rectangle2D r : map.values()) {
      list.add(r);
    }
    log.trace("in leaf {}, added {} so list size now {}", this.hashCode(), map.size(), list.size());
    return list;
  }

  /**
   * the bounding box of this node is held in the children map
   *
   * @return the bounding box of this node
   */
  @Override
  public Rectangle2D getBounds() {
    return map.getBounds();
  }

  /**
   * @param p a point to search for
   * @return the map entry key whose bounds value contains the passed point
   */
  @Override
  public T getPickedObject(Point2D p) {
    T picked = null;
    for (Map.Entry<T, Rectangle2D> entry : map.entrySet()) {
      if (entry.getValue().contains(p)) {
        picked = entry.getKey();
      }
    }
    return picked;
  }

  /**
   * @param shape a shape to filter the visible elements
   * @return a subset of elements whose bounds intersect with the passed shape
   */
  @Override
  public Set<T> getVisibleElements(Set<T> visibleElements, Shape shape) {
    if (shape.intersects(getBounds())) {
      for (Map.Entry<T, Rectangle2D> entry : map.entrySet()) {
        if (shape.intersects(entry.getValue())) {
          visibleElements.add(entry.getKey());
        }
      }
    }
    log.trace("visibleElements of LeafNode inside {} are {}", shape, visibleElements);
    return visibleElements;
  }

  /**
   * @return the number of children in this node
   */
  public int count() {
    return size();
  }

  // to string methods

  public String asString(String margin) {
    StringBuilder s = new StringBuilder();
    s.append(margin);
    s.append("LeafNode:");
    s.append("parent:");
    s.append(parent.isPresent() ? "yes" : "none");
    s.append(" bounds=");
    s.append(Node.asString(this.getBounds()));
    s.append('\n');
    for (Map.Entry<T, Rectangle2D> entry : this.map.entrySet()) {
      s.append(margin);
      s.append(Node.marginIncrement);
      s.append("entry=");
      s.append(asString(entry));
      s.append('\n');
    }
    return s.toString();
  }

  private static <T> String asString(Map.Entry<T, Rectangle2D> entry) {
    return entry.getKey() + "->" + Node.asString(entry.getValue());
  }

  @Override
  public Collection<? extends TreeNode> getChildren() {
    return Collections.emptySet();
  }

  @Override
  public String toString() {
    return asString("");
  }
}
