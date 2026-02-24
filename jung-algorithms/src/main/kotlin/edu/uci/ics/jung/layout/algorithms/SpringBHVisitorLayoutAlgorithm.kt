/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms

import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import edu.uci.ics.jung.algorithms.util.IterativeContext
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree
import edu.uci.ics.jung.layout.spatial.ForceObject
import java.util.ConcurrentModificationException
import java.util.function.Function
import org.slf4j.LoggerFactory

/**
 * This subclass of SpringLayoutAlgorithm applies a Barnes-Hut QuadTree optimization during the
 * calculation of node repulsion. The purpose of the Barnes-Hut optimization is to reduce the number
 * of calculations during the calculateRepulsion method from O(n^2) to O(nlog(n))
 *
 * @author Tom Nelson
 */
class SpringBHVisitorLayoutAlgorithm<N : Any>(
  lengthFunction: Function<in EndpointPair<N>, Int> = Function { 30 }
) : SpringLayoutAlgorithm<N>(lengthFunction), IterativeContext {

  /** Used for optimization of the calculation of repulsion forces between Nodes */
  private lateinit var tree: BarnesHutQuadTree<N>

  /**
   * Override to create the BarnesHutQuadTree
   *
   * @param layoutModel
   */
  override fun visit(layoutModel: LayoutModel<N>) {
    super.visit(layoutModel)
    tree = BarnesHutQuadTree(layoutModel.width.toDouble(), layoutModel.height.toDouble())
  }

  /**
   * Override to rebuild the tree during each step, as every node has been moved. Building of the
   * QuadTree is an O(nlog(n)) operation.
   */
  override fun step() {
    tree.rebuild(layoutModel.locations)
    super.step()
  }

  /**
   * Instead of visiting every other Node (n), visit the QuadTree (log(n)) to gather the forces
   * applied to each Node.
   */
  override fun calculateRepulsion() {
    val graph: Graph<N> = layoutModel.graph

    try {
      for (node in graph.nodes()) {
        if (layoutModel.isLocked(node)) {
          continue
        }

        val svd = springNodeData.getUnchecked(node) ?: continue

        val nodeForceObject = object : ForceObject<N>(node, layoutModel.apply(node)) {
          override fun addForceFrom(other: ForceObject<N>) {
            if (node == other.element) {
              return
            }
            val p = this.p
            val p2 = other.p
            val vx = p.x - p2.x
            val vy = p.y - p2.y
            val distanceSq = p.distanceSquared(p2)
            if (distanceSq == 0.0) {
              f = f.add(random.nextDouble(), random.nextDouble())
            } else if (distanceSq < repulsion_range_sq) {
              val factor = 1.0
              f = f.add(factor * vx / distanceSq, factor * vy / distanceSq)
            }
          }
        }
        tree.applyForcesTo(nodeForceObject)
        val forceResult = nodeForceObject.f
        val dlen = forceResult.x * forceResult.x + forceResult.y * forceResult.y
        if (dlen > 0) {
          val dlenSqrt = Math.sqrt(dlen) / 2
          svd.repulsiondx += forceResult.x / dlenSqrt
          svd.repulsiondy += forceResult.y / dlenSqrt
        }
      }
    } catch (cme: ConcurrentModificationException) {
      calculateRepulsion()
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(SpringBHVisitorLayoutAlgorithm::class.java)
  }
}
