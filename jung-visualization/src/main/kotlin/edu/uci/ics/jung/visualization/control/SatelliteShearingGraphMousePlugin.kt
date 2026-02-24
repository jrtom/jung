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
import java.awt.event.MouseEvent

/**
 * Overrides ShearingGraphMousePlugin so that mouse events in the satellite view cause shearing of
 * the main view
 *
 * @see ShearingGraphMousePlugin
 * @author Tom Nelson
 */
open class SatelliteShearingGraphMousePlugin : ShearingGraphMousePlugin {

    constructor() : super()

    constructor(modifiers: Int) : super(modifiers)

    /** overridden to shear the main view */
    override fun mouseDragged(e: MouseEvent) {
        if (down == null) {
            return
        }
        val vv = e.source as VisualizationViewer<*, *>
        val accepted = checkModifiers(e)
        if (accepted) {
            if (vv is SatelliteVisualizationViewer<*, *>) {
                val vvMaster = vv.master

                val modelTransformerMaster =
                    vvMaster.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)

                vv.cursor = cursor
                val q = down!!
                val p = e.point
                val dx = (p.x - q.x).toFloat()
                val dy = (p.y - q.y).toFloat()

                val d = vv.size
                var shx = 2f * dx / d.height
                var shy = 2f * dy / d.width
                // I want to compute shear based on the view coordinates of the
                // lens center in the satellite view.
                // translate the master view center to layout coords, then translate
                // that point to the satellite view's view coordinate system....
                val center = vv.getRenderContext().getMultiLayerTransformer().transform(
                    vvMaster.getRenderContext().getMultiLayerTransformer()
                        .inverseTransform(vvMaster.getCenter())
                )!!
                if (p.x < center.x) {
                    shy = -shy
                }
                if (p.y < center.y) {
                    shx = -shx
                }
                modelTransformerMaster.shear((-shx).toDouble(), (-shy).toDouble(), vvMaster.getCenter())

                down!!.x = e.x
                down!!.y = e.y
            }
            e.consume()
        }
    }
}
