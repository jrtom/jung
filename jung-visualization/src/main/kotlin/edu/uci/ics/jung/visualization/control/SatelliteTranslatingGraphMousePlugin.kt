/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 15, 2005
 */

package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.Cursor
import java.awt.event.MouseEvent

/**
 * Overrides TranslatingGraphMousePlugin so that mouse events in the satellite view cause
 * translating of the main view
 *
 * @see TranslatingGraphMousePlugin
 * @author Tom Nelson
 */
open class SatelliteTranslatingGraphMousePlugin : TranslatingGraphMousePlugin {

    constructor() : super()

    constructor(modifiers: Int) : super(modifiers)

    /**
     * Check the modifiers. If accepted, translate the main view according to the dragging of the
     * mouse pointer in the satellite view
     *
     * @param e the event
     */
    override fun mouseDragged(e: MouseEvent) {
        val vv = e.source as VisualizationViewer<*, *>
        val accepted = checkModifiers(e)
        if (accepted) {
            if (vv is SatelliteVisualizationViewer<*, *>) {
                val vvMaster = vv.master

                val modelTransformerMaster =
                    vvMaster.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
                vv.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                try {
                    val q = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down!!)
                    val p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.point)
                    val dx = (p.x - q.x).toFloat()
                    val dy = (p.y - q.y).toFloat()

                    modelTransformerMaster.translate((-dx).toDouble(), (-dy).toDouble())
                    down!!.x = e.x
                    down!!.y = e.y
                } catch (ex: RuntimeException) {
                    System.err.println("down = $down, e = $e")
                    throw ex
                }
            }
            e.consume()
        }
    }
}
