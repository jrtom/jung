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
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 * LensTransformer wraps a MutableAffineTransformer and modifies
 * the transform and inverseTransform methods so that they create a
 * projection of the graph points within an elliptical lens.
 * 
 * LensTransformer uses an
 * affine transform to cause translation, scaling, rotation, and shearing
 * while applying a possibly non-affine filter in its transform and
 * inverseTransform methods.
 * 
 * @author Tom Nelson
 */
public abstract class LensTransformer extends MutableTransformerDecorator implements MutableTransformer {

    /**
     * the area affected by the transform
     */
    protected RectangularShape lensShape = new Ellipse2D.Float();
    
    protected float magnification = 0.7f;
    
    /**
     * Create an instance with a possibly shared transform.
     * 
     * @param component the component used for rendering
     * @param delegate the transformer to use
     */
    public LensTransformer(Component component, MutableTransformer delegate) {
    		super(delegate);
        setComponent(component);
        component.addComponentListener(new ComponentListenerImpl());
   }
    
    /**
     * Set values from the passed component.
     * @param component the component used for rendering
     */
    private void setComponent(Component component) {
        Dimension d = component.getSize();
        if(d.width <= 0 || d.height <= 0) {
            d = component.getPreferredSize();
        }
        float ewidth = d.width/1.5f;
        float eheight = d.height/1.5f;
        lensShape.setFrame(d.width/2-ewidth/2, d.height/2-eheight/2, ewidth, eheight);
    }
    
    public float getMagnification() {
        return magnification;
    }

    public void setMagnification(float magnification) {
        this.magnification = magnification;
    }
    
    public Point2D getViewCenter() {
        return new Point2D.Double(lensShape.getCenterX(), lensShape.getCenterY());
    }

    public void setViewCenter(Point2D viewCenter) {
        double width = lensShape.getWidth();
        double height = lensShape.getHeight();
        lensShape.setFrame(viewCenter.getX()-width/2,
                viewCenter.getY()-height/2,
                width, height);
    }

    public double getViewRadius() {
        return lensShape.getHeight()/2;
    }

    public void setViewRadius(double viewRadius) {
        double x = lensShape.getCenterX();
        double y = lensShape.getCenterY();
        double viewRatio = getRatio();
        lensShape.setFrame(x-viewRadius/viewRatio,
                y-viewRadius,
                2*viewRadius/viewRatio,
                2*viewRadius);
    }
    
    /**
     * @return the ratio between the lens height and lens width
     */
    public double getRatio() {
        return lensShape.getHeight()/lensShape.getWidth();
    }
    
    public void setLensShape(RectangularShape ellipse) {
        this.lensShape = ellipse;
    }
    public RectangularShape getLensShape() {
        return lensShape;
    }
    public void setToIdentity() {
        this.delegate.setToIdentity();
    }

    /**
     * react to size changes on a component
     */
    protected class ComponentListenerImpl extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
            setComponent(e.getComponent());
         }
    }
    
    /**
     * override base class transform to project the fisheye effect
     */
    public abstract Point2D transform(Point2D graphPoint);
    
    /**
     * override base class to un-project the fisheye effect
     */
    public abstract Point2D inverseTransform(Point2D viewPoint);
    
    public double getDistanceFromCenter(Point2D p) {
    	
        double dx = lensShape.getCenterX()-p.getX();
        double dy = lensShape.getCenterY()-p.getY();
        dx *= getRatio();
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    /**
     * return the supplied shape, translated to the coordinates
     * that result from calling transform on its center
     */
    public Shape transform(Shape shape) {
    	Rectangle2D bounds = shape.getBounds2D();
    	Point2D center = new Point2D.Double(bounds.getCenterX(),bounds.getCenterY());
    	Point2D newCenter = transform(center);
    	double dx = newCenter.getX()-center.getX();
    	double dy = newCenter.getY()-center.getY();
    	AffineTransform at = AffineTransform.getTranslateInstance(dx,dy);
    	return at.createTransformedShape(shape);
    }
    
    /**
     * Returns the supplied shape, translated to the coordinates
     * that result from calling inverseTransform on its center.
     */
    public Shape inverseTransform(Shape shape) {
    	Rectangle2D bounds = shape.getBounds2D();
    	Point2D center = new Point2D.Double(bounds.getCenterX(),bounds.getCenterY());
    	Point2D newCenter = inverseTransform(center);
    	double dx = newCenter.getX()-center.getX();
    	double dy = newCenter.getY()-center.getY();
    	AffineTransform at = AffineTransform.getTranslateInstance(dx,dy);
    	return at.createTransformedShape(shape);
    }

}