/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 * Created on Oct 9, 2004
 *
  */
package edu.uci.ics.jung.visualization.layout;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;

import edu.uci.ics.jung.algorithms.layout.Layout;

/**
 * interface for PersistentLayout
 * Also holds a nested class Point to serialize the
 * Vertex locations
 * 
 * @author Tom Nelson 
 */
public interface PersistentLayout<V, E> extends Layout<V,E> {
    
    void persist(String fileName) throws IOException;

    void restore(String fileName) throws IOException, ClassNotFoundException;
    
    void lock(boolean state);
    
    /**
     * a serializable class to save locations
     */
    @SuppressWarnings("serial")
	static class Point implements Serializable {
        public double x;
        public double y;
        public Point(double x, double y) {
            this.x=x;
            this.y=y;
        }
        public Point(Point2D p) {
        	this.x = p.getX();
        	this.y = p.getY();
        }
    }

}