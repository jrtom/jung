/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 18, 2005
 */
package edu.uci.ics.jung.visualization.util

import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.EventListenerList

/**
 * Basic implementation of ChangeEventSupport, using standard JDK classes.
 *
 * @author Tom Nelson
 */
open class DefaultChangeEventSupport(
    private val eventSource: Any
) : ChangeEventSupport {

    /** Holds the registered listeners. */
    protected val listenerList: EventListenerList = EventListenerList()

    /**
     * Only one `ChangeEvent` is needed since the event's only state is the source
     * property. The source of events generated is always "this".
     */
    @Transient
    protected var changeEvent: ChangeEvent? = null

    override fun addChangeListener(l: ChangeListener) {
        listenerList.add(ChangeListener::class.java, l)
    }

    override fun removeChangeListener(l: ChangeListener) {
        listenerList.remove(ChangeListener::class.java, l)
    }

    override fun getChangeListeners(): Array<ChangeListener> =
        listenerList.getListeners(ChangeListener::class.java)

    /**
     * Notifies all listeners that have registered interest for notification on this event type. The
     * event instance is lazily created. The primary listeners will be views that need to be repainted
     * because of changes in this model instance.
     *
     * @see EventListenerList
     */
    override fun fireStateChanged() {
        // Guaranteed to return a non-null array
        val listeners = listenerList.listenerList
        // Process the listeners last to first, notifying
        // those that are interested in this event
        var i = listeners.size - 2
        while (i >= 0) {
            if (listeners[i] === ChangeListener::class.java) {
                // Lazily create the event:
                if (changeEvent == null) {
                    changeEvent = ChangeEvent(eventSource)
                }
                (listeners[i + 1] as ChangeListener).stateChanged(changeEvent)
            }
            i -= 2
        }
    }
}
