package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.Multimap;
import java.awt.*;
import java.util.Collection;

/** Created by Tom Nelson */
public interface Spatial<N> {

  Multimap<Integer, N> getMap();

  Collection<N> getVisibleNodes(Rectangle r);

  Collection<N> getVisibleNodes();

  void setVisibleArea(Rectangle r);

  Rectangle getVisibleArea();
}
