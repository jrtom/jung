/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.annotations

import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse
import edu.uci.ics.jung.visualization.control.AnimatedPickingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.RotatingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.ShearingGraphMousePlugin
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.ItemSelectable
import java.awt.event.InputEvent
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.ButtonGroup
import javax.swing.JComboBox
import javax.swing.JMenu
import javax.swing.JRadioButtonMenuItem
import javax.swing.plaf.basic.BasicIconFactory

/**
 * a graph mouse that supplies an annotations mode
 *
 * @author Tom Nelson
 * @param N the node type
 * @param E the edge type
 */
open class AnnotatingModalGraphMouse<N : Any, E : Any> : AbstractModalGraphMouse,
    ModalGraphMouse, ItemSelectable {

    protected val annotatingPlugin: AnnotatingGraphMousePlugin<N, E>
    protected val basicTransformer: MultiLayerTransformer
    protected val rc: RenderContext<N, E>

    /**
     * Create an instance with default values for scale in (1.1) and scale out (1/1.1).
     *
     * @param rc the RenderContext for which this class will be used
     * @param annotatingPlugin the plugin used by this class for annotating
     */
    constructor(
        rc: RenderContext<N, E>, annotatingPlugin: AnnotatingGraphMousePlugin<N, E>
    ) : this(rc, annotatingPlugin, 1.1f, 1 / 1.1f)

    /**
     * Create an instance with the specified scale in and scale out values.
     *
     * @param rc the RenderContext for which this class will be used
     * @param annotatingPlugin the plugin used by this class for annotating
     * @param inScale override value for scale in
     * @param outScale override value for scale out
     */
    constructor(
        rc: RenderContext<N, E>,
        annotatingPlugin: AnnotatingGraphMousePlugin<N, E>,
        inScale: Float,
        outScale: Float
    ) : super(inScale, outScale) {
        this.rc = rc
        this.basicTransformer = rc.getMultiLayerTransformer()
        this.annotatingPlugin = annotatingPlugin
        loadPlugins()
        modeKeyListener = ModeKeyAdapter(this)
    }

    /** create the plugins, and load the plugins for TRANSFORMING mode */
    override fun loadPlugins() {
        this.pickingPlugin = PickingGraphMousePlugin<N, E>()
        this.animatedPickingPlugin = AnimatedPickingGraphMousePlugin<N, E>()
        this.translatingPlugin = TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK)
        this.scalingPlugin = ScalingGraphMousePlugin(CrossoverScalingControl(), 0, `in`, out)
        this.rotatingPlugin = RotatingGraphMousePlugin()
        this.shearingPlugin = ShearingGraphMousePlugin()
        add(scalingPlugin)
        setMode(ModalGraphMouse.Mode.TRANSFORMING)
    }

    /** setter for the Mode. */
    override fun setMode(mode: ModalGraphMouse.Mode) {
        if (this.mode != mode) {
            fireItemStateChanged(
                ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED)
            )
            this.mode = mode
            when (mode) {
                ModalGraphMouse.Mode.TRANSFORMING -> setTransformingMode()
                ModalGraphMouse.Mode.PICKING -> setPickingMode()
                ModalGraphMouse.Mode.ANNOTATING -> setAnnotatingMode()
                else -> {}
            }
            modeBox?.selectedItem = mode
            fireItemStateChanged(
                ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED)
            )
        }
    }

    override fun setPickingMode() {
        remove(translatingPlugin)
        remove(rotatingPlugin)
        remove(shearingPlugin)
        remove(annotatingPlugin)
        add(pickingPlugin)
        add(animatedPickingPlugin)
    }

    override fun setTransformingMode() {
        remove(pickingPlugin)
        remove(animatedPickingPlugin)
        remove(annotatingPlugin)
        add(translatingPlugin)
        add(rotatingPlugin)
        add(shearingPlugin)
    }

    protected fun setEditingMode() {
        remove(pickingPlugin)
        remove(animatedPickingPlugin)
        remove(translatingPlugin)
        remove(rotatingPlugin)
        remove(shearingPlugin)
        remove(annotatingPlugin)
    }

    protected fun setAnnotatingMode() {
        remove(pickingPlugin)
        remove(animatedPickingPlugin)
        remove(translatingPlugin)
        remove(rotatingPlugin)
        remove(shearingPlugin)
        add(annotatingPlugin)
    }

    /**
     * @return Returns the modeBox.
     */
    override fun getModeComboBox(): JComboBox<ModalGraphMouse.Mode> {
        if (modeBox == null) {
            modeBox = JComboBox(
                arrayOf(
                    ModalGraphMouse.Mode.TRANSFORMING,
                    ModalGraphMouse.Mode.PICKING,
                    ModalGraphMouse.Mode.ANNOTATING
                )
            )
            modeBox!!.addItemListener(getModeListener())
        }
        modeBox!!.selectedItem = mode
        return modeBox!!
    }

    /**
     * create (if necessary) and return a menu that will change the mode
     *
     * @return the menu
     */
    override fun getModeMenu(): JMenu {
        if (modeMenu == null) {
            modeMenu = JMenu()
            val icon = BasicIconFactory.getMenuArrowIcon()
            modeMenu!!.icon = BasicIconFactory.getMenuArrowIcon()
            modeMenu!!.preferredSize = Dimension(icon.iconWidth + 10, icon.iconHeight + 10)

            val transformingButton = JRadioButtonMenuItem(ModalGraphMouse.Mode.TRANSFORMING.toString())
            transformingButton.addItemListener(object : ItemListener {
                override fun itemStateChanged(e: ItemEvent) {
                    if (e.stateChange == ItemEvent.SELECTED) {
                        setMode(ModalGraphMouse.Mode.TRANSFORMING)
                    }
                }
            })

            val pickingButton = JRadioButtonMenuItem(ModalGraphMouse.Mode.PICKING.toString())
            pickingButton.addItemListener(object : ItemListener {
                override fun itemStateChanged(e: ItemEvent) {
                    if (e.stateChange == ItemEvent.SELECTED) {
                        setMode(ModalGraphMouse.Mode.PICKING)
                    }
                }
            })

            val radio = ButtonGroup()
            radio.add(transformingButton)
            radio.add(pickingButton)
            transformingButton.isSelected = true
            modeMenu!!.add(transformingButton)
            modeMenu!!.add(pickingButton)
            modeMenu!!.toolTipText = "Menu for setting Mouse Mode"
            addItemListener(object : ItemListener {
                override fun itemStateChanged(e: ItemEvent) {
                    if (e.stateChange == ItemEvent.SELECTED) {
                        when (e.item) {
                            ModalGraphMouse.Mode.TRANSFORMING -> transformingButton.isSelected = true
                            ModalGraphMouse.Mode.PICKING -> pickingButton.isSelected = true
                        }
                    }
                }
            })
        }
        return modeMenu!!
    }

    open class ModeKeyAdapter : KeyAdapter {
        private val t: Char
        private val p: Char
        private val a: Char
        protected val graphMouse: ModalGraphMouse

        constructor(graphMouse: ModalGraphMouse) {
            this.t = 't'
            this.p = 'p'
            this.a = 'a'
            this.graphMouse = graphMouse
        }

        constructor(t: Char, p: Char, a: Char, graphMouse: ModalGraphMouse) {
            this.t = t
            this.p = p
            this.a = a
            this.graphMouse = graphMouse
        }

        override fun keyTyped(event: KeyEvent) {
            val keyChar = event.keyChar
            when (keyChar) {
                t -> {
                    (event.source as Component).cursor =
                        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING)
                }
                p -> {
                    (event.source as Component).cursor =
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    graphMouse.setMode(ModalGraphMouse.Mode.PICKING)
                }
                a -> {
                    (event.source as Component).cursor =
                        Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                    graphMouse.setMode(ModalGraphMouse.Mode.ANNOTATING)
                }
            }
        }
    }
}
