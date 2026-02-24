package edu.uci.ics.jung.visualization.spatial.rtree

import com.google.common.collect.Lists
import java.awt.geom.Rectangle2D
import java.util.Optional
import org.slf4j.LoggerFactory

class RStarSplitter<T> : AbstractSplitter<T>(), Splitter<T> {

  companion object {
    private val log = LoggerFactory.getLogger(RStarSplitter::class.java)
  }

  private val horizontalEdgeComparator: Comparator<Node<T>> = HorizontalEdgeNodeComparator()
  private val verticalEdgeComparator: Comparator<Node<T>> = VerticalEdgeNodeComparator()

  override fun split(children: List<Node<T>>, newEntry: Node<T>): Pair<InnerNode<T>> =
    chooseSplitNodes(children, newEntry)

  private fun chooseSplitNodes(entries: Collection<Node<T>>, newEntry: Node<T>): Pair<InnerNode<T>> {
    val pair = chooseSplit(entries, newEntry)
    val innerNodeLeft = InnerNode.create(pair.left)
    val innerNodeRight = InnerNode.create(pair.right)
    return Pair.of(innerNodeLeft, innerNodeRight)
  }

  private fun chooseSplit(entries: Collection<Node<T>>, newEntry: Node<T>): Pair<List<Node<T>>> {
    // make 2 lists to sort
    val xAxisList: MutableList<Node<T>> = Lists.newArrayList(entries)
    xAxisList.add(newEntry)
    val yAxisList: MutableList<Node<T>> = Lists.newArrayList(entries)
    yAxisList.add(newEntry)

    xAxisList.sortWith(horizontalEdgeComparator)
    yAxisList.sortWith(verticalEdgeComparator)

    val horizontalGroup: MutableList<Pair<List<Node<T>>>> = Lists.newArrayList()
    val verticalGroup: MutableList<Pair<List<Node<T>>>> = Lists.newArrayList()

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
    var sumXMarginValue = 0
    for (pair in horizontalGroup) {
      sumXMarginValue += Node.nodeMargin(pair.left, pair.right).toInt()
    }
    var sumYMarginValue = 0
    for (pair in verticalGroup) {
      sumYMarginValue += Node.nodeMargin(pair.left, pair.right).toInt()
    }
    return if (sumXMarginValue < sumYMarginValue) {
      chooseSplitIndex(horizontalGroup)
    } else {
      chooseSplitIndex(verticalGroup)
    }
  }

  private fun chooseSplitIndex(group: List<Pair<List<Node<T>>>>): Pair<List<Node<T>>> {
    var minOverlap = 0.0
    var minArea = 0.0
    var winner: Optional<Pair<List<Node<T>>>> = Optional.empty()
    // find the Pair of lists with the min overlap or min area
    for (pair in group) {
      val nodeOverlap = Node.nodeOverlap(pair.left, pair.right)
      val nodeArea = Node.nodeArea(pair.left, pair.right)
      if (!winner.isPresent) {
        minOverlap = nodeOverlap
        minArea = nodeArea
        winner = Optional.of(pair)
      } else if (nodeOverlap == minOverlap) {
        // try area
        if (nodeArea < minArea) {
          minOverlap = nodeOverlap
          minArea = nodeArea
          winner = Optional.of(pair)
        }
      } else if (nodeOverlap < minOverlap) {
        minOverlap = nodeOverlap
        minArea = nodeArea
        winner = Optional.of(pair)
      }
    }
    return winner.orElse(null)
  }

  override fun chooseSubtree(nodeToSplit: InnerNode<T>, element: T, bounds: Rectangle2D): Optional<Node<T>> {
    return if (nodeToSplit.isLeafChildren()) {
      leastOverlapThenEnlargementThenAreaThenKids(nodeToSplit, bounds) // R*-Tree
    } else {
      leastEnlargementThenAreaThenKids(nodeToSplit, bounds)
    }
  }
}
