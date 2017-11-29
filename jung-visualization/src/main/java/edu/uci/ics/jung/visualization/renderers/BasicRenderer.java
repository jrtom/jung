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
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import java.awt.geom.Point2D;
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
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      Spatial<V> spatial) {
    if (spatial == null) {
      render(renderContext, visualizationModel);
      return;
    }
    Set<V> visibleNodes = null;
    try {
      visibleNodes =
          (Set)
              spatial.getVisibleNodes(
                  ((VisualizationServer) renderContext.getScreenDevice()).viewOnLayout());
    } catch (ConcurrentModificationException ex) {
      // skip rendering until graph node index is stable,
      // this can happen if the layout relax thread is changing locations while the
      // visualization is rendering
      return;
    }

    try {
      Network<V, E> network = visualizationModel.getNetwork();
      Set<E> visibleEdges = Sets.newHashSet(network.edges());
      for (E edge : network.edges()) {
        EndpointPair<V> endpoints = network.incidentNodes(edge);
        boolean keep = false;
        for (V v : endpoints) {
          keep |= visibleNodes.contains(v);
        }
        if (!keep) {
          visibleEdges.remove(edge);
        }
      }
      // paint all the edges
      for (E e : visibleEdges) {
        renderEdge(renderContext, visualizationModel, e);
        renderEdgeLabel(renderContext, visualizationModel, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : visibleNodes) {
        renderVertex(renderContext, visualizationModel, v);
        renderVertexLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  @Override
  public void render(
      RenderContext<V, E> renderContext, VisualizationModel<V, E, Point2D> visualizationModel) {
    Network<V, E> network = visualizationModel.getNetwork();
    // paint all the edges
    try {
      for (E e : network.edges()) {
        renderEdge(renderContext, visualizationModel, e);
        renderEdgeLabel(renderContext, visualizationModel, e);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }

    // paint all the vertices
    try {
      for (V v : network.nodes()) {
        renderVertex(renderContext, visualizationModel, v);
        renderVertexLabel(renderContext, visualizationModel, v);
      }
    } catch (ConcurrentModificationException cme) {
      renderContext.getScreenDevice().repaint();
    }
  }

  public void renderVertex(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      V v) {
    vertexRenderer.paintVertex(renderContext, visualizationModel, v);
  }

  public void renderVertexLabel(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      V v) {
    vertexLabelRenderer.labelVertex(
        renderContext, visualizationModel, v, renderContext.getVertexLabelTransformer().apply(v));
  }

  public void renderEdge(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      E e) {
    edgeRenderer.paintEdge(renderContext, visualizationModel, e);
  }

  public void renderEdgeLabel(
      RenderContext<V, E> renderContext,
      VisualizationModel<V, E, Point2D> visualizationModel,
      E e) {
    edgeLabelRenderer.labelEdge(
        renderContext, visualizationModel, e, renderContext.getEdgeLabelTransformer().apply(e));
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
