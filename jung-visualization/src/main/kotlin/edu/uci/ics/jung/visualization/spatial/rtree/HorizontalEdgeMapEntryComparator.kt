package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D

/**
 * A comparator to compare along the x-axis, Map.Entries where the values are Rectangle2D
 * First compare the min x values, then the max x values
 *
 * @author Tom Nelson
 * @param T
 */
class HorizontalEdgeMapEntryComparator<T> : Comparator<Map.Entry<T, Rectangle2D>> {

  fun compare(left: Rectangle2D, right: Rectangle2D): Int {
    if (left.minX == right.minX) {
      if (left.maxX == right.maxX) return 0
      return if (left.maxX < right.maxX) -1 else 1
    } else {
      return if (left.minX < right.minX) -1 else 1
    }
  }

  override fun compare(leftNode: Map.Entry<T, Rectangle2D>, rightNode: Map.Entry<T, Rectangle2D>): Int =
    compare(leftNode.value, rightNode.value)

  fun compare(leftNode: Node<*>, rightNode: Node<*>): Int =
    compare(leftNode.getBounds(), rightNode.getBounds())
}
