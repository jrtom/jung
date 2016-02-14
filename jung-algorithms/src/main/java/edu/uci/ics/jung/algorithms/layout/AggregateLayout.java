/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 * 
 * 
 */
package edu.uci.ics.jung.algorithms.layout;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;

/**
 * A {@code Layout} implementation that combines 
 * multiple other layouts so that they may be manipulated
 * as one layout. The relaxer thread will step each layout
 * in sequence.
 * 
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class AggregateLayout<V, E> implements Layout<V,E>, IterativeContext {

	protected Layout<V,E> delegate;
	protected Map<Layout<V,E>,Point2D> layouts = new HashMap<Layout<V,E>,Point2D>();

	/**
	 * Creates an instance backed by the specified {@code delegate}.
	 * @param delegate the layout to which this instance is delegating
	 */
	public AggregateLayout(Layout<V, E> delegate) {
		this.delegate = delegate;
	}

	/**
	 * @return the delegate
	 */
	public Layout<V, E> getDelegate() {
		return delegate;
	}

	/**
	 * @param delegate the delegate to set
	 */
	public void setDelegate(Layout<V, E> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Adds the passed layout as a sublayout, and specifies
	 * the center of where this sublayout should appear.
	 * @param layout the layout algorithm to use as a sublayout
	 * @param center the center of the coordinates for the sublayout
	 */
	public void put(Layout<V,E> layout, Point2D center) {
		layouts.put(layout,center);
	}
	
	/**
	 * @param layout the layout whose center is to be returned
	 * @return the center of the passed layout
	 */
	public Point2D get(Layout<V,E> layout) {
		return layouts.get(layout);
	}
	
	/**
	 * Removes {@code layout} from this instance.
	 * @param layout the layout to remove
	 */
	public void remove(Layout<V,E> layout) {
		layouts.remove(layout);
	}
	
	/**
	 * Removes all layouts from this instance.
	 */
	public void removeAll() {
		layouts.clear();
	}
	
	public Graph<V, E> getGraph() {
		return delegate.getGraph();
	}

	public Dimension getSize() {
		return delegate.getSize();
	}

	public void initialize() {
		delegate.initialize();
		for(Layout<V,E> layout : layouts.keySet()) {
			layout.initialize();
		}
	}

	/**
	 * @param v the vertex whose locked state is to be returned
	 * @return true if v is locked in any of the layouts, and false otherwise
	 */
	public boolean isLocked(V v) {
		for(Layout<V,E> layout : layouts.keySet()) {
			if (layout.isLocked(v)) {
				return true;
			}
		}
		return delegate.isLocked(v);
	}

	/**
	 * Locks this vertex in the main layout and in any sublayouts whose graph contains
	 * this vertex.
	 * @param v the vertex whose locked state is to be set
	 * @param state {@code true} if the vertex is to be locked, and {@code false} if unlocked
	 */
	public void lock(V v, boolean state) {
		for(Layout<V,E> layout : layouts.keySet()) {
			if(layout.getGraph().getVertices().contains(v)) {
				layout.lock(v, state);
			}
		}
		delegate.lock(v, state);
	}

	public void reset() {
		for(Layout<V,E> layout : layouts.keySet()) {
			layout.reset();
		}
		delegate.reset();
	}

	public void setGraph(Graph<V, E> graph) {
		delegate.setGraph(graph);
	}

	public void setInitializer(Function<V, Point2D> initializer) {
		delegate.setInitializer(initializer);
	}

	public void setLocation(V v, Point2D location) {
		boolean wasInSublayout = false;
		for(Layout<V,E> layout : layouts.keySet()) {
			if(layout.getGraph().getVertices().contains(v)) {
				Point2D center = layouts.get(layout);
				// transform by the layout itself, but offset to the
				// center of the sublayout
				Dimension d = layout.getSize();

				AffineTransform at = 
					AffineTransform.getTranslateInstance(-center.getX()+d.width/2,-center.getY()+d.height/2);
				Point2D localLocation = at.transform(location, null);
				layout.setLocation(v, localLocation);
				wasInSublayout = true;
			}
		}
		if(wasInSublayout == false && getGraph().getVertices().contains(v)) {
			delegate.setLocation(v, location);
		}
	}

	public void setSize(Dimension d) {
		delegate.setSize(d);
	}
	
	/**
	 * @return a map from each {@code Layout} instance to its center point.
	 */
	public Map<Layout<V,E>,Point2D> getLayouts() {
		return layouts;
	}

	/**
	 * Returns the location of the vertex.  The location is specified first
	 * by the sublayouts, and then by the base layout if no sublayouts operate
	 * on this vertex.
	 * @return the location of the vertex
	 */
	public Point2D apply(V v) {
		boolean wasInSublayout = false;
		for(Layout<V,E> layout : layouts.keySet()) {
			if(layout.getGraph().getVertices().contains(v)) {
				wasInSublayout = true;
				Point2D center = layouts.get(layout);
				// transform by the layout itself, but offset to the
				// center of the sublayout
				Dimension d = layout.getSize();
				AffineTransform at = 
					AffineTransform.getTranslateInstance(center.getX()-d.width/2,
							center.getY()-d.height/2);
				return at.transform(layout.apply(v),null);
			}
		}
		if(wasInSublayout == false) {
			return delegate.apply(v);
		}
		return null;
	
	}

	/**
	 * @return {@code true} iff the delegate layout and all sublayouts are done
	 */
	public boolean done() {
		for (Layout<V,E> layout : layouts.keySet()) {
			if (layout instanceof IterativeContext) {
				if (! ((IterativeContext) layout).done() ) {
					return false;
				}
			}
		}
		if(delegate instanceof IterativeContext) {
			return ((IterativeContext)delegate).done();
		}
		return true;
	}

	/**
	 * Call step on any sublayout that is also an IterativeContext and is not done
	 */
	public void step() {
		for(Layout<V,E> layout : layouts.keySet()) {
			if(layout instanceof IterativeContext) {
				IterativeContext context = (IterativeContext)layout;
				if(context.done() == false) {
					context.step();
				}
			}
		}
		if(delegate instanceof IterativeContext) {
			IterativeContext context = (IterativeContext)delegate;
			if(context.done() == false) {
				context.step();
			}
		}
	}
	
}
