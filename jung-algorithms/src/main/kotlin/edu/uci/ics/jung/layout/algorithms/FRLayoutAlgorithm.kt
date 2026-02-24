/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms

import com.google.common.base.Preconditions
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import edu.uci.ics.jung.algorithms.util.IterativeContext
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import java.util.ConcurrentModificationException
import org.slf4j.LoggerFactory

/**
 * Implements the Fruchterman-Reingold force-directed algorithm for node layout.
 *
 * Behavior is determined by the following settable parameters:
 *
 *  * attraction multiplier: how much edges try to keep their nodes together
 *  * repulsion multiplier: how much nodes try to push each other apart
 *  * maximum iterations: how many iterations this algorithm will use before stopping
 *
 * Each of the first two defaults to 0.75; the maximum number of iterations defaults to 700.
 *
 * @see "Fruchterman and Reingold, 'Graph Drawing by Force-directed Placement'"
 * @see "http://i11www.ilkd.uni-karlsruhe.de/teaching/SS_04/visualisierung/papers/fruchterman91graph.pdf"
 * @author Scott White, Yan-Biao Boey, Danyel Fisher, Tom Nelson
 */
open class FRLayoutAlgorithm<N : Any> : AbstractIterativeLayoutAlgorithm<N>(), IterativeContext {

  private var forceConstant: Double = 0.0
  private var temperature: Double = 0.0
  private var currentIteration: Int = 0
  private var mMaxIterations: Int = 700

  protected var frNodeData: LoadingCache<N, Point> =
    CacheBuilder.newBuilder()
      .build(object : CacheLoader<N, Point>() {
        override fun load(node: N): Point = Point.ORIGIN
      })

  private var attraction_multiplier: Double = 0.75
  private var attraction_constant: Double = 0.0
  private var repulsion_multiplier: Double = 0.75
  protected var repulsion_constant: Double = 0.0
  private var max_dimension: Double = 0.0
  private var initialized: Boolean = false

  protected var EPSILON: Double = 0.000001

  override fun visit(layoutModel: LayoutModel<N>) {
    if (log.isTraceEnabled) {
      log.trace("visiting $layoutModel")
    }
    super.visit(layoutModel)
    max_dimension = Math.max(layoutModel.width.toDouble(), layoutModel.height.toDouble())
    initialize()
  }

  fun setAttractionMultiplier(attraction: Double) {
    this.attraction_multiplier = attraction
  }

  fun setRepulsionMultiplier(repulsion: Double) {
    this.repulsion_multiplier = repulsion
  }

  fun initialize() {
    doInit()
  }

  private fun doInit() {
    val graph: Graph<N> = layoutModel.graph
    if (graph.nodes().isNotEmpty()) {
      currentIteration = 0
      temperature = layoutModel.width / 10.0
      forceConstant = Math.sqrt(
        layoutModel.height.toDouble() * layoutModel.width.toDouble() / graph.nodes().size
      )
      attraction_constant = attraction_multiplier * forceConstant
      repulsion_constant = repulsion_multiplier * forceConstant
      initialized = true
    }
  }

  /**
   * Moves the iteration forward one notch, calculation attraction and repulsion between nodes and
   * edges and cooling the temperature.
   */
  @Synchronized
  override fun step() {
    if (!initialized) {
      doInit()
    }
    val graph: Graph<N> = layoutModel.graph
    currentIteration++

    /** Calculate repulsion */
    while (true) {
      try {
        for (node1 in graph.nodes()) {
          calcRepulsion(node1)
        }
        break
      } catch (cme: ConcurrentModificationException) {
      }
    }

    /** Calculate attraction */
    while (true) {
      try {
        for (endpoints in graph.edges()) {
          calcAttraction(endpoints)
        }
        break
      } catch (cme: ConcurrentModificationException) {
      }
    }

    while (true) {
      try {
        for (node in graph.nodes()) {
          if (layoutModel.isLocked(node)) {
            continue
          }
          calcPositions(node)
        }
        break
      } catch (cme: ConcurrentModificationException) {
      }
    }
    cool()
  }

