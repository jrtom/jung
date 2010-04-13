/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package edu.uci.ics.jung.visualization.transform;

import java.awt.Component;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.PolarPoint;

/**
 * MagnifyTransformer wraps a MutableAffineTransformer and modifies
 * the transform and inverseTransform methods so that they create an
 * enlarging projection of the graph points.
 * 
 * MagnifyTransformer uses an
 * affine transform to cause translation, scaling, rotation, and shearing
 * while applying a separate magnification filter in its transform and
 * inverseTransform methods.
 * 
 * @author Tom Nelson 
 *
 *
 */
public class MagnifyTransformer extends LensTransformer implements MutableTransformer {

    
    /**
     * create an instance, setting values from the passed component
     * and registering to listen for size changes on the component
     * @param component
     */
    public MagnifyTransformer(Component component) {
        this(component, new MutableAffineTransformer());
    }
    /**
     * create an instance with a possibly shared transform
     * @param component
     * @param delegate
     */
    public MagnifyTransformer(Component component, MutableTransformer delegate) {
    		super(component, delegate);
        this.magnification = 3.f;
   }
    
    /**
     * override base class transform to project the fisheye effect
     */
    public Point2D transform(Point2D graphPoint) {
        if(graphPoint == null) return null;
        Point2D viewCenter = getViewCenter();
        double viewRadius = getViewRadius();
        double ratio = getRatio();
        // transform the point from the graph to the view
        Point2D viewPoint = delegate.transform(graphPoint);
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
        
        double mag = magnification;
        radius *= mag;
        
        radius = Math.min(radius, viewRadius);
        Point2D projectedPoint = PolarPoint.polarToCartesian(theta, radius);
        projectedPoint.setLocation(projectedPoint.getX()/ratio, projectedPoint.getY());
        Point2D translatedBack = new Point2D.Double(projectedPoint.getX()+viewCenter.getX(),
                projectedPoint.getY()+viewCenter.getY());
        return translatedBack;
    }
    
    /**
     * override base class to un-project the fisheye effect
     */
    public Point2D inverseTransform(Point2D viewPoint) {
        
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
        if(radius > viewRadius) return delegate.inverseTransform(viewPoint);
        
        double mag = magnification;
        radius /= mag;
        polar.setRadius(radius);
        Point2D projectedPoint = PolarPoint.polarToCartesian(polar);
        projectedPoint.setLocation(projectedPoint.getX()/ratio, projectedPoint.getY());
        Point2D translatedBack = new Point2D.Double(projectedPoint.getX()+viewCenter.getX(),
                projectedPoint.getY()+viewCenter.getY());
        return delegate.inverseTransform(translatedBack);
    }
    
    /**
     * magnifies the point, without considering the Lens
     * @param graphPoint
     * @return
     */
    public Point2D magnify(Point2D graphPoint) {
        if(graphPoint == null) return null;
        Point2D viewCenter = getViewCenter();
        double ratio = getRatio();
        // transform the point from the graph to the view
        Point2D viewPoint = graphPoint;
        // calculate point from center
        double dx = viewPoint.getX() - viewCenter.getX();
        double dy = viewPoint.getY() - viewCenter.getY();
        // factor out ellipse
        dx *= ratio;
        Point2D pointFromCenter = new Point2D.Double(dx, dy);
        
        PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
        double theta = polar.getTheta();
        double radius = polar.getRadius();
        
        double mag = magnification;
        radius *= mag;
        
//        radius = Math.min(radius, viewRadius);
        Point2D projectedPoint = PolarPoint.polarToCartesian(theta, radius);
        projectedPoint.setLocation(projectedPoint.getX()/ratio, projectedPoint.getY());
        Point2D translatedBack = new Point2D.Double(projectedPoint.getX()+viewCenter.getX(),
                projectedPoint.getY()+viewCenter.getY());
        return translatedBack;
    }

}