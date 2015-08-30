package edu.uci.ics.jung.visualization.layout;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.RenderContext;

public class BoundingRectangleCollector<V,E>  {

	protected RenderContext<V,E> rc;
	protected Graph<V,E> graph;
	protected Layout<V,E> layout;
	protected List<Rectangle2D> rectangles = new ArrayList<Rectangle2D>();
	
	public BoundingRectangleCollector(RenderContext<V, E> rc, Layout<V, E> layout) {
		this.rc = rc;
		this.layout = layout;
		this.graph = layout.getGraph();
		compute();
	}

	/**
	 * @return the rectangles
	 */
	public List<Rectangle2D> getRectangles() {
		return rectangles;
	}

	public void compute() {
		rectangles.clear();
//		Graphics2D g2d = (Graphics2D)g;
//		g.setColor(Color.cyan);

		for(E e : graph.getEdges()) {
			Pair<V> endpoints = graph.getEndpoints(e);
			V v1 = endpoints.getFirst();
			V v2 = endpoints.getSecond();
			Point2D p1 = layout.apply(v1);
			Point2D p2 = layout.apply(v2);
			float x1 = (float)p1.getX();
			float y1 = (float)p1.getY();
			float x2 = (float)p2.getX();
			float y2 = (float)p2.getY();
			
			boolean isLoop = v1.equals(v2);
			Shape s2 = rc.getVertexShapeTransformer().apply(v2);
			Shape edgeShape = rc.getEdgeShapeTransformer().apply(e);

			AffineTransform xform = AffineTransform.getTranslateInstance(x1,y1);
			
			if(isLoop) {
				Rectangle2D s2Bounds = s2.getBounds2D();
				xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
				xform.translate(0, -edgeShape.getBounds2D().getWidth()/2);
			} else {
				float dx = x2-x1;
				float dy = y2-y1;
				float theta = (float)Math.atan2(dy,dx);
				xform.rotate(theta);
				float dist = (float)p1.distance(p2);
				xform.scale(dist, 1.0);
			}
			edgeShape = xform.createTransformedShape(edgeShape);
//			edgeShape = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, edgeShape);
			rectangles.add(edgeShape.getBounds2D());
		}
		
		for(V v : graph.getVertices()) {
			Shape shape = rc.getVertexShapeTransformer().apply(v);
			Point2D p = layout.apply(v);
//			p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
			float x = (float)p.getX();
			float y = (float)p.getY();
			AffineTransform xform = AffineTransform.getTranslateInstance(x, y);
			shape = xform.createTransformedShape(shape);
			rectangles.add(shape.getBounds2D());
		}
	}
}
