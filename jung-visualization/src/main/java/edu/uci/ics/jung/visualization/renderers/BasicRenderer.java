/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.renderers;

import com.google.common.graph.Network;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.util.ConcurrentModificationException;

/**
 * The default implementation of the Renderer used by the VisualizationViewer. Default Vertex and
 * Edge Renderers are supplied, or the user may set custom values. The Vertex and Edge renderers are
 * used in the renderVertex and renderEdge methods, which are called in the render loop of the
 * VisualizationViewer.
 *
 * @author Tom Nelson
 */
public class BasicRenderer<V, E> implements Renderer<V, E> {

  protected Renderer.Vertex<V, E> vertexRenderer = new BasicVertexRenderer<V, E>();
  protected Renderer.VertexLabel<V, E> vertexLabelRenderer = new BasicVertexLabelRenderer<V, E>();
  protected Renderer.Edge<V, E> edgeRenderer = new BasicEdgeRenderer<V, E>();
  protected Renderer.EdgeLabel<V, E> edgeLabelRenderer = new BasicEdgeLabelRenderer<V, E>();

  @Override
  public void render(RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator) {
    Network<V, E> network = renderContext.getNetwork();
    // paint all the edges
    try {
      for (E e : network.edges()) {
        renderEdge(renderContext, layoutMediator, e);
        renderEdgeLabel(renderContext, layoutMediator, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : network.nodes()) {
        renderVertex(renderContext, layoutMediator, v);
        renderVertexLabel(renderContext, layoutMediator, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  public void renderVertex(
      RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, V v) {
    vertexRenderer.paintVertex(renderContext, layoutMediator, v);
  }

  public void renderVertexLabel(
      RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, V v) {
    vertexLabelRenderer.labelVertex(
        renderContext, layoutMediator, v, renderContext.getVertexLabelTransformer().apply(v));
  }

  public void renderEdge(
      RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, E e) {
    edgeRenderer.paintEdge(renderContext, layoutMediator, e);
  }

  public void renderEdgeLabel(
      RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, E e) {
    edgeLabelRenderer.labelEdge(
        renderContext, layoutMediator, e, renderContext.getEdgeLabelTransformer().apply(e));
  }

  public void setVertexRenderer(Renderer.Vertex<V, E> r) {
    this.vertexRenderer = r;
  }

  public void setEdgeRenderer(Renderer.Edge<V, E> r) {
    this.edgeRenderer = r;
  }

  /** @return the edgeLabelRenderer */
  public Renderer.EdgeLabel<V, E> getEdgeLabelRenderer() {
    return edgeLabelRenderer;
  }

  /** @param edgeLabelRenderer the edgeLabelRenderer to set */
  public void setEdgeLabelRenderer(Renderer.EdgeLabel<V, E> edgeLabelRenderer) {
    this.edgeLabelRenderer = edgeLabelRenderer;
  }

  /** @return the vertexLabelRenderer */
  public Renderer.VertexLabel<V, E> getVertexLabelRenderer() {
    return vertexLabelRenderer;
  }

  /** @param vertexLabelRenderer the vertexLabelRenderer to set */
  public void setVertexLabelRenderer(Renderer.VertexLabel<V, E> vertexLabelRenderer) {
    this.vertexLabelRenderer = vertexLabelRenderer;
  }

  /** @return the edgeRenderer */
  public Renderer.Edge<V, E> getEdgeRenderer() {
    return edgeRenderer;
  }

  /** @return the vertexRenderer */
  public Renderer.Vertex<V, E> getVertexRenderer() {
    return vertexRenderer;
  }
}
