/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Dec 4, 2003
 */
package edu.uci.ics.jung.layout.algorithms

import com.google.common.base.Preconditions
import edu.uci.ics.jung.layout.model.LayoutModel
import java.util.ArrayList
import java.util.Collections
import org.slf4j.LoggerFactory

/**
 * A [Layout] implementation that positions nodes equally spaced on a regular circle.
 *
 * @author Masanori Harada
 * @author Tom Nelson - adapted to an algorithm
 */
class CircleLayoutAlgorithm<N : Any> : LayoutAlgorithm<N> {

  /**
   * @return the radius of the circle.
   */
  var radius: Double = 0.0

  private var nodeOrderedList: MutableList<N>? = null

  /**
   * Sets the order of the nodes in the layout according to the ordering specified by
   * [comparator].
   *
   * @param comparator the comparator to use to order the nodes
   */
  fun setNodeOrder(layoutModel: LayoutModel<N>, comparator: Comparator<N>) {
    if (nodeOrderedList == null) {
      nodeOrderedList = ArrayList(layoutModel.graph.nodes())
    }
    Collections.sort(nodeOrderedList!!, comparator)
  }

  /**
   * Sets the order of the nodes in the layout according to the ordering of [nodeList].
   *
   * @param nodeList a list specifying the ordering of the nodes
   */
  fun setNodeOrder(layoutModel: LayoutModel<N>, nodeList: List<N>) {
    Preconditions.checkArgument(
      nodeList.containsAll(layoutModel.graph.nodes()),
      "Supplied list must include all nodes of the graph"
    )
    this.nodeOrderedList = nodeList.toMutableList()
  }

  override fun visit(layoutModel: LayoutModel<N>) {
    setNodeOrder(layoutModel, ArrayList(layoutModel.graph.nodes()))

    val height = layoutModel.height.toDouble()
    val width = layoutModel.width.toDouble()

    if (radius <= 0) {
      radius = 0.45 * (if (height < width) height else width)
    }

    val orderedList = nodeOrderedList ?: return
    var i = 0
    for (node in orderedList) {
      val angle = (2 * Math.PI * i) / orderedList.size
      val posX = Math.cos(angle) * radius + width / 2
      val posY = Math.sin(angle) * radius + height / 2
      layoutModel.set(node, posX, posY)
      log.trace("set {} to {},{} ", node, posX, posY)
      i++
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(CircleLayoutAlgorithm::class.java)
  }
}
