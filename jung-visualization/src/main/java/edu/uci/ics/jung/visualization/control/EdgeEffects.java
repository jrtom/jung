package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;

public interface EdgeEffects {

  void startEdgeEffects(BasicVisualizationServer vv, Point2D down, Point2D out);

  void midEdgeEffects(BasicVisualizationServer vv, Point2D down, Point2D out);

  void endEdgeEffects(BasicVisualizationServer vv);

  void startArrowEffects(BasicVisualizationServer vv, Point2D down, Point2D out);

  void midArrowEffects(BasicVisualizationServer vv, Point2D down, Point2D out);

  void endArrowEffects(BasicVisualizationServer vv);
}
