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

import com.google.common.graph.Graph
import edu.uci.ics.jung.algorithms.shortestpath.Distance
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath
import edu.uci.ics.jung.algorithms.util.IterativeContext
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import org.slf4j.LoggerFactory
import java.util.ConcurrentModificationException
import java.util.function.BiFunction

/**
 * Implements the Kamada-Kawai algorithm for node layout. Does not respect filter calls, and
 * sometimes crashes when the view changes to it.
 *
 * @see "Tomihisa Kamada and Satoru Kawai: An algorithm for drawing general indirect graphs.
 *     Information Processing Letters 31(1):7-15, 1989"
 * @see "Tomihisa Kamada: On visualization of abstract objects and relations. Ph.D. dissertation,
 *     Dept. of Information Science, Univ. of Tokyo, Dec. 1988."
 * @author Masanori Harada
 * @author Tom Nelson
 */
class KKLayoutAlgorithm<N : Any> : AbstractIterativeLayoutAlgorithm<N>, IterativeContext {

  private var EPSILON = 0.1
  private var currentIteration = 0
  private var maxIterations = 2000
  private var status = "KKLayout"

  private var L = 0.0 // the ideal length of an edge
  private var K = 1.0 // arbitrary const number
  private lateinit var dm: Array<DoubleArray> // distance matrix

  private var adjustForGravity = true
  private var exchangenodes = true

  private lateinit var nodes: Array<Any?>
  private lateinit var xydata: Array<Point>

  /** Retrieves graph distances between nodes of the visible graph */
  protected var distance: BiFunction<N, N, Number?>? = null

  /**
   * The diameter of the visible graph. In other words, the maximum over all pairs of nodes of the
   * length of the shortest path between a and bf the visible graph.
   */
  protected var diameter = 0.0

  /** A multiplicative factor which partly specifies the "preferred" length of an edge (L). */
  private var lengthFactor = 0.9

  /**
   * A multiplicative factor which specifies the fraction of the graph's diameter to be used as the
   * inter-node distance between disconnected nodes.
   */
  private var disconnectedMultiplier = 0.5

  constructor()

  constructor(distance: Distance<N>) {
    this.distance = BiFunction { x, y -> distance.getDistance(x, y) }
  }

  override fun visit(layoutModel: LayoutModel<N>) {
    super.visit(layoutModel)

    val graph = layoutModel.graph
    val dist: Distance<N> = UnweightedShortestPath(graph)
    this.distance = BiFunction { x, y -> dist.getDistance(x, y) }
    initialize()
  }

  /**
   * @param lengthFactor a multiplicative factor which partially specifies the preferred length of
   *     an edge
   */
  fun setLengthFactor(lengthFactor: Double) {
    this.lengthFactor = lengthFactor
  }

  /**
   * @param disconnectedMultiplier a multiplicative factor that specifies the fraction of the
   *     graph's diameter to be used as the inter-node distance between disconnected nodes
   */
  fun setDisconnectedDistanceMultiplier(disconnectedMultiplier: Double) {
    this.disconnectedMultiplier = disconnectedMultiplier
  }

  /**
   * @return a string with information about the current status of the algorithm.
   */
  fun getStatus(): String {
    return status + layoutModel.width + " " + layoutModel.height
  }

  fun setMaxIterations(maxIterations: Int) {
    this.maxIterations = maxIterations
  }

  /**
   * @return true if the current iteration has passed the maximum count.
   */
  override fun done(): Boolean {
    return currentIteration > maxIterations
  }

