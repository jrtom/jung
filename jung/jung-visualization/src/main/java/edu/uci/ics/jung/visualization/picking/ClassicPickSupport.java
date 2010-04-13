/*
 * Created on Apr 11, 2005
 *
 * Copyright (c) 2005, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization.picking;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;


/**
 * <code>PickSupport</code> implementation that emulates the picking behavior
 * of versions of <code>VisualizationViewer</code> prior to version 1.6.
 * (<code>VisualizationViewer</code> still has this behavior by default, but
 * the picking behavior can now be changed.)
 * 
 * @see ShapePickSupport
 * 
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class ClassicPickSupport<V,E> extends RadiusPickSupport<V,E> implements GraphElementAccessor<V,E> {
    
    public ClassicPickSupport()
    {
        super();
    }
    
    /** 
     * @return null ClassicPickSupport does not do edges
     */
    public E getEdge(double x, double y) {
        return null;
    }
}