package edu.uci.ics.jung.visualization.control;

import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;

public interface EdgeEffects<V,E> {
	
	void startEdgeEffects(BasicVisualizationServer<V,E> vv,
			Point2D down, Point2D out);
	
	void midEdgeEffects(BasicVisualizationServer<V,E> vv,
			Point2D down, Point2D out);
	
	void endEdgeEffects(BasicVisualizationServer<V,E> vv);
	
	void startArrowEffects(BasicVisualizationServer<V,E> vv,
			Point2D down, Point2D out);
	
	void midArrowEffects(BasicVisualizationServer<V,E> vv,
			Point2D down, Point2D out);

	void endArrowEffects(BasicVisualizationServer<V,E> vv);

}
