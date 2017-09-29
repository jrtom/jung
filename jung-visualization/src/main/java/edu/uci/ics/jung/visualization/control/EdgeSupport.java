package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;

/**
 * interface to support the creation of new edges by the EditingGraphMousePlugin SimpleEdgeSupport
 * is a sample implementation
 *
 * @author Tom Nelson
 */
public interface EdgeSupport {

  void startEdgeCreate(BasicVisualizationServer vv, Object startVertex, Point2D startPoint);

  void midEdgeCreate(BasicVisualizationServer vv, Point2D midPoint);

  void endEdgeCreate(BasicVisualizationServer vv, Object endVertex);

  void abort(BasicVisualizationServer vv);
}
