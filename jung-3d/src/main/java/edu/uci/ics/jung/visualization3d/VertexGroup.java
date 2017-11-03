/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization3d;

/** */
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

/** @author Tom Nelson - tomnelson@dev.java.net */
public class VertexGroup<V> extends TransformGroup {

  V vertex;
  Node shape;
  TransformGroup labelNode = new TransformGroup();

  public VertexGroup(V vertex, Node shape) {
    this.vertex = vertex;
    this.shape = shape;
    setCapability(TransformGroup.ENABLE_PICK_REPORTING);
    addChild(shape);
    addChild(labelNode);
    Transform3D tt = new Transform3D();
    //		 tt.setTranslation(new Vector3f(10,10,0));
    labelNode.setTransform(tt);
  }

  /** @return the shape */
  public Node getShape() {
    return shape;
  }

  /** @param shape the shape to set */
  public void setShape(Node shape) {
    this.shape = shape;
  }

  public String toString() {
    return "VertexGroup for " + vertex.toString();
  }

  /** @return the labelNode */
  public TransformGroup getLabelNode() {
    return labelNode;
  }
}
