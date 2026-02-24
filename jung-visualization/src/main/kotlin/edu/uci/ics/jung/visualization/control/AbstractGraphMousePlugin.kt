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

import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent

/**
 * a base class for GraphMousePlugin instances. Holds some members common to all GraphMousePlugins
 *
 * @author thomasnelson
 */
abstract class AbstractGraphMousePlugin(
    /** modifiers to compare against mouse event modifiers */
    override var modifiers: Int
) : GraphMousePlugin {

    /** the location in the View where the mouse was pressed */
    protected var down: Point? = null

    /** the special cursor that plugins may display */
    var cursor: Cursor? = null

    /**
     * check the mouse event modifiers against the instance member modifiers. Default implementation
     * checks equality. Can be overridden to test with a mask
     */
    override fun checkModifiers(e: MouseEvent): Boolean {
        return e.modifiers == modifiers
    }
}
