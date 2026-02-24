/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 6, 2005
 */

package edu.uci.ics.jung.visualization.control

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
 * Simple extension of MouseAdapter that supplies modifier checking
 *
 * @author Tom Nelson
 */
open class GraphMouseAdapter(
    var modifiers: Int
) : MouseAdapter() {

    protected fun checkModifiers(e: MouseEvent): Boolean {
        return e.modifiers == modifiers
    }
}
