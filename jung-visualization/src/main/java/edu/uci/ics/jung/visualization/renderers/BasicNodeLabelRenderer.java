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

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.TransformingGraphics;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class BasicNodeLabelRenderer<N, E> implements Renderer.NodeLabel<N, E> {

  protected Position position = Position.SE;
  private Positioner positioner = new OutsidePositioner();

  /**
   * @return the position
   */
  public Position getPosition() {
    return position;
  }

  /**
   * @param position the position to set
   */
  public void setPosition(Position position) {
    this.position = position;
  }

  public Component prepareRenderer(
      RenderContext<N, E> renderContext,
      LayoutModel<N> layoutModel,
      NodeLabelRenderer graphLabelRenderer,
      Object value,
      boolean isSelected,
      N node) {
    return renderContext
        .getNodeLabelRenderer()
        .<N>getNodeLabelRendererComponent(
            renderContext.getScreenDevice(),
            value,
            renderContext.getNodeFontFunction().apply(node),
            isSelected,
            node);
  }

  /**
   * Labels the specified node with the specified label. Uses the font specified by this instance's
   * <code>NodeFontFunction</code>. (If the font is unspecified, the existing font for the graphics
   * context is used.) If node label centering is active, the label is centered on the position of
   * the node; otherwise the label is offset slightly.
   */
  public void labelNode(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E> visualizationModel,
      N v,
      String label) {
    if (!renderContext.getNodeIncludePredicate().test(v)) {
      return;
    }
    LayoutModel<N> layoutModel = visualizationModel.getLayoutModel();
    Point pt = layoutModel.apply(v);
    Point2D pt2d =
        renderContext
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, new Point2D.Double(pt.x, pt.y));

    float x = (float) pt2d.getX();
    float y = (float) pt2d.getY();

    Component component =
        prepareRenderer(
            renderContext,
            layoutModel,
            renderContext.getNodeLabelRenderer(),
            label,
            renderContext.getPickedNodeState().isPicked(v),
            v);
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Dimension d = component.getPreferredSize();
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);

    Shape shape = renderContext.getNodeShapeFunction().apply(v);
    shape = xform.createTransformedShape(shape);
    if (renderContext.getGraphicsContext() instanceof TransformingGraphics) {
      BidirectionalTransformer transformer =
          ((TransformingGraphics) renderContext.getGraphicsContext()).getTransformer();
      if (transformer instanceof ShapeTransformer) {
        ShapeTransformer shapeTransformer = (ShapeTransformer) transformer;
        shape = shapeTransformer.transform(shape);
      }
    }
    Rectangle2D bounds = shape.getBounds2D();

    Point p = null;
    if (position == Position.AUTO) {
      Dimension vvd = renderContext.getScreenDevice().getSize();
      if (vvd.width == 0 || vvd.height == 0) {
        vvd = renderContext.getScreenDevice().getPreferredSize();
      }
      p = getAnchorPoint(bounds, d, positioner.getPosition(x, y, vvd));
    } else {
      p = getAnchorPoint(bounds, d, position);
    }

    Paint fillPaint = renderContext.getNodeLabelDrawPaintFunction().apply(v);
    if (fillPaint != null) {
      Paint oldPaint = component.getForeground();
      component.setForeground((Color) fillPaint);
      g.draw(
          component,
          renderContext.getRendererPane(),
          (int) p.x,
          (int) p.y,
          d.width,
          d.height,
          true);
      component.setForeground((Color) oldPaint);
    } else {
      g.draw(
          component,
          renderContext.getRendererPane(),
          (int) p.x,
          (int) p.y,
          d.width,
          d.height,
          true);
    }
  }

  protected Point getAnchorPoint(Rectangle2D nodeBounds, Dimension labelSize, Position position) {
    double x;
    double y;
    int offset = 5;
    switch (position) {
      case N:
        x = nodeBounds.getCenterX() - labelSize.width / 2;
        y = nodeBounds.getMinY() - offset - labelSize.height;
        return Point.of(x, y);

      case NE:
        x = nodeBounds.getMaxX() + offset;
        y = nodeBounds.getMinY() - offset - labelSize.height;
        return Point.of(x, y);

      case E:
        x = nodeBounds.getMaxX() + offset;
        y = nodeBounds.getCenterY() - labelSize.height / 2;
        return Point.of(x, y);

      case SE:
        x = nodeBounds.getMaxX() + offset;
        y = nodeBounds.getMaxY() + offset;
        return Point.of(x, y);

      case S:
        x = nodeBounds.getCenterX() - labelSize.width / 2;
        y = nodeBounds.getMaxY() + offset;
        return Point.of(x, y);

      case SW:
        x = nodeBounds.getMinX() - offset - labelSize.width;
        y = nodeBounds.getMaxY() + offset;
        return Point.of(x, y);

      case W:
        x = nodeBounds.getMinX() - offset - labelSize.width;
        y = nodeBounds.getCenterY() - labelSize.height / 2;
        return Point.of(x, y);

      case NW:
        x = nodeBounds.getMinX() - offset - labelSize.width;
        y = nodeBounds.getMinY() - offset - labelSize.height;
        return Point.of(x, y);

      case CNTR:
        x = nodeBounds.getCenterX() - labelSize.width / 2;
        y = nodeBounds.getCenterY() - labelSize.height / 2;
        return Point.of(x, y);

      default:
        return Point.ORIGIN;
    }
  }

  public static class InsidePositioner implements Positioner {
    public Position getPosition(float x, float y, Dimension d) {
      int cx = d.width / 2;
      int cy = d.height / 2;
      if (x > cx && y > cy) {
        return Position.NW;
      }
      if (x > cx && y < cy) {
        return Position.SW;
      }
      if (x < cx && y > cy) {
        return Position.NE;
      }
      return Position.SE;
    }
  }

  public static class OutsidePositioner implements Positioner {
    public Position getPosition(float x, float y, Dimension d) {
      int cx = d.width / 2;
      int cy = d.height / 2;
      if (x > cx && y > cy) {
        return Position.SE;
      }
      if (x > cx && y < cy) {
        return Position.NE;
      }
      if (x < cx && y > cy) {
        return Position.SW;
      }
      return Position.NW;
    }
  }
  /**
   * @return the positioner
   */
  public Positioner getPositioner() {
    return positioner;
  }

  /**
   * @param positioner the positioner to set
   */
  public void setPositioner(Positioner positioner) {
    this.positioner = positioner;
  }
}
