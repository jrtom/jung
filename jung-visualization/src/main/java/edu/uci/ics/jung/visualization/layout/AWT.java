package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.layout.model.PointModel;
import java.awt.geom.Point2D;

/**
 * A home for singleton models for AWT rendering geometry
 *
 * @author Tom Nelson
 */
public interface AWT {

  PointModel<Point2D> POINT_MODEL = new AWTPointModel();
}
