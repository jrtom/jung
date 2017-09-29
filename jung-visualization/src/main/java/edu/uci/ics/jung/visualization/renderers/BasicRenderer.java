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
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.util.Collection;
import java.util.ConcurrentModificationException;

/**
 * The default implementation of the Renderer used by the VisualizationViewer. Default Vertex and
 * Edge Renderers are supplied, or the user may set custom values. The Vertex and Edge renderers are
 * used in the renderVertex and renderEdge methods, which are called in the render loop of the
 * VisualizationViewer.
 *
 * @author Tom Nelson
 */
public class BasicRenderer implements Renderer {

  protected Renderer.Vertex vertexRenderer = new BasicVertexRenderer();
  protected Renderer.VertexLabel vertexLabelRenderer = new BasicVertexLabelRenderer();
  protected Renderer.Edge edgeRenderer = new BasicEdgeRenderer();
  protected Renderer.EdgeLabel edgeLabelRenderer = new BasicEdgeLabelRenderer();

  public void render(RenderContext renderContext, LayoutMediator layoutMediator, Spatial spatial) {

    Collection visibleNodes = spatial.getVisibleNodes();
    Network network = layoutMediator.getNetwork();
    // paint all the edges
    try {
      for (Object e : network.edges()) {
        renderEdge(renderContext, layoutMediator, e);
        renderEdgeLabel(renderContext, layoutMediator, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (Object v : visibleNodes) {
        renderVertex(renderContext, layoutMediator, v);
        renderVertexLabel(renderContext, layoutMediator, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void render(RenderContext renderContext, LayoutMediator layoutMediator) {
    Network network = layoutMediator.getNetwork();
    // paint all the edges
    try {
      for (Object e : network.edges()) {
        renderEdge(renderContext, layoutMediator, e);
        renderEdgeLabel(renderContext, layoutMediator, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (Object v : network.nodes()) {
        renderVertex(renderContext, layoutMediator, v);
        renderVertexLabel(renderContext, layoutMediator, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  public void renderVertex(RenderContext renderContext, LayoutMediator layoutMediator, Object v) {
    vertexRenderer.paintVertex(renderContext, layoutMediator, v);
  }

  public void renderVertexLabel(
      RenderContext renderContext, LayoutMediator layoutMediator, Object v) {
    vertexLabelRenderer.labelVertex(
        renderContext, layoutMediator, v, renderContext.getVertexLabelTransformer().apply(v));
  }

  public void renderEdge(RenderContext renderContext, LayoutMediator layoutMediator, Object e) {
    edgeRenderer.paintEdge(renderContext, layoutMediator, e);
  }

  public void renderEdgeLabel(
      RenderContext renderContext, LayoutMediator layoutMediator, Object e) {
    edgeLabelRenderer.labelEdge(
        renderContext, layoutMediator, e, renderContext.getEdgeLabelTransformer().apply(e));
  }

  public void setVertexRenderer(Renderer.Vertex r) {
    this.vertexRenderer = r;
  }

  public void setEdgeRenderer(Renderer.Edge r) {
    this.edgeRenderer = r;
  }

  /** @return the edgeLabelRenderer */
  public Renderer.EdgeLabel getEdgeLabelRenderer() {
    return edgeLabelRenderer;
  }

  /** @param edgeLabelRenderer the edgeLabelRenderer to set */
  public void setEdgeLabelRenderer(Renderer.EdgeLabel edgeLabelRenderer) {
    this.edgeLabelRenderer = edgeLabelRenderer;
  }

  /** @return the vertexLabelRenderer */
  public Renderer.VertexLabel getVertexLabelRenderer() {
    return vertexLabelRenderer;
  }

  /** @param vertexLabelRenderer the vertexLabelRenderer to set */
  public void setVertexLabelRenderer(Renderer.VertexLabel vertexLabelRenderer) {
    this.vertexLabelRenderer = vertexLabelRenderer;
  }

  /** @return the edgeRenderer */
  public Renderer.Edge getEdgeRenderer() {
    return edgeRenderer;
  }

  /** @return the vertexRenderer */
  public Renderer.Vertex getVertexRenderer() {
    return vertexRenderer;
  }
}
