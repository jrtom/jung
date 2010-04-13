/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package edu.uci.ics.jung.visualization.transform.shape;

import java.awt.Component;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;


/**
 * HyperbolicShapeTransformer extends HyperbolicTransformer and
 * adds implementations for methods in ShapeFlatnessTransformer.
 * It modifies the shapes (Vertex, Edge, and Arrowheads) so that
 * they are distorted by the hyperbolic transformation
 * 
 * @author Tom Nelson
 *
 *
 */
public class HyperbolicShapeTransformer extends HyperbolicTransformer 
    implements ShapeFlatnessTransformer {

    /**
     * Create an instance, setting values from the passed component
     * and registering to listen for size changes on the component.
     */
    public HyperbolicShapeTransformer(Component component) {
        this(component, null);
    }
    
    /**
     * Create an instance, setting values from the passed component
     * and registering to listen for size changes on the component,
     * with a possibly shared transform <code>delegate</code>.
     */
    public HyperbolicShapeTransformer(Component component, MutableTransformer delegate) {
        super(component, delegate);
   }
    
    /**
     * Transform the supplied shape with the overridden transform
     * method so that the shape is distorted by the hyperbolic 
     * transform.
     * @param shape a shape to transform
     * @return a GeneralPath for the transformed shape
     */
    public Shape transform(Shape shape) {
        return transform(shape, 0);
    }
    public Shape transform(Shape shape, float flatness) {
        GeneralPath newPath = new GeneralPath();
        float[] coords = new float[6];
        PathIterator iterator = null;
        if(flatness == 0) {
            iterator = shape.getPathIterator(null);
        } else {
            iterator = shape.getPathIterator(null, flatness);
        }
        for( ;
            iterator.isDone() == false;
            iterator.next()) {
            int type = iterator.currentSegment(coords);
            switch(type) {
            case PathIterator.SEG_MOVETO:
                Point2D p = _transform(new Point2D.Float(coords[0], coords[1]));
                newPath.moveTo((float)p.getX(), (float)p.getY());
                break;
                
            case PathIterator.SEG_LINETO:
                p = _transform(new Point2D.Float(coords[0], coords[1]));
                newPath.lineTo((float)p.getX(), (float) p.getY());
                break;
                
            case PathIterator.SEG_QUADTO:
                p = _transform(new Point2D.Float(coords[0], coords[1]));
                Point2D q = _transform(new Point2D.Float(coords[2], coords[3]));
                newPath.quadTo((float)p.getX(), (float)p.getY(), (float)q.getX(), (float)q.getY());
                break;
                
            case PathIterator.SEG_CUBICTO:
                p = _transform(new Point2D.Float(coords[0], coords[1]));
                q = _transform(new Point2D.Float(coords[2], coords[3]));
                Point2D r = _transform(new Point2D.Float(coords[4], coords[5]));
                newPath.curveTo((float)p.getX(), (float)p.getY(), 
                        (float)q.getX(), (float)q.getY(),
                        (float)r.getX(), (float)r.getY());
                break;
                
            case PathIterator.SEG_CLOSE:
                newPath.closePath();
                break;
                    
            }
        }
        return newPath;
    }

    public Shape inverseTransform(Shape shape) {
        GeneralPath newPath = new GeneralPath();
        float[] coords = new float[6];
        for(PathIterator iterator=shape.getPathIterator(null);
            iterator.isDone() == false;
            iterator.next()) {
            int type = iterator.currentSegment(coords);
            switch(type) {
            case PathIterator.SEG_MOVETO:
                Point2D p = _inverseTransform(new Point2D.Float(coords[0], coords[1]));
                newPath.moveTo((float)p.getX(), (float)p.getY());
                break;
                
            case PathIterator.SEG_LINETO:
                p = _inverseTransform(new Point2D.Float(coords[0], coords[1]));
                newPath.lineTo((float)p.getX(), (float) p.getY());
                break;
                
            case PathIterator.SEG_QUADTO:
                p = _inverseTransform(new Point2D.Float(coords[0], coords[1]));
                Point2D q = _inverseTransform(new Point2D.Float(coords[2], coords[3]));
                newPath.quadTo((float)p.getX(), (float)p.getY(), (float)q.getX(), (float)q.getY());
                break;
                
            case PathIterator.SEG_CUBICTO:
                p = _inverseTransform(new Point2D.Float(coords[0], coords[1]));
                q = _inverseTransform(new Point2D.Float(coords[2], coords[3]));
                Point2D r = _inverseTransform(new Point2D.Float(coords[4], coords[5]));
                newPath.curveTo((float)p.getX(), (float)p.getY(), 
                        (float)q.getX(), (float)q.getY(),
                        (float)r.getX(), (float)r.getY());
                break;
                
            case PathIterator.SEG_CLOSE:
                newPath.closePath();
                break;
                    
            }
        }
        return newPath;
    }
    /**
     * override base class transform to project the fisheye effect
     */
    private Point2D _transform(Point2D graphPoint) {
        if(graphPoint == null) return null;
        Point2D viewCenter = getViewCenter();
        double viewRadius = getViewRadius();
        double ratio = getRatio();
        // transform the point from the graph to the view
        Point2D viewPoint = graphPoint;//delegate.transform(graphPoint);
        // calculate point from center
        double dx = viewPoint.getX() - viewCenter.getX();
        double dy = viewPoint.getY() - viewCenter.getY();
        // factor out ellipse
        dx *= ratio;
        Point2D pointFromCenter = new Point2D.Double(dx, dy);
        
        PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
        double theta = polar.getTheta();
        double radius = polar.getRadius();
        if(radius > viewRadius) return viewPoint;
        
        double mag = Math.tan(Math.PI/2*magnification);
        radius *= mag;
        
        radius = Math.min(radius, viewRadius);
        radius /= viewRadius;
        radius *= Math.PI/2;
        radius = Math.abs(Math.atan(radius));
        radius *= viewRadius;
        Point2D projectedPoint = PolarPoint.polarToCartesian(theta, radius);
        projectedPoint.setLocation(projectedPoint.getX()/ratio, projectedPoint.getY());
        Point2D translatedBack = new Point2D.Double(projectedPoint.getX()+viewCenter.getX(),
                projectedPoint.getY()+viewCenter.getY());
        return translatedBack;
    }
    
    /**
     * override base class to un-project the fisheye effect
     */
    private Point2D _inverseTransform(Point2D viewPoint) {
    	
        viewPoint = delegate.inverseTransform(viewPoint);
        Point2D viewCenter = getViewCenter();
        double viewRadius = getViewRadius();
        double ratio = getRatio();
        double dx = viewPoint.getX() - viewCenter.getX();
        double dy = viewPoint.getY() - viewCenter.getY();
        // factor out ellipse
        dx *= ratio;

        Point2D pointFromCenter = new Point2D.Double(dx, dy);
        
        PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);

        double radius = polar.getRadius();
        if(radius > viewRadius) return viewPoint;//elegate.inverseTransform(viewPoint);
        
        radius /= viewRadius;
        radius = Math.abs(Math.tan(radius));
        radius /= Math.PI/2;
        radius *= viewRadius;
        double mag = Math.tan(Math.PI/2*magnification);
        radius /= mag;
        polar.setRadius(radius);
        Point2D projectedPoint = PolarPoint.polarToCartesian(polar);
        projectedPoint.setLocation(projectedPoint.getX()/ratio, projectedPoint.getY());
        Point2D translatedBack = new Point2D.Double(projectedPoint.getX()+viewCenter.getX(),
                projectedPoint.getY()+viewCenter.getY());
        return translatedBack;
        //delegate.inverseTransform(translatedBack);
    }
}