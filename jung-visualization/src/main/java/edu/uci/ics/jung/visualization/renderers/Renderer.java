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
import java.awt.geom.Point2D;

/**
 * The interface for drawing vertices, edges, and their labels. Implementations of this class can
 * set specific renderers for each element, allowing custom control of each.
 */
public interface Renderer<N, E> {

  void render(
      RenderContext<N, E> rc,
      VisualizationModel<N, E, Point2D> visualizationModel,
      Spatial<N> spatial);

  void render(
      RenderContext<N, E> renderContext, VisualizationModel<N, E, Point2D> visualizationModel);

  void renderVertex(
      RenderContext<N, E> renderContext, VisualizationModel<N, E, Point2D> visualizationModel, N v);

  void renderVertexLabel(
      RenderContext<N, E> renderContext, VisualizationModel<N, E, Point2D> visualizationModel, N v);

  void renderEdge(
      RenderContext<N, E> renderContext, VisualizationModel<N, E, Point2D> visualizationModel, E e);

  void renderEdgeLabel(
      RenderContext<N, E> renderContext, VisualizationModel<N, E, Point2D> visualizationModel, E e);

  void setVertexRenderer(Renderer.Vertex<N, E> r);

  void setEdgeRenderer(Renderer.Edge<N, E> r);

  void setVertexLabelRenderer(Renderer.VertexLabel<N, E> r);

  void setEdgeLabelRenderer(Renderer.EdgeLabel<N, E> r);

  Renderer.VertexLabel<N, E> getVertexLabelRenderer();

  Renderer.Vertex<N, E> getVertexRenderer();

  Renderer.Edge<N, E> getEdgeRenderer();

  Renderer.EdgeLabel<N, E> getEdgeLabelRenderer();

  interface Vertex<N, E> {
    void paintVertex(
        RenderContext<N, E> renderContext,
        VisualizationModel<N, E, Point2D> visualizationModel,
        N v);

    @SuppressWarnings("rawtypes")
    class NOOP<N, E> implements Vertex<N, E> {
      public void paintVertex(
          RenderContext<N, E> renderContext,
          VisualizationModel<N, E, Point2D> visualizationModel,
          N v) {}
    };
  }

  interface Edge<N, E> {
    void paintEdge(
        RenderContext<N, E> renderContext,
        VisualizationModel<N, E, Point2D> visualizationModel,
        E e);

    EdgeArrowRenderingSupport<N, E> getEdgeArrowRenderingSupport();

    void setEdgeArrowRenderingSupport(EdgeArrowRenderingSupport<N, E> edgeArrowRenderingSupport);

    @SuppressWarnings("rawtypes")
    class NOOP<N, E> implements Edge<N, E> {
      public void paintEdge(
          RenderContext<N, E> renderContext,
          VisualizationModel<N, E, Point2D> visualizationModel,
          E e) {}

      public EdgeArrowRenderingSupport getEdgeArrowRenderingSupport() {
        return null;
      }

      public void setEdgeArrowRenderingSupport(
          EdgeArrowRenderingSupport edgeArrowRenderingSupport) {}
    }
  }

  interface VertexLabel<N, E> {
    void labelVertex(
        RenderContext<N, E> renderContext,
        VisualizationModel<N, E, Point2D> visualizationModel,
        N v,
        String label);

    Position getPosition();

    void setPosition(Position position);

    void setPositioner(Positioner positioner);

    Positioner getPositioner();

    @SuppressWarnings("rawtypes")
    class NOOP<N, E> implements VertexLabel<N, E> {
      public void labelVertex(
          RenderContext<N, E> renderContext,
          VisualizationModel<N, E, Point2D> visualizationModel,
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
        VisualizationModel<N, E, Point2D> visualizationModel,
        E e,
        String label);

    @SuppressWarnings("rawtypes")
    class NOOP<N, E> implements EdgeLabel<N, E> {
      public void labelEdge(
          RenderContext<N, E> renderContext,
          VisualizationModel<N, E, Point2D> visualizationModel,
          E e,
          String label) {}
    }
  }
}
