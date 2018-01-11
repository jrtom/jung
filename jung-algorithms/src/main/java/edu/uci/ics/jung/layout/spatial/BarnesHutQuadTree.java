package edu.uci.ics.jung.layout.spatial;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import java.util.Collection;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A QuadTree that can gather combined forces from visited nodes. Inspired by
 * http://arborjs.org/docs/barnes-hut
 * http://www.cs.princeton.edu/courses/archive/fall03/cs126/assignments/barnes-hut.html
 * https://github.com/chindesaurus/BarnesHut-N-Body
 *
 * @author Tom Nelson
 */
public class BarnesHutQuadTree<T> {

  private static final Logger log = LoggerFactory.getLogger(BarnesHutQuadTree.class);

  /** the root node of the quad tree */
  private Node<T> root;

  /**
   * the bounds of this quad tree
   *
   * @return
   */
  public Rectangle getBounds() {
    return root.getBounds();
  }

  /** @return the root {@code Node} of this tree */
  public Node<T> getRoot() {
    return root;
  }

  private Object lock = new Object();

  public BarnesHutQuadTree(double width, double height) {
    this.root = new Node(new Rectangle(0, 0, width, height));
  }

  public BarnesHutQuadTree(Rectangle r) {
    this.root = new Node(r);
  }

  /*
   * Clears the quadtree
   */
  public void clear() {
    root.clear();
  }

  /**
   * passed node will visit nodes in the quad tree and accumulate their forces
   *
   * @param node
   */
  public void acceptVisitor(ForceObject<T> node) {
    if (root != null && root.forceObject != node) {
      root.acceptVisitor(node);
    }
  }

  /*
   * Insert the object into the quadtree. If the node exceeds the capacity, it
   * will split and add all objects to their corresponding nodes.
   */
  protected void insert(ForceObject node) {
    synchronized (lock) {
      root.insert(node);
    }
  }

  /**
   * package level for unit test use
   *
   * @param nodes
   * @param function
   */
  void rebuild(Collection<T> nodes, Function<T, Point> function) {
    clear();
    synchronized (lock) {
      for (T node : nodes) {
        Point p = function.apply(node);
        ForceObject<T> forceObject = new ForceObject<T>(node, p);
        insert(forceObject);
      }
    }
  }

  /**
   * rebuild the quad tree with the nodes and location mappings of the passed LayoutModel
   *
   * @param layoutModel
   */
  public void rebuild(LayoutModel<T> layoutModel) {
    rebuild(layoutModel.getGraph().nodes(), layoutModel);
  }

  @Override
  public String toString() {
    return "Tree:" + root;
  }
}
