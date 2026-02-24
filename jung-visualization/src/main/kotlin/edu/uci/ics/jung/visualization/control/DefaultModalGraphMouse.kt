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

import java.awt.Component
import java.awt.Cursor
import java.awt.ItemSelectable
import java.awt.event.InputEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

/**
 * DefaultModalGraphMouse is a PluggableGraphMouse class that pre-installs a large collection of
 * plugins for picking and transforming the graph. Additionally, it carries the notion of a Mode:
 * Picking or Translating. Switching between modes allows for a more natural choice of mouse
 * modifiers to be used for the various plugins. The default modifiers are intended to mimick those
 * of mainstream software applications in order to be intuitive to users.
 *
 * To change between modes, two different controls are offered, a combo box and a menu system.
 * These controls are lazily created in their respective 'getter' methods so they don't impact code
 * that does not intend to use them. The menu control can be placed in an unused corner of the
 * GraphZoomScrollPane, which is a common location for mouse mode selection menus in mainstream
 * applications.
 *
 * @author Tom Nelson
 */
open class DefaultModalGraphMouse<N : Any, E : Any> @JvmOverloads constructor(
    `in`: Float = 1.1f,
    out: Float = 1 / 1.1f
) : AbstractModalGraphMouse(`in`, out), ModalGraphMouse, ItemSelectable {

    init {
        loadPlugins()
        modeKeyListener = ModeKeyAdapter(this)
    }

    /** create the plugins, and load the plugins for TRANSFORMING mode */
    override fun loadPlugins() {
        pickingPlugin = PickingGraphMousePlugin<N, E>()
        animatedPickingPlugin = AnimatedPickingGraphMousePlugin<N, E>()
        translatingPlugin = TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK)
        scalingPlugin = ScalingGraphMousePlugin(CrossoverScalingControl(), 0, `in`, out)
        rotatingPlugin = RotatingGraphMousePlugin()
        shearingPlugin = ShearingGraphMousePlugin()

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
