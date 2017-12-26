package edu.uci.ics.jung.visualization.spatial.rtree;

import static edu.uci.ics.jung.visualization.spatial.rtree.Node.M;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.area;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.m;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * splits the passed entries using the quadratic method (for R-Tree)
 *
 * @param <T>
 */
public class QuadraticLeafSplitter<T> implements LeafSplitter<T> {

  @Override
  public Pair<LeafNode<T>> split(
      Collection<Map.Entry<T, Rectangle2D>> entries, Map.Entry<T, Rectangle2D> newEntry) {
    return quadraticSplit(entries, newEntry);
  }

  private Optional<Map.Entry<T, Rectangle2D>> pickNext(
      List<Map.Entry<T, Rectangle2D>> entries, Pair<LeafNode<T>> pickedSeeds) {
    double maxDifference = 0;
    Optional<Map.Entry<T, Rectangle2D>> winner = Optional.empty();
    entries.removeAll(pickedSeeds.left.map.entrySet());
    entries.removeAll(pickedSeeds.right.map.entrySet());
    // for each entry
    for (Map.Entry<T, Rectangle2D> entry : entries) {
      // ... that is not already in the leaf node....
      if (!pickedSeeds.left.map.containsKey(entry.getKey())
          && !pickedSeeds.right.map.containsKey(entry.getKey())) {
        // calculate area increase that would happen
        LeafNode<T> leftNode = pickedSeeds.left;
        LeafNode<T> rightNode = pickedSeeds.right;
        double leftArea = area(leftNode.getBounds());
        double rightArea = area(rightNode.getBounds());
        Rectangle2D leftUnion = leftNode.getBounds().createUnion(entry.getValue());
        Rectangle2D rightUnion = rightNode.getBounds().createUnion(entry.getValue());
        double leftAreaIncrease = area(leftUnion) - leftArea;
        double rightAreaIncrease = area(rightUnion) - rightArea;
        double difference = leftAreaIncrease - rightAreaIncrease;
        // make sure it is positive
        difference = difference < 0 ? -difference : difference;
        if (!winner.isPresent()) {
          winner = Optional.of(entry);
          maxDifference = difference;
        } else if (difference > maxDifference) {
          maxDifference = difference;
          winner = Optional.of(entry);
        }
      }
    }

    winner.ifPresent(entries::remove);
    return winner;
  }

  /**
   * from the list of entries, return the pair that represent the largest increase in area
   *
   * @param entryList
   * @return
   */
  private Pair<LeafNode<T>> pickSeeds(List<Map.Entry<T, Rectangle2D>> entryList) {
    double largestArea = 0;
    Optional<Pair<Map.Entry<T, Rectangle2D>>> winningPair = Optional.empty();
    for (int i = 0; i < entryList.size(); i++) {
      for (int j = i + 1; j < entryList.size(); j++) {
        Pair<Map.Entry<T, Rectangle2D>> entryPair = new Pair<>(entryList.get(i), entryList.get(j));
        Rectangle2D union = entryPair.left.getValue().createUnion(entryPair.right.getValue());
        double area =
            Node.area(union)
                - Node.area(entryPair.left.getValue())
                - Node.area(entryPair.right.getValue());
        if (!winningPair.isPresent()) {
          winningPair = Optional.of(entryPair);
          largestArea = area;
        } else if (area > largestArea) {
          winningPair = Optional.of(entryPair);
        }
      }
    }
    Preconditions.checkArgument(winningPair.isPresent(), "Winning pair not found");
    Map.Entry<T, Rectangle2D> leftEntry = winningPair.get().left;
    LeafNode leftNode = LeafNode.create(leftEntry);
    Map.Entry<T, Rectangle2D> rightEntry = winningPair.get().right;
    LeafNode rightNode = LeafNode.create(rightEntry);

    return Pair.of(leftNode, rightNode);
  }

  private void distributeEntry(
      List<Map.Entry<T, Rectangle2D>> entries, Pair<LeafNode<T>> pickedSeeds) {

    Optional<Map.Entry<T, Rectangle2D>> nextOptional = pickNext(entries, pickedSeeds);
    if (nextOptional.isPresent()) {
      Map.Entry<T, Rectangle2D> next = nextOptional.get();
      // which of the picked seeds should it be added to?
      Rectangle2D leftBounds = pickedSeeds.left.getBounds();
      Rectangle2D rightBounds = pickedSeeds.right.getBounds();
      // which rectangle is enlarged the least?
      double leftArea = Node.area(leftBounds);
      double rightArea = Node.area(rightBounds);
      double leftEnlargement = Node.area(leftBounds.createUnion(next.getValue())) - leftArea;
      double rightEnlargement = Node.area(rightBounds.createUnion(next.getValue())) - rightArea;
      if (leftEnlargement == rightEnlargement) {
        // a tie. consider the smaller area
        if (leftArea == rightArea) {
          // another tie. consider the one with the fewest kids
          int leftKids = pickedSeeds.left.size();
          int rightKids = pickedSeeds.right.size();
          if (leftKids < rightKids) {
            pickedSeeds.left.map.put(next.getKey(), next.getValue());
          } else {
            pickedSeeds.right.map.put(next.getKey(), next.getValue());
          }
        } else if (leftArea < rightArea) {
          pickedSeeds.left.map.put(next.getKey(), next.getValue());
        } else {
          pickedSeeds.right.map.put(next.getKey(), next.getValue());
        }
      } else if (leftEnlargement < rightEnlargement) {
        pickedSeeds.left.map.put(next.getKey(), next.getValue());
      } else {
        pickedSeeds.right.map.put(next.getKey(), next.getValue());
      }
    }
  }

  /**
   * combine the existing map elements with the new one and make a pair of leaf nodes to distrubute
   * the entries into
   *
   * @param entries Collection of entries to split
   * @param newEntry
   * @return
   */
  private Pair<LeafNode<T>> quadraticSplit(
      Collection<Map.Entry<T, Rectangle2D>> entries, Map.Entry<T, Rectangle2D> newEntry) {
    // make a collection of kids from leafNode that also include the new element
    // items will be removed from the entryList as they are distributed
    List<Map.Entry<T, Rectangle2D>> entryList = Lists.newArrayList(entries);
    entryList.add(newEntry);
    // get the best pair to split on trom the leafNode elements
    Pair<LeafNode<T>> pickedSeeds = pickSeeds(entryList);
    // these currently have no parent set....
    while (entryList.size() > 0
        && pickedSeeds.left.size() < M - m + 1
        && pickedSeeds.right.size() < M - m + 1) {
      distributeEntry(entryList, pickedSeeds);
    }
    if (entryList.size() > 0) {
      // take care of entries that were not distributed
      if (pickedSeeds.left.size() >= M - m + 1) {
        // left side too big, give them to the right side
        for (Map.Entry<T, Rectangle2D> entry : entryList) {
          pickedSeeds.right.map.put(entry.getKey(), entry.getValue());
        }
      } else {
        // right side too big, give them to the left side
        for (Map.Entry<T, Rectangle2D> entry : entryList) {
          pickedSeeds.left.map.put(entry.getKey(), entry.getValue());
        }
      }
    }
    return pickedSeeds;
  }
}
