/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Dec 4, 2003
 */
package edu.uci.ics.jung.layout.algorithms

import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import java.util.HashMap
import org.slf4j.LoggerFactory

/**
 * An implementation of [Layout] suitable for tree-like directed acyclic graphs. Parts of it
 * will probably not terminate if the graph is cyclic! The layout will result in directed edges
 * pointing generally upwards. Any nodes with no successors are considered to be level 0, and tend
 * towards the top of the layout. Any node has a level one greater than the maximum level of all its
 * successors.
 *
 * @author John Yesberg
 */
class DAGLayoutAlgorithm<N : Any> : SpringLayoutAlgorithm<N>() {

  /**
   * Each node has a minimumLevel. Any node with no successors has minimumLevel of zero. The
   * minimumLevel of any node must be strictly greater than the minimumLevel of its parents. (node A
   * is a parent of node B iff there is an edge from B to A.) Typically, a node will have a
   * minimumLevel which is one greater than the minimumLevel of its parent's. However, if the node
   * has two parents, its minimumLevel will be one greater than the maximum of the parents'. We need
   * to calculate the minimumLevel for each node. When we layout the graph, nodes cannot be drawn
   * any higher than the minimumLevel. The graphHeight of a graph is the greatest minimumLevel that
   * is used. We will modify the SpringLayout calculations so that nodes cannot move above their
   * assigned minimumLevel.
   */
  private val minLevels: MutableMap<N, Number> = HashMap()

  val SPACEFACTOR: Double = 1.3
  // How much space do we allow for additional floating at the bottom.
  val LEVELATTRACTIONRATE: Double = 0.8

  /**
   * A bunch of parameters to help work out when to stop quivering.
   *
   * If the MeanSquareVel(ocity) ever gets below the MSV_THRESHOLD, then we will start a final
   * cool-down phase of COOL_DOWN_INCREMENT increments. If the MeanSquareVel ever exceeds the
   * threshold, we will exit the cool down phase, and continue looking for another opportunity.
   */
  val MSV_THRESHOLD: Double = 10.0

  private var meanSquareVel: Double = 0.0
  private var stoppingIncrements: Boolean = false
  private var incrementsLeft: Int = 0
  val COOL_DOWN_INCREMENTS: Int = 200

  override fun visit(layoutModel: LayoutModel<N>) {
    super.visit(layoutModel)
    initialize()
  }

  /**
   * Calculates the level of each node in the graph. Level 0 is allocated to each node with no
   * successors. Level n+1 is allocated to any node whose successors' maximum level is n.
   */
  fun setRoot() {
    val graph: Graph<N> = layoutModel.graph
    numRoots = 0
    for (node in graph.nodes()) {
      if (graph.successors(node).isEmpty()) {
        setRoot(node)
        numRoots++
      }
    }
  }

  /**
   * Set node v to be level 0.
   *
   * @param node the node to set as root
   */
  fun setRoot(node: N) {
    minLevels[node] = 0
    // set all the levels.
    propagateMinimumLevel(node)
  }

  /**
   * A recursive method for allocating the level for each node. Ensures that all predecessors of v
   * have a level which is at least one greater than the level of v.
   *
   * @param node the node whose minimum level is to be calculated
   */
  fun propagateMinimumLevel(node: N) {
    val graph: Graph<N> = layoutModel.graph
    val level = minLevels[node]!!.toInt()
    for (child in graph.predecessors(node)) {
      val o = minLevels[child]
      val oldLevel = o?.toInt() ?: 0
      val newLevel = Math.max(oldLevel, level + 1)
      minLevels[child] = newLevel

      if (newLevel > graphHeight) {
        graphHeight = newLevel
      }
      propagateMinimumLevel(child)
    }
  }

  /**
   * Sets a random location for a node within the dimensions of the space.
   *
   * @param node the node whose position is to be set
   * @param coord the coordinates of the node once the position has been set
   */
  private fun initializeLocation(node: N, coord: Point, width: Int, height: Int) {
    val level = minLevels[node]!!.toInt()
    val minY = (level * height / (graphHeight * SPACEFACTOR)).toInt()
    val x = Math.random() * width
    val y = Math.random() * (height - minY) + minY
    layoutModel.set(node, x, y)
  }

  /** Had to override this one as well, to ensure that setRoot() is called. */
  override fun initialize() {
    super.initialize()
    setRoot()
  }

