package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;

/**
 * interface to support the creation of new edges by the EditingGraphMousePlugin SimpleEdgeSupport
 * is a sample implementation
 *
 * @author tanelso
 * @param <V> the vertex type
 * @param <V> the edge type
 */
public interface EdgeSupport<V, E> {

  void startEdgeCreate(BasicVisualizationServer<V, E> vv, V startVertex, Point2D startPoint);

  void midEdgeCreate(BasicVisualizationServer<V, E> vv, Point2D midPoint);

  void endEdgeCreate(BasicVisualizationServer<V, E> vv, V endVertex);

  void abort(BasicVisualizationServer<V, E> vv);
}
