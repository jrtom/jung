/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms

import edu.uci.ics.jung.algorithms.util.IterativeContext
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree
import edu.uci.ics.jung.layout.spatial.ForceObject
import org.slf4j.LoggerFactory

/**
 * This subclass of FRLayoutAlgorithm applies a Barnes-Hut QuadTree optimization during the
 * calculation of node repulsion. The purpose of the Barnes-Hut optimization is to reduce the number
 * of calculations during the calculateRepulsion method from O(n^2) to O(nlog(n))
 *
 * @author Tom Nelson
 */
class FRBHVisitorLayoutAlgorithm<N : Any> : FRLayoutAlgorithm<N>(), IterativeContext {

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
  @Synchronized
  override fun step() {
    tree.rebuild(layoutModel.locations)
    super.step()
  }

  /**
   * Instead of visiting every other Node (n), visit the QuadTree (log(n)) to gather the forces
   * applied to each Node.
   */
  override fun calcRepulsion(node1: N) {
    val fvd1 = getFRData(node1) ?: return
    frNodeData.put(node1, Point.ORIGIN)

    val nodeForceObject = object : ForceObject<N>(node1, layoutModel.apply(node1)) {
      override fun addForceFrom(other: ForceObject<N>) {
        val dx = this.p.x - other.p.x
        val dy = this.p.y - other.p.y
        log.trace("dx, dy:{},{}", dx, dy)
        val dist = Math.sqrt(dx * dx + dy * dy)
        val clampedDist = Math.max(EPSILON, dist)
        log.trace("dist:{}", clampedDist)
        val force = (repulsion_constant * repulsion_constant) / clampedDist
        log.trace("force:{}", force)
        f = f.add(force * (dx / clampedDist), force * (dy / clampedDist))
      }
    }
    tree.applyForcesTo(nodeForceObject)
    frNodeData.put(node1, nodeForceObject.f)
  }

  companion object {
    private val log = LoggerFactory.getLogger(FRBHVisitorLayoutAlgorithm::class.java)
  }
}
