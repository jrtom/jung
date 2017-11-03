/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization3d;

import com.google.common.graph.Graph;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.util.Context;
import java.util.function.Function;
import javax.media.j3d.Appearance;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

/** */
@SuppressWarnings("unchecked")
public class PluggableRenderContext<N, E> implements RenderContext<N, E> {

  protected Function<E, Appearance> edgeAppearanceTransformer;
  protected Function<Context<Graph<N>, E>, Node> edgeShapeTransformer;
  protected PickedState<E> pickedEdgeState;
  protected PickedState<N> pickedVertexState;
  protected Function<N, Appearance> vertexAppearanceTransformer;
  protected Function<Object, String> vertexStringer = n -> null;
  protected Function<N, Node> vertexShapeTransformer;

  public PluggableRenderContext() {
    super();
    Color3f lightGray = new Color3f(0.7f, 0.7f, 0.7f);
    Color3f black = new Color3f(0, 0, 0);
    Color3f white = new Color3f(1, 1, 1);
    Color3f gray = new Color3f(.2f, .2f, .2f);
    Color3f red = new Color3f(1, 0, 0);
    Color3f yellow = new Color3f(0, 1, 1);
    Material lightGrayMaterial = new Material(lightGray, black, lightGray, white, 100.0f);
    Material blackMaterial = new Material(lightGray, black, black, lightGray, 10.0f);
    Material whiteMaterial = new Material(white, white, white, white, 100.0f);
    Material grayMaterial = new Material(gray, black, gray, gray, 100.0f);
    Material redMaterial = new Material(red, black, red, red, 100.0f);
    Material yellowMaterial = new Material(yellow, black, yellow, yellow, 100.0f);

    final Appearance lightGrayLook = new Appearance();
    lightGrayLook.setMaterial(lightGrayMaterial);
    Appearance blackLook = new Appearance();
    blackLook.setMaterial(blackMaterial);
    Appearance whiteLook = new Appearance();
    whiteLook.setMaterial(whiteMaterial);
    Appearance grayLook = new Appearance();
    grayLook.setMaterial(grayMaterial);

    //		grayLook.setCapability(Appearance.ALLOW_MATERIAL_READ);
    //		grayLook.setCapability(Appearance.ALLOW_MATERIAL_WRITE);

    final Appearance redLook = new Appearance();
    redLook.setMaterial(redMaterial);
    final Appearance yellowLook = new Appearance();
    yellowLook.setMaterial(yellowMaterial);

    final Cylinder cylinder =
        new Cylinder(
            1,
            1,
            Cylinder.GENERATE_NORMALS | Cylinder.ENABLE_GEOMETRY_PICKING,
            26,
            26,
            lightGrayLook);
    final Sphere sphere =
        new Sphere(10, Sphere.GENERATE_NORMALS | Sphere.ENABLE_GEOMETRY_PICKING, redLook);
    final Box box =
        new Box(10, 10, 10, Box.GENERATE_NORMALS | Box.ENABLE_GEOMETRY_PICKING, redLook);

    this.edgeAppearanceTransformer = n -> lightGrayLook;
    this.edgeShapeTransformer =
        new Function<Context<Graph<N>, E>, Node>() {

          public Node apply(Context<Graph<N>, E> ec) {
            LineArray lineArray = new LineArray(2, LineArray.COORDINATES | LineArray.COLOR_3);
            lineArray.setCoordinates(
                0, new Point3f[] {new Point3f(0, -.5f, 0), new Point3f(0, .5f, 0)});
            lineArray.setColor(0, new Color3f(1, 1, 1));
            lineArray.setColor(1, new Color3f(1, 1, 1));
            Shape3D shape = new Shape3D();
            shape.setGeometry(lineArray);
            //            return shape;
            return new Cylinder(
                1,
                1,
                Cylinder.GENERATE_NORMALS | Cylinder.ENABLE_GEOMETRY_PICKING,
                26,
                26,
                lightGrayLook);
          }
        };
    this.vertexAppearanceTransformer = n -> redLook;
    this.vertexShapeTransformer =
        new Function<N, Node>() {

          public Node apply(N arg0) {
            return new Sphere(
                7,
                Sphere.GENERATE_NORMALS
                    | Sphere.ENABLE_GEOMETRY_PICKING
                    | Sphere.ENABLE_APPEARANCE_MODIFY,
                redLook);
          }
        };
  }

  public Function<E, Appearance> getEdgeAppearanceTransformer() {
    return edgeAppearanceTransformer;
  }

  public Function<Context<Graph<N>, E>, Node> getEdgeShapeTransformer() {
    return edgeShapeTransformer;
  }

  public PickedState<E> getPickedEdgeState() {
    return pickedEdgeState;
  }

  public PickedState<N> getPickedVertexState() {
    return pickedVertexState;
  }

  public Function<N, Appearance> getVertexAppearanceTransformer() {
    return vertexAppearanceTransformer;
  }

  public Function<N, Node> getVertexShapeTransformer() {
    return vertexShapeTransformer;
  }

  public Function<Object, String> getVertexStringer() {
    return vertexStringer;
  }

  public void setEdgeAppearanceTransformer(Function<E, Appearance> edgeAppearanceTransformer) {
    this.edgeAppearanceTransformer = edgeAppearanceTransformer;
  }

  public void setEdgeShapeTransformer(Function<Context<Graph<N>, E>, Node> edgeShapeTransformer) {
    this.edgeShapeTransformer = edgeShapeTransformer;
  }

  public void setPickedEdgeState(PickedState<E> pickedEdgeState) {
    this.pickedEdgeState = pickedEdgeState;
  }

  public void setPickedVertexState(PickedState<N> pickedVertexState) {
    this.pickedVertexState = pickedVertexState;
  }

  public void setVertexAppearanceTransformer(Function<N, Appearance> vertexAppearanceTransformer) {
    this.vertexAppearanceTransformer = vertexAppearanceTransformer;
  }

  public void setVertexShapeTransformer(Function<N, Node> vertexShapeTransformer) {
    this.vertexShapeTransformer = vertexShapeTransformer;
  }

  public void setVertexStringer(Function<Object, String> vertexStringer) {
    this.vertexStringer = vertexStringer;
  }
}
