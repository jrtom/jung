/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Cursor
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.JComponent

/**
 * AnimatedPickingGraphMousePlugin supports the picking of one Graph Node. When the mouse is
 * released, the graph is translated so that the picked Node is moved to the center of the view.
 * This translation is conducted in an animation Thread so that the graph slides to its new position
 *
 * @author Tom Nelson
 */
open class AnimatedPickingGraphMousePlugin<N, E> @JvmOverloads constructor(
    selectionModifiers: Int = InputEvent.BUTTON1_MASK or InputEvent.CTRL_MASK
) : AbstractGraphMousePlugin(selectionModifiers), MouseListener, MouseMotionListener {

    /** the picked Node */
    protected var node: N? = null

    init {
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    /**
     * If the event occurs on a Node, pick that single Node
     *
     * @param e the event
     */
    @Suppress("UNCHECKED_CAST")
    override fun mousePressed(e: MouseEvent) {
        if (e.modifiers == modifiers) {
            val vv = e.source as VisualizationViewer<N, E>
            val layoutModel = vv.getModel().getLayoutModel()
            val pickSupport = vv.getPickSupport()
            val pickedNodeState = vv.getPickedNodeState()
            if (pickSupport != null && pickedNodeState != null) {
                // p is the screen point for the mouse event
                val p = e.point
                node = pickSupport.getNode(layoutModel, p.getX(), p.getY())
                val currentNode = node
                if (currentNode != null) {
                    if (!pickedNodeState.isPicked(currentNode)) {
                        pickedNodeState.clear()
                        pickedNodeState.pick(currentNode, true)
                    }
                }
            }
            e.consume()
        }
    }

    /**
     * If a Node was picked in the mousePressed event, start a Thread to animate the translation of
     * the graph so that the picked Node moves to the center of the view
     *
     * @param e the event
     */
    @Suppress("UNCHECKED_CAST")
    override fun mouseReleased(e: MouseEvent) {
        if (e.modifiers == modifiers) {
            val vv = e.source as VisualizationViewer<N, E>
            var newCenter: Point
            val currentNode = node
            if (currentNode != null) {
                // center the picked node
                val layoutModel = vv.getModel().getLayoutModel()
                newCenter = layoutModel.apply(currentNode)
            } else {
                // they did not pick a node to center, so
                // just center the graph
                val center = vv.getCenter()
                newCenter = Point.of(center.x, center.y)
            }
            val lvc =
                vv.getRenderContext().getMultiLayerTransformer().inverseTransform(vv.getCenter())
            val dx = (lvc.x - newCenter.x) / 10
            val dy = (lvc.y - newCenter.y) / 10

            val animator = Runnable {
                for (i in 0 until 10) {
                    vv.getRenderContext()
                        .getMultiLayerTransformer()
                        .getTransformer(Layer.LAYOUT)
                        .translate(dx, dy)
                    try {
                        Thread.sleep(100)
                    } catch (ex: InterruptedException) {
                    }
                }
            }
            val thread = Thread(animator)
            thread.start()
        }
    }

    override fun mouseClicked(e: MouseEvent) {}

    /** show a special cursor while the mouse is inside the window */
    override fun mouseEntered(e: MouseEvent) {
        val c = e.source as JComponent
        c.cursor = cursor
    }

    /** revert to the default cursor when the mouse leaves this window */
    override fun mouseExited(e: MouseEvent) {
        val c = e.source as JComponent
        c.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    override fun mouseMoved(e: MouseEvent) {}

    override fun mouseDragged(e: MouseEvent) {}
}
