package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import java.awt.geom.Point2D;

public interface EdgeEffects<N, E> {

  void startEdgeEffects(BasicVisualizationServer<N, E> vv, Point2D down, Point2D out);

  void midEdgeEffects(BasicVisualizationServer<N, E> vv, Point2D down, Point2D out);

  void endEdgeEffects(BasicVisualizationServer<N, E> vv);

  void startArrowEffects(BasicVisualizationServer<N, E> vv, Point2D down, Point2D out);

  void midArrowEffects(BasicVisualizationServer<N, E> vv, Point2D down, Point2D out);

  void endArrowEffects(BasicVisualizationServer<N, E> vv);
}
