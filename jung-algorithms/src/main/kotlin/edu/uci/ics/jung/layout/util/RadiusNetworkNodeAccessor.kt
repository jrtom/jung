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
import java.util.ConcurrentModificationException
import org.slf4j.LoggerFactory

/**
 * Simple implementation of PickSupport that returns the node or edge that is closest to the
 * specified location. This implementation provides the same picking options that were available in
 * previous versions of
 *
 * No element will be returned that is farther away than the specified maximum distance.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
open class RadiusNetworkNodeAccessor<N : Any>(
  protected var maxDistance: Double = Math.sqrt(Double.MAX_VALUE - 1000)
) : NetworkNodeAccessor<N> {

  /**
   * @param layoutModel
   * @param p the pick point
   * @return the node associated with location p
   */
  override fun getNode(layoutModel: LayoutModel<N>, p: Point): N? =
    getNode(layoutModel, p.x, p.y)

  /**
   * Gets the node nearest to the location of the (x,y) location selected, within a distance of
   * `this.maxDistance`. Iterates through all visible nodes and checks their distance from the
   * location. Override this method to provide a more efficient implementation.
   *
   * @param x the x coordinate of the location
   * @param y the y coordinate of the location
   * @return a node which is associated with the location `(x,y)` as given by `layout`
   */
  override fun getNode(layoutModel: LayoutModel<N>, x: Double, y: Double): N? {
    var minDistance = maxDistance * maxDistance * maxDistance
    var closest: N? = null
    while (true) {
      try {
        for (node in layoutModel.locations.keys) {
          val p = layoutModel.apply(node)
          val dx = p.x - x
          val dy = p.y - y
          val dist = dx * dx + dy * dy
          if (dist < minDistance) {
            minDistance = dist
            closest = node
          }
        }
        break
      } catch (cme: ConcurrentModificationException) {
      }
    }
    return closest
  }

  companion object {
    private val log = LoggerFactory.getLogger(RadiusNetworkNodeAccessor::class.java)
  }
}
