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

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import edu.uci.ics.jung.visualization.util.Context;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

/**
 * ShapePickSupport provides access to Vertices and EdgeType based on their actual shapes.
 *
 * @param <N> the vertex type
 * @param <E> the edge type
 * @author Tom Nelson
 */
public class ViewLensShapePickSupport<N, E> extends ShapePickSupport<N, E> {

  public ViewLensShapePickSupport(VisualizationServer<N, E> vv, float pickSize) {
    super(vv, pickSize);
  }

  public ViewLensShapePickSupport(VisualizationServer<N, E> vv) {
    this(vv, 2);
  }

  @Override
  public N getNode(LayoutModel<N, Point2D> layoutModel, double x, double y) {

    N closest = null;
    double minDistance = Double.MAX_VALUE;

    // the pick point in screen coordinates
    Point2D pickPoint = new Point2D.Double(x, y);
    // now the pick point is is layout coordinates
    pickPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(pickPoint);

    while (true) {
      try {

        for (N v : getFilteredVertices()) {
          // get the shape for the vertex (which starts at the origin)
          Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(v);
          // get the vertex location in layout coordinates
          Point2D p = layoutModel.apply(v);
          if (p == null) {
            continue;
          }
          // translate the shape to the vertex location in layout coordinates
          AffineTransform xform = AffineTransform.getTranslateInstance(p.getX(), p.getY());
          shape = xform.createTransformedShape(shape);

          // see if this vertex center is closest to the pick point
          // among any other containing vertices
          // compare each vertex shape in layout coordinates with the pickPoint in layout coordinates
          if (shape.contains(pickPoint.getX(), pickPoint.getY())) {

            if (style == Style.LOWEST) {
              // return the first match
              return v;
            } else if (style == Style.HIGHEST) {
              // will return the last match
              closest = v;
            } else {
              Rectangle2D bounds = shape.getBounds2D();
              double dx = bounds.getCenterX() - pickPoint.getX();
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

  @Override
  public Collection<N> getNodes(LayoutModel<N, Point2D> layoutModel, Shape rectangle) {
    Set<N> pickedVertices = new HashSet<N>();

    // the pick rectangle is in view coordinates.
    // inverse transform the rectangle to a shape in layout coordinates
    rectangle = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(rectangle);
    // now the 'rectangle' is in the layout coordinate system
    final Shape paintableShape = rectangle;
    vv.addPostRenderPaintable(
        new VisualizationServer.Paintable() {
          @Override
          public void paint(Graphics g) {
            Shape viewShape =
                vv.getRenderContext().getMultiLayerTransformer().transform(paintableShape);
            g.setColor(Color.BLACK);
            //        ((Graphics2D)g).draw(paintableShape);
            ((Graphics2D) g).draw(viewShape);
          }

          @Override
          public boolean useTransform() {
            return true;
          }
        });

    while (true) {
      try {
        for (N v : getFilteredVertices()) {
          Point2D p = layoutModel.apply(v);
          if (p == null) {
            continue;
          }
          p = (Point2D) p.clone();
          // the location of the vertex in layout coordinates
          // get the shape for the vertex (at the origin)
          //          Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(v);
          // translate the node shape to its location in layout coordinates
          //          AffineTransform xform = AffineTransform.getTranslateInstance(p.getX(), p.getY());
          //          shape = xform.createTransformedShape(shape);

          //          shape = vv.getRenderContext().getMultiLayerTransformer().transform(shape);
          //          Rectangle2D bounds = shape.getBounds2D();
          //          p.setLocation(bounds.getCenterX(), bounds.getCenterY());
          //
          if (rectangle.contains(p)) {
            pickedVertices.add(v);
          }
        }
        break;
      } catch (ConcurrentModificationException cme) {
      }
    }
    return pickedVertices;
  }

  @Override
  public E getEdge(LayoutModel<N, Point2D> layoutModel, double x, double y) {
    Point2D ip =
        ((MutableTransformerDecorator)
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW))
            .getDelegate()
            .inverseTransform(new Point2D.Double(x, y));
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
        Network<N, E> network = vv.getModel().getNetwork();
        for (E e : getFilteredEdges()) {
          EndpointPair<N> endpoints = network.incidentNodes(e);
          N v1 = endpoints.nodeU();
          N v2 = endpoints.nodeV();
          boolean isLoop = v1.equals(v2);
          Point2D p1 = layoutModel.apply(v1);
          Point2D p2 = layoutModel.apply(v2);
          if (p1 == null || p2 == null) {
            continue;
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
                  .apply(Context.getInstance(network, e));
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

          edgeShape = vv.getRenderContext().getMultiLayerTransformer().transform(edgeShape);

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
}
