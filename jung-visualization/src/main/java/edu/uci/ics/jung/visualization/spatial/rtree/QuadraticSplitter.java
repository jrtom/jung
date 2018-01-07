package edu.uci.ics.jung.visualization.spatial.rtree;

import static edu.uci.ics.jung.visualization.spatial.rtree.Node.M;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.area;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.m;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

public class QuadraticSplitter<T> extends AbstractSplitter<T> implements Splitter<T> {

  public Pair<InnerNode<T>> split(List<Node<T>> children, Node<T> newEntry) {
    return quadraticSplit(children, newEntry);
  }

  private Pair<InnerNode<T>> quadraticSplit(List<Node<T>> children, Node<T> newEntry) {
    // make a collection of kids from leafNode that also include the new element
    // items will be removed from the entryList as they are distributed
    List<Node<T>> entryList = Lists.newArrayList(children);
    entryList.add(newEntry);
    // get the best pair to split on trom the leafNode elements
    Pair<InnerNode<T>> pickedSeeds = pickSeeds(entryList);
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
        for (Node<T> entry : entryList) {
          pickedSeeds.right.addNode(entry);
        }
      } else {
        // right side too big, give them to the left side
        for (Node<T> entry : entryList) {
          pickedSeeds.left.addNode(entry);
        }
      }
    }
    return pickedSeeds;
  }

  private void distributeEntry(List<Node<T>> entries, Pair<InnerNode<T>> pickedSeeds) {

    Optional<Node<T>> nextOptional = pickNext(entries, pickedSeeds);
    if (nextOptional.isPresent()) {
      Node<T> next = nextOptional.get();
      // which of the picked seeds should it be added to?
      Rectangle2D leftBounds = pickedSeeds.left.getBounds();
      Rectangle2D rightBounds = pickedSeeds.right.getBounds();
      // which rectangle is enlarged the least?
      double leftArea = area(leftBounds);
      double rightArea = area(rightBounds);
      double leftEnlargement = area(leftBounds.createUnion(next.getBounds())) - leftArea;
      double rightEnlargement = area(rightBounds.createUnion(next.getBounds())) - rightArea;
      if (leftEnlargement == rightEnlargement) {
        // a tie. consider the smaller area
        if (leftArea == rightArea) {
          // another tie. consider the one with the fewest kids
          int leftKids = pickedSeeds.left.size();
          int rightKids = pickedSeeds.right.size();
          if (leftKids < rightKids) {
            pickedSeeds.left.addNode(next);
          } else {
            pickedSeeds.right.addNode(next);
          }
        } else if (leftArea < rightArea) {
          pickedSeeds.left.addNode(next);
        } else {
          pickedSeeds.right.addNode(next);
        }
      } else if (leftEnlargement < rightEnlargement) {
        pickedSeeds.left.addNode(next);
      } else {
        pickedSeeds.right.addNode(next);
      }
    }
  }

  private Pair<InnerNode<T>> pickSeeds(List<Node<T>> entryList) {
    double largestArea = 0;
    Optional<Pair<Node<T>>> winningPair = Optional.empty();
    for (int i = 0; i < entryList.size(); i++) {
      for (int j = i + 1; j < entryList.size(); j++) {
        Pair<Node<T>> entryPair = new Pair<>(entryList.get(i), entryList.get(j));
        Rectangle2D union = entryPair.left.getBounds().createUnion(entryPair.right.getBounds());
        double area =
            area(union) - area(entryPair.left.getBounds()) - area(entryPair.right.getBounds());
        if (!winningPair.isPresent()) {
          winningPair = Optional.of(entryPair);
          largestArea = area;
        } else if (area > largestArea) {
          winningPair = Optional.of(entryPair);
        }
      }
    }
    Preconditions.checkArgument(winningPair.isPresent(), "No winning pair returned");
    Node<T> leftEntry = winningPair.get().left;
    InnerNode<T> leftNode = InnerNode.create(leftEntry);
    Node<T> rightEntry = winningPair.get().right;
    InnerNode<T> rightNode = InnerNode.create(rightEntry);
    entryList.remove(leftEntry);
    entryList.remove(rightEntry);
    return new Pair<>(leftNode, rightNode);
  }

  private Optional<Node<T>> pickNext(List<Node<T>> entries, Pair<InnerNode<T>> pickedSeeds) {
    double maxDifference = 0;
    Optional<Node<T>> winner = Optional.empty();
    entries.removeAll(pickedSeeds.left.getChildren());
    entries.removeAll(pickedSeeds.right.getChildren());
    // for each entry
    for (Node<T> entry : entries) {
      // ... that is not already in the leaf node....
      if (!pickedSeeds.left.getChildren().contains(entry)
          && !pickedSeeds.right.getChildren().contains(entry)) {
        // calculate area increase that would happen
        InnerNode<T> leftNode = pickedSeeds.left;
        InnerNode<T> rightNode = pickedSeeds.right;
        double leftArea = area(leftNode.getBounds());
        double rightArea = area(rightNode.getBounds());
        Rectangle2D leftUnion = leftNode.getBounds().createUnion(entry.getBounds());
        Rectangle2D rightUnion = rightNode.getBounds().createUnion(entry.getBounds());
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

  public Optional<Node<T>> chooseSubtree(InnerNode<T> nodeToSplit, T element, Rectangle2D bounds) {
    return leastEnlargementThenAreaThenKids(nodeToSplit, bounds);
  }
}
