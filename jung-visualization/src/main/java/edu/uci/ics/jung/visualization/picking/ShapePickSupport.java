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
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.control.TransformSupport;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.SpatialRTree;
import edu.uci.ics.jung.visualization.spatial.TreeNode;
import edu.uci.ics.jung.visualization.spatial.rtree.LeafNode;
import edu.uci.ics.jung.visualization.spatial.rtree.Node;
import edu.uci.ics.jung.visualization.util.Context;
import java.awt.*;
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
public class ShapePickSupport<N, E> implements NetworkElementAccessor<N, E> {

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
   * to retrieve properties such as the layout, renderer, node and edge shapes, and coordinate
   * transformations.
   */
  protected VisualizationServer<N, E> vv;

  /** The current picking heuristic for this instance. Defaults to <code>CENTERED</code>. */
  protected Style style = Style.CENTERED;

  /**
   * Creates a <code>ShapePickSupport</code> for the <code>vv</code> VisualizationServer, with the
   * specified pick footprint and the default pick style. The <code>VisualizationServer</code> is
   * used to access properties of the current visualization (layout, renderer, coordinate
   * transformations, node/edge shapes, etc.).
   *
   * @param vv source of the current <code>Layout</code>.
   * @param pickSize the layoutSize of the pick footprint for line edges
   */
  public ShapePickSupport(VisualizationServer<N, E> vv, float pickSize) {
    this.vv = vv;
    this.pickSize = pickSize;
  }

