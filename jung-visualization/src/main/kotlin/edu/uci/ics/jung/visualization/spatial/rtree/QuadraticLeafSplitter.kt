package edu.uci.ics.jung.visualization.spatial.rtree

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import java.awt.geom.Rectangle2D
import java.util.Optional

/**
 * splits the passed entries using the quadratic method (for R-Tree)
 *
 * @param T
 */
class QuadraticLeafSplitter<T> : LeafSplitter<T> {

  override fun split(
    entries: Collection<Map.Entry<T, Rectangle2D>>,
    newEntry: Map.Entry<T, Rectangle2D>
  ): Pair<LeafNode<T>> = quadraticSplit(entries, newEntry)

  private fun pickNext(
    entries: MutableList<Map.Entry<T, Rectangle2D>>,
    pickedSeeds: Pair<LeafNode<T>>
  ): Optional<Map.Entry<T, Rectangle2D>> {
    var maxDifference = 0.0
    var winner: Optional<Map.Entry<T, Rectangle2D>> = Optional.empty()
    entries.removeAll(pickedSeeds.left.map.entries)
    entries.removeAll(pickedSeeds.right.map.entries)
    // for each entry
    for (entry in entries) {
      // ... that is not already in the leaf node....
      if (!pickedSeeds.left.map.containsKey(entry.key) &&
        !pickedSeeds.right.map.containsKey(entry.key)
      ) {
        // calculate area increase that would happen
        val leftNode = pickedSeeds.left
        val rightNode = pickedSeeds.right
        val leftArea = Node.area(leftNode.getBounds())
        val rightArea = Node.area(rightNode.getBounds())
        val leftUnion = leftNode.getBounds().createUnion(entry.value)
        val rightUnion = rightNode.getBounds().createUnion(entry.value)
        val leftAreaIncrease = Node.area(leftUnion) - leftArea
        val rightAreaIncrease = Node.area(rightUnion) - rightArea
        var difference = leftAreaIncrease - rightAreaIncrease
        // make sure it is positive
        difference = if (difference < 0) -difference else difference
        if (!winner.isPresent) {
          winner = Optional.of(entry)
          maxDifference = difference
        } else if (difference > maxDifference) {
          maxDifference = difference
          winner = Optional.of(entry)
        }
      }
    }

    winner.ifPresent { entries.remove(it) }
    return winner
  }

  /**
   * from the list of entries, return the pair that represent the largest increase in area
   */
  private fun pickSeeds(entryList: List<Map.Entry<T, Rectangle2D>>): Pair<LeafNode<T>> {
    var largestArea = 0.0
    var winningPair: Optional<Pair<Map.Entry<T, Rectangle2D>>> = Optional.empty()
    for (i in entryList.indices) {
      for (j in i + 1 until entryList.size) {
        val entryPair = Pair(entryList[i], entryList[j])
        val union = entryPair.left.value.createUnion(entryPair.right.value)
        val area = Node.area(union) - Node.area(entryPair.left.value) - Node.area(entryPair.right.value)
        if (!winningPair.isPresent) {
          winningPair = Optional.of(entryPair)
          largestArea = area
        } else if (area > largestArea) {
          winningPair = Optional.of(entryPair)
        }
      }
    }
    Preconditions.checkArgument(winningPair.isPresent, "Winning pair not found")
    val leftEntry = winningPair.get().left
    val leftNode = LeafNode.create(leftEntry)
    val rightEntry = winningPair.get().right
    val rightNode = LeafNode.create(rightEntry)

    return Pair.of(leftNode, rightNode)
  }

  private fun distributeEntry(
    entries: MutableList<Map.Entry<T, Rectangle2D>>,
    pickedSeeds: Pair<LeafNode<T>>
  ) {
    val nextOptional = pickNext(entries, pickedSeeds)
    if (nextOptional.isPresent) {
      val next = nextOptional.get()
      // which of the picked seeds should it be added to?
      val leftBounds = pickedSeeds.left.getBounds()
      val rightBounds = pickedSeeds.right.getBounds()
      // which rectangle is enlarged the least?
      val leftArea = Node.area(leftBounds)
      val rightArea = Node.area(rightBounds)
      val leftEnlargement = Node.area(leftBounds.createUnion(next.value)) - leftArea
      val rightEnlargement = Node.area(rightBounds.createUnion(next.value)) - rightArea
      if (leftEnlargement == rightEnlargement) {
        // a tie. consider the smaller area
        if (leftArea == rightArea) {
          // another tie. consider the one with the fewest kids
          val leftKids = pickedSeeds.left.size()
          val rightKids = pickedSeeds.right.size()
          if (leftKids < rightKids) {
            pickedSeeds.left.map.put(next.key, next.value)
          } else {
            pickedSeeds.right.map.put(next.key, next.value)
          }
        } else if (leftArea < rightArea) {
          pickedSeeds.left.map.put(next.key, next.value)
        } else {
          pickedSeeds.right.map.put(next.key, next.value)
        }
      } else if (leftEnlargement < rightEnlargement) {
        pickedSeeds.left.map.put(next.key, next.value)
      } else {
        pickedSeeds.right.map.put(next.key, next.value)
      }
    }
  }

  /**
   * combine the existing map elements with the new one and make a pair of leaf nodes to distribute
   * the entries into
   */
  private fun quadraticSplit(
    entries: Collection<Map.Entry<T, Rectangle2D>>,
    newEntry: Map.Entry<T, Rectangle2D>
  ): Pair<LeafNode<T>> {
    // make a collection of kids from leafNode that also include the new element
    // items will be removed from the entryList as they are distributed
    val entryList = Lists.newArrayList(entries)
    entryList.add(newEntry)
    // get the best pair to split on from the leafNode elements
    val pickedSeeds = pickSeeds(entryList)
    // these currently have no parent set....
    while (entryList.size > 0 &&
      pickedSeeds.left.size() < Node.M - Node.m + 1 &&
      pickedSeeds.right.size() < Node.M - Node.m + 1
    ) {
      distributeEntry(entryList, pickedSeeds)
    }
    if (entryList.size > 0) {
      // take care of entries that were not distributed
      if (pickedSeeds.left.size() >= Node.M - Node.m + 1) {
        // left side too big, give them to the right side
        for (entry in entryList) {
          pickedSeeds.right.map.put(entry.key, entry.value)
        }
      } else {
        // right side too big, give them to the left side
        for (entry in entryList) {
          pickedSeeds.left.map.put(entry.key, entry.value)
        }
      }
    }
    return pickedSeeds
  }
}
