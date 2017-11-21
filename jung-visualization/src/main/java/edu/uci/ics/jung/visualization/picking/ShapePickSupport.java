/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 11, 2005
 *
 */
package edu.uci.ics.jung.visualization.picking;

import com.google.common.collect.Sets;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.layout.SpatialQuadTreeLayoutModel;
import edu.uci.ics.jung.visualization.spatial.SpatialQuadTree;
import edu.uci.ics.jung.visualization.util.Context;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>NetworkElementAccessor</code> that returns elements whose <code>Shape</code> contains the
 * specified pick point or region.
 *
 * @author Tom Nelson
 */
public class ShapePickSupport<V, E> implements NetworkElementAccessor<V, E> {

  private static final Logger log = LoggerFactory.getLogger(ShapePickSupport.class);
  /**
   * The available picking heuristics:
   *
   * <ul>
   *   <li><code>Style.CENTERED</code>: returns the element whose center is closest to the pick
   *       point.
   *   <li><code>Style.LOWEST</code>: returns the first such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the bottom", that
   *       is, the one which is rendered first.)
   *   <li><code>Style.HIGHEST</code>: returns the last such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the top", that
   *       is, the one which is rendered last.)
   * </ul>
   */
  public static enum Style {
    LOWEST,
    CENTERED,
    HIGHEST
  };

  protected float pickSize;

  /**
   * The <code>VisualizationServer</code> in which the this instance is being used for picking. Used
   * to retrieve properties such as the layout, renderer, vertex and edge shapes, and coordinate
   * transformations.
   */
  protected VisualizationServer<V, E> vv;

  /** The current picking heuristic for this instance. Defaults to <code>CENTERED</code>. */
  protected Style style = Style.CENTERED;

  /**
   * Creates a <code>ShapePickSupport</code> for the <code>vv</code> VisualizationServer, with the
   * specified pick footprint and the default pick style. The <code>VisualizationServer</code> is
   * used to access properties of the current visualization (layout, renderer, coordinate
   * transformations, vertex/edge shapes, etc.).
   *
   * @param vv source of the current <code>Layout</code>.
   * @param pickSize the layoutSize of the pick footprint for line edges
   */
  public ShapePickSupport(VisualizationServer<V, E> vv, float pickSize) {
    this.vv = vv;
    this.pickSize = pickSize;
  }

  /**
   * Create a <code>ShapePickSupport</code> for the specified <code>VisualizationServer</code> with
   * a default pick footprint. of layoutSize 2.
   *
   * @param vv the visualization server used for rendering
   */
  public ShapePickSupport(VisualizationServer<V, E> vv) {
    this.vv = vv;
    this.pickSize = 2;
  }

  /**
   * Returns the style of picking used by this instance. This specifies which of the elements, among
   * those whose shapes contain the pick point, is returned. The available styles are:
   *
   * <ul>
   *   <li><code>Style.CENTERED</code>: returns the element whose center is closest to the pick
   *       point.
   *   <li><code>Style.LOWEST</code>: returns the first such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the bottom", that
   *       is, the one which is rendered first.)
   *   <li><code>Style.HIGHEST</code>: returns the last such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the top", that
   *       is, the one which is rendered last.)
   * </ul>
   *
   * @return the style of picking used by this instance
   */
  public Style getStyle() {
    return style;
  }

  /**
   * Specifies the style of picking to be used by this instance. This specifies which of the
   * elements, among those whose shapes contain the pick point, will be returned. The available
   * styles are:
   *
   * <ul>
   *   <li><code>Style.CENTERED</code>: returns the element whose center is closest to the pick
   *       point.
   *   <li><code>Style.LOWEST</code>: returns the first such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the bottom", that
   *       is, the one which is rendered first.)
   *   <li><code>Style.HIGHEST</code>: returns the last such element encountered. (If the element
   *       collection has a consistent ordering, this will also be the element "on the top", that
   *       is, the one which is rendered last.)
   * </ul>
   *
   * @param style the style to set
   */
  public void setStyle(Style style) {
    this.style = style;
  }

