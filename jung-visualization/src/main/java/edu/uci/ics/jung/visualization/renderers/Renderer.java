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
import java.awt.Dimension;

/**
 * The interface for drawing vertices, edges, and their labels. Implementations of this class can
 * set specific renderers for each element, allowing custom control of each.
 */
public interface Renderer {

  void render(RenderContext rc, LayoutMediator layoutMediator, Spatial spatial);

  void render(RenderContext renderContext, LayoutMediator layoutMediator);

  void renderVertex(RenderContext renderContext, LayoutMediator layoutMediator, Object v);

  void renderVertexLabel(RenderContext renderContext, LayoutMediator layoutMediator, Object v);

  void renderEdge(RenderContext renderContext, LayoutMediator layoutMediator, Object e);

  void renderEdgeLabel(RenderContext renderContext, LayoutMediator layoutMediator, Object e);

  void setVertexRenderer(Renderer.Vertex r);

  void setEdgeRenderer(Renderer.Edge r);

  void setVertexLabelRenderer(Renderer.VertexLabel r);

  void setEdgeLabelRenderer(Renderer.EdgeLabel r);

  Renderer.VertexLabel getVertexLabelRenderer();

  Renderer.Vertex getVertexRenderer();

  Renderer.Edge getEdgeRenderer();

  Renderer.EdgeLabel getEdgeLabelRenderer();

  interface Vertex {
    void paintVertex(RenderContext renderContext, LayoutMediator layoutMediator, Object v);

    @SuppressWarnings("rawtypes")
    class NOOP implements Vertex {
      public void paintVertex(
          RenderContext renderContext, LayoutMediator layoutMediator, Object v) {}
    };
  }

  interface Edge {
    void paintEdge(RenderContext renderContext, LayoutMediator layoutMediator, Object e);

    EdgeArrowRenderingSupport getEdgeArrowRenderingSupport();

    void setEdgeArrowRenderingSupport(EdgeArrowRenderingSupport edgeArrowRenderingSupport);

    @SuppressWarnings("rawtypes")
    class NOOP implements Edge {
      public void paintEdge(RenderContext renderContext, LayoutMediator layoutMediator, Object e) {}

      public EdgeArrowRenderingSupport getEdgeArrowRenderingSupport() {
        return null;
      }

      public void setEdgeArrowRenderingSupport(
          EdgeArrowRenderingSupport edgeArrowRenderingSupport) {}
    }
  }

  interface VertexLabel {
    void labelVertex(
        RenderContext renderContext, LayoutMediator layoutMediator, Object v, String label);

    Position getPosition();

    void setPosition(Position position);

    void setPositioner(Positioner positioner);

    Positioner getPositioner();

    @SuppressWarnings("rawtypes")
    class NOOP implements VertexLabel {
      public void labelVertex(
          RenderContext renderContext, LayoutMediator layoutMediator, Object v, String label) {}

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

  interface EdgeLabel {
    void labelEdge(
        RenderContext renderContext, LayoutMediator layoutMediator, Object e, String label);

    @SuppressWarnings("rawtypes")
    class NOOP implements EdgeLabel {
      public void labelEdge(
          RenderContext renderContext, LayoutMediator layoutMediator, Object e, String label) {}
    }
  }
}
