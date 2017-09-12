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
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.function.Predicate;

public class BasicEdgeLabelRenderer<V, E> implements Renderer.EdgeLabel<V, E> {
  private final Layout<V> layout;
  private final RenderContext<V, E> renderContext;

  public BasicEdgeLabelRenderer(Layout<V> layout, RenderContext<V, E> renderContext) {
    this.layout = layout;
    this.renderContext = renderContext;
  }

  public Component prepareRenderer(
      EdgeLabelRenderer graphLabelRenderer, Object value, boolean isSelected, E edge) {
    return renderContext
        .getEdgeLabelRenderer()
        .<E>getEdgeLabelRendererComponent(
            renderContext.getScreenDevice(),
            value,
            renderContext.getEdgeFontTransformer().apply(edge),
            isSelected,
            edge);
  }

  @Override
  public void labelEdge(E e, String label) {
    if (label == null || label.length() == 0) {
      return;
    }

    // don't draw edge if either incident vertex is not drawn
    EndpointPair<V> endpoints = renderContext.getNetwork().incidentNodes(e);
    V v1 = endpoints.nodeU();
    V v2 = endpoints.nodeV();
    Predicate<V> nodeIncludePredicate = renderContext.getVertexIncludePredicate();
    if (!nodeIncludePredicate.test(v1) || !nodeIncludePredicate.test(v2)) {
      return;
    }

    Point2D p1 = layout.apply(v1);
    Point2D p2 = layout.apply(v2);
    p1 = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
    p2 = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
    float x1 = (float) p1.getX();
    float y1 = (float) p1.getY();
    float x2 = (float) p2.getX();
    float y2 = (float) p2.getY();

    GraphicsDecorator g = renderContext.getGraphicsContext();
    float distX = x2 - x1;
    float distY = y2 - y1;
    double totalLength = Math.sqrt(distX * distX + distY * distY);

    float closeness = renderContext.getEdgeLabelCloseness();

    int posX = (int) (x1 + (closeness) * distX);
    int posY = (int) (y1 + (closeness) * distY);

    int xDisplacement = (int) (renderContext.getLabelOffset() * (distY / totalLength));
    int yDisplacement = (int) (renderContext.getLabelOffset() * (-distX / totalLength));

    Component component =
        prepareRenderer(
            renderContext.getEdgeLabelRenderer(),
            label,
            renderContext.getPickedEdgeState().isPicked(e),
            e);

    Dimension d = component.getPreferredSize();

    Shape edgeShape = renderContext.getEdgeShapeTransformer().apply(e);

    double parallelOffset = 1;

    parallelOffset += renderContext.getParallelEdgeIndexFunction().getIndex(e);

    parallelOffset *= d.height;
    if (edgeShape instanceof Ellipse2D) {
      parallelOffset += edgeShape.getBounds().getHeight();
      parallelOffset = -parallelOffset;
    }

    AffineTransform old = g.getTransform();
    AffineTransform xform = new AffineTransform(old);
    xform.translate(posX + xDisplacement, posY + yDisplacement);
    double dx = x2 - x1;
    double dy = y2 - y1;
    if (renderContext.getEdgeLabelRenderer().isRotateEdgeLabels()) {
      double theta = Math.atan2(dy, dx);
      if (dx < 0) {
        theta += Math.PI;
      }
      xform.rotate(theta);
    }
    if (dx < 0) {
      parallelOffset = -parallelOffset;
    }

    xform.translate(-d.width / 2, -(d.height / 2 - parallelOffset));
    g.setTransform(xform);
    g.draw(component, renderContext.getRendererPane(), 0, 0, d.width, d.height, true);

    g.setTransform(old);
  }
}
