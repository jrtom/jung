package edu.uci.ics.jung.visualization.spatial.rtree;

import static edu.uci.ics.jung.visualization.spatial.rtree.Node.M;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.m;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.nodeArea;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.nodeMargin;
import static edu.uci.ics.jung.visualization.spatial.rtree.Node.nodeOverlap;

import com.google.common.collect.Lists;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RStarSplitter<T> extends AbstractSplitter<T> implements Splitter<T> {

  private static Logger log = LoggerFactory.getLogger(RStarSplitter.class);

  private Comparator<Node<T>> horizontalEdgeComparator = new HorizontalEdgeNodeComparator();
  private Comparator verticalEdgeComparator = new VerticalEdgeNodeComparator();

  public Pair<InnerNode<T>> split(List<Node<T>> children, Node<T> newEntry) {
    return chooseSplitNodes(children, newEntry);
  }

  private Pair<InnerNode<T>> chooseSplitNodes(Collection<Node<T>> entries, Node<T> newEntry) {
    Pair<List<Node<T>>> pair = chooseSplit(entries, newEntry);
    InnerNode<T> innerNodeLeft = InnerNode.create(pair.left);
    InnerNode<T> innerNodeRight = InnerNode.create(pair.right);
    return Pair.of(innerNodeLeft, innerNodeRight);
  }

  private Pair<List<Node<T>>> chooseSplit(Collection<Node<T>> entries, Node<T> newEntry) {
    // make 2 lists to sort
    List<Node<T>> xAxisList = Lists.newArrayList(entries);
    xAxisList.add(newEntry);
    List<Node<T>> yAxisList = Lists.newArrayList(entries);
    yAxisList.add(newEntry);

    xAxisList.sort(horizontalEdgeComparator);
    yAxisList.sort(verticalEdgeComparator);

    List<Pair<List<Node<T>>>> horizontalGroup = Lists.newArrayList();
    List<Pair<List<Node<T>>>> verticalGroup = Lists.newArrayList();

    for (int k = 0; k < M - 2 * m + 2; k++) {
      horizontalGroup.add(
          Pair.of(xAxisList.subList(0, m - 1 + k), xAxisList.subList(m - 1 + k, xAxisList.size())));
      verticalGroup.add(
          Pair.of(yAxisList.subList(0, m - 1 + k), yAxisList.subList(m - 1 + k, yAxisList.size())));
    }
    int sumXMarginValue = 0;
    for (Pair<List<Node<T>>> pair : horizontalGroup) {
      sumXMarginValue += nodeMargin(pair.left, pair.right);
    }
    int sumYMarginValue = 0;
    for (Pair<List<Node<T>>> pair : verticalGroup) {
      sumYMarginValue += nodeMargin(pair.left, pair.right);
    }
    //        Axis split = null;
    if (sumXMarginValue < sumYMarginValue) {
      //            split = Axis.X;
      return chooseSplitIndex(horizontalGroup);
    } else {
      //            split = Axis.Y;
      return chooseSplitIndex(verticalGroup);
    }
  }

  private Pair<List<Node<T>>> chooseSplitIndex(List<Pair<List<Node<T>>>> group) {
    double minOverlap = 0;
    double minArea = 0;
    Optional<Pair<List<Node<T>>>> winner = Optional.empty();
    // find the Pair of lists with the mim overlap or min area
    for (Pair<List<Node<T>>> pair : group) {
      double nodeOverlap = nodeOverlap(pair.left, pair.right);
      double nodeArea = nodeArea(pair.left, pair.right);
      if (!winner.isPresent()) {
        minOverlap = nodeOverlap;
        minArea = nodeArea;
        winner = Optional.of(pair);
      } else if (nodeOverlap == minOverlap) {
        // try area
        if (nodeArea < minArea) {
          minOverlap = nodeOverlap;
          minArea = nodeArea;
          winner = Optional.of(pair);
        }
      } else if (nodeOverlap < minOverlap) {
        minOverlap = nodeOverlap;
        minArea = nodeArea;
        winner = Optional.of(pair);
      }
    }
    return winner.orElse(null);
  }

  @Override
  public Optional<Node<T>> chooseSubtree(InnerNode<T> nodeToSplit, T element, Rectangle2D bounds) {
    if (nodeToSplit.isLeafChildren()) {
      return leastOverlapThenEnlargementThenAreaThenKids(nodeToSplit, bounds); // R*-Tree
    } else {
      return leastEnlargementThenAreaThenKids(nodeToSplit, bounds);
    }
  }

  // leaf methods

}
