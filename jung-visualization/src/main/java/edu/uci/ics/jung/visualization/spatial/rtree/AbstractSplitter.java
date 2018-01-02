package edu.uci.ics.jung.visualization.spatial.rtree;

import static edu.uci.ics.jung.visualization.spatial.rtree.Node.area;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.overlap;

import java.awt.geom.Rectangle2D;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * holds implementations of several ways to determine the best node to follow for insertion in the
 * R-Tree
 *
 * @param <T>
 */
public abstract class AbstractSplitter<T> {

  private static final Logger log = LoggerFactory.getLogger(AbstractSplitter.class);

  /**
   * least area enlargement followed by smallest area followed by fewest child nodes
   *
   * @param nodeToSplit the node to split
   * @param incoming the bounds of the new element
   * @return
   */
  protected Optional<Node<T>> leastEnlargementThenAreaThenKids(
      InnerNode<T> nodeToSplit, Rectangle2D incoming) {
    double leastEnlargement = Double.MAX_VALUE;
    Optional<Node<T>> winner = Optional.empty();
    Optional<Rectangle2D> winningUnion = Optional.empty();
    for (Node<T> kid : nodeToSplit.getChildren()) {
      Rectangle2D kidRectangle = kid.getBounds();
      // how much does the kid enlarge when we enlarge it by incoming?
      Rectangle2D union = kidRectangle.createUnion(incoming);
      double kidArea = area(kidRectangle);
      double unionArea = area(union);
      // this should be difference between the new union with incoming and the original kid area
      double enlargement = unionArea - kidArea;
      if (!winner.isPresent()) {
        // first one is the winner
        winner = Optional.of(kid);
        winningUnion = Optional.of(kidRectangle);
      }
      double winnerArea = area(winningUnion.get());
      if (enlargement == leastEnlargement) {
        log.trace("we have a tie for enlargement {}", enlargement);
        log.trace("compare the areas {} and {}", unionArea, winnerArea);
        // a tie. see which is the smaller in area
        if (unionArea == winnerArea) {
          log.trace("a tie for unionArea and winnerArea, now compare child counts");
          // a tie in area, see if kid has fewer elements/children
          if (kid.size() < winner.get().size()) {
            log.trace("the new kid has fewer children. choose it");
            // kid is the new winner
            winner = Optional.of(kid);
            winningUnion = Optional.of(union);
            leastEnlargement = enlargement;
          }
        } else if (unionArea < winnerArea) {
          log.trace("unionArea is smaller than the previous winner");
          // union area is smaller, choose it
          leastEnlargement = enlargement;
          winningUnion = Optional.of(union);
          winner = Optional.of(kid);
        }
      } else if (enlargement < leastEnlargement) {
        // new enlargement is smaller, it wins
        leastEnlargement = enlargement;
        winningUnion = Optional.of(union);
        winner = Optional.of(kid);
      }
    }
    if (winner == null) {
      winner = Optional.of(nodeToSplit);
    }
    return winner;
  }

  /**
   * least overlap then least area enlargement followed by smallest area followed by fewest kids
   *
   * @param bounds
   * @return
   */
  protected Optional<Node<T>> leastOverlapThenEnlargementThenAreaThenKids(
      InnerNode<T> nodeToSplit, Rectangle2D bounds) {
    double leastOverlap = Double.MAX_VALUE;
    double leastEnlargement = Double.MAX_VALUE;
    Optional<Node<T>> winner = Optional.empty();
    Optional<Rectangle2D> winningUnion = Optional.empty();
    for (Node<T> kid : nodeToSplit.getChildren()) {
      Rectangle2D kidRectangle = kid.getBounds();

      double overlap = overlap(kid.getBounds(), bounds);
      if (!winner.isPresent()) {
        winner = Optional.of(kid);
        leastOverlap = overlap;
        winningUnion = Optional.of(kid.getBounds().createUnion(bounds));
        log.trace("won as first");
      } else if (overlap == leastOverlap) {
        log.trace("tie on overlap {} == {}", overlap, leastOverlap);
        // tie for overlap, consider enlargement
        Rectangle2D union = kidRectangle.createUnion(bounds);
        double kidArea = area(kidRectangle);
        double unionArea = area(union);
        // this should be difference between the new union with incoming and the original kid area
        double enlargement = unionArea - kidArea;

        double winnerArea = area(winningUnion.get());
        if (enlargement == leastEnlargement) {
          log.trace("tie on enlargement {} == {}", enlargement, leastEnlargement);
          // tie for enlargement, consider area
          log.trace("we have a tie for enlargement {}", enlargement);
          log.trace("compare the areas {} and {}", unionArea, winnerArea);
          // a tie. see which is the smaller in area
          if (unionArea == winnerArea) {
            // tie for area, consider kid size
            log.trace("tie on area {} == {}", unionArea, winnerArea);
            log.trace("a tie for unionArea and winnerArea, now compare child counts");
            // a tie in area, see if kid has fewer elements/children
            if (kid.size() < winner.get().size()) {
              log.trace("the new kid has fewer children. choose it");
              // kid is the new winner
              winner = Optional.of(kid);
              winningUnion = Optional.of(union);
              leastEnlargement = enlargement;
              log.trace("won on kid size {} < {}", kid.size(), winner.get().size());
            } else {
              log.trace("kept winner based on kid size {} >= {}", kid.size(), winner.get().size());
            }
          } else if (unionArea < winnerArea) {
            // not tie for area, pick smallest area
            log.trace("won on area {} < {}", unionArea, winnerArea);

            // union area is smaller, choose it
            leastEnlargement = enlargement;
            winningUnion = Optional.of(union);
            winner = Optional.of(kid);
          }
        } else if (enlargement < leastEnlargement) {
          // not tie for enlargement, pick smallest enlargement
          // new enlargement is smaller, it wins
          log.trace("won on enlargement {} < {}", enlargement, leastEnlargement);

          leastEnlargement = enlargement;
          winningUnion = Optional.of(union);
          winner = Optional.of(kid);
        }

      } else if (overlap < leastOverlap) {
        log.trace("won on overlap {} < {}", overlap, leastOverlap);
        leastOverlap = overlap;
        winner = Optional.of(kid);
      }
    }
    if (!winner.isPresent()) {
      winner = Optional.of(nodeToSplit);
    }
    return winner;
  }
}
