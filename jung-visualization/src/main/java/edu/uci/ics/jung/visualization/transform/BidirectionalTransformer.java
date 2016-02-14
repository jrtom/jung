/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Apr 16, 2005
 */

package edu.uci.ics.jung.visualization.transform;

import java.awt.geom.Point2D;

/**
 * Provides methods to map points from one coordinate system to
 * another: graph to screen and screen to graph.
 * 
 * @author Tom Nelson 
 */
public interface BidirectionalTransformer {
    
    /**
     * convert the supplied graph coordinate to the screen coordinate
     * @param p graph point to convert
     * @return screen point
     */
    Point2D transform(Point2D p);
    
    /**
     * convert the supplied screen coordinate to the graph coordinate.
     * @param p screen point to convert
     * @return the graph point
     */
    Point2D inverseTransform(Point2D p);
}
