/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * <p>All rights reserved.
 *
 * <p>This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Apr 24, 2008
 */
package edu.uci.ics.jung.visualization.picking;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;

/**
 * A <code>NetworkElementAccessor</code> that finds the closest element to the pick point, and
 * returns it if it is within the element's shape. This is best suited to elements with convex
 * shapes that do not overlap. It differs from <code>ShapePickSupport</code> in that it only checks
 * the closest element to see whether it contains the pick point. Possible unexpected odd behaviors:
 *
 * <ul>
 *   <li>If the elements overlap, this mechanism may pick another element than the one that's "on
 *       top" (rendered last) if the pick point is closer to the center of an obscured vertex.
 *   <li>If element shapes are not convex, then this mechanism may return <code>null</code> even if
 *       the pick point is inside some element's shape, if the pick point is closer to the center of
 *       another element.
 * </ul>
 *
 * Users who want to avoid either of these should use <code>ShapePickSupport</code> instead, which
 * is slower but more flexible. If neither of the above conditions (overlapping elements or
 * non-convex shapes) is true, then <code>ShapePickSupport</code> and this class should have the
 * same behavior.
 */
public class ClosestShapePickSupport<V, E> implements NetworkElementAccessor<V, E> {

  protected VisualizationServer<V, E> vv;
  protected float pickSize;

  /**
   * Creates a <code>ShapePickSupport</code> for the <code>vv</code> VisualizationServer, with the
   * specified pick footprint. The <code>VisualizationServer</code> is used to fetch the current
   * <code>Layout</code>.
   *
   * @param vv source of the current <code>Layout</code>.
   * @param pickSize the size of the pick footprint for line edges
   */
  public ClosestShapePickSupport(VisualizationServer<V, E> vv, float pickSize) {
    this.vv = vv;
    this.pickSize = pickSize;
  }

  /**
   * Create a <code>ShapePickSupport</code> with the <code>vv</code> VisualizationServer and default
   * pick footprint. The footprint defaults to 2.
   *
   * @param vv source of the current <code>Layout</code>.
   */
  public ClosestShapePickSupport(VisualizationServer<V, E> vv) {
    this.vv = vv;
  }

  /** @see edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor#getEdge(double, double) */
  public E getEdge(double x, double y) {
    return null;
  }

  /** @see edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor#getNode(double, double) */
  @Override
  public V getNode(double x, double y) {
    Layout<V> layout = vv.getGraphLayout();
    // first, find the closest vertex to (x,y)
    double minDistance = Double.MAX_VALUE;
    V closest = null;
    while (true) {
      try {
        for (V v : vv.getModel().getNetwork().nodes()) {
          Point2D p = layout.apply(v);
          double dx = p.getX() - x;
          double dy = p.getY() - y;
          double dist = dx * dx + dy * dy;
          if (dist < minDistance) {
            minDistance = dist;
            closest = v;
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }

    // now check to see whether (x,y) is in the shape for this vertex.

    // get the vertex shape
    Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(closest);
    // get the vertex location
    Point2D p = layout.apply(closest);
    // transform the vertex location to screen coords
    p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);

    double ox = x - p.getX();
    double oy = y - p.getY();

    if (shape.contains(ox, oy)) {
      return closest;
    } else {
      return null;
    }
  }

  /** @see edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor#getNodes(java.awt.Shape) */
  @Override
  public Collection<V> getNodes(Shape rectangle) {
    // FIXME: RadiusPickSupport and ShapePickSupport are not using the same mechanism!
    // talk to Tom and make sure I understand which should be used.
    // in particular, there are some transformations that the latter uses; the latter is also
    // doing a couple of kinds of filtering.  (well, only one--just predicate-based.)
    // looks to me like the VV could (should) be doing this filtering.  (maybe.)
    //
    return null;
  }
}