  @Suppress("UNCHECKED_CAST")
  fun initialize() {
    currentIteration = 0
    val graph: Graph<N> = layoutModel.graph
    // KKLayoutAlgorithm will fail if all nodes start at the same location
    layoutModel.setInitializer(
      RandomLocationTransformer(layoutModel.width.toDouble(), layoutModel.height.toDouble(), graph.nodes().size.toLong())
    )
    val height = layoutModel.height.toDouble()
    val width = layoutModel.width.toDouble()

    val n = graph.nodes().size
    dm = Array(n) { DoubleArray(n) }
    nodes = graph.nodes().toTypedArray()
    xydata = Array(n) { Point.ORIGIN }

    // assign IDs to all visible nodes
    while (true) {
      try {
        var index = 0
        for (node in graph.nodes()) {
          val xyd = layoutModel.apply(node)
          nodes[index] = node
          xydata[index] = xyd
          index++
        }
        break
      } catch (cme: ConcurrentModificationException) {
      }
    }

    diameter = DistanceStatistics.diameter(graph, distance!!, true)

    val L0 = Math.min(height, width)
    L = (L0 / diameter) * lengthFactor

    for (i in 0 until n - 1) {
      for (j in i + 1 until n) {
        val d_ij = distance!!.apply(nodes[i] as N, nodes[j] as N)
        log.trace("distance from $i to $j is $d_ij")

        val d_ji = distance!!.apply(nodes[j] as N, nodes[i] as N)
        log.trace("distance from $j to $i is $d_ji")

        var dist = diameter * disconnectedMultiplier
        log.trace("dist:$dist")
        if (d_ij != null) {
          dist = Math.min(d_ij.toDouble(), dist)
        }
        if (d_ji != null) {
          dist = Math.min(d_ji.toDouble(), dist)
        }
        dm[i][j] = dist
        dm[j][i] = dist
      }
    }
  }

  override fun step() {
    val graph: Graph<N> = layoutModel.graph
    currentIteration++
    val energy = calcEnergy()
    status = "Kamada-Kawai N=${graph.nodes().size}(${graph.nodes().size}) IT: $currentIteration E=$energy"

    val n = graph.nodes().size
    if (n == 0) {
      return
    }

    var maxDeltaM = 0.0
    var pm = -1 // the node having max deltaM
    for (i in 0 until n) {
      @Suppress("UNCHECKED_CAST")
      if (layoutModel.isLocked(nodes[i] as N)) {
        continue
      }
      val deltam = calcDeltaM(i)
      if (maxDeltaM < deltam) {
        maxDeltaM = deltam
        pm = i
      }
    }
    if (pm == -1) {
      return
    }

    for (i in 0 until 100) {
      val dxy = calcDeltaXY(pm)
      xydata[pm] = Point.of(xydata[pm].x + dxy[0], xydata[pm].y + dxy[1])
      val deltam = calcDeltaM(pm)
      if (deltam < EPSILON) {
        break
      }
    }

    if (adjustForGravity) {
      adjustForGravity()
    }

    @Suppress("UNCHECKED_CAST")
    if (exchangenodes && maxDeltaM < EPSILON) {
      val currentEnergy = calcEnergy()
      for (i in 0 until n - 1) {
        if (layoutModel.isLocked(nodes[i] as N)) {
          continue
        }
        for (j in i + 1 until n) {
          if (layoutModel.isLocked(nodes[j] as N)) {
            continue
          }
          val xenergy = calcEnergyIfExchanged(i, j)
          if (currentEnergy > xenergy) {
            val sx = xydata[i].x
            val sy = xydata[i].y
            xydata[i] = Point.of(xydata[j].x, xydata[j].y)
            xydata[j] = Point.of(sx, sy)
            return
          }
        }
      }
    }
  }

  /** Shift all nodes so that the center of gravity is located at the center of the screen. */
  @Suppress("UNCHECKED_CAST")
  fun adjustForGravity() {
    val height = layoutModel.height.toDouble()
    val width = layoutModel.width.toDouble()
    var gx = 0.0
    var gy = 0.0
    for (i in xydata.indices) {
      gx += xydata[i].x
      gy += xydata[i].y
    }
    gx /= xydata.size
    gy /= xydata.size
    val diffx = width / 2 - gx
    val diffy = height / 2 - gy
    for (i in xydata.indices) {
      xydata[i] = xydata[i].add(diffx, diffy)
      layoutModel.set(nodes[i] as N, xydata[i])
    }
  }

  fun setAdjustForGravity(on: Boolean) {
    adjustForGravity = on
  }

  fun getAdjustForGravity(): Boolean {
    return adjustForGravity
  }

