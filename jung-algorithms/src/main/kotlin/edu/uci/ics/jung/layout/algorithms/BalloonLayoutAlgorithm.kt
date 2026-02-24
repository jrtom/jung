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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.collect.Iterables
import com.google.common.graph.Graph
import edu.uci.ics.jung.graph.util.TreeUtils
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.model.PolarPoint
import java.util.HashMap
import org.slf4j.LoggerFactory

/**
 * A [Layout] implementation that assigns positions to [Tree] or [Network] nodes
 * using associations with nested circles ("balloons"). A balloon is nested inside another balloon
 * if the first balloon's subtree is a subtree of the second balloon's subtree.
 *
 * @author Tom Nelson
 */
class BalloonLayoutAlgorithm<N : Any> : TreeLayoutAlgorithm<N>() {

  protected var polarLocations: LoadingCache<N, PolarPoint> =
    CacheBuilder.newBuilder()
      .build(object : CacheLoader<N, PolarPoint>() {
        override fun load(node: N): PolarPoint = PolarPoint.ORIGIN
      })

  var radii: MutableMap<N, Double> = HashMap()
    protected set

  override fun visit(layoutModel: LayoutModel<N>) {
    super.visit(layoutModel)
    if (log.isTraceEnabled) {
      log.trace("visit {}", layoutModel)
    }
    super.visit(layoutModel)
    setRootPolars(layoutModel)
  }

  private fun putRadialPointsInModel(layoutModel: LayoutModel<N>) {
    for ((key, _) in polarLocations.asMap()) {
      layoutModel.set(key, getCartesian(layoutModel, key))
    }
  }

  protected fun setRootPolars(layoutModel: LayoutModel<N>) {
    val graph: Graph<N> = layoutModel.graph
    val roots = TreeUtils.roots(graph)
    val width = layoutModel.width
    if (roots.size == 1) {
      // its a Tree
      val root = Iterables.getOnlyElement(roots)
      setRootPolar(layoutModel, root)
      setPolars(layoutModel, graph.successors(root), getCenter(layoutModel), width / 2.0)
    } else if (roots.size > 1) {
      // its a Network
      setPolars(layoutModel, roots, getCenter(layoutModel), width / 2.0)
    }
  }

  protected fun setRootPolar(layoutModel: LayoutModel<N>, root: N) {
    val pp = PolarPoint.ORIGIN
    val p = getCenter(layoutModel)
    polarLocations.put(root, pp)
    layoutModel.set(root, p)
  }

  protected fun setPolars(
    layoutModel: LayoutModel<N>,
    kids: Set<N>,
    parentLocation: Point,
    parentRadius: Double
  ) {
    val childCount = kids.size
    if (childCount == 0) {
      return
    }
    // handle the 1-child case with 0 limit on angle.
    val angle = Math.max(0.0, Math.PI / 2 * (1 - 2.0 / childCount))
    val childRadius = parentRadius * Math.cos(angle) / (1 + Math.cos(angle))
    val radius = parentRadius - childRadius

    val rand = Math.random()

    var i = 0
    for (child in kids) {
      val theta = i++ * 2 * Math.PI / childCount + rand
      radii[child] = childRadius

      val pp = PolarPoint.of(theta, radius)
      polarLocations.put(child, pp)

      var p = PolarPoint.polarToCartesian(pp)
      p = p.add(parentLocation.x, parentLocation.y)
      layoutModel.set(child, p)
      setPolars(layoutModel, layoutModel.graph.successors(child), p, childRadius)
    }
  }

  /**
   * @param node the node whose center is to be returned
   * @return the coordinates of `node`'s parent, or the center of this layout's area if it's a
   *     root.
   */
  fun getCenter(layoutModel: LayoutModel<N>, node: N): Point {
    val graph: Graph<N> = layoutModel.graph
    val parent = Iterables.getOnlyElement(graph.predecessors(node), null)
    return if (parent == null) {
      getCenter(layoutModel)
    } else {
      layoutModel.get(node = parent)
    }
  }

  private fun getCartesian(layoutModel: LayoutModel<N>, node: N): Point {
    val pp = polarLocations.getUnchecked(node)
    val centerX = layoutModel.width / 2.0
    val centerY = layoutModel.height / 2.0
    var cartesian = PolarPoint.polarToCartesian(pp)
    cartesian = cartesian.add(centerX, centerY)
    return cartesian
  }

  override fun setLocation(layoutModel: LayoutModel<N>, node: N, location: Point) {
    val c = getCenter(layoutModel, node)
    val pv = location.add(-c.x, -c.y)
    val newLocation = PolarPoint.cartesianToPolar(pv.x, pv.y)
    polarLocations.put(node, newLocation)

    val center = getCenter(layoutModel, node)
    val result = pv.add(center.x, center.y)
    layoutModel.set(node, pv)
  }

  companion object {
    private val log = LoggerFactory.getLogger(BalloonLayoutAlgorithm::class.java)
  }
}
