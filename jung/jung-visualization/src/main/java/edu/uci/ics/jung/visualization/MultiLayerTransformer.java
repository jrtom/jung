package edu.uci.ics.jung.visualization;

import java.awt.Shape;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;

public interface MultiLayerTransformer extends BidirectionalTransformer, ShapeTransformer, ChangeEventSupport {

	
	/**
	 * @see edu.uci.ics.jung.visualization.VisualizationServer#setViewTransformer(edu.uci.ics.jung.visualization.transform.MutableTransformer)
	 */
	void setTransformer(Layer layer, MutableTransformer transformer);

	/**
	 * @return the layoutTransformer
	 */
	MutableTransformer getTransformer(Layer layer);

	/**
	 */
	Point2D inverseTransform(Layer layer, Point2D p);

	/**
	 */
	Point2D transform(Layer layer, Point2D p);

	/**
	 */
	Shape transform(Layer layer, Shape shape);
	
	Shape inverseTransform(Layer layer, Shape shape);

	void setToIdentity();

}