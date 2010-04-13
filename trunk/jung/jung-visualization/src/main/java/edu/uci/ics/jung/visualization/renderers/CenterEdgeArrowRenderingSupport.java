/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.renderers;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.RenderContext;

public class CenterEdgeArrowRenderingSupport<V,E> implements EdgeArrowRenderingSupport<V,E> {

    /**
     * Returns a transform to position the arrowhead on this edge shape at the
     * point where it intersects the passed vertex shape.
     */
    public AffineTransform getArrowTransform(RenderContext<V,E> rc, Shape edgeShape, Shape vertexShape) {
    	GeneralPath path = new GeneralPath(edgeShape);
        float[] seg = new float[6];
        Point2D p1=null;
        Point2D p2=null;
        AffineTransform at = new AffineTransform();
        // count the segments.
        int middleSegment = 0;
        int current = 0;
        for(PathIterator i=path.getPathIterator(null,1); !i.isDone(); i.next()) {
        	current++;
        }
        middleSegment = current/2;
        // find the middle segment
        current = 0;
        for(PathIterator i=path.getPathIterator(null,1); !i.isDone(); i.next()) {
        	current++;
        	int ret = i.currentSegment(seg);
        	if(ret == PathIterator.SEG_MOVETO) {
        		p2 = new Point2D.Float(seg[0],seg[1]);
        	} else if(ret == PathIterator.SEG_LINETO) {
        		p1 = p2;
        		p2 = new Point2D.Float(seg[0],seg[1]);
        	}
        	if(current > middleSegment) { // done
        		at = getArrowTransform(rc, new Line2D.Float(p1,p2),vertexShape);
        		break;
        	}

        } 
        return at;
    }

    /**
     * Returns a transform to position the arrowhead on this edge shape at the
     * point where it intersects the passed vertex shape.
     */
    public AffineTransform getReverseArrowTransform(RenderContext<V,E> rc, Shape edgeShape, Shape vertexShape) {
        return getReverseArrowTransform(rc, edgeShape, vertexShape, true);
    }
            
    /**
     * <p>Returns a transform to position the arrowhead on this edge shape at the
     * point where it intersects the passed vertex shape.</p>
     * 
     * <p>The Loop edge is a special case because its staring point is not inside
     * the vertex. The passedGo flag handles this case.</p>
     * 
     * @param path
     * @param vertexShape
     * @param passedGo - used only for Loop edges
     */
    public AffineTransform getReverseArrowTransform(RenderContext<V,E> rc, Shape edgeShape, Shape vertexShape,
            boolean passedGo) {
    	GeneralPath path = new GeneralPath(edgeShape);
        float[] seg = new float[6];
        Point2D p1=null;
        Point2D p2=null;
        AffineTransform at = new AffineTransform();
        // count the segments.
        int middleSegment = 0;
        int current = 0;
        for(PathIterator i=path.getPathIterator(null,1); !i.isDone(); i.next()) {
        	current++;
        }
        middleSegment = current/2;
        // find the middle segment
        current = 0;
        for(PathIterator i=path.getPathIterator(null,1); !i.isDone(); i.next()) {
        	current++;
        	int ret = i.currentSegment(seg);
        	if(ret == PathIterator.SEG_MOVETO) {
        		p2 = new Point2D.Float(seg[0],seg[1]);
        	} else if(ret == PathIterator.SEG_LINETO) {
        		p1 = p2;
        		p2 = new Point2D.Float(seg[0],seg[1]);
        	}
        	if(current > middleSegment) { // done
        		at = getReverseArrowTransform(rc, new Line2D.Float(p1,p2),vertexShape);
        		break;
        	}
        }
        return at;
    }

    /**
     * This is used for the arrow of a directed and for one of the
     * arrows for non-directed edges
     * Get a transform to place the arrow shape on the passed edge at the
     * point where it intersects the passed shape
     * @param edgeShape
     * @param vertexShape
     * @return
     */
    public AffineTransform getArrowTransform(RenderContext<V,E> rc, Line2D edgeShape, Shape vertexShape) {
        
        // find the midpoint of the edgeShape line, and use it to make the transform
        Line2D left = new Line2D.Float();
        Line2D right = new Line2D.Float();
        this.subdivide(edgeShape, left, right);
        edgeShape = right;
        float dx = (float) (edgeShape.getX1()-edgeShape.getX2());
        float dy = (float) (edgeShape.getY1()-edgeShape.getY2());
        double atheta = Math.atan2(dx,dy)+Math.PI/2;
        AffineTransform at = 
            AffineTransform.getTranslateInstance(edgeShape.getX1(), edgeShape.getY1());
        at.rotate(-atheta);
        return at;
    }

    /**
     * This is used for the reverse-arrow of a non-directed edge
     * get a transform to place the arrow shape on the passed edge at the
     * point where it intersects the passed shape
     * @param edgeShape
     * @param vertexShape
     * @return
     */
    protected AffineTransform getReverseArrowTransform(RenderContext<V,E> rc, Line2D edgeShape, Shape vertexShape) {
        // find the midpoint of the edgeShape line, and use it to make the transform
        Line2D left = new Line2D.Float();
        Line2D right = new Line2D.Float();
        this.subdivide(edgeShape, left, right);
        edgeShape = right;
        float dx = (float) (edgeShape.getX1()-edgeShape.getX2());
        float dy = (float) (edgeShape.getY1()-edgeShape.getY2());
        // calculate the angle for the arrowhead
        double atheta = Math.atan2(dx,dy)-Math.PI/2;
        AffineTransform at = AffineTransform.getTranslateInstance(edgeShape.getX1(),edgeShape.getY1());
        at.rotate(-atheta);
        return at;
    }
    
    /**
     * divide a Line2D into 2 new Line2Ds that are returned
     * in the passed left and right instances, if non-null
     * @param src the line to divide
     * @param left the left side, or null
     * @param right the right side, or null
     */
    protected void subdivide(Line2D src,
            Line2D left,
            Line2D right) {
        double x1 = src.getX1();
        double y1 = src.getY1();
        double x2 = src.getX2();
        double y2 = src.getY2();
        
        double mx = x1 + (x2-x1)/2.0;
        double my = y1 + (y2-y1)/2.0;
        if (left != null) {
            left.setLine(x1, y1, mx, my);
        }
        if (right != null) {
            right.setLine(mx, my, x2, y2);
        }
    }

}
