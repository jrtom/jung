package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D

interface BoundedMap<T> : MutableMap<T, Rectangle2D> {
  fun getBounds(): Rectangle2D
  fun recalculateBounds()
}