  /**
   * Create a <code>ShapePickSupport</code> for the specified <code>VisualizationServer</code> with
   * a default pick footprint. of layoutSize 2.
   *
   * @param vv the visualization server used for rendering
   */
  public ShapePickSupport(VisualizationServer<N, E> vv) {
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

  @Override
  public N getNode(LayoutModel<N> layoutModel, Point p) {
    return getNode(layoutModel, p.x, p.y);
  }

  /**
   * Returns the node, if any, whose shape contains (x, y). If (x,y) is contained in more than one
   * node's shape, returns the node whose center is closest to the pick point.
   *
   * @param x the x coordinate of the pick point
   * @param y the y coordinate of the pick point
   * @return the node whose shape contains (x,y), and whose center is closest to the pick point
   */
  @Override
  public N getNode(LayoutModel<N> layoutModel, double x, double y) {
    log.trace("look for node at (layout coords) {},{}", x, y);
    TransformSupport<N, E> transformSupport = vv.getTransformSupport();
    N closest = null;
    double minDistance = Double.MAX_VALUE;
    // x,y is in layout coordinate system.
    Point2D pickPoint = new Point2D.Double(x, y);

    Spatial<N> nodeSpatial = vv.getNodeSpatial();
    if (nodeSpatial.isActive()) {
      return getNode(nodeSpatial, layoutModel, pickPoint.getX(), pickPoint.getY());
    }

    // fall back on checking every node
    while (true) {
      try {
        for (N v : getFilteredNodes()) {

          // get the shape for the node (it is at the origin)
          Shape shape = vv.getRenderContext().getNodeShapeFunction().apply(v);
          // get the node location in layout coordinate system
          Point p = layoutModel.apply(v);
          if (p == null) {
            continue;
          }
          // translate the shape to the node location in layout coordinates
          AffineTransform xform = AffineTransform.getTranslateInstance(p.x, p.y);
          shape = xform.createTransformedShape(shape);

          if (shape.contains(pickPoint.getX(), pickPoint.getY())) {

            if (style == Style.LOWEST) {
              // return the first match
              return v;
            } else if (style == Style.HIGHEST) {
              // will return the last match
              closest = v;
            } else {

              // return the node closest to the
              // center of a node shape
              Rectangle2D bounds = shape.getBounds2D();
              double dx = bounds.getCenterX() - pickPoint.getY();
              double dy = bounds.getCenterY() - pickPoint.getY();
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

  /**
   * uses the spatialRTree to find the closest node to the points
   *
   * @param spatial
   * @param layoutModel
   * @param x in the layout coordinate system
   * @param y in the layout coordinate system
   * @return the picked node
   */
  protected N getNode(Spatial<N> spatial, LayoutModel<N> layoutModel, double x, double y) {

    TransformSupport<N, E> transformSupport = vv.getTransformSupport();

    // find the leaf node that would contain a point at x,y
    Collection<? extends TreeNode> containingLeafs =
        spatial.getContainingLeafs(new Point2D.Double(x, y));
    if (log.isTraceEnabled()) {
      log.trace("leaf for {},{} is {}", x, y, containingLeafs);
    }
    if (containingLeafs == null || containingLeafs.size() == 0) return null;
    // make a target circle the same size as the leaf node
    // leaf nodes are small when nodes are close and large when they are sparse
    // union up all the leafs then make a target
    Rectangle2D union = null;
    for (TreeNode r : containingLeafs) {
      if (union == null) {
        union = r.getBounds();
      } else {
        union = union.createUnion(r.getBounds());
      }
    }
    double width = union.getWidth();
    double height = union.getHeight();
    double radiusx = width / 2;
    double radiusy = height / 2;
    Ellipse2D target = new Ellipse2D.Double(x - radiusx, y - radiusy, width, height);
    if (log.isTraceEnabled()) {
      log.trace("target is {}", target);
    }

    double minDistance = Double.MAX_VALUE;

    // will be the picked node
    N closest = null;

    // get the all nodes from any leafs that intersect the target
    Collection<N> nodes = spatial.getVisibleElements(target);
    if (log.isTraceEnabled()) {
      log.trace("instead of checking all nodes: {}", getFilteredNodes());
      log.trace("out of these candidates: {}...", nodes);
    }
    // Check the (smaller) set of eligible nodes
    // to return the one that contains the (x,y)
    for (N node : nodes) {
      // get the shape for the node (centered at the origin)
      Shape shape = vv.getRenderContext().getNodeShapeFunction().apply(node);
      // get the node location
      Point p = layoutModel.apply(node);
      if (p == null) {
        continue;
      }
      // translate the node shape to its location in layout coordinates
      AffineTransform xform = AffineTransform.getTranslateInstance(p.x, p.y);
      shape = xform.createTransformedShape(shape);

      // translate the pick point from layout coords to screen coords
      Point2D layoutPoint = new Point2D.Double(x, y);
      log.trace("layout coords of pick point: {}", layoutPoint);
      Point2D screenPoint = transformSupport.transform(vv, layoutPoint);
      log.trace("screen coords of pick point: {}", screenPoint);
      shape = transformSupport.transform(vv, shape);
      log.trace("looking in a shape at {} for {}", Node.asString(shape.getBounds2D()), screenPoint);

      if (shape.contains(screenPoint)) {

        if (style == Style.LOWEST) {
          // return the first match
          return node;
        } else if (style == Style.HIGHEST) {
          // will return the last match
          closest = node;
        } else {

          // return the node closest to the
          // center of a node shape
          Rectangle2D bounds = shape.getBounds2D();
          double dx = bounds.getCenterX() - x;
          double dy = bounds.getCenterY() - y;
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
   * Returns the nodes whose layout coordinates are contained in <code>Shape</code>. The shape is in
   * screen coordinates, and the graph nodes are transformed to screen coordinates before they are
   * tested for inclusion.
   *
   * @return the <code>Collection</code> of nodes whose <code>layout</code> coordinates are
   *     contained in <code>shape</code>.
   */
  @Override
  public Collection<N> getNodes(LayoutModel<N> layoutModel, Shape shape) {
    Set<N> pickedNodes = new HashSet<>();

    // the pick target shape is in layout coordinate system.

    Spatial spatial = vv.getNodeSpatial();
    if (spatial != null) {
      return getContained(spatial, layoutModel, shape);
    }

    // fall back on checking every node
    while (true) {
      try {
        for (N v : getFilteredNodes()) {
          Point p = layoutModel.apply(v);
          if (p == null) {
            continue;
          }
          if (shape.contains(p.x, p.y)) {
            pickedNodes.add(v);
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return pickedNodes;
  }

  /**
   * use the spatial structure to find nodes inside the passed shape
   *
   * @param spatial
   * @param layoutModel
   * @param shape a target shape in layout coordinates
   * @return the nodes contained in the target shape
   */
  protected Collection<N> getContained(Spatial spatial, LayoutModel<N> layoutModel, Shape shape) {

    Collection<N> visible = Sets.newHashSet(spatial.getVisibleElements(shape));
    if (log.isTraceEnabled()) {
      log.trace("your shape intersects tree cells with these nodes: {}", visible);
    }

    // some of the nodes that the spatial tree considers visible may be outside
    // of the pick target shape. Check this smaller set of nodes and return only
    // those that are inside the shape
    for (Iterator<N> iterator = visible.iterator(); iterator.hasNext(); ) {
      N node = iterator.next();
      Point p = layoutModel.apply(node);
      if (p == null) {
        continue;
      }
      if (!shape.contains(p.x, p.y)) {
        iterator.remove();
      }
    }
    if (log.isTraceEnabled()) {
      log.trace("these were actually picked: {}", visible);
    }
    return visible;
  }
  /**
   * use the spatial R tree to find edges inside the passed shape
   *
   * @param spatial
   * @param layoutModel
   * @param shape a target shape in layout coordinates
   * @return the nodes contained in the target shape
   */
  protected Collection<E> getContained(
      SpatialRTree.Edges<E, N> spatial, LayoutModel<N> layoutModel, Shape shape) {

    Collection<E> visible = spatial.getVisibleElements(shape);
    if (log.isTraceEnabled()) {
      log.trace("your shape intersects tree cells with these nodes: {}", visible);
    }
    //    Network network = spatial.getNetwork();
    // some of the nodes that the spatial tree considers visible may be outside
    // of the pick target shape. Check this smaller set of nodes and return only
    // those that intersect the shape
    for (Iterator<E> iterator = visible.iterator(); iterator.hasNext(); ) {
      E edge = iterator.next();
      Shape edgeShape = getTransformedEdgeShape(edge);
      if (!edgeShape.intersects(shape.getBounds())) {
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
   * @param x the x coordinate of the location (layout coordinate system)
   * @param y the y coordinate of the location (layout coordinate system)
   * @return an edge whose shape intersects the pick area centered on the location {@code (x,y)}
   */
  @Override
  public E getEdge(LayoutModel<N> layoutModel, double x, double y) {

    // as a Line has no area, we can't always use edgeshape.contains(point) so we
    // make a small rectangular pickArea around the point and check if the
    // edgeshape.intersects(pickArea)
    Rectangle2D pickArea =
        new Rectangle2D.Float(
            (float) x - pickSize / 2, (float) y - pickSize / 2, pickSize, pickSize);
    E closest = null;
    double minDistance = Double.MAX_VALUE;
    Point2D pickPoint = new Point2D.Double(x, y);

    Spatial<E> edgeSpatial = vv.getEdgeSpatial();
    if (edgeSpatial != null && edgeSpatial instanceof SpatialRTree.Edges) {
      return getEdge(
          (SpatialRTree.Edges<E, N>) edgeSpatial, layoutModel, pickPoint.getX(), pickPoint.getY());
    }
    while (true) {
      try {
        // this checks every edge.
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

  @Override
  public E getEdge(LayoutModel<N> layoutModel, Point2D p) {
    return getEdge(layoutModel, p.getX(), p.getY());
  }

  /**
   * uses the spatialRTree to find the closest node to the points
   *
   * @param spatial
   * @param layoutModel
   * @param x in the layout coordinate system
   * @param y in the layout coordinate system
   * @return the picked node
   */
  protected E getEdge(
      SpatialRTree.Edges<E, N> spatial, LayoutModel<N> layoutModel, double x, double y) {

    // find the leaf nodes that would contain a point at x,y
    Collection<LeafNode<E>> containingLeafs = spatial.getContainingLeafs(new Point2D.Double(x, y));
    if (log.isTraceEnabled()) {
      log.trace("leaf for {},{} is {}", x, y, containingLeafs);
    }
    if (containingLeafs == null || containingLeafs.size() == 0) return null;
    // make a target circle the same size as the leaf node area union
    // leaf nodes are small when nodes are close and large when they are sparse
    // union up all the leafs then make a target
    Rectangle2D union = null;
    for (LeafNode<E> r : containingLeafs) {
      if (union == null) {
        union = r.getBounds();
      } else {
        union = union.createUnion(r.getBounds());
      }
    }
    double width = union.getWidth();
    double height = union.getHeight();
    double radiusx = width / 2;
    double radiusy = height / 2;
    Ellipse2D target = new Ellipse2D.Double(x - radiusx, y - radiusy, width, height);
    if (log.isTraceEnabled()) {
      log.trace("target is {}", target);
    }

    double minDistance = Double.MAX_VALUE;

    // will be the picked edge
    E closest = null;

    // get the all nodes from any leafs that intersect the target
    Collection<E> edges = spatial.getVisibleElements(target);
    if (log.isTraceEnabled()) {
      log.trace(
          "instead of checking all {} edges: {}", getFilteredEdges().size(), getFilteredEdges());
      log.trace("out of these {} candidates: {}...", edges.size(), edges);
    }

    Rectangle2D pickArea =
        new Rectangle2D.Float(
            (float) x - pickSize / 2, (float) y - pickSize / 2, pickSize, pickSize);
    //    Point2D pickPoint = new Point2D.Double(x, y);

    // Check the (smaller) set of eligible nodes
    // to return the one that contains the (x,y)
    for (E edge : edges) {

      Shape edgeShape = getTransformedEdgeShape(edge);
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
          closest = edge;
        }
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
    EndpointPair<N> endpoints = vv.getModel().getNetwork().incidentNodes(e);
    N v1 = endpoints.nodeU();
    N v2 = endpoints.nodeV();
    boolean isLoop = v1.equals(v2);
    LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
    Point p1 = layoutModel.apply(v1);
    Point p2 = layoutModel.apply(v2);
    if (p1 == null || p2 == null) {
      return null;
    }
    float x1 = (float) p1.x;
    float y1 = (float) p1.y;
    float x2 = (float) p2.x;
    float y2 = (float) p2.y;

    // translate the edge to the starting node
    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

    Shape edgeShape =
        vv.getRenderContext()
            .getEdgeShapeFunction()
            .apply(Context.getInstance(vv.getModel().getNetwork(), e));
    if (isLoop) {
      // make the loops proportional to the layoutSize of the node
      Shape s2 = vv.getRenderContext().getNodeShapeFunction().apply(v2);
      Rectangle2D s2Bounds = s2.getBounds2D();
      xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
      // move the loop so that the nadir is centered in the node
      xform.translate(0, -edgeShape.getBounds2D().getHeight() / 2);
    } else {
      float dx = x2 - x1;
      float dy = y2 - y1;
      // rotate the edge to the angle between the nodes
      double theta = Math.atan2(dy, dx);
      xform.rotate(theta);
      // stretch the edge to span the distance between the nodes
      float dist = (float) Math.sqrt(dx * dx + dy * dy);
      xform.scale(dist, 1.0f);
    }

    // transform the edge to its location and dimensions
    edgeShape = xform.createTransformedShape(edgeShape);
    return edgeShape;
  }

  protected Collection<N> getFilteredNodes() {
    Set<N> nodes = vv.getModel().getNetwork().nodes();
    return nodesAreFiltered()
        ? Sets.filter(nodes, vv.getRenderContext().getNodeIncludePredicate()::test)
        : nodes;
  }

  protected Collection<E> getFilteredEdges() {
    Set<E> edges = vv.getModel().getNetwork().edges();
    return edgesAreFiltered()
        ? Sets.filter(edges, vv.getRenderContext().getEdgeIncludePredicate()::test)
        : edges;
  }

  /**
   * Quick test to allow optimization of <code>getFilteredNodes()</code>.
   *
   * @return <code>true</code> if there is an relaxing node filtering mechanism for this
   *     visualization, <code>false</code> otherwise
   */
  protected boolean nodesAreFiltered() {
    Predicate<N> nodeIncludePredicate = vv.getRenderContext().getNodeIncludePredicate();
    return nodeIncludePredicate != null && !nodeIncludePredicate.equals((Predicate<N>) (n -> true));
  }

  /**
   * Quick test to allow optimization of <code>getFilteredEdges()</code>.
   *
   * @return <code>true</code> if there is an relaxing edge filtering mechanism for this
   *     visualization, <code>false</code> otherwise
   */
  protected boolean edgesAreFiltered() {
    Predicate<E> edgeIncludePredicate = vv.getRenderContext().getEdgeIncludePredicate();
    return edgeIncludePredicate != null && !edgeIncludePredicate.equals((Predicate<N>) (n -> true));
  }

  /**
   * Returns <code>true</code> if this node in this graph is included in the collections of elements
   * to be rendered, and <code>false</code> otherwise.
   *
   * @return <code>true</code> if this node is included in the collections of elements to be
   *     rendered, <code>false</code> otherwise.
   */
  protected boolean isNodeRendered(N node) {
    Predicate<N> nodeIncludePredicate = vv.getRenderContext().getNodeIncludePredicate();
    return nodeIncludePredicate == null || nodeIncludePredicate.test(node);
  }

  /**
   * Returns <code>true</code> if this edge and its endpoints in this graph are all included in the
   * collections of elements to be rendered, and <code>false</code> otherwise.
   *
   * @return <code>true</code> if this edge and its endpoints are all included in the collections of
   *     elements to be rendered, <code>false</code> otherwise.
   */
  protected boolean isEdgeRendered(E edge) {
    Predicate<N> nodeIncludePredicate = vv.getRenderContext().getNodeIncludePredicate();
    Predicate<E> edgeIncludePredicate = vv.getRenderContext().getEdgeIncludePredicate();
    Network<N, E> g = vv.getModel().getNetwork();
    if (edgeIncludePredicate != null && !edgeIncludePredicate.test(edge)) {
      return false;
    }
    EndpointPair<N> endpoints = g.incidentNodes(edge);
    N v1 = endpoints.nodeU();
    N v2 = endpoints.nodeV();
    return nodeIncludePredicate == null
        || (nodeIncludePredicate.test(v1) && nodeIncludePredicate.test(v2));
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
