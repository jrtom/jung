package edu.uci.ics.jung.visualization.spatial.rtree

import com.google.common.collect.Lists
import java.awt.geom.Rectangle2D
import java.util.Optional
import org.slf4j.LoggerFactory

/**
 * splits a Collection of Map.Entries according to R*-Tree semantics
 *
 * @param T
 */
class RStarLeafSplitter<T> : LeafSplitter<T> {

  companion object {
    private val log = LoggerFactory.getLogger(RStarLeafSplitter::class.java)
  }

  private val horizontalEdgeComparator: Comparator<Map.Entry<T, Rectangle2D>> =
    HorizontalEdgeMapEntryComparator()
  private val verticalEdgeComparator: Comparator<Map.Entry<T, Rectangle2D>> =
    VerticalEdgeMapEntryComparator()

  override fun split(
    entries: Collection<Map.Entry<T, Rectangle2D>>,
    newEntry: Map.Entry<T, Rectangle2D>
  ): Pair<LeafNode<T>> = chooseSplitNodes(entries, newEntry)

  private fun chooseSplitNodes(
    entries: Collection<Map.Entry<T, Rectangle2D>>,
    newEntry: Map.Entry<T, Rectangle2D>
  ): Pair<LeafNode<T>> {
    val pair = chooseSplit(entries, newEntry)
    val leafNodeLeft = LeafNode.create(pair.left)
    val leafNodeRight = LeafNode.create(pair.right)
    return Pair.of(leafNodeLeft, leafNodeRight)
  }

  /**
   * R*-Tree method
   */
  private fun chooseSplit(
    entries: Collection<Map.Entry<T, Rectangle2D>>,
    newEntry: Map.Entry<T, Rectangle2D>
  ): Pair<List<Map.Entry<T, Rectangle2D>>> {
    // make 2 lists to sort
    val xAxisList: MutableList<Map.Entry<T, Rectangle2D>> = Lists.newArrayList(entries)
    xAxisList.add(newEntry)
    val yAxisList: MutableList<Map.Entry<T, Rectangle2D>> = Lists.newArrayList(entries)
    yAxisList.add(newEntry)

    // sort them by min value then max value
    xAxisList.sortWith(horizontalEdgeComparator)
    yAxisList.sortWith(verticalEdgeComparator)

    // create containers for the 2 lists to split
    val horizontalGroup: MutableList<Pair<List<Map.Entry<T, Rectangle2D>>>> = Lists.newArrayList()
    val verticalGroup: MutableList<Pair<List<Map.Entry<T, Rectangle2D>>>> = Lists.newArrayList()

    // iterate over the lists to create collections with different midpoints
    for (k in 0 until Node.M - 2 * Node.m + 2) {
      horizontalGroup.add(
        Pair.of(
          xAxisList.subList(0, Node.m - 1 + k),
          xAxisList.subList(Node.m - 1 + k, xAxisList.size)
        )
      )
      verticalGroup.add(
        Pair.of(
          yAxisList.subList(0, Node.m - 1 + k),
          yAxisList.subList(Node.m - 1 + k, yAxisList.size)
        )
      )
    }
    if (log.isTraceEnabled) {
      log.trace("horizontalGroup size is {}", horizontalGroup.size)
      for (pair in horizontalGroup) {
        log.trace("size of pair lists are {} and {}", pair.left.size, pair.right.size)
      }
      log.trace("verticalGroup size is {}", verticalGroup.size)
      for (pair in verticalGroup) {
        log.trace("size of pair lists are {} and {}", pair.left.size, pair.right.size)
      }
    }

    // sum up the margin values from each group
    var sumXMarginValue = 0
    for (pair in horizontalGroup) {
      sumXMarginValue += Node.entryMargin(pair.left, pair.right).toInt()
    }
    var sumYMarginValue = 0
    for (pair in verticalGroup) {
      sumYMarginValue += Node.entryMargin(pair.left, pair.right).toInt()
    }
    // use the group (horizontal or vertical) that has the smallest margin value sum
    return if (sumXMarginValue < sumYMarginValue) {
      // split on x axis
      chooseSplitIndex(horizontalGroup)
    } else {
      // split on y axis
      chooseSplitIndex(verticalGroup)
    }
  }

  /**
   * R*-Tree method
   */
  private fun chooseSplitIndex(
    group: List<Pair<List<Map.Entry<T, Rectangle2D>>>>
  ): Pair<List<Map.Entry<T, Rectangle2D>>> {
    var minOverlap = 0.0
    var minArea = 0.0
    var winner: Optional<Pair<List<Map.Entry<T, Rectangle2D>>>> = Optional.empty()
    // find the Pair of lists with the min overlap or min area
    for (pair in group) {
      val nodeOverlap = Node.entryOverlap(pair.left, pair.right)
      val nodeArea = Node.entryArea(pair.left, pair.right)
      // no winner yet. first node wins by default
      if (!winner.isPresent) {
        minOverlap = nodeOverlap
        minArea = nodeArea
        winner = Optional.of(pair)
      } else if (nodeOverlap == minOverlap) {
        // tie for overlap, try area
        if (nodeArea < minArea) {
          minOverlap = nodeOverlap
          minArea = nodeArea
          winner = Optional.of(pair)
        }
      } else if (nodeOverlap < minOverlap) {
        // winner has the smallest overlap
        minOverlap = nodeOverlap
        minArea = nodeArea
        winner = Optional.of(pair)
      }
    }
    return winner.orElse(null)
  }
}
