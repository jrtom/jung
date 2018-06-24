package edu.uci.ics.jung.layout.spatial;

import com.google.common.base.Preconditions;
import edu.uci.ics.jung.layout.model.Point;
import java.util.Map;
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
   * passed {@code ForceObject} will visit nodes in the quad tree and accumulate their forces
   *
   * @param visitor
   */
  public void applyForcesTo(ForceObject<T> visitor) {
    Preconditions.checkArgument(visitor != null, "Cannot apply forces to a null ForceObject");
    if (root != null && root.forceObject != visitor) {
      root.applyForcesTo(visitor);
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
   * rebuild the quad tree with the nodes and location mappings of the passed LayoutModel
   *
   * @param locations - mapping of elements to locations
   */
  public void rebuild(Map<T, Point> locations) {
    clear();
    synchronized (lock) {
      for (Map.Entry<T, Point> entry : locations.entrySet()) {
        ForceObject<T> forceObject = new ForceObject<T>(entry.getKey(), entry.getValue());
        insert(forceObject);
      }
    }
  }

  @Override
  public String toString() {
    return "Tree:" + root;
  }
}
