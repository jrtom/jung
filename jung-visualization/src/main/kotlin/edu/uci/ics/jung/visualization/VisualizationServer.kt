/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization

import edu.uci.ics.jung.visualization.control.TransformSupport
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.picking.PickedState
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.spatial.Spatial
import java.awt.Graphics
import java.awt.RenderingHints.Key
import java.awt.Shape
import java.awt.geom.Point2D
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.EventListenerList

/**
 * @author Tom Nelson
 * @param N the node type
 * @param E the edge type
 */
interface VisualizationServer<N : Any, E : Any> {

    /**
     * Specify whether this class uses its offscreen image or not.
     *
     * @param doubleBuffered if true, then doubleBuffering in the superclass is set to 'false'
     */
    fun setDoubleBuffered(doubleBuffered: Boolean)

    /**
     * Returns whether this class uses double buffering. The superclass will be the opposite state.
     *
     * @return the double buffered state
     */
    fun isDoubleBuffered(): Boolean

    fun viewOnLayout(): Shape

    fun getNodeSpatial(): Spatial<N>

    fun setNodeSpatial(spatial: Spatial<N>)

    fun getEdgeSpatial(): Spatial<E>

    fun setEdgeSpatial(spatial: Spatial<E>)

    fun getTransformSupport(): TransformSupport<N, E>

    /**
     * @return the model.
     */
    fun getModel(): VisualizationModel<N, E>

    /**
     * @param model the model for this class to use
     */
    fun setModel(model: VisualizationModel<N, E>)

    /**
     * In response to changes from the model, repaint the view, then fire an event to any listeners.
     * Examples of listeners are the GraphZoomScrollPane and the BirdsEyeVisualizationViewer
     *
     * @param e the change event
     */
    fun stateChanged(e: ChangeEvent)

    /**
     * Sets the showing Renderer to be the input Renderer. Also tells the Renderer to refer to this
     * instance as a PickedKey. (Because Renderers maintain a small amount of state, such as the
     * PickedKey, it is important to create a separate instance for each VV instance.)
     *
     * @param r the renderer to use
     */
    fun setRenderer(r: Renderer<N, E>)

    /**
     * @return the renderer used by this instance.
     */
    fun getRenderer(): Renderer<N, E>

    /**
     * Makes the component visible if `aFlag` is true, or invisible if false.
     *
     * @param aFlag true iff the component should be visible
     * @see javax.swing.JComponent.setVisible
     */
    fun setVisible(aFlag: Boolean)

    /**
     * @return the renderingHints
     */
    fun getRenderingHints(): Map<Key, Any>

    /**
     * @param renderingHints The renderingHints to set.
     */
    fun setRenderingHints(renderingHints: Map<Key, Any>)

    /**
     * @param paintable The paintable to add.
     */
    fun addPreRenderPaintable(paintable: Paintable)

    /**
     * @param paintable The paintable to remove.
     */
    fun removePreRenderPaintable(paintable: Paintable)

    /**
     * @param paintable The paintable to add.
     */
    fun addPostRenderPaintable(paintable: Paintable)

    /**
     * @param paintable The paintable to remove.
     */
    fun removePostRenderPaintable(paintable: Paintable)

    /**
     * Adds a `ChangeListener`.
     *
     * @param l the listener to be added
     */
    fun addChangeListener(l: ChangeListener)

    /**
     * Removes a ChangeListener.
     *
     * @param l the listener to be removed
     */
    fun removeChangeListener(l: ChangeListener)

    /**
     * Returns an array of all the `ChangeListener`s added with addChangeListener().
     *
     * @return all of the `ChangeListener`s added or an empty array if no listeners have
     *     been added
     */
    fun getChangeListeners(): Array<ChangeListener>

    /**
     * Notifies all listeners that have registered interest for notification on this event type. The
     * event instance is lazily created.
     *
     * @see EventListenerList
     */
    fun fireStateChanged()

    /**
     * @return the node PickedState instance
     */
    fun getPickedNodeState(): PickedState<N>

    /**
     * @return the edge PickedState instance
     */
    fun getPickedEdgeState(): PickedState<E>

    fun setPickedNodeState(pickedNodeState: PickedState<N>)

    fun setPickedEdgeState(pickedEdgeState: PickedState<E>)

    /**
     * @return the NetworkElementAccessor
     */
    fun getPickSupport(): NetworkElementAccessor<N, E>

    /**
     * @param pickSupport The pickSupport to set.
     */
    fun setPickSupport(pickSupport: NetworkElementAccessor<N, E>)

    fun getCenter(): Point2D

    fun getRenderContext(): RenderContext<N, E>

    fun setRenderContext(renderContext: RenderContext<N, E>)

    fun repaint()

    /** an interface for the preRender and postRender */
    interface Paintable {
        fun paint(g: Graphics)
        fun useTransform(): Boolean
    }
}
