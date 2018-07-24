package edu.uci.ics.jung.layout.spatial;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Node in the BarnesHutQuadTree. Has a rectangular dimension and a ForceObject that may be either
 * a graph node or a representation of the combined forces of the child nodes. May have 4 child
 * nodes.
 *
 * @author Tom Nelson
 */
public class Node<T> {

  private static final Logger log = LoggerFactory.getLogger(Node.class);
  public static final double THETA = 0.5f;

  // a node contains a ForceObject and possibly 4 Nodes
  protected ForceObject<T> forceObject;

  Node NW;
  Node NE;
  Node SE;
  Node SW;

  protected Rectangle area;

  public Node(double x, double y, double width, double height) {
    this(new Rectangle(x, y, width, height));
  }

  public Node(Rectangle r) {
    area = r;
  }

  /**
   * if this is an inner node, the forceObject represents the combined force and mass of the child
   * nodes.
   *
   * @return the forceObject for this node.
   */
  public ForceObject<T> getForceObject() {
    return forceObject;
  }

  /** @return true if this node has no child nodes, false otherwise */
  public boolean isLeaf() {
    return NW == null && NE == null && SE == null && SW == null;
  }

  /** @return the northwest quadrant node */
  public Node getNW() {
    return NW;
  }

  /** @return the northeast quadrant node */
  public Node getNE() {
    return NE;
  }

  /** @return the southeast quadrant node */
  public Node getSE() {
    return SE;
  }

  /** @return the southwest quadrant node */
  public Node getSW() {
    return SW;
  }

  /** @return the rectangular area of this node */
  public Rectangle getArea() {
    return area;
  }

  /**
   * insert a new ForceObject into the tree. This changes the combinedMass and the forceVector for
   * any Node that it is inserted into
   *
   * @param element
   */
  public void insert(ForceObject<T> element) {
    if (log.isTraceEnabled()) {
      log.trace("insert {} into {}", element, this);
    }
    if (forceObject == null) {
      forceObject = element;
      return;
    }
    if (isLeaf()) {
      if (this.forceObject.p.equals(element.p)) {
        // compare points for special case where the 2 elements are at the same location
        // this would cause an infinite attempt to split and re-insert
        // just add the new mass
        this.forceObject = this.forceObject.add(element);
      } else {
        // there already is a forceObject and location is different, so split
        split();
        // put the current resident and the new one into the correct quadrants
        insertForceObject(this.forceObject);
        insertForceObject(element);
        // update the centerOfMass, Mass, and Force on this node
        this.forceObject = this.forceObject.add(element);
      }
    } else {
      if (forceObject == element) {
        log.error("can't insert {} into {}", element, this.forceObject);
      }
      // we're already split, update the forceElement for this new element
      forceObject = forceObject.add(element);
      // and follow down the tree to insert
      insertForceObject(element);
    }
  }

  /**
   * insert into the correct quadrant of this inner node
   *
   * @param forceObject object to insert
   */
  private void insertForceObject(ForceObject forceObject) {
    if (NW.area.contains(forceObject.p)) {
      NW.insert(forceObject);
    } else if (NE.area.contains(forceObject.p)) {
      NE.insert(forceObject);
    } else if (SE.area.contains(forceObject.p)) {
      SE.insert(forceObject);
    } else if (SW.area.contains(forceObject.p)) {
      SW.insert(forceObject);
    }
  }

  /** @return the rectangular bounds of this node */
  public Rectangle getBounds() {
    return area;
  }

  /** remove all child nodes */
  public void clear() {
    forceObject = null;
    NW = NE = SW = SE = null;
  }

  /*
   * Splits the Quadtree into 4 sub-QuadTrees
   */
  protected void split() {
    if (log.isTraceEnabled()) {
      log.trace("splitting {}", this);
    }
    double width = (area.width / 2);
    double height = (area.height / 2);
    double x = area.x;
    double y = area.y;
    NE = new Node(x + width, y, width, height);
    NW = new Node(x, y, width, height);
    SW = new Node(x, y + height, width, height);
    SE = new Node(x + width, y + height, width, height);
  }

  /**
   * accept a visit from the visitor force object, and add this node's forces to the visitor
   *
   * @param visitor the visitor
   */
  public void applyForcesTo(ForceObject<T> visitor) {
    Preconditions.checkArgument(visitor != null, "Cannot apply forces to a null ForceObject");
    if (this.forceObject == null || visitor.getElement().equals(this.forceObject.getElement())) {
      return;
    }

    if (isLeaf()) {

      visitor.addForceFrom(this.forceObject);

    } else {
      // not a leaf. this node is an internal node
      //  calculate s/d
      double s = this.area.width;
      //      distance between the incoming node's position and
      //      the center of mass for this node
      double d = this.forceObject.p.distance(visitor.p);
      if (s / d < THETA) {
        // this node is sufficiently far away, just use this node's forces
        visitor.addForceFrom(this.forceObject);

      } else {
        // down the tree we go
        NW.applyForcesTo(visitor);
        NE.applyForcesTo(visitor);
        SW.applyForcesTo(visitor);
        SE.applyForcesTo(visitor);
      }
    }
  }

  static String asString(Rectangle r) {
    return "[" + (int) r.x + "," + (int) r.y + "," + (int) r.width + "," + (int) r.height + "]";
  }

  static <T> String asString(String label, Node<T> node, String margin) {
    StringBuilder s = new StringBuilder();
    s.append("\n");
    s.append(margin);
    s.append(label);
    s.append("bounds=");
    s.append(asString(node.getBounds()));
    ForceObject forceObject = node.getForceObject();
    if (forceObject != null) {
      s.append(", forceObject:=");
      s.append(forceObject.toString());
    }
    if (node.NW != null) s.append(asString("NW:", node.NW, margin + marginIncrement));
    if (node.NE != null) s.append(asString("NE:", node.NE, margin + marginIncrement));
    if (node.SW != null) s.append(asString("SW:", node.SW, margin + marginIncrement));
    if (node.SE != null) s.append(asString("SE:", node.SE, margin + marginIncrement));

    return s.toString();
  }

  static String marginIncrement = "   ";

  @Override
  public String toString() {
    return asString("", this, "");
  }
}
