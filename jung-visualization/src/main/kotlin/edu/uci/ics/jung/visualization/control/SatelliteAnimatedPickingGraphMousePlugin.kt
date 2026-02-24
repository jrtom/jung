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
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener

/**
 * A version of the AnimatedPickingGraphMousePlugin that is for the SatelliteVisualizationViewer.
 * The difference it that when you pick a Node in the Satellite View, the 'master view' is
 * translated to move that Node to the center.
 *
 * @see AnimatedPickingGraphMousePlugin
 * @author Tom Nelson
 */
open class SatelliteAnimatedPickingGraphMousePlugin<N, E> @JvmOverloads constructor(
    selectionModifiers: Int = InputEvent.BUTTON1_MASK or InputEvent.CTRL_MASK
) : AnimatedPickingGraphMousePlugin<N, E>(selectionModifiers), MouseListener, MouseMotionListener {

    /** override subclass method to translate the master view instead of this satellite view */
    @Suppress("UNCHECKED_CAST")
    override fun mouseReleased(e: MouseEvent) {
        if (e.modifiers == modifiers) {
            val vv = e.source as VisualizationViewer<N, E>
            if (vv is SatelliteVisualizationViewer<*, *>) {
                val vvMaster = (vv as SatelliteVisualizationViewer<N, E>).master

                val currentNode = node
                if (currentNode != null) {
                    val layoutModel = vvMaster.getModel().getLayoutModel()
                    val q = layoutModel.apply(currentNode)
                    val lvc = vvMaster.getRenderContext().getMultiLayerTransformer()
                        .inverseTransform(Layer.LAYOUT, vvMaster.getCenter())!!
                    val dx = (lvc.x - q.x) / 10
                    val dy = (lvc.y - q.y) / 10

                    val animator = Runnable {
                        for (i in 0 until 10) {
                            vvMaster.getRenderContext().getMultiLayerTransformer()
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
        }
    }
}
