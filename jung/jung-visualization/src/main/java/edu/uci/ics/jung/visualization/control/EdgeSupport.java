package edu.uci.ics.jung.visualization.control;

import java.awt.geom.Point2D;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

/**
 * interface to support the creation of new edges by the EditingGraphMousePlugin
 * SimpleEdgeSupport is a sample implementation
 * @author tanelso
 *
 * @param <V>
 * @param <E>
 */
public interface EdgeSupport<V,E> {
	
	void startEdgeCreate(BasicVisualizationServer<V,E> vv, V startVertex, 
			Point2D startPoint, EdgeType edgeType);
	
	void midEdgeCreate(BasicVisualizationServer<V,E> vv, Point2D midPoint);
	
	void endEdgeCreate(BasicVisualizationServer<V,E> vv, V endVertex);

}
