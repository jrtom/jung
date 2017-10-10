/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.renderers;

import com.google.common.collect.Sets;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.util.ConcurrentModificationException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the Renderer used by the VisualizationViewer. Default Vertex and
 * Edge Renderers are supplied, or the user may set custom values. The Vertex and Edge renderers are
 * used in the renderVertex and renderEdge methods, which are called in the render loop of the
 * VisualizationViewer.
 *
 * @author Tom Nelson
 */
public class BasicRenderer<V, E> implements Renderer<V, E> {

  private static final Logger log = LoggerFactory.getLogger(BasicRenderer.class);
  protected Renderer.Vertex<V, E> vertexRenderer = new BasicVertexRenderer<V, E>();
  protected Renderer.VertexLabel<V, E> vertexLabelRenderer = new BasicVertexLabelRenderer<V, E>();
  protected Renderer.Edge<V, E> edgeRenderer = new BasicEdgeRenderer<V, E>();
  protected Renderer.EdgeLabel<V, E> edgeLabelRenderer = new BasicEdgeLabelRenderer<V, E>();

  public void render(
      RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, Spatial<V> spatial) {
    if (spatial == null) {
      render(renderContext, layoutMediator);
      return;
    }
    Set<V> visibleNodes =
        (Set)
            spatial.getVisibleNodes(
                ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());
    Network<V, E> network = layoutMediator.getNetwork();
    Set<E> visibleEdges = Sets.newHashSet(network.edges());
    for (E edge : network.edges()) {
      EndpointPair<V> endpoints = network.incidentNodes(edge);
      boolean keep = false;
      for (V v : endpoints) {
        if (log.isTraceEnabled()) {
          log.trace("keep was:" + keep);
          log.trace("checking to see if endpoint " + v + " is in visibleNodes:" + visibleNodes);
        }
        keep |= visibleNodes.contains(v);
        if (log.isTraceEnabled()) {
          log.trace("keep now:" + keep);
        }
      }
      if (!keep) {
        if (log.isTraceEnabled()) {
          log.trace("removing " + edge + " from visibleEdges:" + visibleEdges);
        }
        visibleEdges.remove(edge);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("visibleNodes:" + visibleNodes);
      log.debug("visibleEdges:" + visibleEdges);
    }
    // paint all the edges
    try {
      for (E e : visibleEdges) {
        renderEdge(renderContext, layoutMediator, e);
        renderEdgeLabel(renderContext, layoutMediator, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : visibleNodes) {
        renderVertex(renderContext, layoutMediator, v);
        renderVertexLabel(renderContext, layoutMediator, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void render(RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator) {
    Network<V, E> network = layoutMediator.getNetwork();
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
