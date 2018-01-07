package edu.uci.ics.jung.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;

public interface BoundedMap<T> extends java.util.Map<T, Rectangle2D> {

  //    void put(T n);
  //
  //    void put(LeafEntry<T> entry);

  Rectangle2D getBounds();

  void recalculateBounds();
}
