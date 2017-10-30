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
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import java.awt.*;
import javax.swing.*;

/**
 * The interface for drawing vertices, edges, and their labels. Implementations of this class can
 * set specific renderers for each element, allowing custom control of each.
 */
public interface Renderer<V, E> {

  void render(RenderContext<V, E> rc, LayoutMediator<V, E> layoutMediator, Spatial<V> spatial);

  void render(RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator);

  void renderVertex(RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, V v);

  void renderVertexLabel(
      RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, V v);

  void renderEdge(RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, E e);

  void renderEdgeLabel(RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, E e);

  void setVertexRenderer(Renderer.Vertex<V, E> r);

  void setEdgeRenderer(Renderer.Edge<V, E> r);

  void setVertexLabelRenderer(Renderer.VertexLabel<V, E> r);

  void setEdgeLabelRenderer(Renderer.EdgeLabel<V, E> r);

  Renderer.VertexLabel<V, E> getVertexLabelRenderer();

  Renderer.Vertex<V, E> getVertexRenderer();

  Renderer.Edge<V, E> getEdgeRenderer();

  Renderer.EdgeLabel<V, E> getEdgeLabelRenderer();

  interface Vertex<V, E> {
    void paintVertex(RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, V v);

    @SuppressWarnings("rawtypes")
    class NOOP<V, E> implements Vertex<V, E> {
      public void paintVertex(
          RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, V v) {}
    };
  }

  interface Edge<V, E> {
    void paintEdge(RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, E e);

    EdgeArrowRenderingSupport<V, E> getEdgeArrowRenderingSupport();

    void setEdgeArrowRenderingSupport(EdgeArrowRenderingSupport<V, E> edgeArrowRenderingSupport);

    @SuppressWarnings("rawtypes")
    class NOOP<V, E> implements Edge<V, E> {
      public void paintEdge(
          RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, E e) {}

      public EdgeArrowRenderingSupport getEdgeArrowRenderingSupport() {
        return null;
      }

      public void setEdgeArrowRenderingSupport(
          EdgeArrowRenderingSupport edgeArrowRenderingSupport) {}
    }
  }

  interface VertexLabel<V, E> {
    void labelVertex(
        RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, V v, String label);

    Position getPosition();

    void setPosition(Position position);

    void setPositioner(Positioner positioner);

    Positioner getPositioner();

    @SuppressWarnings("rawtypes")
    class NOOP<V, E> implements VertexLabel<V, E> {
      public void labelVertex(
          RenderContext<V, E> renderContext,
          LayoutMediator<V, E> layoutMediator,
          V v,
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

  interface EdgeLabel<V, E> {
    void labelEdge(
        RenderContext<V, E> renderContext, LayoutMediator<V, E> layoutMediator, E e, String label);

    @SuppressWarnings("rawtypes")
    class NOOP<V, E> implements EdgeLabel<V, E> {
      public void labelEdge(
          RenderContext<V, E> renderContext,
          LayoutMediator<V, E> layoutMediator,
          E e,
          String label) {}
    }
  }
}
