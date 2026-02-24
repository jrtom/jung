package edu.uci.ics.jung.visualization.spatial.rtree

import java.awt.geom.Rectangle2D
import java.util.Optional
import org.slf4j.LoggerFactory

/**
 * holds implementations of several ways to determine the best node to follow for insertion in the
 * R-Tree
 *
 * @param T
 */
abstract class AbstractSplitter<T> {

  companion object {
    private val log = LoggerFactory.getLogger(AbstractSplitter::class.java)
  }

  /**
   * least area enlargement followed by smallest area followed by fewest child nodes
   *
   * @param nodeToSplit the node to split
   * @param incoming the bounds of the new element
   * @return
   */
  protected fun leastEnlargementThenAreaThenKids(
    nodeToSplit: InnerNode<T>,
    incoming: Rectangle2D
  ): Optional<Node<T>> {
    var leastEnlargement = Double.MAX_VALUE
    var winner: Optional<Node<T>> = Optional.empty()
    var winningUnion: Optional<Rectangle2D> = Optional.empty()
    for (kid in nodeToSplit.getChildren()) {
      val kidRectangle = kid.getBounds()
      // how much does the kid enlarge when we enlarge it by incoming?
      val union = kidRectangle.createUnion(incoming)
      val kidArea = Node.area(kidRectangle)
      val unionArea = Node.area(union)
      // this should be difference between the new union with incoming and the original kid area
      val enlargement = unionArea - kidArea
      if (!winner.isPresent) {
        // first one is the winner
        winner = Optional.of(kid)
        winningUnion = Optional.of(kidRectangle)
      }
      val winnerArea = Node.area(winningUnion.get())
      if (enlargement == leastEnlargement) {
        log.trace("we have a tie for enlargement {}", enlargement)
        log.trace("compare the areas {} and {}", unionArea, winnerArea)
        // a tie. see which is the smaller in area
        if (unionArea == winnerArea) {
          log.trace("a tie for unionArea and winnerArea, now compare child counts")
          // a tie in area, see if kid has fewer elements/children
          if (kid.size() < winner.get().size()) {
            log.trace("the new kid has fewer children. choose it")
            // kid is the new winner
            winner = Optional.of(kid)
            winningUnion = Optional.of(union)
            leastEnlargement = enlargement
          }
        } else if (unionArea < winnerArea) {
          log.trace("unionArea is smaller than the previous winner")
          // union area is smaller, choose it
          leastEnlargement = enlargement
          winningUnion = Optional.of(union)
          winner = Optional.of(kid)
        }
      } else if (enlargement < leastEnlargement) {
        // new enlargement is smaller, it wins
        leastEnlargement = enlargement
        winningUnion = Optional.of(union)
        winner = Optional.of(kid)
      }
    }
    if (!winner.isPresent) {
      winner = Optional.of(nodeToSplit)
    }
    return winner
  }

  /**
   * least overlap then least area enlargement followed by smallest area followed by fewest kids
   *
   * @param bounds
   * @return
   */
  protected fun leastOverlapThenEnlargementThenAreaThenKids(
    nodeToSplit: InnerNode<T>,
    bounds: Rectangle2D
  ): Optional<Node<T>> {
    var leastOverlap = Double.MAX_VALUE
    var leastEnlargement = Double.MAX_VALUE
    var winner: Optional<Node<T>> = Optional.empty()
    var winningUnion: Optional<Rectangle2D> = Optional.empty()
    for (kid in nodeToSplit.getChildren()) {
      val kidRectangle = kid.getBounds()

      val overlap = Node.overlap(kid.getBounds(), bounds)
      if (!winner.isPresent) {
        winner = Optional.of(kid)
        leastOverlap = overlap
        winningUnion = Optional.of(kid.getBounds().createUnion(bounds))
        log.trace("won as first")
      } else if (overlap == leastOverlap) {
        log.trace("tie on overlap {} == {}", overlap, leastOverlap)
        // tie for overlap, consider enlargement
        val union = kidRectangle.createUnion(bounds)
        val kidArea = Node.area(kidRectangle)
        val unionArea = Node.area(union)
        // this should be difference between the new union with incoming and the original kid area
        val enlargement = unionArea - kidArea

        val winnerArea = Node.area(winningUnion.get())
        if (enlargement == leastEnlargement) {
          log.trace("tie on enlargement {} == {}", enlargement, leastEnlargement)
          // tie for enlargement, consider area
          log.trace("we have a tie for enlargement {}", enlargement)
          log.trace("compare the areas {} and {}", unionArea, winnerArea)
          // a tie. see which is the smaller in area
          if (unionArea == winnerArea) {
            // tie for area, consider kid size
            log.trace("tie on area {} == {}", unionArea, winnerArea)
            log.trace("a tie for unionArea and winnerArea, now compare child counts")
            // a tie in area, see if kid has fewer elements/children
            if (kid.size() < winner.get().size()) {
              log.trace("the new kid has fewer children. choose it")
              // kid is the new winner
              winner = Optional.of(kid)
              winningUnion = Optional.of(union)
              leastEnlargement = enlargement
              log.trace("won on kid size {} < {}", kid.size(), winner.get().size())
            } else {
              log.trace("kept winner based on kid size {} >= {}", kid.size(), winner.get().size())
            }
          } else if (unionArea < winnerArea) {
            // not tie for area, pick smallest area
            log.trace("won on area {} < {}", unionArea, winnerArea)

            // union area is smaller, choose it
            leastEnlargement = enlargement
            winningUnion = Optional.of(union)
            winner = Optional.of(kid)
          }
        } else if (enlargement < leastEnlargement) {
          // not tie for enlargement, pick smallest enlargement
          // new enlargement is smaller, it wins
          log.trace("won on enlargement {} < {}", enlargement, leastEnlargement)

          leastEnlargement = enlargement
          winningUnion = Optional.of(union)
          winner = Optional.of(kid)
        }
      } else if (overlap < leastOverlap) {
        log.trace("won on overlap {} < {}", overlap, leastOverlap)
        leastOverlap = overlap
        winner = Optional.of(kid)
      }
    }
    if (!winner.isPresent) {
      winner = Optional.of(nodeToSplit)
    }
    return winner
  }
}
