/*
 * Created on Oct 19, 2004
 *
 * Copyright (c) 2004, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization.util;

import java.awt.geom.GeneralPath;

/**
 * A utility class for creating arrowhead shapes.
 * 
 * @author Joshua O'Madadhain
 */
public class ArrowFactory
{
    /**
     * Returns an arrowhead in the shape of a simple isosceles triangle
     * with the specified base and height measurements.  It is placed
     * with the vertical axis along the negative x-axis, with its base
     * centered on (0,0).
     */
    public static GeneralPath getWedgeArrow(float base, float height)
    {
        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(0,0);
        arrow.lineTo( - height, base/2.0f);
        arrow.lineTo( - height, -base/2.0f);
        arrow.lineTo( 0, 0 );
        return arrow;
    }

    /**
     * Returns an arrowhead in the shape of an isosceles triangle
     * with an isoceles-triangle notch taken out of the base,
     * with the specified base and height measurements.  It is placed
     * with the vertical axis along the negative x-axis, with its base
     * centered on (0,0).
     */
    public static GeneralPath getNotchedArrow(float base, float height, float notch_height)
    {
        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(0,0);
        arrow.lineTo(-height, base/2.0f);
        arrow.lineTo(-(height - notch_height), 0);
        arrow.lineTo(-height, -base/2.0f);
        arrow.lineTo(0,0);
        return arrow;
    }
}
