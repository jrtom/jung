package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;

/**
 * interface to support the creation of new nodes by the EditingGraphMousePlugin. SimpleNodeSupport
 * is a sample implementation.
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public interface NodeSupport<N, E> {

  void startNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point);

  void midNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point);

  void endNodeCreate(BasicVisualizationServer<N, E> vv, Point2D point);
}
