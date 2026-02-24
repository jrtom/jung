/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.layout

import com.google.common.collect.Maps
import com.google.common.graph.Graph
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.Point
import java.awt.geom.AffineTransform
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import org.slf4j.LoggerFactory

/**
 * A `Layout` implementation that combines multiple other layouts so that they may be
 * manipulated as one layout. The relaxer thread will step each layout in sequence.
 *
 * @author Tom Nelson
 * @param N the node type
 */
open class AggregateLayoutModel<N : Any>(protected val delegate: LayoutModel<N>) : LayoutModel<N> {

    protected val layouts: MutableMap<LayoutModel<N>, Point> = Maps.newHashMap()

    /**
     * Adds the passed layout as a sublayout, and specifies the center of where this sublayout should
     * appear.
     *
     * @param layoutModel the layout model to use as a sublayout
     * @param center the center of the coordinates for the sublayout model
     */
    fun put(layoutModel: LayoutModel<N>, center: Point) {
        if (log.isTraceEnabled) {
            log.trace("put layout: {} at {}", layoutModel, center)
        }
        layouts[layoutModel] = center
        connectListeners(layoutModel)
    }

    private fun connectListeners(newLayoutModel: LayoutModel<N>) {
        for (layoutStateChangeListener in delegate.layoutStateChangeSupport.layoutStateChangeListeners) {
            newLayoutModel.layoutStateChangeSupport.addLayoutStateChangeListener(layoutStateChangeListener)
        }
        for (changeListener in delegate.changeSupport.changeListeners) {
            newLayoutModel.changeSupport.addChangeListener(changeListener)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun disconnectListeners(newLayoutModel: LayoutModel<N>) {
        (newLayoutModel.layoutStateChangeSupport.layoutStateChangeListeners as MutableList).clear()
        (newLayoutModel.changeSupport.changeListeners as MutableCollection).clear()
    }

    /**
     * @param layout the layout whose center is to be returned
     * @return the center of the passed layout
     */
    operator fun get(layout: LayoutModel<N>): Point? = layouts[layout]

    override fun accept(layoutAlgorithm: LayoutAlgorithm<N>) {
        delegate.accept(layoutAlgorithm)
    }

    override val locations: Map<N, Point> get() = delegate.locations

    override fun setSize(width: Int, height: Int) {
        delegate.setSize(width, height)
    }

    override fun stopRelaxer() {
        delegate.stopRelaxer()
        for (childLayoutModel in layouts.keys) {
            childLayoutModel.stopRelaxer()
        }
    }

    override fun setRelaxing(relaxing: Boolean) {
        delegate.setRelaxing(relaxing)
    }

    override val isRelaxing: Boolean get() = delegate.isRelaxing

    override val theFuture: CompletableFuture<*>? get() = delegate.theFuture

    override fun set(node: N, location: Point) {
        delegate.set(node, location)
    }

    override fun set(node: N, x: Double, y: Double) {
        delegate.set(node, x, y)
    }

    override fun get(node: N): Point = delegate.get(node)

    override var graph: Graph<N>
        get() = delegate.graph
        set(value) { delegate.graph = value }

    /**
     * Removes `layout` from this instance.
     *
     * @param layout the layout to remove
     */
    fun remove(layout: LayoutModel<N>) {
        layouts.remove(layout)
    }

    /** Removes all layouts from this instance. */
    fun removeAll() {
        layouts.clear()
    }

    override val width: Int get() = delegate.width

    override val height: Int get() = delegate.height

    /**
     * @param node the node whose locked state is to be returned
     * @return true if v is locked in any of the layouts, and false otherwise
     */
    override fun isLocked(node: N): Boolean {
        for (layoutModel in layouts.keys) {
            if (layoutModel.isLocked(node)) {
                return true
            }
        }
        return delegate.isLocked(node)
    }

    /**
     * Locks this node in the main layout and in any sublayouts whose graph contains this node.
     *
     * @param node the node whose locked state is to be set
     * @param state `true` if the node is to be locked, and `false` if unlocked
     */
    override fun lock(node: N, state: Boolean) {
        for (layoutModel in layouts.keys) {
            if (layoutModel.graph.nodes().contains(node)) {
                layoutModel.lock(node, state)
            }
        }
        delegate.lock(node, state)
    }

    override fun lock(locked: Boolean) {
        delegate.lock(locked)
        for (model in layouts.keys) {
            model.lock(locked)
        }
    }

    override val isLocked: Boolean get() = delegate.isLocked

    override fun setInitializer(initializer: Function<N, Point>) {
        delegate.setInitializer(initializer)
    }

    override val layoutStateChangeSupport: LayoutModel.LayoutStateChangeSupport
        get() = delegate.layoutStateChangeSupport

    override val changeSupport: LayoutModel.ChangeSupport get() = delegate.changeSupport

    /**
     * Returns the location of the node. The location is specified first by the sublayouts, and then
     * by the base layout if no sublayouts operate on this node.
     *
     * @return the location of the node
     */
    override fun apply(node: N): Point {
        for (layoutModel in layouts.keys) {
            if (layoutModel.graph.nodes().contains(node)) {
                val center = layouts[layoutModel]!!
                // transform by the layout itself, but offset to the
                // center of the sublayout
                val width = layoutModel.width
                val height = layoutModel.height
                val at = AffineTransform.getTranslateInstance(
                    center.x - width / 2.0, center.y - height / 2.0
                )
                val nodeCenter = layoutModel.apply(node)
                log.trace("sublayout center is {}", nodeCenter)
                val srcPoints = doubleArrayOf(nodeCenter.x, nodeCenter.y)
                val destPoints = DoubleArray(2)
                at.transform(srcPoints, 0, destPoints, 0, 1)
                return Point.of(destPoints[0], destPoints[1])
            }
        }
        return delegate.apply(node)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AggregateLayoutModel::class.java)
    }
}
