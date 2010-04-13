/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Apr 16, 2005
 */

package edu.uci.ics.jung.visualization.transform.shape;

import java.awt.Shape;

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;

/**
 * Provides methods to map points from one coordinate system to
 * another: graph to screen and screen to graph.
 * 
 * @author Tom Nelson 
 */
public interface  ShapeTransformer extends BidirectionalTransformer {
    
    /**
     * map a shape from graph coordinate system to the
     * screen coordinate system
     * @param shape
     * @return a GeneralPath (Shape) representing the screen points of the shape
     */
    Shape transform(Shape shape);
    
    Shape inverseTransform(Shape shape);
}
