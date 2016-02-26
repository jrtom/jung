/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 * Created on Jul 7, 2003
 * 
 */
package edu.uci.ics.jung.algorithms.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.graph.Graph;

/**
 * Abstract class for implementations of {@code Layout}.  It handles some of the
 * basic functions: storing coordinates, maintaining the dimensions, initializing
 * the locations, maintaining locked vertices.
 * 
 * @author Danyel Fisher, Scott White
 * @author Tom Nelson - converted to jung2
 * @param <V> the vertex type
 * @param <E> the edge type
 */
abstract public class AbstractLayout<V, E> implements Layout<V,E> {

    /**
     * A set of vertices that are fixed in place and not affected by the layout algorithm
     */
	private Set<V> dontmove = new HashSet<V>();

	protected Dimension size;
	protected Graph<V, E> graph;
	protected boolean initialized;

    protected LoadingCache<V, Point2D> locations =
    	CacheBuilder.newBuilder().build(new CacheLoader<V, Point2D>() {
	    	public Point2D load(V vertex) {
	    		return new Point2D.Double();
	    	}
    });

	/**
	 * Creates an instance for {@code graph} which does not initialize the vertex locations.
	 * 
	 * @param graph the graph on which the layout algorithm is to operate
	 */
	protected AbstractLayout(Graph<V, E> graph) {
	    if (graph == null) 
	    {
	        throw new IllegalArgumentException("Graph must be non-null");
	    }
		this.graph = graph;
	}
	
