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

import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

/**
 * ScalingGraphMouse applies a scaling transformation to the graph layout. The Nodes get closer or
 * farther apart, but do not themselves change layoutSize. ScalingGraphMouse uses MouseWheelEvents
 * to apply the scaling.
 *
 * @author Tom Nelson
 */
open class ScalingGraphMousePlugin(
    /** controls scaling operations */
    var scaler: ScalingControl,
    modifiers: Int,
    /** the amount to zoom in by */
    var `in`: Float = 1.1f,
    /** the amount to zoom out by */
    var out: Float = 1 / 1.1f
) : AbstractGraphMousePlugin(modifiers), MouseWheelListener {

    /** whether to center the zoom at the current mouse position */
    var _zoomAtMouse: Boolean = true

    fun setZoomAtMouse(zoomAtMouse: Boolean) {
        this._zoomAtMouse = _zoomAtMouse
    }

    override fun checkModifiers(e: MouseEvent): Boolean {
        return e.modifiers == modifiers || (e.modifiers and modifiers) != 0
    }

    /** zoom the display in or out, depending on the direction of the mouse wheel motion. */
    override fun mouseWheelMoved(e: MouseWheelEvent) {
        val accepted = checkModifiers(e)
        if (accepted) {
            val vv = e.source as VisualizationViewer<*, *>
            val mouse = e.point
            val center = vv.getCenter()
            val amount = e.wheelRotation
            if (_zoomAtMouse) {
                if (amount > 0) {
                    scaler.scale(vv, `in`, mouse)
                } else if (amount < 0) {
                    scaler.scale(vv, out, mouse)
                }
            } else {
                if (amount > 0) {
                    scaler.scale(vv, `in`, center)
                } else if (amount < 0) {
                    scaler.scale(vv, out, center)
                }
            }
            e.consume()
            vv.repaint()
        }
    }
}
