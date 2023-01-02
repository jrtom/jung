package edu.uci.ics.jung.visualization.spatial.rtree;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import edu.uci.ics.jung.visualization.spatial.TreeNode;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * R-Tree or R*-Tree implementation, depending on the type of Splitters passed in the
 * SplitterContext
 *
 * <p>Based on <a href="http://dbs.mathematik.uni-marburg.de/publications/myPapers/1990/BKSS90.pdf">
 * The R*-tree: An Efficient and Robust Access Method for Points and Rectangles+</a> Norbert
 * Beckmann, Hans-Peter begel, Ralf Schneider, Bernhard Seeger Praktuche Informatlk, Umversltaet
 * Bremen, D-2800 Bremen 33, West Germany
 *
 * @author Tom Nelson
 */
public class RTree<T> {

  private static final Logger log = LoggerFactory.getLogger(RTree.class);

  /** the root of the R-Tree */
  private final Optional<Node<T>> root;

  /**
   * @return the root of the R-Tree
   */
  public Optional<Node<T>> getRoot() {
    return root;
  }

  /** create an empty R-Tree */
  private RTree() {
    root = Optional.empty();
  }

  /**
   * create an R-Tree with the passed Node as the root
   *
   * @param node the node that will be the root
   */
  private RTree(Node<T> node) {
    Preconditions.checkArgument(
        !node.getParent().isPresent(), "Error creating R-Tree with root that has parent");
    root = Optional.of(node);
  }

  /**
   * create and return an empty R-Tree
   *
   * @param <T>
   * @return an empty R-Tree
   */
  public static <T> RTree<T> create() {
    return new RTree();
  }

  /**
   * add one element to the RTree
   *
   * @param splitterContext the R*Tree or R-Tree rules
   * @param element to add to the tree
   * @param bounds for the element to add
   * @return a new RTree containing the added element
   */
  public RTree<T> add(SplitterContext<T> splitterContext, T element, Rectangle2D bounds) {
    // see if the root is not present (i.e. the RTree is empty
    if (!root.isPresent()) {
      // The first element addded to an empty RTree
      // Return a new RTree with the new LeafNode as its root
      return new RTree(LeafNode.create(element, bounds));
    }
    // otherwise...
    Node<T> node = root.get();
    if (node instanceof LeafNode) {
      // the root is a LeafNode
      LeafNode<T> leafNode = (LeafNode) node;

      Node<T> got = leafNode.add(splitterContext, element, bounds);
      Preconditions.checkArgument(
          !got.getParent().isPresent(), "return from LeafNode add has a parent");
      return new RTree(got);

    } else {

      InnerNode<T> innerNode = (InnerNode) node;
      Node<T> got = innerNode.add(splitterContext, element, bounds);
      Preconditions.checkArgument(
          !got.getParent().isPresent(), "return from InnerNode add has a parent");
      return new RTree(got);
    }
  }

  /**
   * remove an element from the tree
   *
   * @param element
   * @return
   */
  public RTree<T> remove(T element) {
    log.trace("want to remove {} from tree of size {}", element, count());
    if (!root.isPresent()) {
      // this tree is empty
      return new RTree();
    }
    Node<T> rootNode = root.get();
    Node<T> newRoot = rootNode.remove(element);
    return new RTree(newRoot);
  }

  /**
   * return an object at point p
   *
   * @param p point to search
   * @return an element that contains p or null
   */
  public T getPickedObject(Point2D p) {
    Node<T> root = this.root.get();
    if (root instanceof LeafNode) {
      LeafNode<T> leafNode = (LeafNode) root;
      return leafNode.getPickedObject(p);
    } else if (root instanceof InnerNode) {
      InnerNode<T> innerNode = (InnerNode) root;
      return innerNode.getPickedObject(p);
    } else {
      return null;
    }
  }

  /**
   * @return a collection of rectangular bounds of the R-Tree nodes
   */
  public Set<Shape> getGrid() {
    Set<Shape> areas = Sets.newHashSet();
    if (root.isPresent()) {
      Node<T> node = root.get();
      node.collectGrids(areas);
    }
    return areas;
  }

  /**
   * get the R-Tree leaf nodes that would contain the passed point
   *
   * @param p the point to search
   * @return a Collection of R-Tree nodes that would contain p
   */
  public Collection<TreeNode> getContainingLeafs(Point2D p) {
    if (root.isPresent()) {
      Node<T> theRoot = root.get();

      if (theRoot instanceof LeafNode) {
        return Collections.singleton(theRoot);
      } else if (theRoot instanceof InnerNode) {
        return ((InnerNode) theRoot).getContainingLeafs(Sets.newHashSet(), p);
      }
    }
    return Collections.emptySet();
  }

  /**
   * count all the elements in the R-Tree
   *
   * @return the count
   */
  public int count() {
    int count = 0;
    if (root.isPresent()) {
      Node<T> node = root.get();
      count += node.count();
    }
    return count;
  }

  private String asString() {
    if (root.isPresent()) {
      return root.get().asString("");
    } else {
      return "Empty RTree";
    }
  }

  private static final String marginIncrement = "   ";

  private static String asString(Rectangle2D r) {
    return "["
        + (int) r.getX()
        + ","
        + (int) r.getY()
        + ","
        + (int) r.getWidth()
        + ","
        + (int) r.getHeight()
        + "]";
  }

  private static <T> String asString(Collection<RTree<T>> trees) {
    StringBuilder sb = new StringBuilder();
    for (RTree<T> tree : trees) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(tree.asString());
    }
    return sb.toString();
  }

  private static <T> String asString(Map<T, Rectangle2D> map) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<T, Rectangle2D> entry : map.entrySet()) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(asString(entry));
    }
    return sb.toString();
  }

  private static <T> String asString(Map.Entry<T, Rectangle2D> entry) {
    return entry.getKey() + "->" + asString(entry.getValue());
  }

  @Override
  public String toString() {
    return this.asString();
  }
}
