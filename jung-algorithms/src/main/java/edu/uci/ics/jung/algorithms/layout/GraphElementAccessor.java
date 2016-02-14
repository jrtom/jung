/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 *
 * Created on Apr 12, 2005
 */
package edu.uci.ics.jung.algorithms.layout;

import java.awt.Shape;
import java.util.Collection;

/**
 * Interface for coordinate-based selection of graph components.
 * @author Tom Nelson
 * @author Joshua O'Madadhain
 */
public interface GraphElementAccessor<V, E>
{
	/** 
     * Returns the vertex, if any, associated with (x, y).
     * 
     * @param layout the layout instance that records the positions for all vertices
     * @param x the x coordinate of the pick point
     * @param y the y coordinate of the pick point
     * @return the vertex associated with (x, y)
     */
    V getVertex(Layout<V,E> layout, double x, double y);
    
    /**
     * @param layout the layout instance that records the positions for all vertices
     * @param rectangle the region in which the returned vertices are located
     * @return the vertices whose locations given by {@code layout}
     *     are contained within {@code rectangle}
     */
    Collection<V> getVertices(Layout<V,E> layout, Shape rectangle);

    /**
	 * @param layout the context in which the location is defined
	 * @param x the x coordinate of the location
	 * @param y the y coordinate of the location
     * @return an edge which is associated with the location {@code (x,y)}
     *     as given by {@code layout}, generally by reference to the edge's endpoints
     */
    E getEdge(Layout<V,E> layout, double x, double y);
}