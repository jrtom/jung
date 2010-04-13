/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 * Created on March 10, 2005
 */
package edu.uci.ics.jung.visualization.decorators;

import java.awt.Shape;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;




/**
 * An interface for decorators that return a 
 * <code>Shape</code> for a specified edge.
 *  
 * @author Tom Nelson
 */
public abstract class AbstractEdgeShapeTransformer<V,E> implements Transformer<Context<Graph<V,E>,E>,Shape> {

    /**
     * Specifies how far apart to place the control points for edges being
     * drawn in parallel.
     */
    protected float control_offset_increment = 20.f;
    
    /**
     * Sets the value of <code>control_offset_increment</code>.
     */
    public void setControlOffsetIncrement(float y) {
        control_offset_increment = y;
    }
    
}
