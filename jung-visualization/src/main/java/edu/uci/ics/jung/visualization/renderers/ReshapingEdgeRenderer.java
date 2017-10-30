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

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.TransformingGraphics;
import edu.uci.ics.jung.visualization.util.Context;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 * uses a flatness argument to break edges into smaller segments. This produces a more detailed
 * transformation of the edge shape
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 * @param <V> the vertex type
 * @param <V> the edge type
 */
public class ReshapingEdgeRenderer<V, E> extends BasicEdgeRenderer<V, E>
    implements Renderer.Edge<V, E> {

  /**
   * Draws the edge <code>e</code>, whose endpoints are at <code>(x1,y1)</code> and <code>(x2,y2)
   * </code>, on the graphics context <code>g</code>. The <code>Shape</code> provided by the <code>
   * EdgeShapeFunction</code> instance is scaled in the x-direction so that its width is equal to
   * the distance between <code>(x1,y1)</code> and <code>(x2,y2)</code>.
   */
  protected void drawSimpleEdge(
      RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, E e) {

    TransformingGraphics g = (TransformingGraphics) renderContext.getGraphicsContext();
    Network<V, E> graph = layoutMediator.getNetwork();
    EndpointPair<V> endpoints = graph.incidentNodes(e);
    V v1 = endpoints.nodeU();
    V v2 = endpoints.nodeV();
    Point2D p1 = layoutMediator.getLayout().apply(v1);
    Point2D p2 = layoutMediator.getLayout().apply(v2);
    p1 = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
    p2 = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
    float x1 = (float) p1.getX();
    float y1 = (float) p1.getY();
    float x2 = (float) p2.getX();
    float y2 = (float) p2.getY();

    float flatness = 0;
    MutableTransformer transformer =
        renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW);
    if (transformer instanceof LensTransformer) {
      LensTransformer ht = (LensTransformer) transformer;
      RectangularShape lensShape = ht.getLensShape();
      if (lensShape.contains(x1, y1) || lensShape.contains(x2, y2)) {
        flatness = .05f;
      }
    }

    boolean isLoop = v1.equals(v2);
    Shape s2 = renderContext.getVertexShapeTransformer().apply(v2);
    Shape edgeShape = renderContext.getEdgeShapeTransformer().apply(Context.getInstance(graph, e));

    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

    if (isLoop) {
      // this is a self-loop. scale it is larger than the vertex
      // it decorates and translate it so that its nadir is
      // at the center of the vertex.
      Rectangle2D s2Bounds = s2.getBounds2D();
      xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
      xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
    } else {
      // this is a normal edge. Rotate it to the angle between
      // vertex endpoints, then scale it to the distance between
      // the vertices
      float dx = x2 - x1;
      float dy = y2 - y1;
      float thetaRadians = (float) Math.atan2(dy, dx);
      xform.rotate(thetaRadians);
      float dist = (float) Math.sqrt(dx * dx + dy * dy);
      xform.scale(dist, 1.0);
    }

    edgeShape = xform.createTransformedShape(edgeShape);

    Paint oldPaint = g.getPaint();

    // get Paints for filling and drawing
    // (filling is done first so that drawing and label use same Paint)
    Paint fill_paint = renderContext.getEdgeFillPaintTransformer().apply(e);
    if (fill_paint != null) {
      g.setPaint(fill_paint);
      g.fill(edgeShape, flatness);
    }
    Paint draw_paint = renderContext.getEdgeDrawPaintTransformer().apply(e);
    if (draw_paint != null) {
      g.setPaint(draw_paint);
      g.draw(edgeShape, flatness);
    }

    float scalex = (float) g.getTransform().getScaleX();
    float scaley = (float) g.getTransform().getScaleY();
    // see if arrows are too small to bother drawing
    if (scalex < .3 || scaley < .3) {
      return;
    }

    if (renderContext.renderEdgeArrow()) {

      Shape destVertexShape = renderContext.getVertexShapeTransformer().apply(v2);

      AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
      destVertexShape = xf.createTransformedShape(destVertexShape);

      AffineTransform at =
          edgeArrowRenderingSupport.getArrowTransform(
              renderContext, new GeneralPath(edgeShape), destVertexShape);
      if (at == null) {
        return;
      }
      Shape arrow = renderContext.getEdgeArrow();
      arrow = at.createTransformedShape(arrow);
      g.setPaint(renderContext.getArrowFillPaintTransformer().apply(e));
      g.fill(arrow);
      g.setPaint(renderContext.getArrowDrawPaintTransformer().apply(e));
      g.draw(arrow);

      if (!graph.isDirected()) {
        Shape vertexShape = renderContext.getVertexShapeTransformer().apply(v1);
        xf = AffineTransform.getTranslateInstance(x1, y1);
        vertexShape = xf.createTransformedShape(vertexShape);

        at =
            edgeArrowRenderingSupport.getReverseArrowTransform(
                renderContext, new GeneralPath(edgeShape), vertexShape, !isLoop);
        if (at == null) {
          return;
        }
        arrow = renderContext.getEdgeArrow();
        arrow = at.createTransformedShape(arrow);
        g.setPaint(renderContext.getArrowFillPaintTransformer().apply(e));
        g.fill(arrow);
        g.setPaint(renderContext.getArrowDrawPaintTransformer().apply(e));
        g.draw(arrow);
      }
    }
    // use existing paint for text if no draw paint specified
    if (draw_paint == null) {
      g.setPaint(oldPaint);
    }

    // restore old paint
    g.setPaint(oldPaint);
  }
}
