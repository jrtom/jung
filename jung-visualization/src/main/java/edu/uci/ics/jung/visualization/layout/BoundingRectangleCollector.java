package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.util.Context;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class BoundingRectangleCollector<V, E> {

  protected RenderContext<V, E> rc;
  protected Network<V, E> graph;
  protected LayoutModel<V, Point2D> layoutModel;
  protected List<Rectangle2D> rectangles = new ArrayList<Rectangle2D>();

  public BoundingRectangleCollector(
      RenderContext<V, E> rc, VisualizationModel<V, E, Point2D> visualizationModel) {
    this.rc = rc;
    this.layoutModel = visualizationModel.getLayoutModel();
    this.graph = visualizationModel.getNetwork();
    compute();
  }

  /** @return the rectangles */
  public List<Rectangle2D> getRectangles() {
    return rectangles;
  }

  public void compute() {
    rectangles.clear();

    for (E e : graph.edges()) {
      EndpointPair<V> endpoints = graph.incidentNodes(e);
      V v1 = endpoints.nodeU();
      V v2 = endpoints.nodeV();
      Point2D p1 = layoutModel.apply(v1);
      Point2D p2 = layoutModel.apply(v2);
      float x1 = (float) p1.getX();
      float y1 = (float) p1.getY();
      float x2 = (float) p2.getX();
      float y2 = (float) p2.getY();

      boolean isLoop = v1.equals(v2);
      Shape s2 = rc.getVertexShapeTransformer().apply(v2);
      Shape edgeShape = rc.getEdgeShapeTransformer().apply(Context.getInstance(graph, e));

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

    for (V v : graph.nodes()) {
      Shape shape = rc.getVertexShapeTransformer().apply(v);
      Point2D p = layoutModel.apply(v);
      //			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
      float x = (float) p.getX();
      float y = (float) p.getY();
      AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
      shape = xform.createTransformedShape(shape);
      rectangles.add(shape.getBounds2D());
    }
  }
}
