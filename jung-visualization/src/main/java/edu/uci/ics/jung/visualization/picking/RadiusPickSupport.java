/*
 * Copyright (c) 2003, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
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
     * Creates an instance with the specified maximum distance.
     * @param maxDistance the farthest that a vertex can be from the selection point
     *     and still be 'picked'
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
     * @param layout the layout instance that records the positions for all vertices
     * @param x the x coordinate of the pick point
     * @param y the y coordinate of the pick point
	 * @param maxDistance vertices whose from (x, y) is &gt; this cannot be returned
     * @return the vertex whose center is closest to the pick point (x, y)
	 */
	public V getVertex(Layout<V,E> layout, double x, double y, double maxDistance) {
	    return super.getVertex(layout, x, y, maxDistance);
	}
	
	/**
	 * Gets the edge nearest to the location of the (x,y) location selected.
	 * Calls the longer form of the call.
     * @param layout the layout instance that records the positions for all vertices
     * @param x the x coordinate of the pick point
     * @param y the y coordinate of the pick point
     * @return the vertex whose center is closest to the pick point (x, y)
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
     * @param x the x coordinate of the pick point
     * @param y the y coordinate of the pick point
	 * @param maxDistance vertices whose from (x, y) is &gt; this cannot be returned
	 * @return Edge closest to the click.
	 */
	public E getEdge(Layout<V,E> layout, double x, double y, double maxDistance) {
	    return super.getEdge(layout, x, y, maxDistance);
	}
}
