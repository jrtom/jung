package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.Multimap;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.function.Function;

/** Created by Tom Nelson */
public interface Spatial<N> {

  Multimap<Integer, N> getMap();

  Collection<N> getVisibleNodes(Shape r);

  Rectangle2D getLayoutArea();

  void recalculate(Function<N, Point2D> layout, Collection<N> nodes);

  void setBounds(Rectangle2D bounds);

  int getBoxNumberFromLocation(Point2D p);

  void update(N node, Point2D p);

  default Rectangle2D getUnion(Rectangle2D rect, Point2D p) {
    double left = Math.min(p.getX(), rect.getX());
    double top = Math.min(p.getY(), rect.getY());
    double right = Math.max(p.getX(), rect.getX() + rect.getWidth());
    double bottom = Math.max(p.getY(), rect.getY() + rect.getHeight());

    return new Rectangle2D.Double(left, top, right - left, bottom - top);
  }
}
