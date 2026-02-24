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

import javax.swing.event.ChangeListener

/**
 * The implementing class provides support for ChangeEvents.
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 */
interface ChangeEventSupport {

    fun addChangeListener(l: ChangeListener)

    /**
     * Removes a ChangeListener.
     *
     * @param l the listener to be removed
     */
    fun removeChangeListener(l: ChangeListener)

    /**
     * Returns an array of all the `ChangeListener`s added with addChangeListener().
     *
     * @return all of the `ChangeListener`s added or an empty array if no listeners have
     *     been added
     */
    fun getChangeListeners(): Array<ChangeListener>

    fun fireStateChanged()
}
