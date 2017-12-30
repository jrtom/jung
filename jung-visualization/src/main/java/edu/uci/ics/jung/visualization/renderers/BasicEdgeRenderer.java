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
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeFunction;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.Context;
import edu.uci.ics.jung.visualization.util.EdgeIndexFunction;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Predicate;

public class BasicEdgeRenderer<N, E> implements Renderer.Edge<N, E> {

  protected EdgeArrowRenderingSupport<N, E> edgeArrowRenderingSupport =
      new BasicEdgeArrowRenderingSupport<N, E>();

  @Override
  public void paintEdge(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e) {
    GraphicsDecorator g2d = renderContext.getGraphicsContext();
    if (!renderContext.getEdgeIncludePredicate().test(e)) {
      return;
    }

    // don't draw edge if either incident node is not drawn
    EndpointPair<N> endpoints = visualizationModel.getNetwork().incidentNodes(e);
    N u = endpoints.nodeU();
    N v = endpoints.nodeV();
    Predicate<N> nodeIncludePredicate = renderContext.getNodeIncludePredicate();
    if (!nodeIncludePredicate.test(u) || !nodeIncludePredicate.test(v)) {
      return;
    }

    Stroke new_stroke = renderContext.edgeStrokeFunction().apply(e);
    Stroke old_stroke = g2d.getStroke();
    if (new_stroke != null) {
      g2d.setStroke(new_stroke);
    }

    drawSimpleEdge(renderContext, visualizationModel, e);

    // restore paint and stroke
    if (new_stroke != null) {
      g2d.setStroke(old_stroke);
    }
  }

