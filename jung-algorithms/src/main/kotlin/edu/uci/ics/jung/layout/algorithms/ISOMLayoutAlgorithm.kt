/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.graph.Graph
import edu.uci.ics.jung.algorithms.util.IterativeContext
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.NetworkNodeAccessor
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import java.util.ConcurrentModificationException
import org.slf4j.LoggerFactory

/**
 * Implements a self-organizing map layout algorithm, based on Meyer's self-organizing graph
 * methods.
 *
 * @author Yan Biao Boey
 */
class ISOMLayoutAlgorithm<N : Any> : AbstractIterativeLayoutAlgorithm<N>(), IterativeContext {

  protected var isomNodeData: LoadingCache<N, ISOMNodeData> =
    CacheBuilder.newBuilder()
      .build(object : CacheLoader<N, ISOMNodeData>() {
        override fun load(node: N): ISOMNodeData = ISOMNodeData()
      })

  private var maxEpoch: Int = 0
  private var epoch: Int = 0

  private var radiusConstantTime: Int = 0
  private var radius: Int = 0
  private var minRadius: Int = 0

  private var adaption: Double = 0.0
  private var initialAdaption: Double = 0.0
  private var minAdaption: Double = 0.0

  private lateinit var elementAccessor: NetworkNodeAccessor<N>

  private var coolingFactor: Double = 0.0

  private val queue: MutableList<N> = ArrayList()
  private var status: String? = null

  /**
   * @return the current number of epochs and execution status, as a string.
   */
  fun getStatus(): String? = status

  override fun visit(layoutModel: LayoutModel<N>) {
    if (log.isTraceEnabled) {
      log.trace("visiting {}", layoutModel)
    }
    super.visit(layoutModel)
    this.elementAccessor = RadiusNetworkNodeAccessor()
    initialize()
  }

  fun initialize() {
    layoutModel.setInitializer(
      RandomLocationTransformer(layoutModel.width.toDouble(), layoutModel.height.toDouble())
    )

    maxEpoch = 2000
    epoch = 1

    radiusConstantTime = 100
    radius = 5
    minRadius = 1

    initialAdaption = 90.0 / 100.0
    adaption = initialAdaption
    minAdaption = 0.0

    coolingFactor = 2.0
  }

  /** Advances the current positions of the graph elements. */
  override fun step() {
    status = "epoch: $epoch; "
    if (epoch < maxEpoch) {
      adjust()
      updateParameters()
      status += " status: running"
    } else {
      status += "adaption: $adaption; "
      status += "status: done"
    }
  }

  @Synchronized
  private fun adjust() {
    val width = layoutModel.width.toDouble()
    val height = layoutModel.height.toDouble()
    // Generate random position in graph space
    // creates a new XY data location
    val tempXYD = Point.of(10 + Math.random() * width, 10 + Math.random() * height)

    // Get closest node to random position
    val winner = elementAccessor.getNode(layoutModel, tempXYD.x, tempXYD.y)

    while (true) {
      try {
        for (node in layoutModel.graph.nodes()) {
          val ivd = getISOMNodeData(node)
          ivd.distance = 0
          ivd.visited = false
        }
        break
      } catch (cme: ConcurrentModificationException) {
      }
    }
    if (winner != null) {
      adjustNode(winner, tempXYD)
    }
  }

  @Synchronized
  private fun updateParameters() {
    epoch++
    val factor = Math.exp(-1 * coolingFactor * (1.0 * epoch / maxEpoch))
    adaption = Math.max(minAdaption, factor * initialAdaption)
    if ((radius > minRadius) && (epoch % radiusConstantTime == 0)) {
      radius--
    }
  }

  @Synchronized
  private fun adjustNode(node: N, tempXYD: Point) {
    val graph: Graph<N> = layoutModel.graph
    queue.clear()
    val ivd = getISOMNodeData(node)
    ivd.distance = 0
    ivd.visited = true
    queue.add(node)

    while (queue.isNotEmpty()) {
      val current = queue.removeAt(0)
      val currData = getISOMNodeData(current)
      val currXYData = layoutModel.apply(current)

      val dx = tempXYD.x - currXYData.x
      val dy = tempXYD.y - currXYData.y
      val factor = adaption / Math.pow(2.0, currData.distance.toDouble())

      layoutModel.set(current, currXYData.x + (factor * dx), currXYData.y + (factor * dy))

      if (currData.distance < radius) {
        val s = graph.adjacentNodes(current)
        while (true) {
          try {
            for (child in s) {
              val childData = getISOMNodeData(child)
              if (!childData.visited) {
                childData.visited = true
                childData.distance = currData.distance + 1
                queue.add(child)
              }
            }
            break
          } catch (cme: ConcurrentModificationException) {
          }
        }
      }
    }
  }

  protected fun getISOMNodeData(node: N): ISOMNodeData = isomNodeData.getUnchecked(node)

  /**
   * Returns `true` if the node positions are no longer being updated. Currently
   * `ISOMLayout` stops updating node positions after a certain number of iterations have taken
   * place.
   *
   * @return `true` if the node position updates have stopped, `false` otherwise
   */
  override fun done(): Boolean = epoch >= maxEpoch

  protected class ISOMNodeData {
    var distance: Int = 0
    var visited: Boolean = false
  }

  /**
   * Resets the layout iteration count to 0, which allows the layout algorithm to continue updating
   * node positions.
   */
  fun reset() {
    epoch = 0
  }

  companion object {
    private val log = LoggerFactory.getLogger(ISOMLayoutAlgorithm::class.java)
  }
}
