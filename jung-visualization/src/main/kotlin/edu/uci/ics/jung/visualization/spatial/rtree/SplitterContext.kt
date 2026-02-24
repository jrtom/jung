package edu.uci.ics.jung.visualization.spatial.rtree

/**
 * a container for the functions that support R-Tree and R*-Tree
 *
 * @param T the type of element in the RTree
 * @author Tom Nelson
 */
class SplitterContext<T> private constructor(
  @JvmField val leafSplitter: LeafSplitter<T>,
  @JvmField val splitter: Splitter<T>
) {
  companion object {
    @JvmStatic
    fun <T> of(leafSplitter: LeafSplitter<T>, splitter: Splitter<T>): SplitterContext<T> =
      SplitterContext(leafSplitter, splitter)
  }
}
