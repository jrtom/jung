/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 26, 2005
 */

package edu.uci.ics.jung.visualization.control

import java.awt.event.InputEvent

/**
 * @author Tom Nelson
 */
open class ModalSatelliteGraphMouse @JvmOverloads constructor(
    `in`: Float = 1.1f,
    out: Float = 1 / 1.1f
) : DefaultModalGraphMouse<Any, Any>(`in`, out), ModalGraphMouse {

    override fun loadPlugins() {
        pickingPlugin = PickingGraphMousePlugin<Any, Any>()
        animatedPickingPlugin = SatelliteAnimatedPickingGraphMousePlugin<Any, Any>()
        translatingPlugin = SatelliteTranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK)
        scalingPlugin = SatelliteScalingGraphMousePlugin(CrossoverScalingControl(), 0)
        rotatingPlugin = SatelliteRotatingGraphMousePlugin()
        shearingPlugin = SatelliteShearingGraphMousePlugin()

        add(scalingPlugin)

        setMode(ModalGraphMouse.Mode.TRANSFORMING)
    }
}