  /**
   * Returns the vertex, if any, whose shape contains (x, y). If (x,y) is contained in more than one
   * vertex's shape, returns the vertex whose center is closest to the pick point.
   *
   * @param x the x coordinate of the pick point
   * @param y the y coordinate of the pick point
   * @return the vertex whose shape contains (x,y), and whose center is closest to the pick point
   */
  @Override
  public V getNode(LayoutModel<V, Point2D> layoutModel, double x, double y) {

    V closest = null;
    double minDistance = Double.MAX_VALUE;
    Point2D ip =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(Layer.VIEW, new Point2D.Double(x, y));
    x = ip.getX();
    y = ip.getY();
    //    LayoutModel<V, Point2D> layoutModel = vv.getModel().getLayoutModel();
    if (layoutModel instanceof SpatialQuadTreeLayoutModel) {
      SpatialQuadTree<V> tree =
          (SpatialQuadTree<V>) ((SpatialQuadTreeLayoutModel) layoutModel).getSpatial();
      return getClosest(tree, layoutModel, x, y);
    }

    while (true) {
      try {
        for (V v : getFilteredVertices()) {

          Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(v);
          // get the vertex location
          Point2D p = layoutModel.apply(v);
          if (p == null) {
            continue;
          }
          // transform the vertex location to screen coords
          p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);

          double ox = x - p.getX();
          double oy = y - p.getY();

          if (shape.contains(ox, oy)) {

            if (style == Style.LOWEST) {
              // return the first match
              return v;
            } else if (style == Style.HIGHEST) {
              // will return the last match
              closest = v;
            } else {

              // return the vertex closest to the
              // center of a vertex shape
              Rectangle2D bounds = shape.getBounds2D();
              double dx = bounds.getCenterX() - ox;
              double dy = bounds.getCenterY() - oy;
              double dist = dx * dx + dy * dy;
              if (dist < minDistance) {
                minDistance = dist;
                closest = v;
              }
            }
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }

  protected V getClosest(
      SpatialQuadTree<V> spatial, LayoutModel<V, Point2D> layoutModel, double x, double y) {
    SpatialQuadTree<V> leaf = spatial.getContainingQuadTreeLeaf(x, y);
    double diameter = leaf.getLayoutArea().getWidth();
    double radius = diameter / 2;
    double minDistance = Double.MAX_VALUE;

    V closest = null;
    Ellipse2D target = new Ellipse2D.Double(x - radius, y - radius, diameter, diameter);
    Collection<V> nodes = spatial.getVisibleNodes(target);
    if (log.isTraceEnabled()) {
      log.trace("instead of checking all nodes: {}", getFilteredVertices());
      log.trace("out of these candidates: {}...", nodes);
    }
    for (V node : nodes) {
      Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(node);
      // get the vertex location
      Point2D p = layoutModel.apply(node);
      if (p == null) {
        continue;
      }
      // transform the vertex location to screen coords
      p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);

      double ox = x - p.getX();
      double oy = y - p.getY();

      if (shape.contains(ox, oy)) {

        if (style == Style.LOWEST) {
          // return the first match
          return node;
        } else if (style == Style.HIGHEST) {
          // will return the last match
          closest = node;
        } else {

          // return the vertex closest to the
          // center of a vertex shape
          Rectangle2D bounds = shape.getBounds2D();
          double dx = bounds.getCenterX() - ox;
          double dy = bounds.getCenterY() - oy;
          double dist = dx * dx + dy * dy;
          if (dist < minDistance) {
            minDistance = dist;
            closest = node;
          }
        }
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("picked {} with spatial quadtree", closest);
    }
    return closest;
  }
  /**
   * Returns the vertices whose layout coordinates are contained in <code>Shape</code>. The shape is
   * in screen coordinates, and the graph vertices are transformed to screen coordinates before they
   * are tested for inclusion.
   *
   * @return the <code>Collection</code> of vertices whose <code>layout</code> coordinates are
   *     contained in <code>shape</code>.
   */
  @Override
  public Collection<V> getNodes(LayoutModel<V, Point2D> layoutModel, Shape shape) {
    Set<V> pickedVertices = new HashSet<V>();

    // remove the view transform from the rectangle
    shape = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, shape);

    if (layoutModel instanceof SpatialQuadTreeLayoutModel) {
      SpatialQuadTree<V> spatial =
          (SpatialQuadTree) ((SpatialQuadTreeLayoutModel) layoutModel).getSpatial();
      return getContained(spatial, layoutModel, shape);
    }

    while (true) {
      try {
        for (V v : getFilteredVertices()) {
          Point2D p = layoutModel.apply(v);
          if (p == null) {
            continue;
          }

          p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);
          if (shape.contains(p)) {
            pickedVertices.add(v);
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return pickedVertices;
  }

  protected Collection<V> getContained(
      SpatialQuadTree<V> spatial, LayoutModel<V, Point2D> layoutModel, Shape shape) {
    Collection<V> visible = spatial.getVisibleNodes(shape);
    if (log.isTraceEnabled()) {
      log.trace("your shape intersects tree cells with these nodes: {}", visible);
    }

    for (Iterator<V> iterator = visible.iterator(); iterator.hasNext(); ) {
      V node = iterator.next();
      Point2D p = layoutModel.apply(node);
      if (p == null) {
        continue;
      }
      p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);
      if (!shape.contains(p)) {
        iterator.remove();
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("these were actually picked: {}", visible);
    }
    return visible;
  }

  /**
   * Returns an edge whose shape intersects the 'pickArea' footprint of the passed x,y, coordinates.
   *
   * @param x the x coordinate of the location
   * @param y the y coordinate of the location
   * @return an edge whose shape intersects the pick area centered on the location {@code (x,y)}
   */
  @Override
  public E getEdge(LayoutModel<V, Point2D> layoutModel, double x, double y) {

    Point2D ip =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .inverseTransform(Layer.VIEW, new Point2D.Double(x, y));
    x = ip.getX();
    y = ip.getY();

    // as a Line has no area, we can't always use edgeshape.contains(point) so we
    // make a small rectangular pickArea around the point and check if the
    // edgeshape.intersects(pickArea)
    Rectangle2D pickArea =
        new Rectangle2D.Float(
            (float) x - pickSize / 2, (float) y - pickSize / 2, pickSize, pickSize);
    E closest = null;
    double minDistance = Double.MAX_VALUE;
    while (true) {
      try {
        for (E e : getFilteredEdges()) {

          Shape edgeShape = getTransformedEdgeShape(e);
          if (edgeShape == null) {
            continue;
          }

          // because of the transform, the edgeShape is now a GeneralPath
          // see if this edge is the closest of any that intersect
          if (edgeShape.intersects(pickArea)) {
            float cx = 0;
            float cy = 0;
            float[] f = new float[6];
            PathIterator pi = new GeneralPath(edgeShape).getPathIterator(null);
            if (pi.isDone() == false) {
              pi.next();
              pi.currentSegment(f);
              cx = f[0];
              cy = f[1];
              if (pi.isDone() == false) {
                pi.currentSegment(f);
                cx = f[0];
                cy = f[1];
              }
            }
            float dx = (float) (cx - x);
            float dy = (float) (cy - y);
            float dist = dx * dx + dy * dy;
            if (dist < minDistance) {
              minDistance = dist;
              closest = e;
            }
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return closest;
  }

  /**
   * Retrieves the shape template for <code>e</code> and transforms it according to the positions of
   * its endpoints in <code>layout</code>.
   *
   * @param e the edge whose shape is to be returned
   * @return the transformed shape
   */
  private Shape getTransformedEdgeShape(E e) {
    EndpointPair<V> endpoints = vv.getModel().getNetwork().incidentNodes(e);
    V v1 = endpoints.nodeU();
    V v2 = endpoints.nodeV();
    boolean isLoop = v1.equals(v2);
    LayoutModel<V, Point2D> layoutModel = vv.getModel().getLayoutModel();
    Point2D p1 =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, layoutModel.apply(v1));
    Point2D p2 =
        vv.getRenderContext()
            .getMultiLayerTransformer()
            .transform(Layer.LAYOUT, layoutModel.apply(v2));
    if (p1 == null || p2 == null) {
      return null;
    }
    float x1 = (float) p1.getX();
    float y1 = (float) p1.getY();
    float x2 = (float) p2.getX();
    float y2 = (float) p2.getY();

    // translate the edge to the starting vertex
    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

    Shape edgeShape =
        vv.getRenderContext()
            .getEdgeShapeTransformer()
            .apply(Context.getInstance(vv.getModel().getNetwork(), e));
    if (isLoop) {
      // make the loops proportional to the layoutSize of the vertex
      Shape s2 = vv.getRenderContext().getVertexShapeTransformer().apply(v2);
      Rectangle2D s2Bounds = s2.getBounds2D();
      xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
      // move the loop so that the nadir is centered in the vertex
      xform.translate(0, -edgeShape.getBounds2D().getHeight() / 2);
    } else {
      float dx = x2 - x1;
      float dy = y2 - y1;
      // rotate the edge to the angle between the vertices
      double theta = Math.atan2(dy, dx);
      xform.rotate(theta);
      // stretch the edge to span the distance between the vertices
      float dist = (float) Math.sqrt(dx * dx + dy * dy);
      xform.scale(dist, 1.0f);
    }

    // transform the edge to its location and dimensions
    edgeShape = xform.createTransformedShape(edgeShape);
    return edgeShape;
  }

  protected Collection<V> getFilteredVertices() {
    Set<V> nodes = vv.getModel().getNetwork().nodes();
    return verticesAreFiltered()
        ? Sets.filter(nodes, vv.getRenderContext().getVertexIncludePredicate()::test)
        : nodes;
  }

  protected Collection<E> getFilteredEdges() {
    Set<E> edges = vv.getModel().getNetwork().edges();
    return edgesAreFiltered()
        ? Sets.filter(edges, vv.getRenderContext().getEdgeIncludePredicate()::test)
        : edges;
  }

  /**
   * Quick test to allow optimization of <code>getFilteredVertices()</code>.
   *
   * @return <code>true</code> if there is an active vertex filtering mechanism for this
   *     visualization, <code>false</code> otherwise
   */
  protected boolean verticesAreFiltered() {
    Predicate<V> vertexIncludePredicate = vv.getRenderContext().getVertexIncludePredicate();
    return vertexIncludePredicate != null
        && !vertexIncludePredicate.equals((Predicate<V>) (n -> true));
  }

  /**
   * Quick test to allow optimization of <code>getFilteredEdges()</code>.
   *
   * @return <code>true</code> if there is an active edge filtering mechanism for this
   *     visualization, <code>false</code> otherwise
   */
  protected boolean edgesAreFiltered() {
    Predicate<E> edgeIncludePredicate = vv.getRenderContext().getEdgeIncludePredicate();
    return edgeIncludePredicate != null && !edgeIncludePredicate.equals((Predicate<V>) (n -> true));
  }

  /**
   * Returns <code>true</code> if this vertex in this graph is included in the collections of
   * elements to be rendered, and <code>false</code> otherwise.
   *
   * @return <code>true</code> if this vertex is included in the collections of elements to be
   *     rendered, <code>false</code> otherwise.
   */
  protected boolean isVertexRendered(V vertex) {
    Predicate<V> vertexIncludePredicate = vv.getRenderContext().getVertexIncludePredicate();
    return vertexIncludePredicate == null || vertexIncludePredicate.test(vertex);
  }

  /**
   * Returns <code>true</code> if this edge and its endpoints in this graph are all included in the
   * collections of elements to be rendered, and <code>false</code> otherwise.
   *
   * @return <code>true</code> if this edge and its endpoints are all included in the collections of
   *     elements to be rendered, <code>false</code> otherwise.
   */
  protected boolean isEdgeRendered(E edge) {
    Predicate<V> vertexIncludePredicate = vv.getRenderContext().getVertexIncludePredicate();
    Predicate<E> edgeIncludePredicate = vv.getRenderContext().getEdgeIncludePredicate();
    Network<V, E> g = vv.getModel().getNetwork();
    if (edgeIncludePredicate != null && !edgeIncludePredicate.test(edge)) {
      return false;
    }
    EndpointPair<V> endpoints = g.incidentNodes(edge);
    V v1 = endpoints.nodeU();
    V v2 = endpoints.nodeV();
    return vertexIncludePredicate == null
        || (vertexIncludePredicate.test(v1) && vertexIncludePredicate.test(v2));
  }

  /**
   * Returns the layoutSize of the edge picking area. The picking area is square; the layoutSize is
   * specified as the length of one side, in view coordinates.
   *
   * @return the layoutSize of the edge picking area
   */
  public float getPickSize() {
    return pickSize;
  }

  /**
   * Sets the layoutSize of the edge picking area.
   *
   * @param pickSize the length of one side of the (square) picking area, in view coordinates
   */
  public void setPickSize(float pickSize) {
    this.pickSize = pickSize;
  }
}
