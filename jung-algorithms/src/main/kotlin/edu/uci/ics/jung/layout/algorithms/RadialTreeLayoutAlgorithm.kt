/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 9, 2005
 */
package edu.uci.ics.jung.layout.algorithms

import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.model.PolarPoint
import java.util.HashMap
import org.slf4j.LoggerFactory

/**
 * A radial layout for Tree or Forest graphs. Positions vertices in concentric circles with the root
 * at the center
 *
 * @author Tom Nelson
 */
class RadialTreeLayoutAlgorithm<N : Any>(
  distx: Int = DEFAULT_DISTX,
  disty: Int = DEFAULT_DISTY
) : TreeLayoutAlgorithm<N>(distx, disty) {

  var polarLocations: MutableMap<N, PolarPoint> = HashMap()
    protected set

  override fun buildTree(layoutModel: LayoutModel<N>) {
    super.buildTree(layoutModel)
    setRadialLocations(layoutModel)
    putRadialPointsInModel(layoutModel)
  }

  private fun putRadialPointsInModel(layoutModel: LayoutModel<N>) {
    for ((key, _) in polarLocations) {
      layoutModel.set(key, getCartesian(layoutModel, key))
    }
  }

  override fun setLocation(layoutModel: LayoutModel<N>, node: N, location: Point) {
    val c = getCenter(layoutModel)
    val pv = location.add(-c.x, -c.y)
    val newLocation = PolarPoint.cartesianToPolar(pv)
    polarLocations[node] = newLocation
  }

  private fun getCartesian(layoutModel: LayoutModel<N>, node: N): Point {
    val pp = polarLocations[node]!!
    val centerX = layoutModel.width / 2.0
    val centerY = layoutModel.height / 2.0
    var cartesian = PolarPoint.polarToCartesian(pp)
    cartesian = cartesian.add(centerX, centerY)
    return cartesian
  }

  private fun getMaxXY(layoutModel: LayoutModel<N>): Point {
    var maxx = 0.0
    var maxy = 0.0
    for (node in layoutModel.graph.nodes()) {
      val location = layoutModel.apply(node)
      maxx = Math.max(maxx, location.x)
      maxy = Math.max(maxy, location.y)
    }
    return Point.of(maxx, maxy)
  }

  private fun setRadialLocations(layoutModel: LayoutModel<N>) {
    val width = layoutModel.width
    val max = getMaxXY(layoutModel)
    val maxx = Math.max(max.x, width.toDouble())
    val maxy = max.y
    val theta = 2 * Math.PI / maxx

    val deltaRadius = width / 2.0 / maxy
    for (node in layoutModel.graph.nodes()) {
      val p = layoutModel.get(node)
      val polarPoint = PolarPoint.of(p.x * theta, (p.y - this.distY) * deltaRadius)
      polarLocations[node] = polarPoint
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(RadialTreeLayoutAlgorithm::class.java)
  }
}
