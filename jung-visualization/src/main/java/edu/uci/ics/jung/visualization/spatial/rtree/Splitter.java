package edu.uci.ics.jung.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

/**
 * interface for classes that hold semantics for R-Tree and R*-Tree
 *
 * @author Tom Nelson
 * @param <T> the type of element stored in the RTree
 */
public interface Splitter<T> {

  Pair<InnerNode<T>> split(List<Node<T>> children, Node<T> newEntry);

  Optional<Node<T>> chooseSubtree(InnerNode<T> nodeToSplit, T element, Rectangle2D bounds);
}
