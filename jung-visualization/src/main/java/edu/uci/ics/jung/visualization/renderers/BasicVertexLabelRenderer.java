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
import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.TransformingGraphics;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class BasicVertexLabelRenderer<V> implements Renderer.VertexLabel<V> {

  protected Position position = Position.SE;
  private Positioner positioner = new OutsidePositioner();
  protected final Layout<V> layout;
  protected final RenderContext<V, ?> renderContext;

  public BasicVertexLabelRenderer(Layout<V> layout, RenderContext<V, ?> rc) {
    this.layout = layout;
    this.renderContext = rc;
  }

  /** @return the position */
  public Position getPosition() {
    return position;
  }

  /** @param position the position to set */
  public void setPosition(Position position) {
    this.position = position;
  }

  public Component prepareRenderer(
      VertexLabelRenderer graphLabelRenderer, Object value, boolean isSelected, V vertex) {
    return renderContext
        .getVertexLabelRenderer()
        .<V>getVertexLabelRendererComponent(
            renderContext.getScreenDevice(),
            value,
            renderContext.getVertexFontTransformer().apply(vertex),
            isSelected,
            vertex);
  }

  /**
   * Labels the specified vertex with the specified label. Uses the font specified by this
   * instance's <code>VertexFontFunction</code>. (If the font is unspecified, the existing font for
   * the graphics context is used.) If vertex label centering is active, the label is centered on
   * the position of the vertex; otherwise the label is offset slightly.
   */
  public void labelVertex(V v, String label) {
    if (!renderContext.getVertexIncludePredicate().test(v)) {
      return;
    }
    Point2D pt = layout.apply(v);
    pt = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, pt);

    float x = (float) pt.getX();
    float y = (float) pt.getY();

    Component component =
        prepareRenderer(
            renderContext.getVertexLabelRenderer(),
            label,
            renderContext.getPickedVertexState().isPicked(v),
            v);
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Dimension d = component.getPreferredSize();
    AffineTransform xform = AffineTransform.getTranslateInstance(x, y);

    Shape shape = renderContext.getVertexShapeTransformer().apply(v);
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
    g.draw(component, renderContext.getRendererPane(), p.x, p.y, d.width, d.height, true);
  }

  protected Point getAnchorPoint(Rectangle2D vertexBounds, Dimension labelSize, Position position) {
    double x;
    double y;
    int offset = 5;
    switch (position) {
      case N:
        x = vertexBounds.getCenterX() - labelSize.width / 2;
        y = vertexBounds.getMinY() - offset - labelSize.height;
        return new Point((int) x, (int) y);

      case NE:
        x = vertexBounds.getMaxX() + offset;
        y = vertexBounds.getMinY() - offset - labelSize.height;
        return new Point((int) x, (int) y);

      case E:
        x = vertexBounds.getMaxX() + offset;
        y = vertexBounds.getCenterY() - labelSize.height / 2;
        return new Point((int) x, (int) y);

      case SE:
        x = vertexBounds.getMaxX() + offset;
        y = vertexBounds.getMaxY() + offset;
        return new Point((int) x, (int) y);

      case S:
        x = vertexBounds.getCenterX() - labelSize.width / 2;
        y = vertexBounds.getMaxY() + offset;
        return new Point((int) x, (int) y);

      case SW:
        x = vertexBounds.getMinX() - offset - labelSize.width;
        y = vertexBounds.getMaxY() + offset;
        return new Point((int) x, (int) y);

      case W:
        x = vertexBounds.getMinX() - offset - labelSize.width;
        y = vertexBounds.getCenterY() - labelSize.height / 2;
        return new Point((int) x, (int) y);

      case NW:
        x = vertexBounds.getMinX() - offset - labelSize.width;
        y = vertexBounds.getMinY() - offset - labelSize.height;
        return new Point((int) x, (int) y);

      case CNTR:
        x = vertexBounds.getCenterX() - labelSize.width / 2;
        y = vertexBounds.getCenterY() - labelSize.height / 2;
        return new Point((int) x, (int) y);

      default:
        return new Point();
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
  /** @return the positioner */
  public Positioner getPositioner() {
    return positioner;
  }

  /** @param positioner the positioner to set */
  public void setPositioner(Positioner positioner) {
    this.positioner = positioner;
  }
}
