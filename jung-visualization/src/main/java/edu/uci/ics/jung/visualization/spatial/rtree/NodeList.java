package edu.uci.ics.jung.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A list of elements that are Bounded by a Rectangle2D The list is also Bounded by the combined
 * union of its elements
 *
 * @author Tom Nelson
 */
public class NodeList<B extends Bounded> extends ArrayList<B> implements BoundedList<B>, Bounded {

  private static final Logger log = LoggerFactory.getLogger(NodeList.class);
  private Rectangle2D bounds;

  public NodeList() {}

  public NodeList(int initialCapacity) {
    super(initialCapacity);
  }

  public NodeList(Collection<B> list) {
    for (B node : list) {
      add(node);
    }
  }

  public NodeList(B... nodes) {
    for (B node : nodes) {
      add(node);
    }
  }

  @Override
  public boolean add(B n) {
    if (n instanceof Node) {
      Node node = (Node) n;
      if (node.getParent() == null || !node.getParent().isPresent()) {
        log.error("adding a node {} with unset parent {}", node, node.getParent());
      }
    } else {
      log.error("adding something that is not a Node: {}", n);
    }
    addBoundsFor(n);
    return super.add(n);
  }

  @Override
  public void add(int index, B element) {
    addBoundsFor(element);
    super.add(index, element);
  }

  @Override
  public B remove(int index) {
    B removed = super.remove(index);
    recalculateBounds();
    return removed;
  }

  @Override
  public boolean remove(Object o) {
    boolean removed = super.remove(o);
    recalculateBounds();
    return removed;
  }

  @Override
  public void clear() {
    super.clear();
    bounds = null;
  }

  @Override
  public boolean addAll(Collection<? extends B> c) {
    addBoundsFor(c);
    return super.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends B> c) {
    return super.addAll(index, c);
  }

  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    super.removeRange(fromIndex, toIndex);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return super.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return super.retainAll(c);
  }

  @Override
  public Rectangle2D getBounds() {
    return bounds;
  }

  private void addBoundsFor(Collection<? extends B> kids) {
    for (B kid : kids) {
      addBoundsFor(kid);
    }
  }

  private void addBoundsFor(B kid) {
    if (bounds == null) {
      bounds = kid.getBounds();
    } else {
      bounds = bounds.createUnion(kid.getBounds());
    }
  }
  /** iterate over all children and update the bounds Called after removing from the collection */
  public void recalculateBounds() {
    bounds = null;
    for (B n : this) {
      addBoundsFor(n);
    }
  }
}
