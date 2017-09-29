/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on March 10, 2005
 */
package edu.uci.ics.jung.visualization.decorators;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.visualization.util.ArrowFactory;
import edu.uci.ics.jung.visualization.util.Context;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.function.Function;

/**
 * An interface for decorators that return a <code>Shape</code> for a specified edge.
 *
 * <p>All edge shapes must be defined so that their endpoints are at (0,0) and (1,0). They will be
 * scaled, rotated and translated into position by the PluggableRenderer.
 *
 * @author Tom Nelson
 */
public class EdgeShape {
  private static final Line2D LINE = new Line2D.Float(0.0f, 0.0f, 1.0f, 0.0f);
  private static final GeneralPath BENT_LINE = new GeneralPath();
  private static final QuadCurve2D QUAD_CURVE = new QuadCurve2D.Float();
  private static final CubicCurve2D CUBIC_CURVE = new CubicCurve2D.Float();
  private static final Ellipse2D ELLIPSE = new Ellipse2D.Float(-.5f, -.5f, 1, 1);
  private static final Rectangle2D BOX = new Rectangle2D.Float();
  private static final GeneralPath BOW_TIE = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

  private static GeneralPath triangle;

  /**
   * A convenience instance for other edge shapes to use for self-loop edges where parallel
   * instances will not overlay each other.
   */
  protected static final Loop loop = new Loop();

  private static boolean isLoop(Network graph, Object edge) {
    EndpointPair<?> endpoints = graph.incidentNodes(edge);
    checkNotNull(endpoints);
    return endpoints.nodeU().equals(endpoints.nodeV());
  }

  public static Line line() {
    return new Line();
  }

  public static QuadCurve quadCurve() {
    return new QuadCurve();
  }

  public static CubicCurve cubicCurve() {
    return new CubicCurve();
  }

  public static Orthogonal orthogonal() {
    return new Orthogonal();
  }

  public static Wedge wedge(int width) {
    return new Wedge(width);
  }

  /** An edge shape that renders as a straight line between the vertex endpoints. */
  public static class Line extends EdgeShape implements Function<Context<Network, Object>, Shape> {
    public Line() {
      //      super(graph);
      // TODO: decide whether this check is reasonable
      //    		Preconditions.checkArgument(!graph.allowsParallelEdges(),
      //    				"Line does not support graphs that allow parallel edges");
    }

    public Shape apply(Context<Network, Object> context) {
      Network graph = context.graph;
      Object e = context.element;
      return isLoop(graph, e) ? ELLIPSE : LINE;
    }
  }

  private static int getIndex(Object e, EdgeIndexFunction edgeIndexFunction) {
    return edgeIndexFunction == null ? 1 : edgeIndexFunction.getIndex(e);
  }

  /** An edge shape that renders as a bent-line between the vertex endpoints. */
  public static class BentLine extends ParallelEdgeShapeTransformer {
    public void setEdgeIndexFunction(EdgeIndexFunction edgeIndexFunction) {
      this.edgeIndexFunction = edgeIndexFunction;
      loop.setEdgeIndexFunction(edgeIndexFunction);
    }

    /**
     * Get the shape for this edge, returning either the shared instance or, in the case of
     * self-loop edges, the Loop shared instance.
     */
    public Shape apply(Context<Network, Object> context) {
      Network graph = context.graph;
      Object e = context.element;
      if (isLoop(graph, e)) {
        return loop.apply(context);
      }

      int index = getIndex(e, edgeIndexFunction);
      float controlY = control_offset_increment + control_offset_increment * index;
      BENT_LINE.reset();
      BENT_LINE.moveTo(0.0f, 0.0f);
      BENT_LINE.lineTo(0.5f, controlY);
      BENT_LINE.lineTo(1.0f, 1.0f);
      return BENT_LINE;
    }
  }

  /** An edge shape that renders as a QuadCurve between vertex endpoints. */
  public static class QuadCurve extends ParallelEdgeShapeTransformer {
    @Override
    public void setEdgeIndexFunction(EdgeIndexFunction parallelEdgeIndexFunction) {
      this.edgeIndexFunction = parallelEdgeIndexFunction;
      loop.setEdgeIndexFunction(parallelEdgeIndexFunction);
    }

