package edu.uci.ics.jung.visualization.spatial;

import com.google.common.collect.Multimap;
import java.awt.*;
import java.util.Collection;

/** Created by Tom Nelson on 9/22/17. */
public interface Spatial<N> {
  /**
   * A Multimap of box number to Lists of nodes in that box
   *
   * @return
   */
  Multimap<Integer, N> getMap();

  //    /**
  //     * given a rectangular area and an offset, return the nodes that are contained in it.
  //     * @param d
  //     * @param xOffset
  //     * @param yOffset
  //     * @return
  //     */
  //    Collection<N> getVisibleNodes(Dimension d, int xOffset, int yOffset);

  Collection<N> getVisibleNodes(Rectangle r);

  Collection<N> getVisibleNodes();

  void setVisibleArea(Rectangle r);

  Rectangle getVisibleArea();
}
