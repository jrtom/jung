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

import java.awt.Component
import java.awt.Cursor
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

/**
 * an implementation of the AbstractModalGraphMouse that includes plugins for manipulating a view
 * that is using a LensTransformer.
 *
 * @author Tom Nelson
 */
open class ModalLensGraphMouse @JvmOverloads constructor(
    `in`: Float = 1.1f,
    out: Float = 1 / 1.1f,
    /** not included in the base class */
    protected var magnificationPlugin: LensMagnificationGraphMousePlugin = LensMagnificationGraphMousePlugin()
) : AbstractModalGraphMouse(`in`, out), ModalGraphMouse {

    init {
        loadPlugins()
        modeKeyListener = ModeKeyAdapter(this)
    }

    override fun loadPlugins() {
        pickingPlugin = LensPickingGraphMousePlugin<Any, Any>()
        translatingPlugin = LensTranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK)
        scalingPlugin = ScalingGraphMousePlugin(CrossoverScalingControl(), 0, `in`, out)
        rotatingPlugin = RotatingGraphMousePlugin()
        shearingPlugin = ShearingGraphMousePlugin()

        add(magnificationPlugin)
        add(scalingPlugin)

        setMode(ModalGraphMouse.Mode.TRANSFORMING)
    }

    open class ModeKeyAdapter : KeyAdapter {
        private var t = 't'
        private var p = 'p'
        protected var graphMouse: ModalGraphMouse

        constructor(graphMouse: ModalGraphMouse) {
            this.graphMouse = graphMouse
        }

        constructor(t: Char, p: Char, graphMouse: ModalGraphMouse) {
            this.t = t
            this.p = p
            this.graphMouse = graphMouse
        }

        override fun keyTyped(event: KeyEvent) {
            val keyChar = event.keyChar
            if (keyChar == t) {
                (event.source as Component).cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)
            } else if (keyChar == p) {
                (event.source as Component).cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                graphMouse.setMode(ModalGraphMouse.Mode.PICKING)
            }
        }
    }
}
