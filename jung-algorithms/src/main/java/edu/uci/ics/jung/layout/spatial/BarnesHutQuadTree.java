package edu.uci.ics.jung.layout.spatial;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class BarnesHutQuadTree<T> {

  private static final Logger log = LoggerFactory.getLogger(BarnesHutQuadTree.class);

  private Node<T> root;

  public Rectangle getBounds() {
    return root.getBounds();
  }

  public Node<T> getRoot() {
    return root;
  }

  protected LayoutModel<T> layoutModel;

  /** @param layoutModel */
  public BarnesHutQuadTree(LayoutModel<T> layoutModel) {
    this.layoutModel = layoutModel;
    this.root = new Node(new Rectangle(0, 0, layoutModel.getWidth(), layoutModel.getHeight()));
  }

  /*
   * Clears the quadtree
   */
  public void clear() {
    root.clear();
  }

  public ForceObject<T> calculateForce(ForceObject<T> node) {
    if (root != null && root.forceObject != node) {
      return root.calculateForce(node);
    }
    return node;
  }

  /*
   * Insert the object into the quadtree. If the node exceeds the capacity, it
   * will split and add all objects to their corresponding nodes.
   */
  protected void insert(ForceObject node) {
    root.insert(node);
  }

  public void rebuild() {
    clear();
    for (T node : layoutModel.getGraph().nodes()) {
      Point p = layoutModel.apply(node);
      insert(new ForceObject(node, p));
    }
  }
}
