/*
 * Copyright (c) 2005, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 11, 2005
 *
 */
package edu.uci.ics.jung.visualization.picking;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;

/**
 * A <code>GraphElementAccessor</code> that returns elements whose <code>Shape</code>
 * contains the specified pick point or region.
 * 
 * @author Tom Nelson
 *
 */
public class ShapePickSupport<V, E> implements GraphElementAccessor<V,E> {

	/**
	 * The available picking heuristics:
     * <ul>
     * <li><code>Style.CENTERED</code>: returns the element whose 
     * center is closest to the pick point.
     * <li><code>Style.LOWEST</code>: returns the first such element
     * encountered.  (If the element collection has a consistent
     * ordering, this will also be the element "on the bottom", 
     * that is, the one which is rendered first.) 
     * <li><code>Style.HIGHEST</code>: returns the last such element
     * encountered.  (If the element collection has a consistent
     * ordering, this will also be the element "on the top", 
     * that is, the one which is rendered last.)
     * </ul>
	 *
	 */
	public static enum Style { LOWEST, CENTERED, HIGHEST };

    protected float pickSize;
    
    /**
     * The <code>VisualizationServer</code> in which the 
     * this instance is being used for picking.  Used to 
     * retrieve properties such as the layout, renderer,
     * vertex and edge shapes, and coordinate transformations.
     */
    protected VisualizationServer<V,E> vv;
    
    /**
     * The current picking heuristic for this instance.  Defaults
     * to <code>CENTERED</code>.
     */
    protected Style style = Style.CENTERED;
    
    /**
     * Creates a <code>ShapePickSupport</code> for the <code>vv</code>
     * VisualizationServer, with the specified pick footprint and
     * the default pick style.
     * The <code>VisualizationServer</code> is used to access 
     * properties of the current visualization (layout, renderer,
     * coordinate transformations, vertex/edge shapes, etc.).
     * @param vv source of the current <code>Layout</code>.
     * @param pickSize the size of the pick footprint for line edges
     */
    public ShapePickSupport(VisualizationServer<V,E> vv, float pickSize) {
    	this.vv = vv;
        this.pickSize = pickSize;
    }
    
    /**
     * Create a <code>ShapePickSupport</code> for the specified
     * <code>VisualizationServer</code> with a default pick footprint.
     * of size 2.
     * @param vv the visualization server used for rendering
     */
    public ShapePickSupport(VisualizationServer<V,E> vv) {
        this.vv = vv;
        this.pickSize = 2;
    }
    
    /**
     * Returns the style of picking used by this instance.
     * This specifies which of the elements, among those
     * whose shapes contain the pick point, is returned.
     * The available styles are:
     * <ul>
     * <li><code>Style.CENTERED</code>: returns the element whose 
     * center is closest to the pick point.
     * <li><code>Style.LOWEST</code>: returns the first such element
     * encountered.  (If the element collection has a consistent
     * ordering, this will also be the element "on the bottom", 
     * that is, the one which is rendered first.) 
     * <li><code>Style.HIGHEST</code>: returns the last such element
     * encountered.  (If the element collection has a consistent
     * ordering, this will also be the element "on the top", 
     * that is, the one which is rendered last.)
     * </ul>
     * 
	 * @return the style of picking used by this instance
	 */
	public Style getStyle() {
		return style;
	}

	/**
	 * Specifies the style of picking to be used by this instance.
     * This specifies which of the elements, among those
     * whose shapes contain the pick point, will be returned.
     * The available styles are:
     * <ul>
     * <li><code>Style.CENTERED</code>: returns the element whose 
     * center is closest to the pick point.
     * <li><code>Style.LOWEST</code>: returns the first such element
     * encountered.  (If the element collection has a consistent
     * ordering, this will also be the element "on the bottom", 
     * that is, the one which is rendered first.) 
     * <li><code>Style.HIGHEST</code>: returns the last such element
     * encountered.  (If the element collection has a consistent
     * ordering, this will also be the element "on the top", 
     * that is, the one which is rendered last.)
     * </ul>
	 * @param style the style to set
	 */
	public void setStyle(Style style) {
		this.style = style;
	}

