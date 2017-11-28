package edu.uci.ics.jung.visualization.spatial;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * Basic interface for Spatial data
 *
 * @author Tom Nelson
 * @param <N> the node type
 */
public interface Spatial<N> {

  /**
   * @param shape the possibly non-rectangular area of interest
   * @return all nodes that are contained within the passed Shape
   */
  Collection<N> getVisibleNodes(Shape shape);

  /** @return the 2 dimensional area of interest for this class */
  Rectangle2D getLayoutArea();

  /**
   * rebuild the data structure
   *
   * @param nodes the nodes to insert into the data structure
   */
  void recalculate(Collection<N> nodes);

  /** @param bounds the new bounds for the data struture */
  void setBounds(Rectangle2D bounds);

  /**
   * update the position of the passed node
   *
   * @param node the node to update in the structure
   */
  void update(N node);

  /**
   * expands the passed rectangle so that it includes the passed point
   *
   * @param rect the area to consider
   * @param p the point that may be outside of the area
   * @return a new rectangle
   */
  default Rectangle2D getUnion(Rectangle2D rect, Point2D p) {
    double left = Math.min(p.getX(), rect.getX());
    double top = Math.min(p.getY(), rect.getY());
    double right = Math.max(p.getX(), rect.getX() + rect.getWidth());
    double bottom = Math.max(p.getY(), rect.getY() + rect.getHeight());

    return new Rectangle2D.Double(left, top, right - left, bottom - top);
  }
}
