package edu.uci.ics.jung.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;

/**
 * Interface for items that present a bounding box rectangle
 *
 * @author Tom Nelson
 */
public interface Bounded {

  /**
   * return the Rectangle of the bounding box
   *
   * @return
   */
  Rectangle2D getBounds();
}
