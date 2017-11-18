/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Apr 12, 2005
 */
package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.layout.model.LayoutModel;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Collection;

/**
 * Interface for coordinate-based selection of graph components.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */

/**
 * interface for support for node information about Networks (nodes and edges).
 *
 * @param <N>
 * @param <E>
 */
public interface NetworkElementAccessor<N, E> {
  /**
   * Returns the node, if any, associated with (x, y).
   *
   * @param x the x coordinate of the pick point
   * @param y the y coordinate of the pick point
   * @return the node associated with (x, y)
   */
  N getNode(LayoutModel<N, Point2D> layoutModel, double x, double y);

  /**
   * @param rectangle the region in which the returned nodes are located
   * @return the nodes whose locations given by {@code layout} are contained within {@code
   *     rectangle}
   */
  Collection<N> getNodes(LayoutModel<N, Point2D> layoutModel, Shape rectangle);

  /**
   * @param x the x coordinate of the location
   * @param y the y coordinate of the location
   * @return an edge which is associated with the location {@code (x,y)} as given by {@code layout},
   *     generally by reference to the edge's endpoints
   */
  E getEdge(LayoutModel<N, Point2D> layoutModel, double x, double y);
}
