/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 23, 2005
 */

package edu.uci.ics.jung.visualization.layout;

import java.awt.geom.Point2D;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.MapMaker;

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
    
    protected Map<V,Point2D> locationMap;

    public CachingLayout(Layout<V, E> delegate) {
    	super(delegate);
    	this.locationMap = 
    		new MapMaker().makeComputingMap(
    				Functions.<V,Point2D,Point2D>compose(new Function<Point2D,Point2D>(){
//						@Override
						public Point2D apply(Point2D p) {
							return (Point2D)p.clone();
						}}, delegate)
    		);
    		//LazyMap.<V,Point2D>decorate(new HashMap<V,Point2D>(), 
    		//	new ChainedTransformer<V, Point2D>(new Function[]{delegate, CloneTransformer.<Point2D>getInstance()}));
    }
    
    @Override
    public void setGraph(Graph<V, E> graph) {
        delegate.setGraph(graph);
    }

	public void clear() {
		this.locationMap.clear();
	}

	public void init() {
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.layout.LayoutDecorator#transform(java.lang.Object)
	 */
//	@Override
	public Point2D apply(V v) {
		return locationMap.get(v);
	}
}
