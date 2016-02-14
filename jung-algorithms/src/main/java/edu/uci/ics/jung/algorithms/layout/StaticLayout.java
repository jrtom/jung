/*
 * Created on Jul 21, 2005
 *
 * Copyright (c) 2005, The JUNG Authors 
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
 * StaticLayout places the vertices in the locations specified by its initializer,
 * and has no other behavior.
 * Vertex locations can be placed in a {@code Map<V,Point2D>} and then supplied to
 * this layout as follows: {@code Function<V,Point2D> vertexLocations = Functions.forMap(map);}
 * @author Tom Nelson - tomnelson@dev.java.net
 */
public class StaticLayout<V, E> extends AbstractLayout<V,E> {
	
    public StaticLayout(Graph<V,E> graph, Function<V,Point2D> initializer, Dimension size) {
        super(graph, initializer, size);
    }
    
    public StaticLayout(Graph<V,E> graph, Function<V,Point2D> initializer) {
        super(graph, initializer);
    }
    
    public StaticLayout(Graph<V,E> graph) {
    	super(graph);
    }
    
    public StaticLayout(Graph<V,E> graph, Dimension size) {
    	super(graph, size);
    }
    
    public void initialize() {}

	public void reset() {}
}
