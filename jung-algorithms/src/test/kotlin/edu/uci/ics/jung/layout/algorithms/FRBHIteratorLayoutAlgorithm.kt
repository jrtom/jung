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
import java.util.ConcurrentModificationException
import org.slf4j.LoggerFactory

/**
 * This subclass of FRLayoutAlgorithm applies a Barnes-Hut QuadTree optimization during the
 * calculation of node repulsion. It uses an Iterator over the reduced set of nodes for comparison.
 * As it is not as performant as the Visitor model, it is in the test module for comparison of
 * output between the implementations
 *
 * @author Tom Nelson
 */
class FRBHIteratorLayoutAlgorithm<N : Any> : FRLayoutAlgorithm<N>(), IterativeContext {

  private lateinit var tree: BarnesHutQuadTree<N>

  override fun visit(layoutModel: LayoutModel<N>) {
    super.visit(layoutModel)
    tree = BarnesHutQuadTree(layoutModel.width.toDouble(), layoutModel.height.toDouble())
  }

  @Synchronized
  override fun step() {
    tree.rebuild(layoutModel.locations)
    super.step()
  }

  override fun calcRepulsion(node1: N) {
    var fvd1 = getFRData(node1) ?: return
    log.trace("fvd1 for {} starts as {}", node1, fvd1)
    frNodeData.put(node1, Point.ORIGIN)
    val nodeForceObject = ForceObject(node1, layoutModel.apply(node1))
    val forceObjectIterator = ForceObjectIterator(tree, nodeForceObject)
    try {
      while (forceObjectIterator.hasNext()) {
        val nextForceObject = forceObjectIterator.next()
        if (nextForceObject != null && nextForceObject != nodeForceObject) {
          if (log.isTraceEnabled) {
            log.trace(
              "Iter {} at {} visiting {} at {}",
              nextForceObject.element,
              nextForceObject.p,
              nodeForceObject.element,
              nodeForceObject.p
            )
          }
          fvd1 = getFRData(node1)!!

          val p1 = nodeForceObject.p
          val p2 = nextForceObject.p
          val xDelta = p1.x - p2.x
          val yDelta = p1.y - p2.y
          log.trace("xDelta,yDelta:{},{}", xDelta, yDelta)

          val deltaLength = Math.max(EPSILON, Math.sqrt((xDelta * xDelta) + (yDelta * yDelta)))
          log.trace("deltaLength:{}", deltaLength)

          val force = (repulsion_constant * repulsion_constant) / deltaLength
          log.trace("force:{}", force)

          if (java.lang.Double.isNaN(force)) {
            throw RuntimeException(
              "Unexpected mathematical result in FRLayout:calcPositions [repulsion]"
            )
          }
          if (log.isTraceEnabled) {
            log.trace("frNodeData for {} went from {}...", node1, frNodeData.getUnchecked(node1))
          }
          fvd1 = fvd1.add((xDelta / deltaLength) * force, (yDelta / deltaLength) * force)
          frNodeData.put(node1, fvd1)
          log.trace("...to {}", frNodeData.getUnchecked(node1))
        }
      }
    } catch (cme: ConcurrentModificationException) {
      calcRepulsion(node1)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(FRBHIteratorLayoutAlgorithm::class.java)
  }
}
