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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeListener;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.MapMaker;

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
public class ObservableCachingLayout<V, E> extends LayoutDecorator<V,E> 
	implements ChangeEventSupport, Caching, LayoutEventSupport<V,E> {
    
    protected ChangeEventSupport changeSupport =
        new DefaultChangeEventSupport(this);
    
    protected Map<V,Point2D> locationMap;
    
    private List<LayoutChangeListener<V,E>> layoutChangeListeners = 
    	new ArrayList<LayoutChangeListener<V,E>>();

    public ObservableCachingLayout(Layout<V, E> delegate) {
    	super(delegate);
    	this.locationMap = 
    		new MapMaker().makeComputingMap(
    				Functions.<V,Point2D,Point2D>compose(new Function<Point2D,Point2D>(){
//						@Override
						public Point2D apply(Point2D p) {
							return (Point2D)p.clone();
						}}, delegate)
    		);
//    		LazyMap.<V,Point2D>decorate(new HashMap<V,Point2D>(), 
//    			new ChainedTransformer<V, Point2D>(new Function[]{delegate, CloneTransformer.<Point2D>getInstance()}));
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
		fireLayoutChanged(v);
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
//	@Override
	public Point2D apply(V v) {
		return locationMap.get(v);
	}

	private void fireLayoutChanged(V v) {
		LayoutEvent<V,E> evt = new LayoutEvent<V,E>(v, this.getGraph());
		for(LayoutChangeListener<V,E> listener : layoutChangeListeners) {
			listener.layoutChanged(evt);
		}
	}
	
//	@Override
	public void addLayoutChangeListener(LayoutChangeListener<V, E> listener) {
		layoutChangeListeners.add(listener);
		
	}

//	@Override
	public void removeLayoutChangeListener(LayoutChangeListener<V, E> listener) {
		layoutChangeListeners.remove(listener);
		
	}
}
