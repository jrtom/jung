/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */

package edu.uci.ics.jung.visualization.layout;

import java.awt.geom.Point2D;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.util.Caching;

/**
 * A LayoutDecorator that caches locations in a clearable Map. This can be used to ensure that
 * edge endpoints are always the same as vertex locations when they are drawn in the render loop 
 * during the time that the layout's relaxer thread is changing the locations.
 * 
 * @see LayoutDecorator
 * @author Tom Nelson 
 *
 */
public class CachingLayout<V, E> extends LayoutDecorator<V,E> implements Caching {
    
    protected LoadingCache<V, Point2D> locations;

    public CachingLayout(Layout<V, E> delegate) {
    	super(delegate);
    	Function<V, Point2D> chain = Functions.<V,Point2D,Point2D>compose(
    		new Function<Point2D,Point2D>() {
    			public Point2D apply(Point2D p) {
    				return (Point2D)p.clone();
    		}}, 
    		delegate);
    	this.locations = CacheBuilder.newBuilder().build(CacheLoader.from(chain));
    }
    
    @Override
    public void setGraph(Graph<V, E> graph) {
        delegate.setGraph(graph);
    }

	public void clear() {
	    this.locations = CacheBuilder.newBuilder().build(new CacheLoader<V, Point2D>() {
	    	public Point2D load(V vertex) {
	    		return new Point2D.Double();
	    	}
	    });
	}

	public void init() {
	}

	public Point2D apply(V v) {
		return locations.getUnchecked(v);
	}
}
