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
package edu.uci.ics.jung.layout.util

import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point

/**
 * Interface for coordinate-based selection of graph nodes.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
interface NetworkNodeAccessor<N : Any> {

  /**
   * @param layoutModel
   * @param p the pick point
   * @return the node associated with the pick point
   */
  fun getNode(layoutModel: LayoutModel<N>, p: Point): N?

  /**
   * Returns the node, if any, associated with (x, y).
   *
   * @param x the x coordinate of the pick point
   * @param y the y coordinate of the pick point
   * @return the node associated with (x, y)
   */
  fun getNode(layoutModel: LayoutModel<N>, x: Double, y: Double): N?
}
