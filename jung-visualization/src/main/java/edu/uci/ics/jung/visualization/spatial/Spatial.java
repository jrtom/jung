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

  Collection<N> getVisibleNodes(Rectangle2D r);

  Rectangle2D getLayoutArea();

  void setLayoutArea(Rectangle2D layoutArea);

  void recalculate(Function<N, Point2D> layout, Collection<N> nodes);

  void setBounds(Rectangle2D bounds);

  int getBoxNumberFromLocation(Point2D p);
}
