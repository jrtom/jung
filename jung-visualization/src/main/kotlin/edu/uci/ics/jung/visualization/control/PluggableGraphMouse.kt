/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 7, 2005
 */

package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.util.LinkedHashSet

/**
 * a GraphMouse that accepts plugins for various mouse events.
 *
 * @author Tom Nelson
 */
open class PluggableGraphMouse : VisualizationViewer.GraphMouse {

    private var mouseListeners: Array<MouseListener>? = null
    private var mouseMotionListeners: Array<MouseMotionListener>? = null
    private var mouseWheelListeners: Array<MouseWheelListener>? = null
    var mousePluginList: MutableSet<GraphMousePlugin> = LinkedHashSet()
    var mouseMotionPluginList: MutableSet<MouseMotionListener> = LinkedHashSet()
    var mouseWheelPluginList: MutableSet<MouseWheelListener> = LinkedHashSet()

    fun add(plugin: GraphMousePlugin) {
        if (plugin is MouseListener) {
            mousePluginList.add(plugin)
            mouseListeners = null
        }
        if (plugin is MouseMotionListener) {
            mouseMotionPluginList.add(plugin)
            mouseMotionListeners = null
        }
        if (plugin is MouseWheelListener) {
            mouseWheelPluginList.add(plugin)
            mouseWheelListeners = null
        }
    }

    fun remove(plugin: GraphMousePlugin) {
        if (plugin is MouseListener) {
            val wasThere = mousePluginList.remove(plugin)
            if (wasThere) {
                mouseListeners = null
            }
        }
        if (plugin is MouseMotionListener) {
            val wasThere = mouseMotionPluginList.remove(plugin)
            if (wasThere) {
                mouseMotionListeners = null
            }
        }
        if (plugin is MouseWheelListener) {
            val wasThere = mouseWheelPluginList.remove(plugin)
            if (wasThere) {
                mouseWheelListeners = null
            }
        }
    }

    private fun checkMouseListeners() {
        if (mouseListeners == null) {
            mouseListeners = mousePluginList.filterIsInstance<MouseListener>().toTypedArray()
        }
    }

    private fun checkMouseMotionListeners() {
        if (mouseMotionListeners == null) {
            mouseMotionListeners = mouseMotionPluginList.toTypedArray()
        }
    }

    private fun checkMouseWheelListeners() {
        if (mouseWheelListeners == null) {
            mouseWheelListeners = mouseWheelPluginList.toTypedArray()
        }
    }

    override fun mouseClicked(e: MouseEvent) {
        checkMouseListeners()
        for (listener in mouseListeners!!) {
            listener.mouseClicked(e)
            if (e.isConsumed) break
        }
    }

    override fun mousePressed(e: MouseEvent) {
        checkMouseListeners()
        for (listener in mouseListeners!!) {
            listener.mousePressed(e)
            if (e.isConsumed) break
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        checkMouseListeners()
        for (listener in mouseListeners!!) {
            listener.mouseReleased(e)
            if (e.isConsumed) break
        }
    }

    override fun mouseEntered(e: MouseEvent) {
        checkMouseListeners()
        for (listener in mouseListeners!!) {
            listener.mouseEntered(e)
            if (e.isConsumed) break
        }
    }

    override fun mouseExited(e: MouseEvent) {
        checkMouseListeners()
        for (listener in mouseListeners!!) {
            listener.mouseExited(e)
            if (e.isConsumed) break
        }
    }

    override fun mouseDragged(e: MouseEvent) {
        checkMouseMotionListeners()
        for (listener in mouseMotionListeners!!) {
            listener.mouseDragged(e)
            if (e.isConsumed) break
        }
    }

    override fun mouseMoved(e: MouseEvent) {
        checkMouseMotionListeners()
        for (listener in mouseMotionListeners!!) {
            listener.mouseMoved(e)
            if (e.isConsumed) break
        }
    }

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        checkMouseWheelListeners()
        for (listener in mouseWheelListeners!!) {
            listener.mouseWheelMoved(e)
            if (e.isConsumed) break
        }
    }
}
