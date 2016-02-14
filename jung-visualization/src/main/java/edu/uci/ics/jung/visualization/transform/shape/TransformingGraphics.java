/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 11, 2005
 */

package edu.uci.ics.jung.visualization.transform.shape;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;


/**
 * subclassed to pass certain operations thru the Function
 * before the base class method is applied
 * This is useful when you want to apply non-affine transformations
 * to the Graphics2D used to draw elements of the graph.
 * 
 * @author Tom Nelson 
 *
 *
 */
public class TransformingGraphics extends GraphicsDecorator {
    
    /**
     * the Function to apply
     */
    protected BidirectionalTransformer transformer;
    
    public TransformingGraphics(BidirectionalTransformer transformer) {
        this(transformer, null);
    }
    
    public TransformingGraphics(BidirectionalTransformer Function, Graphics2D delegate) {
        super(delegate);
        this.transformer = Function;
    }
    
    /**
     * @return Returns the Function.
     */
    public BidirectionalTransformer getTransformer() {
        return transformer;
    }
    
    /**
     * @param Function The Function to set.
     */
    public void setTransformer(BidirectionalTransformer Function) {
        this.transformer = Function;
    }
    
    /**
     * transform the shape before letting the delegate draw it
     */
    public void draw(Shape s) {
        Shape shape = ((ShapeTransformer)transformer).transform(s);
        delegate.draw(shape);
    }
    
    public void draw(Shape s, float flatness) {
        Shape shape = null;
        if(transformer instanceof ShapeFlatnessTransformer) {
            shape = ((ShapeFlatnessTransformer)transformer).transform(s, flatness);
        } else {
            shape = ((ShapeTransformer)transformer).transform(s);
        }
        delegate.draw(shape);
        
    }
    
    /**
     * transform the shape before letting the delegate fill it
     */
    public void fill(Shape s) {
        Shape shape = ((ShapeTransformer)transformer).transform(s);
        delegate.fill(shape);
    }
    
    public void fill(Shape s, float flatness) {
        Shape shape = null;
        if(transformer instanceof ShapeFlatnessTransformer) {
            shape = ((ShapeFlatnessTransformer)transformer).transform(s, flatness);
        } else {
            shape = ((ShapeTransformer)transformer).transform(s);
        }
        delegate.fill(shape);
    }
    
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
    	Image image = null;
        if(transformer instanceof ShapeFlatnessTransformer) {
        	Rectangle2D r = new Rectangle2D.Double(x,y,img.getWidth(observer),img.getHeight(observer));
        	Rectangle2D s = ((ShapeTransformer)transformer).transform(r).getBounds2D();
        	image = img.getScaledInstance((int)s.getWidth(), (int)s.getHeight(), Image.SCALE_SMOOTH);
        	x = (int) s.getMinX();
        	y = (int) s.getMinY();
        } else {
            image = img;
        }
         return delegate.drawImage(image, x, y, observer);
    }

    public boolean drawImage(Image img, AffineTransform at, ImageObserver observer) {
    	Image image = null;
    	int x = (int)at.getTranslateX();
    	int y = (int)at.getTranslateY();
        if(transformer instanceof ShapeFlatnessTransformer) {
        	Rectangle2D r = new Rectangle2D.Double(x,y,img.getWidth(observer),img.getHeight(observer));
        	Rectangle2D s = ((ShapeTransformer)transformer).transform(r).getBounds2D();
        	image = img.getScaledInstance((int)s.getWidth(), (int)s.getHeight(), Image.SCALE_SMOOTH);
        	x = (int) s.getMinX();
        	y = (int) s.getMinY();
        	at.setToTranslation(s.getMinX(), s.getMinY());
        } else {
            image = img;
        }
         return delegate.drawImage(image, at, observer);
    }

    /**
     * transform the shape before letting the delegate apply 'hit'
     * with it
     */
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        Shape shape = ((ShapeTransformer)transformer).transform(s);
        return delegate.hit(rect, shape, onStroke);
    }
    
    public Graphics create() {
        return delegate.create();
    }
    
    public void dispose() {
        delegate.dispose();
    }
    
}