  @Synchronized
  protected open fun calcPositions(node: N) {
    val fvd = getFRData(node) ?: return
    val xyd = layoutModel.apply(node)
    val deltaLength = Math.max(EPSILON, fvd.length())

    var positionX = xyd.x
    var positionY = xyd.y
    val newXDisp = fvd.x / deltaLength * Math.min(deltaLength, temperature)
    val newYDisp = fvd.y / deltaLength * Math.min(deltaLength, temperature)

    positionX += newXDisp
    positionY += newYDisp

    val borderWidth = layoutModel.width / 50.0
    if (positionX < borderWidth) {
      positionX = borderWidth + random.nextDouble() * borderWidth * 2.0
    } else if (positionX > layoutModel.width - borderWidth * 2) {
      positionX = layoutModel.width - borderWidth - random.nextDouble() * borderWidth * 2.0
    }

    if (positionY < borderWidth) {
      positionY = borderWidth + random.nextDouble() * borderWidth * 2.0
    } else if (positionY > layoutModel.width - borderWidth * 2) {
      positionY = layoutModel.width - borderWidth - random.nextDouble() * borderWidth * 2.0
    }

    layoutModel.set(node, positionX, positionY)
  }

  protected open fun calcAttraction(endpoints: EndpointPair<N>) {
    val node1 = endpoints.nodeU()
    val node2 = endpoints.nodeV()
    val v1_locked = layoutModel.isLocked(node1)
    val v2_locked = layoutModel.isLocked(node2)

    if (v1_locked && v2_locked) {
      // both locked, do nothing
      return
    }
    val p1 = layoutModel.apply(node1)
    val p2 = layoutModel.apply(node2)
    val xDelta = p1.x - p2.x
    val yDelta = p1.y - p2.y

    val deltaLength = Math.max(EPSILON, Math.sqrt(xDelta * xDelta + yDelta * yDelta))

    val force = (deltaLength * deltaLength) / attraction_constant

    Preconditions.checkState(
      !java.lang.Double.isNaN(force),
      "Unexpected mathematical result in FRLayout:calcPositions [force]"
    )

    val dx = (xDelta / deltaLength) * force
    val dy = (yDelta / deltaLength) * force
    if (!v1_locked) {
      val fvd1 = getFRData(node1)
      frNodeData.put(node1, fvd1!!.add(-dx, -dy))
    }
    if (!v2_locked) {
      val fvd2 = getFRData(node2)
      frNodeData.put(node2, fvd2!!.add(dx, dy))
    }
  }

  protected open fun calcRepulsion(node1: N) {
    val fvd1 = getFRData(node1) ?: return
    frNodeData.put(node1, Point.ORIGIN)

    try {
      for (node2 in layoutModel.graph.nodes()) {
        if (node1 !== node2) {
          var currentFvd1 = getFRData(node1)
          val p1 = layoutModel.apply(node1)
          val p2 = layoutModel.apply(node2)
          val xDelta = p1.x - p2.x
          val yDelta = p1.y - p2.y

          val deltaLength = Math.max(EPSILON, Math.sqrt(xDelta * xDelta + yDelta * yDelta))

          val force = (repulsion_constant * repulsion_constant) / deltaLength

          if (java.lang.Double.isNaN(force)) {
            throw RuntimeException(
              "Unexpected mathematical result in FRLayout:calcPositions [repulsion]"
            )
          }
          currentFvd1 = currentFvd1!!.add((xDelta / deltaLength) * force, (yDelta / deltaLength) * force)
          frNodeData.put(node1, currentFvd1)
        }
      }
    } catch (cme: ConcurrentModificationException) {
      calcRepulsion(node1)
    }
  }

  private fun cool() {
    temperature *= (1.0 - currentIteration / mMaxIterations.toDouble())
  }

  fun setMaxIterations(maxIterations: Int) {
    mMaxIterations = maxIterations
  }

  protected fun getFRData(node: N): Point? = frNodeData.getUnchecked(node)

  /**
   * @return true once the current iteration has passed the maximum count.
   */
  override fun done(): Boolean =
    currentIteration > mMaxIterations || temperature < 1.0 / max_dimension

  companion object {
    private val log = LoggerFactory.getLogger(FRLayoutAlgorithm::class.java)
  }
}
