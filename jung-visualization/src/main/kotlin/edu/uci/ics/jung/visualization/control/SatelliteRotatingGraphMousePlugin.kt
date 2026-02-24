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
import java.awt.geom.Point2D

/**
 * Mouse events in the SatelliteView that match the modifiers will cause the Main view to rotate
 *
 * @see RotatingGraphMousePlugin
 * @author Tom Nelson
 */
open class SatelliteRotatingGraphMousePlugin @JvmOverloads constructor(
    modifiers: Int = MouseEvent.BUTTON1_MASK or MouseEvent.SHIFT_MASK
) : RotatingGraphMousePlugin(modifiers) {

    /**
     * check the modifiers. If accepted, use the mouse drag motion to rotate the graph in the master
     * view
     */
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

                // rotate
                vv.cursor = cursor
                // I want to compute rotation based on the view coordinates of the
                // lens center in the satellite view.
                // translate the master view center to layout coords, then translate
                // that point to the satellite view's view coordinate system....
                val center = vv.getRenderContext().getMultiLayerTransformer().transform(
                    vvMaster.getRenderContext().getMultiLayerTransformer()
                        .inverseTransform(vvMaster.getCenter())
                )!!
                val q = down!!
                val p = e.point
                val v1 = Point2D.Double(center.x - p.x, center.y - p.y)
                val v2 = Point2D.Double(center.x - q.x, center.y - q.y)
                val theta = angleBetween(v1, v2)
                modelTransformerMaster.rotate(
                    -theta,
                    vvMaster.getRenderContext().getMultiLayerTransformer()
                        .inverseTransform(Layer.VIEW, vvMaster.getCenter())!!
                )
                down!!.x = e.x
                down!!.y = e.y
            }
            e.consume()
        }
    }
}
