package edu.uci.ics.jung.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Map of elements to Rectangle2D where the union of the child elements is kept up to date with
 * the values in the Map
 *
 * @author Tom Nelson
 */
public class NodeMap<N> extends HashMap<N, Rectangle2D> implements BoundedMap<N>, Bounded {

  private static final Logger log = LoggerFactory.getLogger(NodeMap.class);
  private Rectangle2D bounds;

  public NodeMap() {}

  public NodeMap(int initialCapacity) {
    super(initialCapacity);
  }

  public NodeMap(Map<N, Rectangle2D> map) {
    super(map);
    recalculateBounds();
  }

  public void put(Entry<N, Rectangle2D> entry) {
    put(entry.getKey(), entry.getValue());
  }

  @Override
  public Rectangle2D put(N n, Rectangle2D b) {
    addBoundsFor(b);
    return super.put(n, b);
  }

  @Override
  public Rectangle2D remove(Object o) {
    Rectangle2D removed = super.remove(o);
    recalculateBounds();
    return removed;
  }

  @Override
  public void clear() {
    super.clear();
    bounds = null;
  }

  @Override
  public Rectangle2D getBounds() {
    if (bounds == null) {
      return new Rectangle2D.Double();
    }
    return bounds;
  }

  private void addBoundsFor(Map<? extends N, Rectangle2D> kids) {
    for (Entry<? extends N, Rectangle2D> kid : kids.entrySet()) {
      addBoundsFor(kid.getValue());
    }
  }

  private void addBoundsFor(Rectangle2D r) {
    if (bounds == null) {
      bounds = r;
    } else {
      bounds = bounds.createUnion(r);
    }
  }
  /** iterate over all children and update the bounds Called after removing from the collection */
  public void recalculateBounds() {
    bounds = null;
    for (Rectangle2D r : this.values()) {
      addBoundsFor(r);
    }
  }
}
