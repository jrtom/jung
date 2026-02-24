/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.control

import java.awt.event.MouseEvent
import java.awt.event.MouseListener

abstract class AbstractPopupGraphMousePlugin @JvmOverloads constructor(
    modifiers: Int = MouseEvent.BUTTON3_MASK
) : AbstractGraphMousePlugin(modifiers), MouseListener {

    override fun mousePressed(e: MouseEvent) {
        if (e.isPopupTrigger) {
            handlePopup(e)
            e.consume()
        }
    }

    /** if this is the popup trigger, process here, otherwise defer to the superclass */
    override fun mouseReleased(e: MouseEvent) {
        if (e.isPopupTrigger) {
            handlePopup(e)
            e.consume()
        }
    }

    protected abstract fun handlePopup(e: MouseEvent)

    override fun mouseClicked(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) {}
}
