package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D
import java.util.Optional

/**
 * interface for classes that hold semantics for R-Tree and R*-Tree
 *
 * @author Tom Nelson
 * @param T the type of element stored in the RTree
 */
interface Splitter<T> {
  fun split(children: List<Node<T>>, newEntry: Node<T>): Pair<InnerNode<T>>
  fun chooseSubtree(nodeToSplit: InnerNode<T>, element: T, bounds: Rectangle2D): Optional<Node<T>>
}
