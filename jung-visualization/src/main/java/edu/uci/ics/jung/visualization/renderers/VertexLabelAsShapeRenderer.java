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
import edu.uci.ics.jung.visualization.layout.LayoutModel;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Renders Vertex Labels, but can also supply Shapes for vertices. This has the effect of making the
 * vertex label the actual vertex shape. The user will probably want to center the vertex label on
 * the vertex location.
 *
 * @author Tom Nelson
 * @param <N> the vertex type
 * @param <N> the edge type
 */
public class VertexLabelAsShapeRenderer<N, E>
    implements Renderer.VertexLabel<N, E>, Function<N, Shape> {

  protected Map<N, Shape> shapes = new HashMap<N, Shape>();
  protected final LayoutModel<N, Point2D> layoutModel;
  protected final RenderContext<N, ?> renderContext;

  public VertexLabelAsShapeRenderer(
      VisualizationModel<N, E, Point2D> visualizationModel, RenderContext<N, ?> rc) {
    this.layoutModel = visualizationModel.getLayoutModel();
    this.renderContext = rc;
  }

  public Component prepareRenderer(
      RenderContext<N, ?> rc,
      VertexLabelRenderer graphLabelRenderer,
      Object value,
      boolean isSelected,
      N vertex) {
    return renderContext
        .getVertexLabelRenderer()
        .<N>getVertexLabelRendererComponent(
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
  public void labelVertex(
      RenderContext<N, E> renderContext,
      VisualizationModel<N, E, Point2D> visualizationModel,
      N v,
      String label) {
    if (!renderContext.getVertexIncludePredicate().test(v)) {
      return;
    }
    GraphicsDecorator g = renderContext.getGraphicsContext();
    Component component =
        prepareRenderer(
            renderContext,
            renderContext.getVertexLabelRenderer(),
            label,
            renderContext.getPickedVertexState().isPicked(v),
            v);
    Dimension d = component.getPreferredSize();

    int h_offset = -d.width / 2;
    int v_offset = -d.height / 2;

    Point2D p = layoutModel.apply(v);
    p = renderContext.getMultiLayerTransformer().transform(Layer.LAYOUT, p);

    int x = (int) p.getX();
    int y = (int) p.getY();

    g.draw(
        component,
        renderContext.getRendererPane(),
        x + h_offset,
        y + v_offset,
        d.width,
        d.height,
        true);

    Dimension size = component.getPreferredSize();
    Rectangle bounds =
        new Rectangle(-size.width / 2 - 2, -size.height / 2 - 2, size.width + 4, size.height);
    shapes.put(v, bounds);
  }

  public Shape apply(N v) {
    Component component =
        prepareRenderer(
            renderContext,
            renderContext.getVertexLabelRenderer(),
            renderContext.getVertexLabelTransformer().apply(v),
            renderContext.getPickedVertexState().isPicked(v),
            v);
    Dimension size = component.getPreferredSize();
    Rectangle bounds =
        new Rectangle(-size.width / 2 - 2, -size.height / 2 - 2, size.width + 4, size.height);
    return bounds;
    //		Shape shape = shapes.get(v);
    //		if(shape == null) {
    //			return new Rectangle(-20,-20,40,40);
    //		}
    //		else return shape;
  }

  public Renderer.VertexLabel.Position getPosition() {
    return Renderer.VertexLabel.Position.CNTR;
  }

  public Renderer.VertexLabel.Positioner getPositioner() {
    return new Positioner() {
      public Renderer.VertexLabel.Position getPosition(float x, float y, Dimension d) {
        return Renderer.VertexLabel.Position.CNTR;
      }
    };
  }

  public void setPosition(Renderer.VertexLabel.Position position) {
    // noop
  }

  public void setPositioner(Renderer.VertexLabel.Positioner positioner) {
    //noop
  }
}
