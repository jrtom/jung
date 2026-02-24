/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import edu.uci.ics.jung.algorithms.util.IterativeContext
import edu.uci.ics.jung.layout.model.Point
import java.util.ConcurrentModificationException
import java.util.function.Function
import org.slf4j.LoggerFactory

/**
 * The SpringLayout package represents a visualization of a set of nodes. The SpringLayout, which is
 * initialized with a Graph, assigns X/Y locations to each node. When called `step()`,
 * the SpringLayout moves the visualization forward one step.
 *
 * @author Danyel Fisher
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 */
open class SpringLayoutAlgorithm<N : Any>(
  protected var lengthFunction: Function<in EndpointPair<N>, Int> = Function { 30 }
) : AbstractIterativeLayoutAlgorithm<N>(), IterativeContext {

  protected var stretch: Double = 0.70
  protected var repulsion_range_sq: Int = 100 * 100
  protected var force_multiplier: Double = 1.0 / 3.0

  protected var springNodeData: LoadingCache<N, SpringNodeData> =
    CacheBuilder.newBuilder().build(object : CacheLoader<N, SpringNodeData>() {
      override fun load(key: N): SpringNodeData = SpringNodeData()
    })

  fun getRepulsionRange(): Int = Math.sqrt(repulsion_range_sq.toDouble()).toInt()

  fun setRepulsionRange(range: Int) {
    this.repulsion_range_sq = range * range
  }

  fun getForceMultiplier(): Double = force_multiplier

  fun setForceMultiplier(force: Double) {
    this.force_multiplier = force
  }

  open fun initialize() {}

  override fun step() {
    val graph: Graph<N> = layoutModel.graph
    try {
      for (node in graph.nodes()) {
        val svd = springNodeData.getUnchecked(node) ?: continue
        svd.dx /= 4
        svd.dy /= 4
        svd.edgedx = 0.0
        svd.edgedy = 0.0
        svd.repulsiondx = 0.0
        svd.repulsiondy = 0.0
      }
    } catch (cme: ConcurrentModificationException) {
      step()
      return
    }

    relaxEdges()
    calculateRepulsion()
    moveNodes()
  }

  protected open fun relaxEdges() {
    val graph: Graph<N> = layoutModel.graph
    try {
      for (endpoints in layoutModel.graph.edges()) {
        val node1 = endpoints.nodeU()
        val node2 = endpoints.nodeV()

        val p1 = this.layoutModel.get(node1)
        val p2 = this.layoutModel.get(node2)
        val vx = p1.x - p2.x
        val vy = p1.y - p2.y
        var len = Math.sqrt(vx * vx + vy * vy)

        val desiredLen = lengthFunction.apply(endpoints).toDouble()

        // round from zero, if needed [zero would be Bad.].
        len = if (len == 0.0) .0001 else len

        var f = force_multiplier * (desiredLen - len) / len
        f *= Math.pow(stretch, (graph.degree(node1) + graph.degree(node2) - 2).toDouble())

        // the actual movement distance 'dx' is the force multiplied by the
        // distance to go.
        val dx = f * vx
        val dy = f * vy
        val v1D = springNodeData.getUnchecked(node1)
        val v2D = springNodeData.getUnchecked(node2)
        v1D.edgedx += dx
        v1D.edgedy += dy
        v2D.edgedx += -dx
        v2D.edgedy += -dy
      }
    } catch (cme: ConcurrentModificationException) {
      relaxEdges()
    }
  }

  protected open fun calculateRepulsion() {
    val graph: Graph<N> = layoutModel.graph

    try {
      for (node in graph.nodes()) {
        if (layoutModel.isLocked(node)) {
          continue
        }

        val svd = springNodeData.getUnchecked(node) ?: continue
        var dx = 0.0
        var dy = 0.0

        for (node2 in graph.nodes()) {
          if (node === node2) {
            continue
          }
          val p = layoutModel.apply(node)
          val p2 = layoutModel.apply(node2)
          val vx = p.x - p2.x
          val vy = p.y - p2.y
          val distanceSq = p.distanceSquared(p2)
          if (distanceSq == 0.0) {
            dx += random.nextDouble()
            dy += random.nextDouble()
          } else if (distanceSq < repulsion_range_sq) {
            val factor = 1.0
            dx += factor * vx / distanceSq
            dy += factor * vy / distanceSq
          }
        }
        val dlen = dx * dx + dy * dy
        if (dlen > 0) {
          val dlenSqrt = Math.sqrt(dlen) / 2
          svd.repulsiondx += dx / dlenSqrt
          svd.repulsiondy += dy / dlenSqrt
        }
      }
    } catch (cme: ConcurrentModificationException) {
      calculateRepulsion()
    }
  }

  protected open fun moveNodes() {
    val graph: Graph<N> = layoutModel.graph

    synchronized(layoutModel) {
      try {
        for (node in graph.nodes()) {
          if (layoutModel.isLocked(node)) {
            continue
          }
          val vd = springNodeData.getUnchecked(node) ?: continue
          val xyd = layoutModel.apply(node)
          var posX = xyd.x
          var posY = xyd.y

          vd.dx += vd.repulsiondx + vd.edgedx
          vd.dy += vd.repulsiondy + vd.edgedy
          // keeps nodes from moving any faster than 5 per time unit
          posX += Math.max(-5.0, Math.min(5.0, vd.dx))
          posY += Math.max(-5.0, Math.min(5.0, vd.dy))

          val width = layoutModel.width
          val height = layoutModel.height

          if (posX < 0) {
            posX = 0.0
          } else if (posX > width) {
            posX = width.toDouble()
          }
          if (posY < 0) {
            posY = 0.0
          } else if (posY > height) {
            posY = height.toDouble()
          }
          // after the bounds have been honored above, really set the location
          // in the layout model
          layoutModel.set(node, posX, posY)
        }
      } catch (cme: ConcurrentModificationException) {
        moveNodes()
      }
    }
  }

  protected open class SpringNodeData {
    var edgedx: Double = 0.0
    var edgedy: Double = 0.0
    var repulsiondx: Double = 0.0
    var repulsiondy: Double = 0.0

    /** movement speed, x */
    var dx: Double = 0.0

    /** movement speed, y */
    var dy: Double = 0.0

    override fun toString(): String =
      "{edge=${Point.of(edgedx, edgedy)}, rep=${Point.of(repulsiondx, repulsiondy)}, dx=$dx, dy=$dy}"
  }

  /**
   * @return false
   */
  override fun done(): Boolean = false

  companion object {
    private val log = LoggerFactory.getLogger(SpringLayoutAlgorithm::class.java)
  }
}
