/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Graph;

/**
 * A generalized interface is a mechanism for returning (x,y) coordinates 
 * from vertices. In general, most of these methods are used to both control and
 * get information from the layout algorithm.
 * <p>
 * @author danyelf
 * @author tom nelson
 */
public interface Layout<V, E> extends Function<V,Point2D> {
    
	/**
	 * Initializes fields in the node that may not have
	 * been set during the constructor. Must be called before
	 * the iterations begin.
	 */
	void initialize();
	
	/**
	 * @param initializer a function that specifies initial locations for all vertices
	 */
	void setInitializer(Function<V,Point2D> initializer);
    
	/**
	 * @param graph the graph that this algorithm is to operate on
	 */
    void setGraph(Graph<V,E> graph);

	/**
	 * @return the graph that this Layout refers to
	 */
	Graph<V,E> getGraph();
	
	void reset();
	
	/**
	 * @param d the space to use to lay out this graph
	 */
	void setSize(Dimension d);
	
	/**
	 * @return the current size of the visualization's space
	 */
	Dimension getSize();


	/**
	 * Locks or unlocks the specified vertex.  Locking the vertex fixes it at its current position,
	 * so that it will not be affected by the layout algorithm.  Unlocking it allows the layout
	 * algorithm to change the vertex's position.
     * 
	 * @param v	the vertex to lock/unlock
	 * @param state {@code true} to lock the vertex, {@code false} to unlock it
	 */
	void lock(V v, boolean state);

    /**
     * @param v the vertex whose locked state is being queried
     * @return <code>true</code> if the position of vertex <code>v</code> is locked
     */
    boolean isLocked(V v);

    /**
     * Changes the layout coordinates of {@code v} to {@code location}.
     * @param v the vertex whose location is to be specified
     * @param location the coordinates of the specified location
     */
	void setLocation(V v, Point2D location);
	
}