	/** 
     * Returns the vertex, if any, whose shape contains (x, y).
     * If (x,y) is contained in more than one vertex's shape, returns
     * the vertex whose center is closest to the pick point.
     * 
     * @param layout the layout instance that records the positions for all vertices
     * @param x the x coordinate of the pick point
     * @param y the y coordinate of the pick point
     * @return the vertex whose shape contains (x,y), and whose center is closest to the pick point
     */
    public V getVertex(Layout<V, E> layout, double x, double y) {

        V closest = null;
        double minDistance = Double.MAX_VALUE;
        Point2D ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, 
        		new Point2D.Double(x,y));
        x = ip.getX();
        y = ip.getY();

        while(true) {
            try {
                for(V v : getFilteredVertices(layout)) {
                	
                    Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(v);
                    // get the vertex location
                    Point2D p = layout.apply(v);
                    if(p == null) continue;
                    // transform the vertex location to screen coords
                    p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);
                    
                    double ox = x - p.getX();
                    double oy = y - p.getY();

                    if(shape.contains(ox, oy)) {
                    	
                    	if(style == Style.LOWEST) {
                    		// return the first match
                    		return v;
                    	} else if(style == Style.HIGHEST) {
                    		// will return the last match
                    		closest = v;
                    	} else {
                    		
                    		// return the vertex closest to the
                    		// center of a vertex shape
	                        Rectangle2D bounds = shape.getBounds2D();
	                        double dx = bounds.getCenterX() - ox;
	                        double dy = bounds.getCenterY() - oy;
	                        double dist = dx * dx + dy * dy;
	                        if (dist < minDistance) {
	                        	minDistance = dist;
	                        	closest = v;
	                        }
                    	}
                    }
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }
        return closest;
    }

    /**
     * Returns the vertices whose layout coordinates are contained in 
     * <code>Shape</code>.
     * The shape is in screen coordinates, and the graph vertices
     * are transformed to screen coordinates before they are tested
     * for inclusion.
     * @return the <code>Collection</code> of vertices whose <code>layout</code>
     * coordinates are contained in <code>shape</code>.
     */
    public Collection<V> getVertices(Layout<V, E> layout, Shape shape) {
    	Set<V> pickedVertices = new HashSet<V>();
    	
    	// remove the view transform from the rectangle
    	shape = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, shape);

        while(true) {
            try {
                for(V v : getFilteredVertices(layout)) {
                    Point2D p = layout.apply(v);
                    if(p == null) continue;

                    p = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);
                    if(shape.contains(p)) {
                    	pickedVertices.add(v);
                    }
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }
        return pickedVertices;
    }
    
    /**
     * Returns an edge whose shape intersects the 'pickArea' footprint of the passed
     * x,y, coordinates.
     * 
	 * @param layout the context in which the location is defined
	 * @param x the x coordinate of the location
	 * @param y the y coordinate of the location
     * @return an edge whose shape intersects the pick area centered on the location {@code (x,y)}
     */
    public E getEdge(Layout<V, E> layout, double x, double y) {

        Point2D ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, new Point2D.Double(x,y));
        x = ip.getX();
        y = ip.getY();