	/**
	 * Creates an instance for {@code graph} which initializes the vertex locations
	 * using {@code initializer}.
	 * 
	 * @param graph the graph on which the layout algorithm is to operate
	 * @param initializer specifies the starting positions of the vertices
	 */
    protected AbstractLayout(Graph<V,E> graph, Function<V,Point2D> initializer) {
		this.graph = graph;
		Function<V, Point2D> chain = 
			Functions.<V,Point2D,Point2D>compose(
					new Function<Point2D,Point2D>(){
						public Point2D apply(Point2D p) {
							return (Point2D)p.clone();
						}}, 
					initializer
					);
		this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain)); 
		initialized = true;
	}
	
	/**
	 * Creates an instance for {@code graph} which sets the size of the layout to {@code size}.
	 * 
	 * @param graph the graph on which the layout algorithm is to operate
     * @param size the dimensions of the region in which the layout algorithm will place vertices
	 */
	protected AbstractLayout(Graph<V,E> graph, Dimension size) {
		this.graph = graph;
		this.size = size;
	}
	
	/**
	 * Creates an instance for {@code graph} which initializes the vertex locations
	 * using {@code initializer} and sets the size of the layout to {@code size}.
	 * 
	 * @param graph the graph on which the layout algorithm is to operate
	 * @param initializer specifies the starting positions of the vertices
     * @param size the dimensions of the region in which the layout algorithm will place vertices
	 */
    protected AbstractLayout(Graph<V,E> graph, Function<V,Point2D> initializer, Dimension size) {
		this.graph = graph;
		Function<V, Point2D> chain = 
			Functions.<V,Point2D,Point2D>compose(
					new Function<Point2D,Point2D>(){
						public Point2D apply(Point2D p) {
							return (Point2D)p.clone();
						}}, 
					initializer
					);
		this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain)); 
		this.size = size;
	}
    
    public void setGraph(Graph<V,E> graph) {
        this.graph = graph;
        if(size != null && graph != null) {
        	initialize();
        }
    }
    
	/**
	 * When a visualization is resized, it presumably wants to fix the
	 * locations of the vertices and possibly to reinitialize its data. The
	 * current method calls <tt>initializeLocations</tt> followed by <tt>initialize_local</tt>.
	 */
	public void setSize(Dimension size) {
		
		if(size != null && graph != null) {
			
			Dimension oldSize = this.size;
			this.size = size;
			initialize();
			
			if(oldSize != null) {
				adjustLocations(oldSize, size);
			}
		}
	}
	
	private void adjustLocations(Dimension oldSize, Dimension size) {

		int xOffset = (size.width - oldSize.width) / 2;
		int yOffset = (size.height - oldSize.height) / 2;

		// now, move each vertex to be at the new screen center
		while(true) {
		    try {
                for(V v : getGraph().getVertices()) {
		            offsetVertex(v, xOffset, yOffset);
		        }
		        break;
		    } catch(ConcurrentModificationException cme) {
		    }
		}
	}
    
    public boolean isLocked(V v) {
        return dontmove.contains(v);
    }
    
    public void setInitializer(Function<V,Point2D> initializer) {
    	if(this.equals(initializer)) {
    		throw new IllegalArgumentException("Layout cannot be initialized with itself");
    	}
		Function<V, Point2D> chain = 
			Functions.<V,Point2D,Point2D>compose(
					new Function<Point2D,Point2D>(){
						public Point2D apply(Point2D p) {
							return (Point2D)p.clone();
						}}, 
					initializer
					);
		this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain)); 
    	initialized = true;
    }
    
	/**
	 * Returns the current size of the visualization space, accoring to the
	 * last call to resize().
	 * 
	 * @return the current size of the screen
	 */
	public Dimension getSize() {
		return size;
	}

	/**
	 * Returns the Coordinates object that stores the vertex' x and y location.
	 * 
	 * @param v
	 *            A Vertex that is a part of the Graph being visualized.
	 * @return A Coordinates object with x and y locations.
	 */
	private Point2D getCoordinates(V v) {
        return locations.getUnchecked(v);
	}
	
	public Point2D apply(V v) {
		return getCoordinates(v);
	}
	
	/**
	 * Returns the x coordinate of the vertex from the Coordinates object.
	 * in most cases you will be better off calling transform(v).
	 * 
	 * @param v the vertex whose x coordinate is to be returned
	 * @return the x coordinate of {@code v}
	 */
	public double getX(V v) {
        Preconditions.checkNotNull(getCoordinates(v), "Cannot getX for an unmapped vertex "+v);
        return getCoordinates(v).getX();
	}

	/**
	 * Returns the y coordinate of the vertex from the Coordinates object.
	 * In most cases you will be better off calling transform(v).
	 * 
	 * @param v the vertex whose y coordinate is to be returned
	 * @return the y coordinate of {@code v}
	 */
	public double getY(V v) {
        Preconditions.checkNotNull(getCoordinates(v), "Cannot getY for an unmapped vertex "+v);
        return getCoordinates(v).getY();
	}
	
	/**
	 * @param v the vertex whose coordinates are to be offset
	 * @param xOffset the change to apply to this vertex's x coordinate
	 * @param yOffset the change to apply to this vertex's y coordinate
	 */
	protected void offsetVertex(V v, double xOffset, double yOffset) {
		Point2D c = getCoordinates(v);
        c.setLocation(c.getX()+xOffset, c.getY()+yOffset);
		setLocation(v, c);
	}

	/**
	 * @return the graph that this layout operates on
	 */
	public Graph<V, E> getGraph() {
	    return graph;
	}
	
	/**
	 * Forcibly moves a vertex to the (x,y) location by setting its x and y
	 * locations to the specified location. Does not add the vertex to the
	 * "dontmove" list, and (in the default implementation) does not make any
	 * adjustments to the rest of the graph.
	 * @param picked the vertex whose location is being set
	 * @param x the x coordinate of the location to set
	 * @param y the y coordinate of the location to set
	 */
	public void setLocation(V picked, double x, double y) {
		Point2D coord = getCoordinates(picked);
		coord.setLocation(x, y);
	}

	public void setLocation(V picked, Point2D p) {
		Point2D coord = getCoordinates(picked);
		coord.setLocation(p);
	}

	/**
	 * Locks {@code v} in place if {@code state} is {@code true}, otherwise unlocks it.
	 * @param v the vertex whose position is to be (un)locked
	 * @param state {@code true} if the vertex is to be locked, {@code false} if to be unlocked
	 */
	public void lock(V v, boolean state) {
		if(state == true) 
		    dontmove.add(v);
		else 
		    dontmove.remove(v);
	}
	
	/**
	 * @param lock {@code true} to lock all vertices in place, {@code false} to unlock all vertices
	 */
	public void lock(boolean lock) {
		for(V v : graph.getVertices()) {
			lock(v, lock);
		}
	}
}
