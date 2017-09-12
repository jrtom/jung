/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.swing.JComponent;

/**
 * A renderer that will fill vertex shapes with a GradientPaint
 *
 * @author Tom Nelson
 * @param <V> the vertex type
 * @param <V> the edge type
 */
public class GradientVertexRenderer<V> implements Renderer.Vertex<V> {

  Color colorOne;
  Color colorTwo;
  Color pickedColorOne;
  Color pickedColorTwo;
  PickedState<V> pickedState;
  boolean cyclic;
  protected final Layout<V> layout;
  protected final RenderContext<V, ?> renderContext;

  public GradientVertexRenderer(
      VisualizationServer<V, ?> vv, Color colorOne, Color colorTwo, boolean cyclic) {
    this.colorOne = colorOne;
    this.colorTwo = colorTwo;
    this.cyclic = cyclic;
    this.layout = vv.getGraphLayout();
    this.renderContext = vv.getRenderContext();
  }

  public GradientVertexRenderer(
      VisualizationServer<V, ?> vv,
      Color colorOne,
      Color colorTwo,
      Color pickedColorOne,
      Color pickedColorTwo,
      boolean cyclic) {
    this.colorOne = colorOne;
    this.colorTwo = colorTwo;
    this.pickedColorOne = pickedColorOne;
    this.pickedColorTwo = pickedColorTwo;
    this.pickedState = vv.getPickedVertexState();
    this.cyclic = cyclic;
    this.layout = vv.getGraphLayout();
    this.renderContext = vv.getRenderContext();
  }

  public void paintVertex(V v) {
    if (renderContext.getVertexIncludePredicate().test(v)) {
      boolean vertexHit = true;
      // get the shape to be rendered
      Shape shape = renderContext.getVertexShapeTransformer().apply(v);

      Point2D p = layout.apply(v);
      p = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p);

      float x = (float) p.getX();
      float y = (float) p.getY();

      // create a transform that translates to the location of
      // the vertex to be rendered
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      // transform the vertex shape with xtransform
      shape = xform.createTransformedShape(shape);

      vertexHit = vertexHit(shape);

      if (vertexHit) {
        paintShapeForVertex(v, shape);
      }
    }
  }

  protected boolean vertexHit(Shape s) {
    JComponent vv = renderContext.getScreenDevice();
    Rectangle deviceRectangle = null;
    if (vv != null) {
      Dimension d = vv.getSize();
      deviceRectangle = new Rectangle(0, 0, d.width, d.height);
    }
    return renderContext
        .getMultiLayerTransformer()
        .getTransformer(Layer.VIEW)
        .transform(s)
        .intersects(deviceRectangle);
  }

  protected void paintShapeForVertex(V v, Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Paint oldPaint = g.getPaint();
    Rectangle r = shape.getBounds();
    float y2 = (float) r.getMaxY();
    if (cyclic) {
      y2 = (float) (r.getMinY() + r.getHeight() / 2);
    }

    Paint fillPaint = null;
    if (pickedState != null && pickedState.isPicked(v)) {
      fillPaint =
          new GradientPaint(
              (float) r.getMinX(),
              (float) r.getMinY(),
              pickedColorOne,
              (float) r.getMinX(),
              y2,
              pickedColorTwo,
              cyclic);
    } else {
      fillPaint =
          new GradientPaint(
              (float) r.getMinX(),
              (float) r.getMinY(),
              colorOne,
              (float) r.getMinX(),
              y2,
              colorTwo,
              cyclic);
    }
    if (fillPaint != null) {
      g.setPaint(fillPaint);
      g.fill(shape);
      g.setPaint(oldPaint);
    }
    Paint drawPaint = renderContext.getVertexDrawPaintTransformer().apply(v);
    if (drawPaint != null) {
      g.setPaint(drawPaint);
    }
    Stroke oldStroke = g.getStroke();
    Stroke stroke = renderContext.getVertexStrokeTransformer().apply(v);
    if (stroke != null) {
      g.setStroke(stroke);
    }
    g.draw(shape);
    g.setPaint(oldPaint);
    g.setStroke(oldStroke);
  }
}
