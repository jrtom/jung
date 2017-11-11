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

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.layout.LayoutModel;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A renderer that will fill vertex shapes with a GradientPaint
 *
 * @author Tom Nelson
 * @param <N> the vertex type
 * @param <N> the edge type
 */
public class GradientVertexRenderer<N, E> implements Renderer.Vertex<N, E> {

  private static final Logger log = LoggerFactory.getLogger(GradientVertexRenderer.class);

  Color colorOne;
  Color colorTwo;
  Color pickedColorOne;
  Color pickedColorTwo;
  PickedState<N> pickedState;
  boolean cyclic;

  public GradientVertexRenderer(
      VisualizationServer<N, ?> vv, Color colorOne, Color colorTwo, boolean cyclic) {
    this.colorOne = colorOne;
    this.colorTwo = colorTwo;
    this.cyclic = cyclic;
  }

  public GradientVertexRenderer(
      VisualizationServer<N, ?> vv,
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
  }

  public void paintVertex(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E, Point2D> visualizationModel,
      N v) {
    if (renderContext.getVertexIncludePredicate().test(v)) {
      // get the shape to be rendered
      Shape shape = renderContext.getVertexShapeTransformer().apply(v);
      LayoutModel<N, Point2D> layoutModel = visualizationModel.getLayoutModel();
      Point2D p = layoutModel.apply(v);
      p = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p);

      float x = (float) p.getX();
      float y = (float) p.getY();

      // create a transform that translates to the location of
      // the vertex to be rendered
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      // transform the vertex shape with xtransform
      shape = xform.createTransformedShape(shape);
      log.trace("prepared a shape for " + v + " to go at " + p);

      paintShapeForVertex(renderContext, v, shape);
    }
  }

  protected void paintShapeForVertex(RenderContext<N, E> renderContext, N v, Shape shape) {
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
