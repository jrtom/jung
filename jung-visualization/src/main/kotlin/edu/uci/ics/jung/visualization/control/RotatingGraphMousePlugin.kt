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
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.util.Collections

/**
 * RotatingGraphMouse provides the abiity to rotate the graph using the mouse. By default, it is
 * activated by mouse button one drag with the shift key pressed. The modifiers can be overridden so
 * that a different mouse/key combination activates the rotation
 *
 * @author Tom Nelson
 */
open class RotatingGraphMousePlugin @JvmOverloads constructor(
    modifiers: Int = MouseEvent.BUTTON1_MASK or MouseEvent.SHIFT_MASK
) : AbstractGraphMousePlugin(modifiers), MouseListener, MouseMotionListener {

    init {
        val cd = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16)
        val cursorImage = BufferedImage(cd.width, cd.height, BufferedImage.TYPE_INT_ARGB)
        val g = cursorImage.createGraphics()
        g.addRenderingHints(
            Collections.singletonMap(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
            )
        )
        g.color = Color(0, 0, 0, 0)
        g.fillRect(0, 0, 16, 16)

        val left = 0
        val top = 0
        val right = 15
        val bottom = 15

        g.color = Color.white
        g.stroke = BasicStroke(3f)
        // top bent line
        g.drawLine(left + 2, top + 6, right / 2 + 1, top)
        g.drawLine(right / 2 + 1, top, right - 2, top + 5)
        // bottom bent line
        g.drawLine(left + 2, bottom - 6, right / 2, bottom)
        g.drawLine(right / 2, bottom, right - 2, bottom - 6)
        // top arrow
        g.drawLine(left + 2, top + 6, left + 5, top + 6)
        g.drawLine(left + 2, top + 6, left + 2, top + 3)
        // bottom arrow
        g.drawLine(right - 2, bottom - 6, right - 6, bottom - 6)
        g.drawLine(right - 2, bottom - 6, right - 2, bottom - 3)

        g.color = Color.black
        g.stroke = BasicStroke(1f)
        // top bent line
        g.drawLine(left + 2, top + 6, right / 2 + 1, top)
        g.drawLine(right / 2 + 1, top, right - 2, top + 5)
        // bottom bent line
        g.drawLine(left + 2, bottom - 6, right / 2, bottom)
        g.drawLine(right / 2, bottom, right - 2, bottom - 6)
        // top arrow
        g.drawLine(left + 2, top + 6, left + 5, top + 6)
        g.drawLine(left + 2, top + 6, left + 2, top + 3)
        // bottom arrow
        g.drawLine(right - 2, bottom - 6, right - 6, bottom - 6)
        g.drawLine(right - 2, bottom - 6, right - 2, bottom - 3)

        g.dispose()

        cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, Point(), "RotateCursor")
    }

    /**
     * save the 'down' point and check the modifiers. If the modifiers are accepted, set the cursor
     * to the 'hand' cursor
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

    /** unset the down point and change the cursor back to the default */
    override fun mouseReleased(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<*, *>
        down = null
        vv.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    /** check the modifiers. If accepted, use the mouse drag motion to rotate the graph */
    override fun mouseDragged(e: MouseEvent) {
        if (down == null) {
            return
        }
        val vv = e.source as VisualizationViewer<*, *>
        val accepted = checkModifiers(e)
        if (accepted) {
            val modelTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            // rotate
            vv.cursor = cursor

            val center = vv.getCenter()
            val q = down!!
            val p = e.point
            val v1 = Point2D.Double(center.x - p.x, center.y - p.y)
            val v2 = Point2D.Double(center.x - q.x, center.y - q.y)
            val theta = angleBetween(v1, v2)
            modelTransformer.rotate(
                theta,
                vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, center)!!
            )
            down!!.x = e.x
            down!!.y = e.y

            e.consume()
        }
    }

    /**
     * Returns the angle between two vectors from the origin to points v1 and v2.
     *
     * @param v1 the first point
     * @param v2 the second point
     * @return the angle between two vectors from the origin through points v1 and v2
     */
    protected fun angleBetween(v1: Point2D, v2: Point2D): Double {
        val x1 = v1.x
        val y1 = v1.y
        val x2 = v2.x
        val y2 = v2.y
        // cross product for direction
        val cross = x1 * y2 - x2 * y1
        val cw = if (cross > 0) -1 else 1
        // dot product for angle
        var angle = cw * Math.acos(
            (x1 * x2 + y1 * y2) /
                (Math.sqrt(x1 * x1 + y1 * y1) * Math.sqrt(x2 * x2 + y2 * y2))
        )
        if (java.lang.Double.isNaN(angle)) {
            angle = 0.0
        }
        return angle
    }

    override fun mouseClicked(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) {}

    override fun mouseMoved(e: MouseEvent) {}
}
