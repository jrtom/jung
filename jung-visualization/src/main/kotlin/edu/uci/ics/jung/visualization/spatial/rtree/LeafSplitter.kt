package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D

/**
 * Interface for splitting LeafNodes containing Map.Entries as children
 *
 * @param T the type of the elements
 * @author Tom Nelson
 */
interface LeafSplitter<T> {
  fun split(
    entries: Collection<Map.Entry<T, Rectangle2D>>,
    newEntry: Map.Entry<T, Rectangle2D>
  ): Pair<LeafNode<T>>
}
