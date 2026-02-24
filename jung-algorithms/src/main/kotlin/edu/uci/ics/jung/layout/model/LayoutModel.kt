/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.model

import com.google.common.collect.Maps
import com.google.common.graph.Graph
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.function.Function

/** two dimensional layoutmodel */
interface LayoutModel<N : Any> : Function<N, Point> {

  /**
   * @return the width of the layout area
   */
  val width: Int

  /**
   * @return the height of the layout area
   */
  val height: Int

  /**
   * allow the passed LayoutAlgorithm to operate on this LayoutModel
   *
   * @param layoutAlgorithm the algorithm to apply to this model's Points
   */
  fun accept(layoutAlgorithm: LayoutAlgorithm<N>)

  /**
   * @return a mapping of Nodes to Point locations
   */
  val locations: Map<N, Point>
    get() = Collections.unmodifiableMap(Maps.asMap(graph.nodes()) { this.apply(it) })

  /**
   * @param width to set
   * @param height to set
   */
  fun setSize(width: Int, height: Int)

  /** stop a relaxer Thread from continuing to operate */
  fun stopRelaxer()

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @param relaxing
   */
  fun setRelaxing(relaxing: Boolean)

  /**
   * indicates that there is a relaxer thread operating on this LayoutModel
   *
   * @return relaxing
   */
  val isRelaxing: Boolean

  /**
   * a handle to the relaxer thread; may be used to attach a process to run after relax is complete
   *
   * @return the CompletableFuture
   */
  val theFuture: CompletableFuture<*>?

  /**
   * @param node the node whose locked state is being queried
   * @return `true` if the position of node `v` is locked
   */
  fun isLocked(node: N): Boolean

  /**
   * Changes the layout coordinates of `node` to `location`.
   *
   * @param node the node whose location is to be specified
   * @param location the coordinates of the specified location
   */
  fun set(node: N, location: Point)

  /**
   * Changes the layout coordinates of `node` to `x, y`.
   *
   * @param node the node to set location for
   * @param x coordinate to set
   * @param y coordinate to set
   */
  fun set(node: N, x: Double, y: Double)

  /**
   * @param node the node of interest
   * @return the Point location for node
   */
  operator fun get(node: N): Point

  /**
   * @return the `Graph` that this model is mediating
   */
  var graph: Graph<N>

  fun lock(node: N, locked: Boolean)

  fun lock(locked: Boolean)

  val isLocked: Boolean

  fun setInitializer(initializer: Function<N, Point>)

  interface ChangeListener {
    fun changed()
  }

  /**
   * This exists so that LayoutModel will not have dependencies on java awt or swing event classes.
   * This event type tells the viewing system that it should re-draw itself to show the latest
   * changes.
   */
  interface ChangeSupport {

    var isFireEvents: Boolean

    fun addChangeListener(l: ChangeListener)

    fun removeChangeListener(l: ChangeListener)

    fun fireChanged()

    val changeListeners: Collection<ChangeListener>
  }

  val changeSupport: ChangeSupport

  /**
   * @return the support for LayoutStateChange events
   */
  val layoutStateChangeSupport: LayoutStateChangeSupport

  /** support for LayoutStateChangeEvents and their Listeners. */
  interface LayoutStateChangeSupport {
    var isFireEvents: Boolean

    fun addLayoutStateChangeListener(l: LayoutStateChangeListener)

    fun removeLayoutStateChangeListener(l: LayoutStateChangeListener)

    fun fireLayoutStateChanged(source: LayoutModel<*>, state: Boolean)

    val layoutStateChangeListeners: List<LayoutStateChangeListener>
  }

  /**
   * This event type alerts listeners whether the LayoutModel is active or not. When the layout
   * model is 'active', during the relax phase, a listener can choose not to update until the layout
   * model is inactive. The Spatial Data structures on the view side would waste a lot of competing
   * compute cycles staying up to date with a changing layout model.
   */
  class LayoutStateChangeEvent(
    val layoutModel: LayoutModel<*>,
    val active: Boolean
  ) {
    override fun toString(): String =
      "LayoutStateChangeEvent{layoutModel=$layoutModel, active=$active}"
  }

  /**
   * a consumer for a LayoutStateChangeEvent. In jung-visualization, this event is consumed by the
   * view side spatial data structures. This event stops them from recomputing the R-Trees while the
   * GraphLayoutAlgorithm is relaxing, and tells the view side to build the R-Trees as soon as the
   * relax work is complete.
   */
  interface LayoutStateChangeListener {
    fun layoutStateChanged(evt: LayoutStateChangeEvent)
  }
}

/** convenience extension for Java-style getWidth() */
fun <N : Any> LayoutModel<N>.getWidth(): Int = width

/** convenience extension for Java-style getHeight() */
fun <N : Any> LayoutModel<N>.getHeight(): Int = height

/** convenience extension for Java-style getGraph() */
fun <N : Any> LayoutModel<N>.getGraph(): Graph<N> = graph

/** convenience extension for Java-style getLocations() */
fun <N : Any> LayoutModel<N>.getLocations(): Map<N, Point> = locations