        // as a Line has no area, we can't always use edgeshape.contains(point) so we
        // make a small rectangular pickArea around the point and check if the
        // edgeshape.intersects(pickArea)
        Rectangle2D pickArea = 
            new Rectangle2D.Float((float)x-pickSize/2,(float)y-pickSize/2,pickSize,pickSize);
        E closest = null;
        double minDistance = Double.MAX_VALUE;
        while(true) {
            try {
                for(E e : getFilteredEdges(layout)) {

                    Shape edgeShape = getTransformedEdgeShape(layout, e);
                    if (edgeShape == null)
                    	continue;

                    // because of the transform, the edgeShape is now a GeneralPath
                    // see if this edge is the closest of any that intersect
                    if(edgeShape.intersects(pickArea)) {
                        float cx=0;
                        float cy=0;
                        float[] f = new float[6];
                        PathIterator pi = new GeneralPath(edgeShape).getPathIterator(null);
                        if(pi.isDone()==false) {
                            pi.next();
                            pi.currentSegment(f);
                            cx = f[0];
                            cy = f[1];
                            if(pi.isDone()==false) {
                                pi.currentSegment(f);
                                cx = f[0];
                                cy = f[1];
                            }
                        }
                        float dx = (float) (cx - x);
                        float dy = (float) (cy - y);
                        float dist = dx * dx + dy * dy;
                        if (dist < minDistance) {
                            minDistance = dist;
                            closest = e;
                        }
                    }
		        }
		        break;
		    } catch(ConcurrentModificationException cme) {}
		}
		return closest;
    }

    /**
     * Retrieves the shape template for <code>e</code> and
     * transforms it according to the positions of its endpoints
     * in <code>layout</code>.
     * @param layout the <code>Layout</code> which specifies
     * <code>e</code>'s endpoints' positions
     * @param e the edge whose shape is to be returned
     * @return the transformed shape
     */
	private Shape getTransformedEdgeShape(Layout<V, E> layout, E e) {
		Pair<V> pair = layout.getGraph().getEndpoints(e);
		V v1 = pair.getFirst();
		V v2 = pair.getSecond();
		boolean isLoop = v1.equals(v2);
		Point2D p1 = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, layout.apply(v1));
		Point2D p2 = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, layout.apply(v2));
        if(p1 == null || p2 == null) 
        	return null;
		float x1 = (float) p1.getX();
		float y1 = (float) p1.getY();
		float x2 = (float) p2.getX();
		float y2 = (float) p2.getY();

		// translate the edge to the starting vertex
		AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

		Shape edgeShape = vv.getRenderContext().getEdgeShapeTransformer().apply(e);
		if(isLoop) {
		    // make the loops proportional to the size of the vertex
		    Shape s2 = vv.getRenderContext().getVertexShapeTransformer().apply(v2);
		    Rectangle2D s2Bounds = s2.getBounds2D();
		    xform.scale(s2Bounds.getWidth(),s2Bounds.getHeight());
		    // move the loop so that the nadir is centered in the vertex
		    xform.translate(0, -edgeShape.getBounds2D().getHeight()/2);
		} else {
		    float dx = x2 - x1;
		    float dy = y2 - y1;
		    // rotate the edge to the angle between the vertices
		    double theta = Math.atan2(dy,dx);
		    xform.rotate(theta);
		    // stretch the edge to span the distance between the vertices
		    float dist = (float) Math.sqrt(dx*dx + dy*dy);
		    xform.scale(dist, 1.0f);
		}

		// transform the edge to its location and dimensions
		edgeShape = xform.createTransformedShape(edgeShape);
		return edgeShape;
	}

    protected Collection<V> getFilteredVertices(Layout<V,E> layout) {
    	if(verticesAreFiltered()) {
    		Collection<V> unfiltered = layout.getGraph().getVertices();
    		Collection<V> filtered = new LinkedHashSet<V>();
    		for(V v : unfiltered) {
    			if(isVertexRendered(Context.<Graph<V,E>,V>getInstance(layout.getGraph(),v))) {
    				filtered.add(v);
    			}
    		}
    		return filtered;
    	} else {
    		return layout.getGraph().getVertices();
    	}
    }

    protected Collection<E> getFilteredEdges(Layout<V,E> layout) {
    	if(edgesAreFiltered()) {
    		Collection<E> unfiltered = layout.getGraph().getEdges();
    		Collection<E> filtered = new LinkedHashSet<E>();
    		for(E e : unfiltered) {
    			if(isEdgeRendered(Context.<Graph<V,E>,E>getInstance(layout.getGraph(),e))) {
    				filtered.add(e);
    			}
    		}
    		return filtered;
    	} else {
    		return layout.getGraph().getEdges();
    	}
    }
    
    /**
     * Quick test to allow optimization of <code>getFilteredVertices()</code>.
     * @return <code>true</code> if there is an active vertex filtering
     * mechanism for this visualization, <code>false</code> otherwise
     */
    protected boolean verticesAreFiltered() {
		Predicate<Context<Graph<V,E>,V>> vertexIncludePredicate =
			vv.getRenderContext().getVertexIncludePredicate();
		return vertexIncludePredicate != null &&
			vertexIncludePredicate.equals(Predicates.alwaysTrue()) == false;
    }
    
    /**
     * Quick test to allow optimization of <code>getFilteredEdges()</code>.
     * @return <code>true</code> if there is an active edge filtering
     * mechanism for this visualization, <code>false</code> otherwise
     */
    protected boolean edgesAreFiltered() {
		Predicate<Context<Graph<V,E>,E>> edgeIncludePredicate =
			vv.getRenderContext().getEdgeIncludePredicate();
		return edgeIncludePredicate != null &&
			edgeIncludePredicate.equals(Predicates.alwaysTrue()) == false;
    }
    
	/**
	 * Returns <code>true</code> if this vertex in this graph is included 
	 * in the collections of elements to be rendered, and <code>false</code> otherwise.
	 * @param context the vertex and graph to be queried
	 * @return <code>true</code> if this vertex is 
	 * included in the collections of elements to be rendered, <code>false</code>
	 * otherwise.
	 */
	protected boolean isVertexRendered(Context<Graph<V,E>,V> context) {
		Predicate<Context<Graph<V,E>,V>> vertexIncludePredicate =
			vv.getRenderContext().getVertexIncludePredicate();
		return vertexIncludePredicate == null || vertexIncludePredicate.apply(context);
	}
	
	/**
	 * Returns <code>true</code> if this edge and its endpoints
	 * in this graph are all included in the collections of
	 * elements to be rendered, and <code>false</code> otherwise.
	 * @param context the edge and graph to be queried
	 * @return <code>true</code> if this edge and its endpoints are all
	 * included in the collections of elements to be rendered, <code>false</code>
	 * otherwise.
	 */
	protected boolean isEdgeRendered(Context<Graph<V,E>,E> context) {
		Predicate<Context<Graph<V,E>,V>> vertexIncludePredicate =
			vv.getRenderContext().getVertexIncludePredicate();
		Predicate<Context<Graph<V,E>,E>> edgeIncludePredicate =
			vv.getRenderContext().getEdgeIncludePredicate();
		Graph<V,E> g = context.graph;
		E e = context.element;
		boolean edgeTest = edgeIncludePredicate == null || edgeIncludePredicate.apply(context);
		Pair<V> endpoints = g.getEndpoints(e);
		V v1 = endpoints.getFirst();
		V v2 = endpoints.getSecond();
		boolean endpointsTest = vertexIncludePredicate == null ||
			(vertexIncludePredicate.apply(Context.<Graph<V,E>,V>getInstance(g,v1)) && 
					vertexIncludePredicate.apply(Context.<Graph<V,E>,V>getInstance(g,v2)));
		return edgeTest && endpointsTest;
	}

	/**
	 * Returns the size of the edge picking area.
	 * The picking area is square; the size is specified as the length of one
	 * side, in view coordinates. 
	 * @return the size of the edge picking area
	 */
	public float getPickSize() {
		return pickSize;
	}

	/**
	 * Sets the size of the edge picking area.
	 * @param pickSize the length of one side of the (square) picking area, in view coordinates
	 */
	public void setPickSize(float pickSize) {
		this.pickSize = pickSize;
	}

}
