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
import java.util.Set;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;

/**
 * ShapePickSupport provides access to Vertices and EdgeType based on
 * their actual shapes. 
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 * 
 * @author Tom Nelson
 */
public class ViewLensShapePickSupport<V, E> extends ShapePickSupport<V,E>
	implements GraphElementAccessor<V,E> {

    public ViewLensShapePickSupport(VisualizationServer<V,E> vv, float pickSize) {
    	super(vv, pickSize);
    }
    
    public ViewLensShapePickSupport(VisualizationServer<V,E> vv) {
        this(vv, 2);
    }

    public V getVertex(Layout<V, E> layout, double x, double y) {

        V closest = null;
        double minDistance = Double.MAX_VALUE;
        Point2D ip = ((MutableTransformerDecorator)vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)).getDelegate().inverseTransform(new Point2D.Double(x,y));
        x = ip.getX();
        y = ip.getY();

        while(true) {
            try {
                for(V v : getFilteredVertices(layout)) {
                	// get the shape
                    Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(v);
                    // transform the vertex location to screen coords
                    Point2D p = layout.apply(v);
                    if(p == null) continue;
                    AffineTransform xform = 
                        AffineTransform.getTranslateInstance(p.getX(), p.getY());
                    shape = xform.createTransformedShape(shape);
                    
                    // use the LAYOUT transform to move the shape center without
                    // modifying the actual shape
                    Point2D lp = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p);
                    AffineTransform xlate = AffineTransform.getTranslateInstance(
                    		lp.getX()-p.getX(),lp.getY()-p.getY());
                    shape = xlate.createTransformedShape(shape);
                    // now use the VIEW transform to modify the actual shape
                    
                    shape = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.VIEW, shape);
                    	//vv.getRenderContext().getMultiLayerTransformer().transform(shape);
                    
                    // see if this vertex center is closest to the pick point
                    // among any other containing vertices
                    if(shape.contains(x, y)) {

                    	if(style == Style.LOWEST) {
                    		// return the first match
                    		return v;
                    	} else if(style == Style.HIGHEST) {
                    		// will return the last match
                    		closest = v;
                    	} else {
                    		Rectangle2D bounds = shape.getBounds2D();
                    		double dx = bounds.getCenterX() - x;
                    		double dy = bounds.getCenterY() - y;
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

    public Collection<V> getVertices(Layout<V, E> layout, Shape rectangle) {
    	Set<V> pickedVertices = new HashSet<V>();
    	
//    	 remove the view transform from the rectangle
    	rectangle = ((MutableTransformerDecorator)vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)).getDelegate().inverseTransform(rectangle);

        while(true) {
            try {
                for(V v : getFilteredVertices(layout)) {
                    Point2D p = layout.apply(v);
                    if(p == null) continue;
                   	// get the shape
                    Shape shape = vv.getRenderContext().getVertexShapeTransformer().apply(v);

                    AffineTransform xform = 
                        AffineTransform.getTranslateInstance(p.getX(), p.getY());
                    shape = xform.createTransformedShape(shape);
                    
                    shape = vv.getRenderContext().getMultiLayerTransformer().transform(shape);
                    Rectangle2D bounds = shape.getBounds2D();
                    p.setLocation(bounds.getCenterX(),bounds.getCenterY());

                    if(rectangle.contains(p)) {
                    	pickedVertices.add(v);
                    }
                }
                break;
            } catch(ConcurrentModificationException cme) {}
        }
        return pickedVertices;
    }
    
    public E getEdge(Layout<V, E> layout, double x, double y) {

        Point2D ip = ((MutableTransformerDecorator)vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)).getDelegate().inverseTransform(new Point2D.Double(x,y));
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

                    Pair<V> pair = layout.getGraph().getEndpoints(e);
                    V v1 = pair.getFirst();
                    V v2 = pair.getSecond();
                    boolean isLoop = v1.equals(v2);
                    Point2D p1 = layout.apply(v1);
                    	//vv.getRenderContext().getBasicTransformer().transform(layout.transform(v1));
                    Point2D p2 = layout.apply(v2);
                    	//vv.getRenderContext().getBasicTransformer().transform(layout.transform(v2));
                    if(p1 == null || p2 == null) continue;
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
                    
                    edgeShape = vv.getRenderContext().getMultiLayerTransformer().transform(edgeShape);

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
}