  /**
   * Enable or disable the local minimum escape technique by exchanging nodes.
   *
   * @param on iff the local minimum escape technique is to be enabled
   */
  fun setExchangenodes(on: Boolean) {
    exchangenodes = on
  }

  fun getExchangenodes(): Boolean {
    return exchangenodes
  }

  /** Determines a step to new position of the node m. */
  private fun calcDeltaXY(m: Int): DoubleArray {
    var dE_dxm = 0.0
    var dE_dym = 0.0
    var d2E_d2xm = 0.0
    var d2E_dxmdym = 0.0
    var d2E_d2ym = 0.0

    for (i in nodes.indices) {
      if (i != m) {
        val dist = dm[m][i]
        val l_mi = L * dist
        val k_mi = K / (dist * dist)
        val dx = xydata[m].x - xydata[i].x
        val dy = xydata[m].y - xydata[i].y
        val d = Math.sqrt(dx * dx + dy * dy)
        val ddd = d * d * d

        dE_dxm += k_mi * (1 - l_mi / d) * dx
        dE_dym += k_mi * (1 - l_mi / d) * dy
        d2E_d2xm += k_mi * (1 - l_mi * dy * dy / ddd)
        d2E_dxmdym += k_mi * l_mi * dx * dy / ddd
        d2E_d2ym += k_mi * (1 - l_mi * dx * dx / ddd)
      }
    }
    // d2E_dymdxm equals to d2E_dxmdym.
    val d2E_dymdxm = d2E_dxmdym

    val denomi = d2E_d2xm * d2E_d2ym - d2E_dxmdym * d2E_dymdxm
    val deltaX = (d2E_dxmdym * dE_dym - d2E_d2ym * dE_dxm) / denomi
    val deltaY = (d2E_dymdxm * dE_dxm - d2E_d2xm * dE_dym) / denomi
    return doubleArrayOf(deltaX, deltaY)
  }

  /** Calculates the gradient of energy function at the node m. */
  private fun calcDeltaM(m: Int): Double {
    var dEdxm = 0.0
    var dEdym = 0.0
    for (i in nodes.indices) {
      if (i != m) {
        val dist = dm[m][i]
        val l_mi = L * dist
        val k_mi = K / (dist * dist)

        val dx = xydata[m].x - xydata[i].x
        val dy = xydata[m].y - xydata[i].y
        val d = Math.sqrt(dx * dx + dy * dy)

        val common = k_mi * (1 - l_mi / d)
        dEdxm += common * dx
        dEdym += common * dy
      }
    }
    return Math.sqrt(dEdxm * dEdxm + dEdym * dEdym)
  }

  /** Calculates the energy function E. */
  private fun calcEnergy(): Double {
    var energy = 0.0
    for (i in 0 until nodes.size - 1) {
      for (j in i + 1 until nodes.size) {
        val dist = dm[i][j]
        val l_ij = L * dist
        val k_ij = K / (dist * dist)
        val dx = xydata[i].x - xydata[j].x
        val dy = xydata[i].y - xydata[j].y
        val d = Math.sqrt(dx * dx + dy * dy)

        energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij - 2 * l_ij * d)
      }
    }
    return energy
  }

  /** Calculates the energy function E as if positions of the specified nodes are exchanged. */
  private fun calcEnergyIfExchanged(p: Int, q: Int): Double {
    if (p >= q) {
      throw RuntimeException("p should be < q")
    }
    var energy = 0.0 // < 0
    for (i in 0 until nodes.size - 1) {
      for (j in i + 1 until nodes.size) {
        var ii = i
        var jj = j
        if (i == p) {
          ii = q
        }
        if (j == q) {
          jj = p
        }

        val dist = dm[i][j]
        val l_ij = L * dist
        val k_ij = K / (dist * dist)
        val dx = xydata[ii].x - xydata[jj].x
        val dy = xydata[ii].y - xydata[jj].y
        val d = Math.sqrt(dx * dx + dy * dy)

        energy += k_ij / 2 * (dx * dx + dy * dy + l_ij * l_ij - 2 * l_ij * d)
      }
    }
    return energy
  }

  companion object {
    private val log = LoggerFactory.getLogger(KKLayoutAlgorithm::class.java)
  }
}
