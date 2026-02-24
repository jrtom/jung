package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D

/**
 * A comparator to compare along the y-axis, Nodes where the values are Rectangle2D
 * First compare the min y values, then the max y values
 *
 * @author Tom Nelson
 * @param T
 */
class VerticalEdgeNodeComparator<T> : Comparator<Node<T>> {

  fun compare(left: Rectangle2D, right: Rectangle2D): Int {
    if (left.minY == right.minY) {
      if (left.maxY == right.maxY) return 0
      return if (left.maxY < right.maxY) -1 else 1
    } else {
      return if (left.minY < right.minY) -1 else 1
    }
  }

  fun compare(leftNode: Map.Entry<*, Rectangle2D>, rightNode: Map.Entry<*, Rectangle2D>): Int =
    compare(leftNode.value, rightNode.value)

  override fun compare(leftNode: Node<T>, rightNode: Node<T>): Int =
    compare(leftNode.getBounds(), rightNode.getBounds())
}
