package edu.uci.ics.jung.visualization.spatial.rtree

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import java.awt.geom.Rectangle2D
import java.util.Optional

class QuadraticSplitter<T> : AbstractSplitter<T>(), Splitter<T> {

  override fun split(children: List<Node<T>>, newEntry: Node<T>): Pair<InnerNode<T>> =
    quadraticSplit(children, newEntry)

  private fun quadraticSplit(children: List<Node<T>>, newEntry: Node<T>): Pair<InnerNode<T>> {
    // make a collection of kids from leafNode that also include the new element
    // items will be removed from the entryList as they are distributed
    val entryList = Lists.newArrayList(children)
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
          pickedSeeds.right.addNode(entry)
        }
      } else {
        // right side too big, give them to the left side
        for (entry in entryList) {
          pickedSeeds.left.addNode(entry)
        }
      }
    }
    return pickedSeeds
  }

  private fun distributeEntry(entries: MutableList<Node<T>>, pickedSeeds: Pair<InnerNode<T>>) {
    val nextOptional = pickNext(entries, pickedSeeds)
    if (nextOptional.isPresent) {
      val next = nextOptional.get()
      // which of the picked seeds should it be added to?
      val leftBounds = pickedSeeds.left.getBounds()
      val rightBounds = pickedSeeds.right.getBounds()
      // which rectangle is enlarged the least?
      val leftArea = Node.area(leftBounds)
      val rightArea = Node.area(rightBounds)
      val leftEnlargement = Node.area(leftBounds.createUnion(next.getBounds())) - leftArea
      val rightEnlargement = Node.area(rightBounds.createUnion(next.getBounds())) - rightArea
      if (leftEnlargement == rightEnlargement) {
        // a tie. consider the smaller area
        if (leftArea == rightArea) {
          // another tie. consider the one with the fewest kids
          val leftKids = pickedSeeds.left.size()
          val rightKids = pickedSeeds.right.size()
          if (leftKids < rightKids) {
            pickedSeeds.left.addNode(next)
          } else {
            pickedSeeds.right.addNode(next)
          }
        } else if (leftArea < rightArea) {
          pickedSeeds.left.addNode(next)
        } else {
          pickedSeeds.right.addNode(next)
        }
      } else if (leftEnlargement < rightEnlargement) {
        pickedSeeds.left.addNode(next)
      } else {
        pickedSeeds.right.addNode(next)
      }
    }
  }

  private fun pickSeeds(entryList: MutableList<Node<T>>): Pair<InnerNode<T>> {
    var largestArea = 0.0
    var winningPair: Optional<Pair<Node<T>>> = Optional.empty()
    for (i in entryList.indices) {
      for (j in i + 1 until entryList.size) {
        val entryPair = Pair(entryList[i], entryList[j])
        val union = entryPair.left.getBounds().createUnion(entryPair.right.getBounds())
        val area = Node.area(union) - Node.area(entryPair.left.getBounds()) - Node.area(entryPair.right.getBounds())
        if (!winningPair.isPresent) {
          winningPair = Optional.of(entryPair)
          largestArea = area
        } else if (area > largestArea) {
          winningPair = Optional.of(entryPair)
        }
      }
    }
    Preconditions.checkArgument(winningPair.isPresent, "No winning pair returned")
    val leftEntry = winningPair.get().left
    val leftNode = InnerNode.create(leftEntry)
    val rightEntry = winningPair.get().right
    val rightNode = InnerNode.create(rightEntry)
    entryList.remove(leftEntry)
    entryList.remove(rightEntry)
    return Pair(leftNode, rightNode)
  }

  private fun pickNext(entries: MutableList<Node<T>>, pickedSeeds: Pair<InnerNode<T>>): Optional<Node<T>> {
    var maxDifference = 0.0
    var winner: Optional<Node<T>> = Optional.empty()
    entries.removeAll(pickedSeeds.left.getChildren())
    entries.removeAll(pickedSeeds.right.getChildren())
    // for each entry
    for (entry in entries) {
      // ... that is not already in the leaf node....
      if (!pickedSeeds.left.getChildren().contains(entry) &&
        !pickedSeeds.right.getChildren().contains(entry)
      ) {
        // calculate area increase that would happen
        val leftNode = pickedSeeds.left
        val rightNode = pickedSeeds.right
        val leftArea = Node.area(leftNode.getBounds())
        val rightArea = Node.area(rightNode.getBounds())
        val leftUnion = leftNode.getBounds().createUnion(entry.getBounds())
        val rightUnion = rightNode.getBounds().createUnion(entry.getBounds())
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

  override fun chooseSubtree(nodeToSplit: InnerNode<T>, element: T, bounds: Rectangle2D): Optional<Node<T>> =
    leastEnlargementThenAreaThenKids(nodeToSplit, bounds)
}
