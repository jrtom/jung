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

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Cursor
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener

/**
 * ViewTranslatingGraphMousePlugin uses a MouseButtonOne press and drag gesture to translate the
 * graph display in the x and y direction by changing the AffineTransform applied to the Graphics2D.
 * The default MouseButtonOne modifier can be overridden to cause a different mouse gesture to
 * translate the display.
 *
 * @author Tom Nelson
 */
open class ViewTranslatingGraphMousePlugin @JvmOverloads constructor(
    modifiers: Int = MouseEvent.BUTTON1_MASK
) : AbstractGraphMousePlugin(modifiers), MouseListener, MouseMotionListener {

    init {
        this.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
    }

    /**
     * Check the event modifiers. Set the 'down' point for later use. If this event satisfies the
     * modifiers, change the cursor to the system 'move cursor'
     *
     * @param e the event
     */
    override fun mousePressed(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<*, *>
        val accepted = checkModifiers(e)
        down = e.point
        if (accepted) {
            vv.cursor = cursor
        }
    }

    /** unset the 'down' point and change the cursoe back to the system default cursor */
    override fun mouseReleased(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<*, *>
        down = null
        vv.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    /**
     * chack the modifiers. If accepted, translate the graph according to the dragging of the mouse
     * pointer
     *
     * @param e the event
     */
    override fun mouseDragged(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<*, *>
        val accepted = checkModifiers(e)
        if (accepted) {
            val viewTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
            vv.cursor = cursor
            try {
                val q = viewTransformer.inverseTransform(down!!)
                val p = viewTransformer.inverseTransform(e.point)
                val dx = (p.x - q.x).toFloat()
                val dy = (p.y - q.y).toFloat()

                viewTransformer.translate(dx.toDouble(), dy.toDouble())
                down!!.x = e.x
                down!!.y = e.y
            } catch (ex: RuntimeException) {
                System.err.println("down = $down, e = $e")
                throw ex
            }

            e.consume()
        }
    }

    override fun mouseClicked(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) {}

    override fun mouseMoved(e: MouseEvent) {}
}
