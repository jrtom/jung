package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;

/**
 * interface to support the creation of new vertices by the EditingGraphMousePlugin.
 * SimpleVertexSupport is a sample implementation.
 *
 * @author tanelso
 * @param <V> the vertex type
 */
public interface VertexSupport<V, E> {

  void startVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point);

  void midVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point);

  void endVertexCreate(BasicVisualizationServer<V, E> vv, Point2D point);
}
