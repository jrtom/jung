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

import java.awt.Dimension
import java.awt.ItemSelectable
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.KeyListener
import javax.swing.ButtonGroup
import javax.swing.JComboBox
import javax.swing.JMenu
import javax.swing.JRadioButtonMenuItem
import javax.swing.event.EventListenerList
import javax.swing.plaf.basic.BasicIconFactory

/**
 * AbstractModalGraphMouse is a PluggableGraphMouse class that manages a collection of plugins for
 * picking and transforming the graph. Additionally, it carries the notion of a Mode: Picking or
 * Translating. Switching between modes allows for a more natural choice of mouse modifiers to be
 * used for the various plugins. The default modifiers are intended to mimick those of mainstream
 * software applications in order to be intuitive to users.
 *
 * To change between modes, two different controls are offered, a combo box and a menu system.
 * These controls are lazily created in their respective 'getter' methods so they don't impact code
 * that does not intend to use them. The menu control can be placed in an unused corner of the
 * GraphZoomScrollPane, which is a common location for mouse _mode selection menus in mainstream
 * applications.
 *
 * Users must implement the loadPlugins() method to create and install the GraphMousePlugins. The
 * order of the plugins is important, as they are evaluated against the mask parameters in the order
 * that they are added.
 *
 * @author Tom Nelson
 */
