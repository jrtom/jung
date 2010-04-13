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
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ChainedTransformer;
import org.apache.commons.collections15.functors.CloneTransformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.LayoutDecorator;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.util.Caching;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;

/**
 * A LayoutDecorator that fires ChangeEvents when certain methods 
 * are called. Used to wrap a Layout so that the visualization
 * components can be notified of changes.
 * 
 * @see LayoutDecorator
 * @author Tom Nelson 
 *
 */
public class ObservableCachingLayout<V, E> extends LayoutDecorator<V,E> implements ChangeEventSupport, Caching {
    
    protected ChangeEventSupport changeSupport =
        new DefaultChangeEventSupport(this);
    
    protected Map<V,Point2D> locationMap;

    public ObservableCachingLayout(Layout<V, E> delegate) {
    	super(delegate);
    	this.locationMap = LazyMap.<V,Point2D>decorate(new HashMap<V,Point2D>(), 
    			new ChainedTransformer<V, Point2D>(new Transformer[]{delegate, CloneTransformer.<Point2D>getInstance()}));
    }
    
    /**
     * @see edu.uci.ics.jung.algorithms.layout.Layout#step()
     */
    @Override
    public void step() {
    	super.step();
    	fireStateChanged();
    }

    /**
	 * 
	 * @see edu.uci.ics.jung.algorithms.layout.Layout#initialize()
	 */
	@Override
    public void initialize() {
		super.initialize();
		fireStateChanged();
	}
	
    /**
     * @see edu.uci.ics.jung.algorithms.util.IterativeContext#done()
     */
    @Override
    public boolean done() {
    	if(delegate instanceof IterativeContext) {
    		return ((IterativeContext)delegate).done();
    	}
    	return true;
    }


	/**
	 * @param v
	 * @param location
	 * @see edu.uci.ics.jung.algorithms.layout.Layout#setLocation(java.lang.Object, java.awt.geom.Point2D)
	 */
	@Override
    public void setLocation(V v, Point2D location) {
		super.setLocation(v, location);
		fireStateChanged();
	}

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public ChangeListener[] getChangeListeners() {
        return changeSupport.getChangeListeners();
    }

    public void fireStateChanged() {
        changeSupport.fireStateChanged();
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
	@Override
	public Point2D transform(V v) {
		return locationMap.get(v);
	}
}
