/*
 * Created on Oct 19, 2004
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.util

import java.awt.geom.GeneralPath

/**
 * A utility class for creating arrowhead shapes.
 *
 * @author Joshua O'Madadhain
 */
object ArrowFactory {

    /**
     * Returns an arrowhead in the shape of a simple isosceles triangle with the specified base and
     * height measurements. It is placed with the vertical axis along the negative x-axis, with its
     * base centered on (0,0).
     *
     * @param base the width of the arrow's base
     * @param height the arrow's height
     * @return a path in the form of an isosceles triangle with dimensions `(base, height)`
     */
    @JvmStatic
    fun getWedgeArrow(base: Float, height: Float): GeneralPath {
        val arrow = GeneralPath()
        arrow.moveTo(0f, 0f)
        arrow.lineTo(-height, base / 2.0f)
        arrow.lineTo(-height, -base / 2.0f)
        arrow.lineTo(0f, 0f)
        return arrow
    }

    /**
     * Returns an arrowhead in the shape of an isosceles triangle with an isosceles-triangle notch
     * taken out of the base, with the specified base and height measurements. It is placed with the
     * vertical axis along the negative x-axis, with its base centered on (0,0).
     *
     * @param base the width of the arrow's base
     * @param height the arrow's height
     * @param notch_height the height of the arrow's notch
     * @return a path in the form of a notched isosceles triangle
     */
    @JvmStatic
    fun getNotchedArrow(base: Float, height: Float, notch_height: Float): GeneralPath {
        val arrow = GeneralPath()
        arrow.moveTo(0f, 0f)
        arrow.lineTo(-height, base / 2.0f)
        arrow.lineTo(-(height - notch_height), 0f)
        arrow.lineTo(-height, -base / 2.0f)
        arrow.lineTo(0f, 0f)
        return arrow
    }
}