abstract class AbstractModalGraphMouse protected constructor(
    /** used by the scaling plugins for zoom in */
    protected var `in`: Float,
    /** used by the scaling plugins for zoom out */
    protected var out: Float
) : PluggableGraphMouse(), ModalGraphMouse, ItemSelectable {

    /** a listener for mode changes */
    protected var _modeListener: ItemListener? = null

    /** a JComboBox control available to set the mode */
    protected var modeBox: JComboBox<ModalGraphMouse.Mode>? = null

    /** a menu available to set the mode */
    @JvmField
    protected var modeMenu: JMenu? = null

    /** the current mode */
    @JvmField
    protected var mode: ModalGraphMouse.Mode? = null

    /** listeners for _mode changes */
    protected var listenerList: EventListenerList = EventListenerList()

    protected lateinit var pickingPlugin: GraphMousePlugin
    protected lateinit var translatingPlugin: GraphMousePlugin
    protected lateinit var animatedPickingPlugin: GraphMousePlugin
    protected lateinit var scalingPlugin: GraphMousePlugin
    protected lateinit var rotatingPlugin: GraphMousePlugin
    protected lateinit var shearingPlugin: GraphMousePlugin
    var modeKeyListener: KeyListener? = null

    /** create the plugins, and load the plugins for TRANSFORMING _mode */
    protected abstract fun loadPlugins()

    /** setter for the Mode. */
    override fun setMode(mode: ModalGraphMouse.Mode) {
        if (this.mode != mode) {
            fireItemStateChanged(
                ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this.mode, ItemEvent.DESELECTED)
            )
            this.mode = mode
            if (mode == ModalGraphMouse.Mode.TRANSFORMING) {
                setTransformingMode()
            } else if (mode == ModalGraphMouse.Mode.PICKING) {
                setPickingMode()
            }
            modeBox?.selectedItem = mode
            fireItemStateChanged(
                ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, mode, ItemEvent.SELECTED)
            )
        }
    }

    protected open fun setPickingMode() {
        remove(translatingPlugin)
        remove(rotatingPlugin)
        remove(shearingPlugin)
        add(pickingPlugin)
        add(animatedPickingPlugin)
    }

    protected open fun setTransformingMode() {
        remove(pickingPlugin)
        remove(animatedPickingPlugin)
        add(translatingPlugin)
        add(rotatingPlugin)
        add(shearingPlugin)
    }

    /**
     * @param zoomAtMouse The zoomAtMouse to set.
     */
    fun setZoomAtMouse(zoomAtMouse: Boolean) {
        (scalingPlugin as ScalingGraphMousePlugin).setZoomAtMouse(zoomAtMouse)
    }

    /** listener to set the _mode from an external event source */
    internal inner class ModeListener : ItemListener {
        override fun itemStateChanged(e: ItemEvent) {
            setMode(e.item as ModalGraphMouse.Mode)
        }
    }

    override fun getModeListener(): ItemListener {
        if (_modeListener == null) {
            _modeListener = ModeListener()
        }
        return _modeListener!!
    }

    /**
     * @return Returns the modeBox.
     */
    open fun getModeComboBox(): JComboBox<ModalGraphMouse.Mode> {
        if (modeBox == null) {
            modeBox = JComboBox(arrayOf(ModalGraphMouse.Mode.TRANSFORMING, ModalGraphMouse.Mode.PICKING))
            modeBox!!.addItemListener(getModeListener())
        }
        modeBox!!.selectedItem = mode
        return modeBox!!
    }

    /**
     * create (if necessary) and return a menu that will change the _mode
     *
     * @return the menu
     */
    open fun getModeMenu(): JMenu {
        if (modeMenu == null) {
            modeMenu = JMenu()
            val icon = BasicIconFactory.getMenuArrowIcon()
            modeMenu!!.icon = BasicIconFactory.getMenuArrowIcon()
            modeMenu!!.preferredSize = Dimension(icon.iconWidth + 10, icon.iconHeight + 10)

            val transformingButton = JRadioButtonMenuItem(ModalGraphMouse.Mode.TRANSFORMING.toString())
            transformingButton.addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    setMode(ModalGraphMouse.Mode.TRANSFORMING)
                }
            }

            val pickingButton = JRadioButtonMenuItem(ModalGraphMouse.Mode.PICKING.toString())
            pickingButton.addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    setMode(ModalGraphMouse.Mode.PICKING)
                }
            }

            val radio = ButtonGroup()
            radio.add(transformingButton)
            radio.add(pickingButton)
            transformingButton.isSelected = true
            modeMenu!!.add(transformingButton)
            modeMenu!!.add(pickingButton)
            modeMenu!!.toolTipText = "Menu for setting Mouse Mode"
            addItemListener { e ->
                if (e.stateChange == ItemEvent.SELECTED) {
                    if (e.item == ModalGraphMouse.Mode.TRANSFORMING) {
                        transformingButton.isSelected = true
                    } else if (e.item == ModalGraphMouse.Mode.PICKING) {
                        pickingButton.isSelected = true
                    }
                }
            }
        }
        return modeMenu!!
    }

    /** add a listener for _mode changes */
    override fun addItemListener(aListener: ItemListener) {
        listenerList.add(ItemListener::class.java, aListener)
    }

    /** remove a listener for _mode changes */
    override fun removeItemListener(aListener: ItemListener) {
        listenerList.remove(ItemListener::class.java, aListener)
    }

    /**
     * Returns an array of all the `ItemListener`s added to this JComboBox with
     * addItemListener().
     *
     * @return all of the `ItemListener`s added or an empty array if no listeners have been
     *     added
     * @since 1.4
     */
    fun getItemListeners(): Array<ItemListener> {
        return listenerList.getListeners(ItemListener::class.java)
    }

    override fun getSelectedObjects(): Array<Any> {
        return if (mode == null) {
            emptyArray()
        } else {
            arrayOf(mode!!)
        }
    }

    /**
     * Notifies all listeners that have registered interest for notification on this event type.
     *
     * @param e the event of interest
     * @see EventListenerList
     */
    protected fun fireItemStateChanged(e: ItemEvent) {
        // Guaranteed to return a non-null array
        val listeners = listenerList.listenerList
        // Process the listeners last to first, notifying
        // those that are interested in this event
        var i = listeners.size - 2
        while (i >= 0) {
            if (listeners[i] == ItemListener::class.java) {
                (listeners[i + 1] as ItemListener).itemStateChanged(e)
            }
            i -= 2
        }
    }
}
