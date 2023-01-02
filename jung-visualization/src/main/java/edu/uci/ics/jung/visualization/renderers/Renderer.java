/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import java.awt.Dimension;

/**
 * The interface for drawing nodes, edges, and their labels. Implementations of this class can set
 * specific renderers for each element, allowing custom control of each.
 */
public interface Renderer<N, E> {

  void render(
      RenderContext<N, E> rc,
      VisualizationModel<N, E> visualizationModel,
      Spatial<N> nodeSpatial,
      Spatial<E> edgeSpatial);

  void render(RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel);

  void renderNode(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v);

  void renderNodeLabel(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v);

  void renderEdge(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e);

  void renderEdgeLabel(
      RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e);

  void setNodeRenderer(Node<N, E> r);

  void setEdgeRenderer(Renderer.Edge<N, E> r);

  void setNodeLabelRenderer(NodeLabel<N, E> r);

  void setEdgeLabelRenderer(Renderer.EdgeLabel<N, E> r);

  NodeLabel<N, E> getNodeLabelRenderer();

  Node<N, E> getNodeRenderer();

  Renderer.Edge<N, E> getEdgeRenderer();

  Renderer.EdgeLabel<N, E> getEdgeLabelRenderer();

  interface Node<N, E> {
    void paintNode(
        RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v);

    @SuppressWarnings("rawtypes")
    class NOOP<N, E> implements Node<N, E> {
      public void paintNode(
          RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, N v) {}
    }
    ;
  }

  interface Edge<N, E> {
    void paintEdge(
        RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e);

    EdgeArrowRenderingSupport<N, E> getEdgeArrowRenderingSupport();

    void setEdgeArrowRenderingSupport(EdgeArrowRenderingSupport<N, E> edgeArrowRenderingSupport);

    @SuppressWarnings("rawtypes")
    class NOOP<N, E> implements Edge<N, E> {
      public void paintEdge(
          RenderContext<N, E> renderContext, VisualizationModel<N, E> visualizationModel, E e) {}

      public EdgeArrowRenderingSupport getEdgeArrowRenderingSupport() {
        return null;
      }

      public void setEdgeArrowRenderingSupport(
          EdgeArrowRenderingSupport edgeArrowRenderingSupport) {}
    }
  }

  interface NodeLabel<N, E> {
    void labelNode(
        RenderContext<N, E> renderContext,
        VisualizationModel<N, E> visualizationModel,
        N v,
        String label);

    Position getPosition();

    void setPosition(Position position);

    void setPositioner(Positioner positioner);

    Positioner getPositioner();

    @SuppressWarnings("rawtypes")
    class NOOP<N, E> implements NodeLabel<N, E> {
      public void labelNode(
          RenderContext<N, E> renderContext,
          VisualizationModel<N, E> visualizationModel,
          N v,
          String label) {}

      public Position getPosition() {
        return Position.CNTR;
      }

      public void setPosition(Position position) {}

      public Positioner getPositioner() {
        return new Positioner() {
          public Position getPosition(float x, float y, Dimension d) {
            return Position.CNTR;
          }
        };
      }

      public void setPositioner(Positioner positioner) {}
    }

    enum Position {
      N,
      NE,
      E,
      SE,
      S,
      SW,
      W,
      NW,
      CNTR,
      AUTO
    }

    interface Positioner {
      Position getPosition(float x, float y, Dimension d);
    }
  }

  interface EdgeLabel<N, E> {
    void labelEdge(
        RenderContext<N, E> renderContext,
        VisualizationModel<N, E> visualizationModel,
        E e,
        String label);

    @SuppressWarnings("rawtypes")
    class NOOP<N, E> implements EdgeLabel<N, E> {
      public void labelEdge(
          RenderContext<N, E> renderContext,
          VisualizationModel<N, E> visualizationModel,
          E e,
          String label) {}
    }
  }
}
