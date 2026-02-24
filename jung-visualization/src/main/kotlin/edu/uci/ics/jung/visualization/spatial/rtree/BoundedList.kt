package edu.uci.ics.jung.visualization.spatial.rtree

/**
 * interface for a list of Bounded items. The bounding box of the list is the union of the list
 * contents bounding boxes
 *
 * @author Tom Nelson
 * @param B the type of item stored in the list
 */
interface BoundedList<B : Bounded> : Bounded, MutableList<B> {
  /** recompute the bounding box for the list */
  fun recalculateBounds()
}
