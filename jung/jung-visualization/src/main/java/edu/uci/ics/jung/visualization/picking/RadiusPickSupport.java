/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
/*
 * Created on Mar 19, 2005
 *
 */
package edu.uci.ics.jung.visualization.picking;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.RadiusGraphElementAccessor;



/**
 * Simple implementation of PickSupport that returns the vertex or edge
 * that is closest to the specified location.  This implementation
 * provides the same picking options that were available in
 * previous versions of AbstractLayout.
 * 
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public class RadiusPickSupport<V, E> 
    extends RadiusGraphElementAccessor<V, E> implements GraphElementAccessor<V,E> {
    
    public RadiusPickSupport() {
        this(Math.sqrt(Double.MAX_VALUE - 1000));
    }
    
    /**
     * the layout will always be provided by the VisualizationViewer
     * this is supporting picking for
     * @param maxDistance
     */
    public RadiusPickSupport(double maxDistance) {
        super(maxDistance);
    }
    
	/**
	 * Gets the vertex nearest to the location of the (x,y) location selected,
	 * within a distance of <tt>maxDistance</tt>. Iterates through all
	 * visible vertices and checks their distance from the click. Override this
	 * method to provide a more efficient implementation.
	 */
	public V getVertex(Layout<V,E> layout, double x, double y) {
	    return getVertex(layout, x, y, this.maxDistance);
	}

	/**
	 * Gets the vertex nearest to the location of the (x,y) location selected,
	 * within a distance of <tt>maxDistance</tt>. Iterates through all
	 * visible vertices and checks their distance from the click. Override this
	 * method to provide a more efficient implementation.
	 * @param x
	 * @param y
	 * @param maxDistance temporarily overrides member maxDistance
	 */
	public V getVertex(Layout<V,E> layout, double x, double y, double maxDistance) {
	    return super.getVertex(layout, x, y, maxDistance);
	}
	
	/**
	 * Gets the edge nearest to the location of the (x,y) location selected.
	 * Calls the longer form of the call.
	 */
	public E getEdge(Layout<V,E> layout, double x, double y) {
	    return getEdge(layout, x, y, this.maxDistance);
	}

	/**
	 * Gets the edge nearest to the location of the (x,y) location selected,
	 * within a distance of <tt>maxDistance</tt>, Iterates through all
	 * visible edges and checks their distance from the click. Override this
	 * method to provide a more efficient implementation.
	 * 
	 * @param x
	 * @param y
	 * @param maxDistance temporarily overrides member maxDistance
	 * @return Edge closest to the click.
	 */
	public E getEdge(Layout<V,E> layout, double x, double y, double maxDistance) {
	    return super.getEdge(layout, x, y, maxDistance);
	}
}