    /**
     * Get the shape for this edge, returning either the shared instance or, in the case of
     * self-loop edges, the Loop shared instance.
     */
    public Shape apply(Context<Network, Object> context) {
      Network graph = context.graph;
      Object e = context.element;
      if (isLoop(graph, e)) {
        return loop.apply(context);
      }

      int index = getIndex(e, edgeIndexFunction);

      float controlY = control_offset_increment + control_offset_increment * index;
      QUAD_CURVE.setCurve(0.0f, 0.0f, 0.5f, controlY, 1.0f, 0.0f);
      return QUAD_CURVE;
    }
  }

  /**
   * An edge shape that renders as a CubicCurve between vertex endpoints. The two control points are
   * at (1/3*length, 2*controlY) and (2/3*length, controlY) giving a 'spiral' effect.
   */
  public static class CubicCurve extends ParallelEdgeShapeTransformer {
    public void setEdgeIndexFunction(EdgeIndexFunction edgeIndexFunction) {
      this.edgeIndexFunction = edgeIndexFunction;
      loop.setEdgeIndexFunction(edgeIndexFunction);
    }

    /**
     * Get the shape for this edge, returning either the shared instance or, in the case of
     * self-loop edges, the Loop shared instance.
     */
    public Shape apply(Context<Network, Object> context) {
      Network graph = context.graph;
      Object e = context.element;
      if (isLoop(graph, e)) {
        return loop.apply(context);
      }

      int index = getIndex(e, edgeIndexFunction);

      float controlY = control_offset_increment + control_offset_increment * index;
      CUBIC_CURVE.setCurve(0.0f, 0.0f, 0.33f, 2 * controlY, .66f, -controlY, 1.0f, 0.0f);
      return CUBIC_CURVE;
    }
  }

  /**
   * An edge shape that renders as a loop with its nadir at the center of the vertex. Parallel
   * instances will overlap.
   *
   * @author Tom Nelson
   */
  public static class SimpleLoop implements Function<Object, Shape> {
    public Shape apply(Object e) {
      return ELLIPSE;
    }
  }

  private static Shape buildFrame(RectangularShape shape, int index) {
    float x = -.5f;
    float y = -.5f;
    float diam = 1.f;
    diam += diam * index / 2;
    x += x * index / 2;
    y += y * index / 2;

    shape.setFrame(x, y, diam, diam);

    return shape;
  }

  /**
   * An edge shape that renders as a loop with its nadir at the center of the vertex. Parallel
   * instances will not overlap.
   */
  public static class Loop extends ParallelEdgeShapeTransformer {
    public Shape apply(Context<Network, Object> context) {
      Network graph = context.graph;
      Object e = context.element;
      return buildFrame(ELLIPSE, getIndex(e, edgeIndexFunction));
    }
  }

  /**
   * An edge shape that renders as an isosceles triangle whose apex is at the destination vertex for
   * directed edges, and as a "bowtie" shape for undirected edges.
   *
   * @author Joshua O'Madadhain
   */
  public static class Wedge extends ParallelEdgeShapeTransformer {
    public Wedge(int width) {
      triangle = ArrowFactory.getWedgeArrow(width, 1);
      triangle.transform(AffineTransform.getTranslateInstance(1, 0));
      BOW_TIE.moveTo(0, width / 2);
      BOW_TIE.lineTo(1, -width / 2);
      BOW_TIE.lineTo(1, width / 2);
      BOW_TIE.lineTo(0, -width / 2);
      BOW_TIE.closePath();
    }

    public Shape apply(Context<Network, Object> context) {
      Network graph = context.graph;
      Object e = context.element;
      if (isLoop(graph, e)) {
        return loop.apply(context);
      }
      return graph.isDirected() ? triangle : BOW_TIE;
    }
  }

  /**
   * An edge shape that renders as a diamond with its nadir at the center of the vertex. Parallel
   * instances will not overlap.
   */
  public static class Box extends ParallelEdgeShapeTransformer {
    public Shape apply(Context<Network, Object> context) {
      Network graph = context.graph;
      Object e = context.element;
      return buildFrame(BOX, getIndex(e, edgeIndexFunction));
    }
  }

  /** An edge shape that renders as a bent-line between the vertex endpoints. */
  public static class Orthogonal extends ParallelEdgeShapeTransformer {
    Box box;

    public Orthogonal() {
      this.box = new Box();
    }

    public Shape apply(Context<Network, Object> context) {
      Network graph = context.graph;
      Object e = context.element;
      return isLoop(graph, e) ? box.apply(context) : LINE;
    }
  }
}
