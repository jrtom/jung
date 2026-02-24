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
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage
import java.util.Collections

/**
 * ShearingGraphMousePlugin allows the user to drag with the mouse to shear the transform either in
 * the horizontal or vertical direction. By default, the control or meta key must be depressed to
 * activate shearing.
 *
 * @author Tom Nelson
 */
open class ShearingGraphMousePlugin @JvmOverloads constructor(
    modifiers: Int = MouseEvent.BUTTON1_MASK or mask
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
        g.drawLine(left + 2, top + 5, right - 2, top + 5)
        g.drawLine(left + 2, bottom - 5, right - 2, bottom - 5)
        g.drawLine(left + 2, top + 5, left + 4, top + 3)
        g.drawLine(left + 2, top + 5, left + 4, top + 7)
        g.drawLine(right - 2, bottom - 5, right - 4, bottom - 3)
        g.drawLine(right - 2, bottom - 5, right - 4, bottom - 7)

        g.color = Color.black
        g.stroke = BasicStroke(1f)
        g.drawLine(left + 2, top + 5, right - 2, top + 5)
        g.drawLine(left + 2, bottom - 5, right - 2, bottom - 5)
        g.drawLine(left + 2, top + 5, left + 4, top + 3)
        g.drawLine(left + 2, top + 5, left + 4, top + 7)
        g.drawLine(right - 2, bottom - 5, right - 4, bottom - 3)
        g.drawLine(right - 2, bottom - 5, right - 4, bottom - 7)
        g.dispose()
        cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, Point(), "RotateCursor")
    }

    override fun mousePressed(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<*, *>
        val accepted = checkModifiers(e)
        down = e.point
        if (accepted) {
            vv.cursor = cursor
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<*, *>
        down = null
        vv.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    override fun mouseDragged(e: MouseEvent) {
        if (down == null) {
            return
        }
        val vv = e.source as VisualizationViewer<*, *>
        val accepted = checkModifiers(e)
        if (accepted) {
            val modelTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            vv.cursor = cursor
            val q = down!!
            val p = e.point
            val dx = (p.x - q.x).toFloat()
            val dy = (p.y - q.y).toFloat()

            val d = vv.size
            var shx = 2f * dx / d.height
            var shy = 2f * dy / d.width
            val center = vv.getCenter()
            if (p.x < center.x) {
                shy = -shy
            }
            if (p.y < center.y) {
                shx = -shx
            }
            modelTransformer.shear(shx.toDouble(), shy.toDouble(), center)
            down!!.x = e.x
            down!!.y = e.y

            e.consume()
        }
    }

    override fun mouseClicked(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) {}

    override fun mouseMoved(e: MouseEvent) {}

    companion object {
        @JvmStatic
        var mask = MouseEvent.CTRL_MASK
            internal set

        init {
            if (System.getProperty("os.name").startsWith("Mac")) {
                mask = MouseEvent.META_MASK
            }
        }
    }
}
