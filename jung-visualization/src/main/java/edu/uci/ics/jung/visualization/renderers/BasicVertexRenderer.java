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
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import javax.swing.JComponent;

public class BasicVertexRenderer<V> implements Renderer.Vertex<V> {
  protected final Layout<V> layout;
  protected final RenderContext<V, ?> renderContext;

  public BasicVertexRenderer(Layout<V> layout, RenderContext<V, ?> rc) {
    this.layout = layout;
    this.renderContext = rc;
  }

  public void paintVertex(V v) {
    if (renderContext.getVertexIncludePredicate().test(v)) {
      paintIconForVertex(v);
    }
  }

  /**
   * Returns the vertex shape in view coordinates.
   *
   * @param v the vertex whose shape is to be returned
   * @param coords the x and y view coordinates
   * @return the vertex shape in view coordinates
   */
  protected Shape prepareFinalVertexShape(V v, int[] coords) {

    // get the shape to be rendered
    Shape shape = renderContext.getVertexShapeTransformer().apply(v);
    Point2D p = layout.apply(v);
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
  protected void paintIconForVertex(V v) {
    GraphicsDecorator g = renderContext.getGraphicsContext();
    boolean vertexHit = true;
    int[] coords = new int[2];
    Shape shape = prepareFinalVertexShape(v, coords);
    vertexHit = vertexHit(shape);

    if (vertexHit) {
      if (renderContext.getVertexIconTransformer() != null) {
        Icon icon = renderContext.getVertexIconTransformer().apply(v);
        if (icon != null) {

          g.draw(icon, renderContext.getScreenDevice(), shape, coords[0], coords[1]);

        } else {
          paintShapeForVertex(v, shape);
        }
      } else {
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
    MutableTransformer vt = renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW);
    if (vt instanceof MutableTransformerDecorator) {
      vt = ((MutableTransformerDecorator) vt).getDelegate();
    }
    return vt.transform(s).intersects(deviceRectangle);
  }

  protected void paintShapeForVertex(V v, Shape shape) {
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
