package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;

/**
 * interface to support the creation of new vertices by the EditingGraphMousePlugin.
 * SimpleVertexSupport is a sample implementation.
 *
 * @author Tom Nelson
 */
public interface VertexSupport {

  void startVertexCreate(BasicVisualizationServer vv, Point2D point);

  void midVertexCreate(BasicVisualizationServer vv, Point2D point);

  void endVertexCreate(BasicVisualizationServer vv, Point2D point);
}
