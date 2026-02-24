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
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener

/**
 * HyperbolicMagnificationGraphMousePlugin changes the magnification within the Hyperbolic
 * projection of the HyperbolicTransformer.
 *
 * @author Tom Nelson
 */
open class LensMagnificationGraphMousePlugin @JvmOverloads constructor(
    modifiers: Int = MouseEvent.CTRL_MASK,
    protected val floor: Float = 0.5f,
    protected val ceiling: Float = 4.0f,
    protected val delta: Float = .2f
) : AbstractGraphMousePlugin(modifiers), MouseWheelListener {

    /** override to check equality with a mask */
    override fun checkModifiers(e: MouseEvent): Boolean {
        return (e.modifiers and modifiers) != 0
    }

    private fun changeMagnification(lens: Lens, delta: Float) {
        var magnification = lens.magnification + delta
        magnification = Math.max(floor, magnification)
        magnification = Math.min(magnification, ceiling)
        lens.magnification = magnification
    }

    /** change magnification of the lens, depending on the direction of the mouse wheel motion. */
    override fun mouseWheelMoved(e: MouseWheelEvent) {
        val accepted = checkModifiers(e)
        var delta = this.delta
        if (accepted) {
            val vv = e.source as VisualizationViewer<*, *>
            val layoutTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
            val viewTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
            val amount = e.wheelRotation
            if (amount < 0) {
                delta = -delta
            }
            val lens = when {
                layoutTransformer is LensTransformer -> layoutTransformer.lens
                viewTransformer is LensTransformer -> viewTransformer.lens
                else -> null
            }
            if (lens != null) {
                changeMagnification(lens, delta)
            }
            vv.repaint()
            e.consume()
        }
    }
}