  /**
   * Override the moveNodes() method from SpringLayout. The only change we need to make is to make
   * sure that nodes don't float higher than the minY coordinate, as calculated by their
   * minimumLevel.
   */
  override fun moveNodes() {
    val width = layoutModel.width
    val height = layoutModel.height
    val graph: Graph<N> = layoutModel.graph
    val oldMSV = meanSquareVel
    meanSquareVel = 0.0

    synchronized(layoutModel) {
      for (node in graph.nodes()) {
        if (layoutModel.isLocked(node)) {
          continue
        }
        val vd = springNodeData.getUnchecked(node)
        val xyd = layoutModel.apply(node)

        // (JY addition: three lines are new)
        val level = minLevels[node]!!.toInt()
        val minY = (level * height / (graphHeight * SPACEFACTOR)).toInt()
        val maxY = if (level == 0) (height / (graphHeight * SPACEFACTOR * 2)).toInt() else height

        // JY added 2* - double the sideways repulsion.
        vd.dx += 2 * vd.repulsiondx + vd.edgedx
        vd.dy += vd.repulsiondy + vd.edgedy

        // JY Addition: Attract the node towards it's minimumLevel
        // height.
        val delta = xyd.y - minY
        vd.dy -= delta * LEVELATTRACTIONRATE
        if (level == 0) {
          vd.dy -= delta * LEVELATTRACTIONRATE
        }
        // twice as much at the top.

        // JY addition:
        meanSquareVel += (vd.dx * vd.dx + vd.dy * vd.dy)

        var posX = xyd.x + Math.max(-5.0, Math.min(5.0, vd.dx))
        var posY = xyd.y + Math.max(-5.0, Math.min(5.0, vd.dy))

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

        // (JY addition: if there's only one root, anchor it in the
        // middle-top of the screen)
        if (numRoots == 1 && level == 0) {
          posX = width / 2.0
        }
        setLocation(node, posX, posY)
      }
    }
    if (!stoppingIncrements && Math.abs(meanSquareVel - oldMSV) < MSV_THRESHOLD) {
      stoppingIncrements = true
      incrementsLeft = COOL_DOWN_INCREMENTS
    } else if (stoppingIncrements && Math.abs(meanSquareVel - oldMSV) <= MSV_THRESHOLD) {
      incrementsLeft--
      if (incrementsLeft <= 0) {
        incrementsLeft = 0
      }
    }
  }

  /** Override incrementsAreDone so that we can eventually stop. */
  override fun done(): Boolean = stoppingIncrements && incrementsLeft == 0

  /**
   * Override forceMove so that if someone moves a node, we can re-layout everything.
   *
   * @param picked the node whose location is to be set
   * @param x the x coordinate of the location to set
   * @param y the y coordinate of the location to set
   */
  fun setLocation(picked: N, x: Double, y: Double) {
    val coord = layoutModel.apply(picked)
    layoutModel.set(picked, coord)
    stoppingIncrements = false
  }

  /**
   * Override forceMove so that if someone moves a node, we can re-layout everything.
   *
   * @param picked the node whose location is to be set
   * @param p the location to set
   */
  fun setLocation(picked: N, p: Point) {
    setLocation(picked, p.x, p.y)
  }

  /**
   * Overridden relaxEdges. This one reduces the effect of edges between greatly different levels.
   */
  override fun relaxEdges() {
    val graph: Graph<N> = layoutModel.graph
    for (endpoints in graph.edges()) {
      val node1 = endpoints.nodeU()
      val node2 = endpoints.nodeV()

      val p1 = layoutModel.apply(node1)
      val p2 = layoutModel.apply(node2)
      val vx = p1.x - p2.x
      val vy = p1.y - p2.y
      var len = Math.sqrt(vx * vx + vy * vy)

      // JY addition.
      val level1 = minLevels[node1]!!.toInt()
      val level2 = minLevels[node2]!!.toInt()

      val desiredLen = lengthFunction.apply(endpoints).toDouble()

      // round from zero, if needed [zero would be Bad.].
      len = if (len == 0.0) .0001 else len

      // force factor: optimal length minus actual length,
      // is made smaller as the current actual length gets larger.
      // why?
      var f = force_multiplier * (desiredLen - len) / len
      f *= Math.pow(stretch / 100.0, (graph.degree(node1) + graph.degree(node2) - 2).toDouble())

      // JY addition. If this is an edge which stretches a long way,
      // don't be so concerned about it.
      if (level1 != level2) {
        f /= Math.pow(Math.abs(level2 - level1).toDouble(), 1.5)
      }

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
  }

  companion object {
    private val log = LoggerFactory.getLogger(DAGLayoutAlgorithm::class.java)

    // Simpler than the "pair" technique.
    @JvmStatic
    var graphHeight: Int = 0

    @JvmStatic
    var numRoots: Int = 0
  }
}
