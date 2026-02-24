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

import com.google.common.base.Preconditions
import com.google.common.collect.Sets
import edu.uci.ics.jung.graph.util.TreeUtils
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import java.util.HashMap
import java.util.function.Function
import org.slf4j.LoggerFactory

/**
 * @author Karlheinz Toni
 * @author Tom Nelson - converted to jung2, refactored into Algorithm/Visitor
 */
open class TreeLayoutAlgorithm<N : Any>(
  /** The horizontal node spacing. Defaults to [DEFAULT_DISTX]. */
  protected var distX: Int = DEFAULT_DISTX,
  /** The vertical node spacing. Defaults to [DEFAULT_DISTY]. */
  protected var distY: Int = DEFAULT_DISTY
) : LayoutAlgorithm<N> {

  protected var basePositions: MutableMap<N, Int> = HashMap()

  @Transient
  protected var alreadyDone: MutableSet<N> = HashSet()

  protected var currentX: Double = 0.0
  protected var currentY: Double = 0.0

  init {
    Preconditions.checkArgument(distX >= 1, "X distance must be positive")
    Preconditions.checkArgument(distY >= 1, "Y distance must be positive")
    this.currentX = 0.0
    this.currentY = 0.0
  }

  override fun visit(layoutModel: LayoutModel<N>) {
    buildTree(layoutModel)
  }

  protected open fun buildTree(layoutModel: LayoutModel<N>) {
    alreadyDone = Sets.newHashSet()
    this.currentX = 0.0
    this.currentY = 20.0
    val roots = TreeUtils.roots(layoutModel.graph)
    Preconditions.checkArgument(roots.size > 0)
    calculateDimensionX(layoutModel, roots)
    for (node in roots) {
      calculateDimensionX(layoutModel, node)
      val posX = this.basePositions[node]!! / 2 + this.distX
      buildTree(layoutModel, node, posX)
    }
  }

  protected fun buildTree(layoutModel: LayoutModel<N>, node: N, x: Int) {
    if (alreadyDone.add(node)) {
      // go one level further down
      val newY = this.currentY + this.distY
      this.currentX = x.toDouble()
      this.currentY = newY
      this.setCurrentPositionFor(layoutModel, node)

      val sizeXofCurrent = basePositions[node]!!

      var lastX = x - sizeXofCurrent / 2

      for (element in layoutModel.graph.successors(node)) {
        val sizeXofChild = this.basePositions[element]!!
        val startXofChild = lastX + sizeXofChild / 2
        buildTree(layoutModel, element, startXofChild)
        lastX = lastX + sizeXofChild + distX
      }

      this.currentY -= this.distY
    }
  }

  private fun calculateDimensionX(layoutModel: LayoutModel<N>, node: N): Int {
    var size = 0
    val childrenNum = layoutModel.graph.successors(node).size

    if (childrenNum != 0) {
      for (element in layoutModel.graph.successors(node)) {
        size += calculateDimensionX(layoutModel, element) + distX
      }
    }
    size = Math.max(0, size - distX)
    basePositions[node] = size
    return size
  }

  private fun calculateDimensionX(layoutModel: LayoutModel<N>, roots: Collection<N>): Int {
    var size = 0
    for (node in roots) {
      size += calculateDimensionX(layoutModel, node)
    }
    return size
  }

  protected fun setCurrentPositionFor(layoutModel: LayoutModel<N>, node: N) {
    var width = layoutModel.width
    var height = layoutModel.height
    val x = this.currentX.toInt()
    val y = this.currentY.toInt()
    if (x < 0) {
      width -= x
    }
    if (x >= width - distX) {
      width = x + distX
    }
    if (y < 0) {
      height -= y
    }
    if (y >= height - distY) {
      height = y + distY
    }
    if (layoutModel.width < width || layoutModel.height < height) {
      layoutModel.setSize(width, height)
    }
    setLocation(layoutModel, node, this.currentX, this.currentY)
  }

  /**
   * can be overridden to add behavior
   *
   * @param layoutModel
   * @param node
   * @param location
   */
  protected open fun setLocation(layoutModel: LayoutModel<N>, node: N, location: Point) {
    layoutModel.set(node, location)
  }

  protected open fun setLocation(layoutModel: LayoutModel<N>, node: N, x: Double, y: Double) {
    layoutModel.set(node, x, y)
  }

  fun isLocked(node: N): Boolean = false

  fun lock(node: N, state: Boolean) {}

  fun setInitializer(initializer: Function<N, Point>) {}

  /**
   * @return the center of this layout's area.
   */
  fun getCenter(layoutModel: LayoutModel<N>): Point =
    Point.of((layoutModel.width / 2).toDouble(), (layoutModel.height / 2).toDouble())

  companion object {
    private val log = LoggerFactory.getLogger(TreeLayoutAlgorithm::class.java)

    /** The default horizontal node spacing. Initialized to 50. */
    const val DEFAULT_DISTX: Int = 50

    /** The default vertical node spacing. Initialized to 50. */
    const val DEFAULT_DISTY: Int = 50
  }
}
