/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.renderers;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.RenderContext;
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

  protected Renderer.Vertex<V> vertexRenderer;
  protected Renderer.VertexLabel<V> vertexLabelRenderer;
  protected Renderer.Edge<V, E> edgeRenderer;
  protected Renderer.EdgeLabel<V, E> edgeLabelRenderer;

  protected final Layout<V> layout;
  protected final RenderContext<V, E> renderContext;

  public BasicRenderer(Layout<V> layout, RenderContext<V, E> rc) {
    this.layout = layout;
    this.renderContext = rc;
    this.vertexRenderer = new BasicVertexRenderer<V>(layout, rc);
    this.vertexLabelRenderer = new BasicVertexLabelRenderer<V>(layout, rc);
    this.edgeRenderer = new BasicEdgeRenderer<V, E>(layout, rc);
    this.edgeLabelRenderer = new BasicEdgeLabelRenderer<V, E>(layout, rc);
  }

  @Override
  public void render() {
    Network<V, E> network = renderContext.getNetwork();
    // paint all the edges
    try {
      for (E e : network.edges()) {
        renderEdge(e);
        renderEdgeLabel(e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : network.nodes()) {
        renderVertex(v);
        renderVertexLabel(v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  public void renderVertex(V v) {
    vertexRenderer.paintVertex(v);
  }

  public void renderVertexLabel(V v) {
    vertexLabelRenderer.labelVertex(v, renderContext.getVertexLabelTransformer().apply(v));
  }

  public void renderEdge(E e) {
    edgeRenderer.paintEdge(e);
  }

  public void renderEdgeLabel(E e) {
    edgeLabelRenderer.labelEdge(e, renderContext.getEdgeLabelTransformer().apply(e));
  }

  public void setVertexRenderer(Renderer.Vertex<V> r) {
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
  public Renderer.VertexLabel<V> getVertexLabelRenderer() {
    return vertexLabelRenderer;
  }

  /** @param vertexLabelRenderer the vertexLabelRenderer to set */
  public void setVertexLabelRenderer(Renderer.VertexLabel<V> vertexLabelRenderer) {
    this.vertexLabelRenderer = vertexLabelRenderer;
  }

  /** @return the edgeRenderer */
  public Renderer.Edge<V, E> getEdgeRenderer() {
    return edgeRenderer;
  }

  /** @return the vertexRenderer */
  public Renderer.Vertex<V> getVertexRenderer() {
    return vertexRenderer;
  }
}
