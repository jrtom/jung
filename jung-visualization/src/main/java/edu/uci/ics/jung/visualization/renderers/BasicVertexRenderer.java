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
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicVertexRenderer<V, E> implements Renderer.Vertex<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BasicVertexRenderer.class);

  public void paintVertex(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      V v) {
    if (renderContext.getVertexIncludePredicate().test(v)) {
      paintIconForVertex(renderContext, visualizationModel, v);
    }
  }

  /**
   * Returns the vertex shape in view coordinates.
   *
   * @param v the vertex whose shape is to be returned
   * @param coords the x and y view coordinates
   * @return the vertex shape in view coordinates
   */
  protected Shape prepareFinalVertexShape(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      V v,
      int[] coords) {

    // get the shape to be rendered
    Shape shape = renderContext.getVertexShapeTransformer().apply(v);
    Point2D p = visualizationModel.getLayoutModel().apply(v);
    log.trace("prepared a shape for " + v + " to go at " + p);
    p = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
    float x = (float) p.getX();
    float y = (float) p.getY();
    coords[0] = (int) x;
    coords[1] = (int) y;
    // create a transform that translates to the location of
    // the vertex to be rendered
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
    // transform the vertex shape with xtransform
    shape = xform.createTransformedShape(shape);
    return shape;
  }

  /**
   * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>.
   *
   * @param v the vertex to be painted
   */
  protected void paintIconForVertex(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      V v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(renderContext, visualizationModel, v, coords);

    if (renderContext.getVertexIconTransformer() != null) {
      Icon icon = renderContext.getVertexIconTransformer().apply(v);
      if (icon != null) {

        g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1]);

      } else {
        paintShapeForVertex(renderContext, visualizationModel, v, shape);
      }
    } else {
      paintShapeForVertex(renderContext, visualizationModel, v, shape);
    }
  }

  protected void paintShapeForVertex(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      V v,
      Shape shape) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Paint oldPaint = g.getPaint();
    Paint fillPaint = renderContext.getVertexFillPaintTransformer().apply(v);
    if (fillPaint != null) {
      g.setPaint(fillPaint);
      g.fill(shape);
      g.setPaint(oldPaint);
    }
    Paint drawPaint = renderContext.getVertexDrawPaintTransformer().apply(v);
    if (drawPaint != null) {
      g.setPaint(drawPaint);
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
}
