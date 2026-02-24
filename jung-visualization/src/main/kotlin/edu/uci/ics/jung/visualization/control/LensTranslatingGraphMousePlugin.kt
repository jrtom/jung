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
import edu.uci.ics.jung.visualization.transform.Lens
import edu.uci.ics.jung.visualization.transform.LensTransformer
import java.awt.Cursor
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.geom.Point2D

/**
 * Extends TranslatingGraphMousePlugin and adds the capability to drag and resize the viewing lens
 * in the graph view. Mouse1 in the center moves the lens, mouse1 on the edge resizes the lens. The
 * default mouse button and modifiers can be overridden in the constructor.
 *
 * @author Tom Nelson
 */
open class LensTranslatingGraphMousePlugin @JvmOverloads constructor(
    modifiers: Int = MouseEvent.BUTTON1_MASK
) : TranslatingGraphMousePlugin(modifiers), MouseListener, MouseMotionListener {

    protected var dragOnLens: Boolean = false
    protected var dragOnEdge: Boolean = false
    protected var edgeOffset: Double = 0.0

    /**
     * Check the event modifiers. Set the 'down' point for later use. If this event satisfies the
     * modifiers, change the cursor to the system 'move cursor'
     *
     * @param e the event
     */
    override fun mousePressed(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<*, *>
        val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
        val viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW)
        val layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT)
        var p: Point2D = e.point
        if (viewTransformer is LensTransformer) {
            p = (viewTransformer as LensTransformer).delegate.inverseTransform(p)
        } else {
            p = viewTransformer.inverseTransform(p)
        }
        val accepted = checkModifiers(e)
        if (accepted) {
            vv.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
            if (layoutTransformer is LensTransformer) {
                val lens = (layoutTransformer as LensTransformer).lens
                testViewCenter(lens, p)
            }
            if (viewTransformer is LensTransformer) {
                val lens = (viewTransformer as LensTransformer).lens
                testViewCenter(lens, p)
            }
            vv.repaint()
        }
        super.mousePressed(e)
    }

    /**
     * called to change the location of the lens
     *
     * @param lens
     * @param point
     */
    private fun setViewCenter(lens: Lens, point: Point2D) {
        lens.setCenter(point)
    }

    /**
     * called to change the radius of the lens
     *
     * @param lens
     * @param point
     */
    private fun setViewRadius(lens: Lens, point: Point2D) {
        val distanceFromCenter = lens.getDistanceFromCenter(point)
        lens.setRadius(distanceFromCenter + edgeOffset)
    }

    /**
     * called to set up translating the lens center or changing the layoutSize
     *
     * @param lens
     * @param point
     */
    private fun testViewCenter(lens: Lens, point: Point2D) {
        val distanceFromCenter = lens.getDistanceFromCenter(point)
        if (distanceFromCenter < 10.0) {
            lens.setCenter(point)
            dragOnLens = true
        } else if (Math.abs(distanceFromCenter - lens.getRadius()) < 10.0) {
            edgeOffset = lens.getRadius() - distanceFromCenter
            lens.setRadius(distanceFromCenter + edgeOffset)
            dragOnEdge = true
        }
    }

    /** unset the 'down' point and change the cursoe back to the system default cursor */
    override fun mouseReleased(e: MouseEvent) {
        super.mouseReleased(e)
        dragOnLens = false
        dragOnEdge = false
        edgeOffset = 0.0
    }

    /**
     * check the modifiers. If accepted, move or resize the lens according to the dragging of the
     * mouse pointer
     *
     * @param e the event
     */
    override fun mouseDragged(e: MouseEvent) {
        val accepted = checkModifiers(e)
        if (accepted) {

            val vv = e.source as VisualizationViewer<*, *>
            val multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer()
            val layoutTransformer = multiLayerTransformer.getTransformer(Layer.LAYOUT)
            val viewTransformer = multiLayerTransformer.getTransformer(Layer.VIEW)

            val lens = when {
                layoutTransformer is LensTransformer -> (layoutTransformer as LensTransformer).lens
                viewTransformer is LensTransformer -> (viewTransformer as LensTransformer).lens
                else -> null
            }
            if (lens != null) {
                var p: Point2D = e.point
                if (viewTransformer is LensTransformer) {
                    p = (viewTransformer as LensTransformer).delegate.inverseTransform(p)
                } else {
                    p = viewTransformer.inverseTransform(p)
                }

                vv.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                if (dragOnLens) {
                    setViewCenter(lens, p)
                    e.consume()
                    vv.repaint()

                } else if (dragOnEdge) {
                    setViewRadius(lens, p)
                    e.consume()
                    vv.repaint()

                } else {
                    super.mouseDragged(e)
                }
            }
        }
    }
}
