/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * 
 */
package edu.uci.ics.jung.visualization.transform;

import java.awt.Component;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.PolarPoint;

/**
 * HyperbolicTransformer wraps a MutableAffineTransformer and modifies
 * the transform and inverseTransform methods so that they create a
 * fisheye projection of the graph points, with points near the
 * center spread out and points near the edges collapsed onto the
 * circumference of an ellipse.
 * 
 * HyperbolicTransformer is not an affine transform, but it uses an
 * affine transform to cause translation, scaling, rotation, and shearing
 * while applying a non-affine hyperbolic filter in its transform and
 * inverseTransform methods.
 * 
 * @author Tom Nelson 
 */
public class HyperbolicTransformer extends LensTransformer implements MutableTransformer {
    /**
     * create an instance, setting values from the passed component
     * and registering to listen for size changes on the component
     * @param component the component used for rendering
     */
    public HyperbolicTransformer(Component component) {
        this(component, new MutableAffineTransformer());
    }

    /**
     * Create an instance with a possibly shared transform.
     * 
     * @param component the component used for rendering
     * @param delegate the transformer to use
     */
    public HyperbolicTransformer(Component component, MutableTransformer delegate) {
    		super(component, delegate);
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
        return delegate.inverseTransform(translatedBack);
    }
}