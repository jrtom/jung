package edu.uci.ics.jung.layout.spatial;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
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

  private Node<T> root;

  public Rectangle getBounds() {
    return root.getBounds();
  }

  public Node<T> getRoot() {
    return root;
  }

  protected LayoutModel<T> layoutModel;

  private Object lock = new Object();

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

  /**
   * visit nodes in the quad tree and accumulate the forces to apply to the element for the passed
   * node
   *
   * @param forceConstant
   * @param node
   */
  public void visit(double forceConstant, ForceObject<T> node) {
    if (root != null && root.forceObject != node) {
      root.visit(forceConstant, node);
    }
  }

  public Set<ForceObject<T>> getForceObjectsFor(
      Set<ForceObject<T>> forceObjects, ForceObject<T> target) {
    if (root != null && root.forceObject != target) {
      return root.getForceObjectsFor(forceObjects, target);
    } else {
      return Collections.emptySet();
    }
  }

  public static class ForceObjectIterator<T> implements Iterator<ForceObject<T>> {
    private BarnesHutQuadTree<T> tree;
    private ForceObject<T> target;
    private ForceObject<T> next;
    private Set<ForceObject<T>> forceObjects;
    private Iterator<ForceObject<T>> iterator;

    public ForceObjectIterator(BarnesHutQuadTree<T> tree, ForceObject<T> target) {
      this.tree = tree;
      this.target = target;
      this.forceObjects = tree.getForceObjectsFor(Sets.newLinkedHashSet(), target);
      if (log.isTraceEnabled()) {
        log.trace("forceObjects for Iterator are {}", forceObjects);
      }
      this.iterator = forceObjects.iterator();
    }

    @Override
    public boolean hasNext() {
      return this.iterator.hasNext();
    }

    @Override
    public ForceObject<T> next() {
      return this.iterator.next();
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

  public void rebuild() {
    clear();
    synchronized (lock) {
      for (T node : layoutModel.getGraph().nodes()) {
        Point p = layoutModel.apply(node);
        ForceObject<T> forceObject = new ForceObject<T>(node, p);
        insert(forceObject);
      }
    }
  }

  @Override
  public String toString() {
    return "Tree:" + root;
  }
}
