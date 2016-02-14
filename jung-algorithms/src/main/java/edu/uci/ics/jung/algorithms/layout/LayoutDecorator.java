/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */

package edu.uci.ics.jung.algorithms.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;

/**
 * a pure decorator for the Layout interface. Intended to be overridden
 * to provide specific behavior decoration
 * 
 * @author Tom Nelson 
 *
 */
public abstract class LayoutDecorator<V, E> implements Layout<V, E>, IterativeContext {
    
    protected Layout<V, E> delegate;
    
	/**
	 * Creates an instance backed by the specified {@code delegate}.
	 * @param delegate the layout to which this instance is delegating
	 */
    public LayoutDecorator(Layout<V, E> delegate) {
        this.delegate = delegate;
    }

    /**
     * @return the backing (delegate) layout.
     */
    public Layout<V,E> getDelegate() {
        return delegate;
    }

    public void setDelegate(Layout<V,E> delegate) {
        this.delegate = delegate;
    }

    public void step() {
    	if(delegate instanceof IterativeContext) {
    		((IterativeContext)delegate).step();
    	}
    }

	public void initialize() {
		delegate.initialize();
	}

	public void setInitializer(Function<V, Point2D> initializer) {
		delegate.setInitializer(initializer);
	}

	public void setLocation(V v, Point2D location) {
		delegate.setLocation(v, location);
	}

    public Dimension getSize() {
        return delegate.getSize();
    }

    public Graph<V, E> getGraph() {
        return delegate.getGraph();
    }

    public Point2D transform(V v) {
        return delegate.apply(v);
    }

    public boolean done() {
    	if(delegate instanceof IterativeContext) {
    		return ((IterativeContext)delegate).done();
    	}
    	return true;
    }

    public void lock(V v, boolean state) {
        delegate.lock(v, state);
    }

    public boolean isLocked(V v) {
        return delegate.isLocked(v);
    }
    
    public void setSize(Dimension d) {
        delegate.setSize(d);
    }

    public void reset() {
    	delegate.reset();
    }
    
    public void setGraph(Graph<V, E> graph) {
        delegate.setGraph(graph);
    }
}
