package edu.uci.ics.jung.layout.spatial;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class Node<T> {

  private static final Logger log = LoggerFactory.getLogger(Node.class);
  public static final double THETA = 0.5f;

  // a node contains a ForceObject and possibly 4 Nodes
  protected ForceObject<T> forceObject;

  Node NW;
  Node NE;
  Node SE;
  Node SW;

  private Rectangle area;

  public Node(double x, double y, double width, double height) {
    this(new Rectangle(x, y, width, height));
  }

  public Node(Rectangle r) {
    area = r;
  }

  public ForceObject<T> getForceObject() {
    return forceObject;
  }

  public boolean isLeaf() {
    return NW == null && NE == null && SE == null && SW == null;
  }

  public Node getNW() {
    return NW;
  }

  public Node getNE() {
    return NE;
  }

  public Node getSE() {
    return SE;
  }

  public Node getSW() {
    return SW;
  }

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
      // there already is a forceObject, so split
      split();
      // put the current resident and the new one into the correct quardrants
      insertForceObject(this.forceObject);
      insertForceObject(element);
      // update the centerOfMass, Mass, and Force on this node
      this.forceObject = this.forceObject.add(element);

    } else {
      if (forceObject == element) {
        log.error("can't insert {} into {}", element, this.forceObject);
      }
      // we're already split, update the forceElement for this new element
      forceObject = forceObject.add(element);
      //and follow down the tree to insert
      insertForceObject(element);
    }
  }

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

  public Rectangle getBounds() {
    return area;
  }

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

  public void visit(ForceObject<T> target, double forceConstant) {
    if (this.forceObject == null || target.element.equals(this.forceObject.element)) {
      return;
    }

    if (isLeaf()) {
      if (log.isTraceEnabled()) {
        log.trace(
            "isLeaf, Node {} at {} visiting {} at {}",
            this.forceObject.getElement(),
            this.forceObject.p,
            target.getElement(),
            target.p);
      }
      target.addForceFrom(this.forceObject, forceConstant);
      log.trace("added force from {} so its now {}", this.forceObject, target);
    } else {
      // not a leaf
      //  this node is an internal node
      //  calculate s/d
      double s = this.area.width;
      //      distance between the incoming node's position and
      //      the center of mass for this node
      double d = this.forceObject.p.distance(target.p);
      if (s / d < THETA) {
        // this node is sufficiently far away
        // just use this node's forces
        if (log.isTraceEnabled()) {
          log.trace(
              "Node {} at {} visiting {} at {}",
              this.forceObject.getElement(),
              this.forceObject.p,
              target.getElement(),
              target.p);
        }
        target.addForceFrom(this.forceObject, forceConstant);
        log.trace("added force from {} so its now {}", this.forceObject, target);

      } else {
        // down the tree we go
        NW.visit(target, forceConstant);
        NE.visit(target, forceConstant);
        SW.visit(target, forceConstant);
        SE.visit(target, forceConstant);
      }
    }
  }

  public Set<ForceObject<T>> getForceObjectsFor(
      Set<ForceObject<T>> forceObjects, ForceObject<T> target) {
    if (this.forceObject == null || target.equals(this.forceObject)) {
      forceObjects.add(target);
    }

    if (isLeaf()) {
      forceObjects.add(this.forceObject);
    } else {
      // not a leaf
      //  this node is an internal node
      //  calculate s/d
      double s = this.area.width;
      //      distance between the incoming node's position and
      //      the center of mass for this node
      double d = this.forceObject.p.distance(target.p);
      if (s / d < THETA) {
        // this node is sufficiently far away
        // just use this node's forces
        forceObjects.add(this.forceObject);
      } else {
        // down the tree we go
        NW.getForceObjectsFor(forceObjects, target);
        NE.getForceObjectsFor(forceObjects, target);
        SW.getForceObjectsFor(forceObjects, target);
        SE.getForceObjectsFor(forceObjects, target);
      }
    }
    return forceObjects;
  }

  static String asString(Rectangle r) {
    return "[" + (int) r.x + "," + (int) r.y + "," + (int) r.width + "," + (int) r.height + "]";
  }

  static String asString(Node node, String margin) {
    StringBuilder s = new StringBuilder();
    s.append("\n");
    s.append(margin);
    s.append("bounds=");
    s.append(asString(node.getBounds()));
    ForceObject forceObject = node.getForceObject();
    if (forceObject != null) {
      s.append(", forceObject:=");
      s.append(forceObject.toString());
    }
    if (node.NW != null) s.append(asString(node.NW, margin + marginIncrement));
    if (node.NE != null) s.append(asString(node.NE, margin + marginIncrement));
    if (node.SW != null) s.append(asString(node.SW, margin + marginIncrement));
    if (node.SE != null) s.append(asString(node.SE, margin + marginIncrement));

    return s.toString();
  }

  static String marginIncrement = "   ";

  @Override
  public String toString() {
    return asString(this, "");
  }
}