  protected Shape prepareFinalEdgeShape(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E> visualizationModel,
      E e,
      int[] coords,
      boolean[] loop) {
    EndpointPair<N> endpoints = visualizationModel.getNetwork().incidentNodes(e);
    N v1 = endpoints.nodeU();
    N v2 = endpoints.nodeV();

    Point p1 = visualizationModel.getLayoutModel().apply(v1);
    Point p2 = visualizationModel.getLayoutModel().apply(v2);
    Point2D p2d1 =
        renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, new Point2D.Double(p1.x, p1.y));
    Point2D p2d2 =
        renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, new Point2D.Double(p2.x, p2.y));
    float x1 = (float) p2d1.getX();
    float y1 = (float) p2d1.getY();
    float x2 = (float) p2d2.getX();
    float y2 = (float) p2d2.getY();
    coords[0] = (int) x1;
    coords[1] = (int) y1;
    coords[2] = (int) x2;
    coords[3] = (int) y2;

    boolean isLoop = loop[0] = v1.equals(v2);
    Shape s2 = renderContext.getNodeShapeFunction().apply(v2);
    Shape edgeShape =
        renderContext
            .getEdgeShapeFunction()
            .apply(Context.getInstance(visualizationModel.getNetwork(), e));

    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

    if (isLoop) {
      // this is a self-loop. scale it is larger than the node
      // it decorates and translate it so that its nadir is
      // at the center of the node.
      Rectangle2D s2Bounds = s2.getBounds2D();
      xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
      xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
    } else if (renderContext.getEdgeShapeFunction() instanceof EdgeShape.Orthogonal) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      int index = 0;
      if (renderContext.getEdgeShapeFunction() instanceof ParallelEdgeShapeFunction) {
        @SuppressWarnings("unchecked")
        EdgeIndexFunction<N, E> peif =
            ((ParallelEdgeShapeFunction<N, E>) renderContext.getEdgeShapeFunction())
                .getEdgeIndexFunction();
        index = peif.getIndex(Context.getInstance(visualizationModel.getNetwork(), e));
        index *= 20;
      }
      GeneralPath gp = new GeneralPath();
      gp.moveTo(0, 0); // the xform will do the translation to x1,y1
      if (x1 > x2) {
        if (y1 > y2) {
          gp.lineTo(0, index);
          gp.lineTo(dx - index, index);
          gp.lineTo(dx - index, dy);
          gp.lineTo(dx, dy);
        } else {
          gp.lineTo(0, -index);
          gp.lineTo(dx - index, -index);
          gp.lineTo(dx - index, dy);
          gp.lineTo(dx, dy);
        }

      } else {
        if (y1 > y2) {
          gp.lineTo(0, index);
          gp.lineTo(dx + index, index);
          gp.lineTo(dx + index, dy);
          gp.lineTo(dx, dy);

        } else {
          gp.lineTo(0, -index);
          gp.lineTo(dx + index, -index);
          gp.lineTo(dx + index, dy);
          gp.lineTo(dx, dy);
        }
      }

      edgeShape = gp;

    } else {
      // this is a normal edge. Rotate it to the angle between
      // node endpoints, then scale it to the distance between
      // the nodes
      float dx = x2 - x1;
      float dy = y2 - y1;
      float thetaRadians = (float) Math.atan2(dy, dx);
      xform.rotate(thetaRadians);
      float dist = (float) Math.sqrt(dx * dx + dy * dy);
      xform.scale(dist, 1.0);
    }

    edgeShape = xform.createTransformedShape(edgeShape);

    return edgeShape;
  }

  /**
   * Draws the edge <code>e</code>, whose endpoints are at <code>(x1,y1)</code> and <code>(x2,y2)
   * </code>, on the graphics context <code>g</code>. The <code>Shape</code> provided by the <code>
   * EdgeShapeFunction</code> instance is scaled in the x-direction so that its width is equal to
   * the distance between <code>(x1,y1)</code> and <code>(x2,y2)</code>.
   *
   * @param e the edge to be drawn
   */
  protected void drawSimpleEdge(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e) {

    int[] coords = new int[4];
    boolean[] loop = new boolean[1];
    Shape edgeShape = prepareFinalEdgeShape(renderContext, visualizationModel, e, coords, loop);

    int x1 = coords[0];
    int y1 = coords[1];
    int x2 = coords[2];
    int y2 = coords[3];
    boolean isLoop = loop[0];

    GraphicsDecorator g = renderContext.getGraphicsContext();
    Network<N, E> network = visualizationModel.getNetwork();

    Paint oldPaint = g.getPaint();

    // get Paints for filling and drawing
    // (filling is done first so that drawing and label use same Paint)
    Paint fill_paint = renderContext.getEdgeFillPaintFunction().apply(e);
    if (fill_paint != null) {
      g.setPaint(fill_paint);
      g.fill(edgeShape);
    }
    Paint draw_paint = renderContext.getEdgeDrawPaintFunction().apply(e);
    if (draw_paint != null) {
      g.setPaint(draw_paint);
      g.draw(edgeShape);
    }

    float scalex = (float) g.getTransform().getScaleX();
    float scaley = (float) g.getTransform().getScaleY();
    // see if arrows are too small to bother drawing
    if (scalex < .3 || scaley < .3) {
      return;
    }

    if (renderContext.renderEdgeArrow()) {

      Stroke new_stroke = renderContext.getEdgeArrowStrokeFunction().apply(e);
      Stroke old_stroke = g.getStroke();
      if (new_stroke != null) {
        g.setStroke(new_stroke);
      }

      Shape destNodeShape =
          renderContext.getNodeShapeFunction().apply(network.incidentNodes(e).nodeV());

      AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
      destNodeShape = xf.createTransformedShape(destNodeShape);

      AffineTransform at =
          edgeArrowRenderingSupport.getArrowTransform(renderContext, edgeShape, destNodeShape);
      if (at == null) {
        return;
      }
      Shape arrow = renderContext.getEdgeArrow();
      arrow = at.createTransformedShape(arrow);
      g.setPaint(renderContext.getArrowFillPaintFunction().apply(e));
      g.fill(arrow);
      g.setPaint(renderContext.getArrowDrawPaintFunction().apply(e));
      g.draw(arrow);

      if (!network.isDirected()) {
        Shape nodeShape =
            renderContext.getNodeShapeFunction().apply(network.incidentNodes(e).nodeU());
        xf = AffineTransform.getTranslateInstance(x1, y1);
        nodeShape = xf.createTransformedShape(nodeShape);
        at =
            edgeArrowRenderingSupport.getReverseArrowTransform(
                renderContext, edgeShape, nodeShape, !isLoop);
        if (at == null) {
          return;
        }
        arrow = renderContext.getEdgeArrow();
        arrow = at.createTransformedShape(arrow);
        g.setPaint(renderContext.getArrowFillPaintFunction().apply(e));
        g.fill(arrow);
        g.setPaint(renderContext.getArrowDrawPaintFunction().apply(e));
        g.draw(arrow);
      }
      // restore paint and stroke
      if (new_stroke != null) {
        g.setStroke(old_stroke);
      }
    }

    // restore old paint
    g.setPaint(oldPaint);
  }

  public EdgeArrowRenderingSupport<N, E> getEdgeArrowRenderingSupport() {
    return edgeArrowRenderingSupport;
  }

  public void setEdgeArrowRenderingSupport(
      EdgeArrowRenderingSupport<N, E> edgeArrowRenderingSupport) {
    this.edgeArrowRenderingSupport = edgeArrowRenderingSupport;
  }
}
