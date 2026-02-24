/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms

import com.google.common.graph.EndpointPair
import edu.uci.ics.jung.algorithms.util.IterativeContext
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree
import edu.uci.ics.jung.layout.spatial.ForceObject
import java.util.ConcurrentModificationException
import java.util.function.Function
import org.slf4j.LoggerFactory

/**
 * This subclass of SpringLayoutAlgorithm applies a Barnes-Hut QuadTree optimization during the
 * calculation of node repulsion. It uses an Iterator over the reduced set of nodes for comparison.
 * As it is not as performant as the Visitor model, it is in the test module for comparison of
 * output between the implementations.
 *
 * @author Tom Nelson
 */
class SpringBHIteratorLayoutAlgorithm<N : Any>(
  lengthFunction: Function<in EndpointPair<N>, Int> = Function { 30 }
) : SpringLayoutAlgorithm<N>(lengthFunction), IterativeContext {

  private lateinit var tree: BarnesHutQuadTree<N>

  override fun visit(layoutModel: LayoutModel<N>) {
    super.visit(layoutModel)
    tree = BarnesHutQuadTree(layoutModel.width.toDouble(), layoutModel.height.toDouble())
  }

  override fun step() {
    tree.rebuild(layoutModel.locations)
    super.step()
  }

  override fun calculateRepulsion() {
    val graph = layoutModel.graph

    try {
      for (node in graph.nodes()) {
        if (layoutModel.isLocked(node)) {
          continue
        }

        val svd = springNodeData.getUnchecked(node) ?: continue
        var dx = 0.0
        var dy = 0.0

        val nodeForceObject = ForceObject(node, layoutModel.apply(node))
        val forceObjectIterator = ForceObjectIterator(tree, nodeForceObject)
        while (forceObjectIterator.hasNext()) {
          val nextForceObject = forceObjectIterator.next()
          if (nextForceObject == null || node === nextForceObject.element) {
            continue
          }
          val p = nodeForceObject.p
          val p2 = nextForceObject.p
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

  companion object {
    private val log = LoggerFactory.getLogger(SpringBHIteratorLayoutAlgorithm::class.java)
  }
}
