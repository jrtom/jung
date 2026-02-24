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

import java.awt.ItemSelectable

/**
 * An interface for classes that keep track of the "picked" state of edges or nodes.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
interface PickedState<T> : PickedInfo<T>, ItemSelectable {
    /**
     * Marks `v` as "picked" if `b == true`, and unmarks `v` as
     * picked if `b == false`.
     *
     * @param v the element to be picked/unpicked
     * @param b true if `v` is to be marked as picked, false if to be marked as unpicked
     * @return the "picked" state of `v` prior to this call
     */
    fun pick(v: T, b: Boolean): Boolean

    /** Clears the "picked" state from all elements. */
    fun clear()

    /**
     * @return all "picked" elements.
     */
    fun getPicked(): Set<T>

    /**
     * @return `true` if `v` is currently "picked".
     */
    override fun isPicked(v: T): Boolean
}
