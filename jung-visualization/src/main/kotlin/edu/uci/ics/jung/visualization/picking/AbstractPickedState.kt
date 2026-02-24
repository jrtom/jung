/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Apr 2, 2005
 */
package edu.uci.ics.jung.visualization.picking

import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.event.EventListenerList

/**
 * An abstract class to support ItemEvents for PickedState
 *
 * @author Tom Nelson
 */
abstract class AbstractPickedState<T> : PickedState<T> {

    protected val listenerList = EventListenerList()

    override fun addItemListener(l: ItemListener) {
        listenerList.add(ItemListener::class.java, l)
    }

    override fun removeItemListener(l: ItemListener) {
        listenerList.remove(ItemListener::class.java, l)
    }

    protected fun fireItemStateChanged(e: ItemEvent) {
        val listeners = listenerList.listenerList
        var i = listeners.size - 2
        while (i >= 0) {
            if (listeners[i] === ItemListener::class.java) {
                (listeners[i + 1] as ItemListener).itemStateChanged(e)
            }
            i -= 2
        }
    }
}
