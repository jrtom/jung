package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D

/**
 * A comparator to compare along the x-axis, Nodes where the values are Rectangle2D
 * First compare the min x values, then the max x values
 *
 * @author Tom Nelson
 * @param T
 */
class HorizontalEdgeNodeComparator<T> : Comparator<Node<T>> {

  fun compare(left: Rectangle2D, right: Rectangle2D): Int {
    if (left.minX == right.minX) {
      if (left.maxX == right.maxX) return 0
      return if (left.maxX < right.maxX) -1 else 1
    } else {
      return if (left.minX < right.minX) -1 else 1
    }
  }

  fun compare(leftNode: Map.Entry<*, Rectangle2D>, rightNode: Map.Entry<*, Rectangle2D>): Int =
    compare(leftNode.value, rightNode.value)

  override fun compare(leftNode: Node<T>, rightNode: Node<T>): Int =
    compare(leftNode.getBounds(), rightNode.getBounds())
}
