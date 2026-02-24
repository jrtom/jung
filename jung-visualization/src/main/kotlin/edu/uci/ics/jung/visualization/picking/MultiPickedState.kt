/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Mar 28, 2005
 */
package edu.uci.ics.jung.visualization.picking

import java.awt.event.ItemEvent
import java.util.ArrayList
import java.util.Collections
import java.util.LinkedHashSet

/**
 * Maintains the state of what has been '_picked' in the graph. The `Sets` are constructed
 * so that their iterators will traverse them in the order in which they are _picked.
 *
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
open class MultiPickedState<T> : AbstractPickedState<T>(), PickedState<T> {

    /** the '_picked' nodes */
    protected val _picked: MutableSet<T> = LinkedHashSet()

    override fun pick(v: T, state: Boolean): Boolean {
        val priorState = _picked.contains(v)
        if (state) {
            _picked.add(v)
            if (!priorState) {
                fireItemStateChanged(
                    ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, v, ItemEvent.SELECTED)
                )
            }
        } else {
            _picked.remove(v)
            if (priorState) {
                fireItemStateChanged(
                    ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, v, ItemEvent.DESELECTED)
                )
            }
        }
        return priorState
    }

    override fun clear() {
        val unpicks = ArrayList(_picked)
        for (v in unpicks) {
            pick(v, false)
        }
        _picked.clear()
    }

    override fun getPicked(): Set<T> = Collections.unmodifiableSet(_picked)

    override fun isPicked(v: T): Boolean = _picked.contains(v)

    /** for the ItemSelectable interface contract */
    override fun getSelectedObjects(): Array<Any?> {
        val list = ArrayList<T>(_picked)
        return list.toTypedArray()
    }
}
