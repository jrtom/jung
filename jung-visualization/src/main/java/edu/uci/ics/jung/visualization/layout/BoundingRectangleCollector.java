package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.EndpointPair;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.util.Context;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BoundingRectangleCollector<T> {

  protected RenderContext rc;
  protected VisualizationModel visualizationModel;
  protected List<Rectangle2D> rectangles = new ArrayList<>();

  public BoundingRectangleCollector(RenderContext rc, VisualizationModel visualizationModel) {
    this.rc = rc;
    this.visualizationModel = visualizationModel;
    compute();
  }

  public static class Point<V> extends BoundingRectangleCollector<V> {
    private static final Logger log =
        LoggerFactory.getLogger(BoundingRectangleCollector.Point.class);

    public Point(RenderContext rc, VisualizationModel visualizationModel) {
      super(rc, visualizationModel);
    }

    public Rectangle2D getForElement(V node) {
      Shape shape = new Rectangle2D.Double();
      Point2D p = (Point2D) visualizationModel.getLayoutModel().apply(node);

      float x = (float) p.getX();
      float y = (float) p.getY();
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      Rectangle2D xfs = xform.createTransformedShape(shape).getBounds2D();
      log.trace("node {} with shape bounds {} is at {}", node, xfs, p);
      return xfs;
    }

    /**
     * @param node
     * @param p1
     * @param p2 ignored for Nodes
     * @return
     */
    public Rectangle2D getForElement(V node, Point2D p1, Point2D p2) {
      return getForElement(node, p1);
    }

    public Rectangle2D getForElement(V node, Point2D p) {
      Shape shape = (Shape) rc.getNodeShapeFunction().apply(node);
      //      Point2D p = (Point2D) layoutModel.apply(node);
      log.trace("node is at {}", p);

      float x = (float) p.getX();
      float y = (float) p.getY();
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      return xform.createTransformedShape(shape).getBounds2D();
    }

    public void compute(Collection nodes) {
      super.compute();

      for (Object v : nodes) {
        Shape shape = (Shape) rc.getNodeShapeFunction().apply(v);
        Point2D p = (Point2D) visualizationModel.getLayoutModel().apply(v);
        //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float) p.getX();
        float y = (float) p.getY();
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }

    public void compute() {
      super.compute();

      for (Object v : visualizationModel.getNetwork().nodes()) {
        Shape shape = (Shape) rc.getNodeShapeFunction().apply(v);
        Point2D p = (Point2D) visualizationModel.getLayoutModel().apply(v);
        //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float) p.getX();
        float y = (float) p.getY();
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }
  }

  public static class Node<V> extends BoundingRectangleCollector<V> {
    private static final Logger log =
        LoggerFactory.getLogger(BoundingRectangleCollector.Node.class);

    public Node(RenderContext rc, VisualizationModel visualizationModel) {
      super(rc, visualizationModel);
    }

    public Rectangle2D getForElement(V node) {
      Shape shape = (Shape) rc.getNodeShapeFunction().apply(node);
      Point2D p = (Point2D) visualizationModel.getLayoutModel().apply(node);

      float x = (float) p.getX();
      float y = (float) p.getY();
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      Rectangle2D xfs = xform.createTransformedShape(shape).getBounds2D();
      log.trace("node {} with shape bounds {} is at {}", node, xfs, p);
      return xfs;
    }

    /**
     * @param node
     * @param p1
     * @param p2 ignored for Nodes
     * @return
     */
    public Rectangle2D getForElement(V node, Point2D p1, Point2D p2) {
      return getForElement(node, p1);
    }

    public Rectangle2D getForElement(V node, Point2D p) {
      Shape shape = (Shape) rc.getNodeShapeFunction().apply(node);
      //      Point2D p = (Point2D) layoutModel.apply(node);
      log.trace("node is at {}", p);

      float x = (float) p.getX();
      float y = (float) p.getY();
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      return xform.createTransformedShape(shape).getBounds2D();
    }

    public void compute(Collection nodes) {
      super.compute();

      for (Object v : nodes) {
        Shape shape = (Shape) rc.getNodeShapeFunction().apply(v);
        Point2D p = (Point2D) visualizationModel.getLayoutModel().apply(v);
        //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float) p.getX();
        float y = (float) p.getY();
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }

    public void compute() {
      super.compute();

      for (Object v : visualizationModel.getNetwork().nodes()) {
        Shape shape = (Shape) rc.getNodeShapeFunction().apply(v);
        Point2D p = (Point2D) visualizationModel.getLayoutModel().apply(v);
        //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float) p.getX();
        float y = (float) p.getY();
        AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
        shape = xform.createTransformedShape(shape);
        rectangles.add(shape.getBounds2D());
      }
    }
  }

  public static class Edge<E> extends BoundingRectangleCollector<E> {
    public Edge(RenderContext rc, VisualizationModel visualizationModel) {
      super(rc, visualizationModel);
    }

    public Rectangle2D getForElement(E edge) {
      EndpointPair endpoints = visualizationModel.getNetwork().incidentNodes(edge);
      Object v1 = endpoints.nodeU();
      Object v2 = endpoints.nodeV();
      Point2D p1 = (Point2D) visualizationModel.getLayoutModel().apply(v1);
      Point2D p2 = (Point2D) visualizationModel.getLayoutModel().apply(v2);
      float x1 = (float) p1.getX();
      float y1 = (float) p1.getY();
      float x2 = (float) p2.getX();
      float y2 = (float) p2.getY();

      boolean isLoop = v1.equals(v2);
      Shape s2 = (Shape) rc.getNodeShapeFunction().apply(v2);
      Shape edgeShape =
          (Shape)
              rc.getEdgeShapeFunction()
                  .apply(Context.getInstance(visualizationModel.getNetwork(), edge));

      AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

      if (isLoop) {
        Rectangle2D s2Bounds = s2.getBounds2D();
        xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
        xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
      } else {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float theta = (float) Math.atan2(dy, dx);
        xform.rotate(theta);
        float dist = (float) p1.distance(p2);
        xform.scale(dist, 1.0);
      }
      edgeShape = xform.createTransformedShape(edgeShape);
      return edgeShape.getBounds2D();
    }

    @Override
    public Rectangle2D getForElement(E element, Point2D p) {
      return getForElement(element, p, p);
    }

    public Rectangle2D getForElement(E edge, Point2D p1, Point2D p2) {
      EndpointPair endpoints = visualizationModel.getNetwork().incidentNodes(edge);
      Object v1 = endpoints.nodeU();
      Object v2 = endpoints.nodeV();
      //      Point2D p1 = (Point2D) layoutModel.apply(v1);
      //      Point2D p2 = (Point2D) layoutModel.apply(v2);
      float x1 = (float) p1.getX();
      float y1 = (float) p1.getY();
      float x2 = (float) p2.getX();
      float y2 = (float) p2.getY();

      boolean isLoop = v1.equals(v2);
      Shape s2 = (Shape) rc.getNodeShapeFunction().apply(v2);
      Shape edgeShape =
          (Shape)
              rc.getEdgeShapeFunction()
                  .apply(Context.getInstance(visualizationModel.getNetwork(), edge));

      AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

      if (isLoop) {
        Rectangle2D s2Bounds = s2.getBounds2D();
        xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
        xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
      } else {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float theta = (float) Math.atan2(dy, dx);
        xform.rotate(theta);
        float dist = (float) p1.distance(p2);
        xform.scale(dist, 1.0);
      }
      edgeShape = xform.createTransformedShape(edgeShape);
      return edgeShape.getBounds2D();
    }

    public void compute() {
      super.compute();

      for (Object e : visualizationModel.getNetwork().edges()) {
        EndpointPair endpoints = visualizationModel.getNetwork().incidentNodes(e);
        Object v1 = endpoints.nodeU();
        Object v2 = endpoints.nodeV();
        Point2D p1 = (Point2D) visualizationModel.getLayoutModel().apply(v1);
        Point2D p2 = (Point2D) visualizationModel.getLayoutModel().apply(v2);
        float x1 = (float) p1.getX();
        float y1 = (float) p1.getY();
        float x2 = (float) p2.getX();
        float y2 = (float) p2.getY();

        boolean isLoop = v1.equals(v2);
        Shape s2 = (Shape) rc.getNodeShapeFunction().apply(v2);
        Shape edgeShape =
            (Shape)
                rc.getEdgeShapeFunction()
                    .apply(Context.getInstance(visualizationModel.getNetwork(), e));

        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

        if (isLoop) {
          Rectangle2D s2Bounds = s2.getBounds2D();
          xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
          xform.translate(0, -edgeShape.getBounds2D().getWidth() / 2);
        } else {
          float dx = x2 - x1;
          float dy = y2 - y1;
          float theta = (float) Math.atan2(dy, dx);
          xform.rotate(theta);
          float dist = (float) p1.distance(p2);
          xform.scale(dist, 1.0);
        }
        edgeShape = xform.createTransformedShape(edgeShape);
        rectangles.add(edgeShape.getBounds2D());
      }
    }
  }

  public abstract Rectangle2D getForElement(T element);

  public abstract Rectangle2D getForElement(T element, Point2D p);

  public abstract Rectangle2D getForElement(T element, Point2D p1, Point2D p2);

  /** @return the rectangles */
  public List<Rectangle2D> getRectangles() {
    return rectangles;
  }

  public void compute() {
    rectangles.clear();
  }
}
