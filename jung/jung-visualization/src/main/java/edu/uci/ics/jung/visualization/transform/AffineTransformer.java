/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Apr 16, 2005
 */

package edu.uci.ics.jung.visualization.transform;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;

/**
 *
 * Provides methods to map points from one coordinate system to
 * another, by delegating to a wrapped AffineTransform (uniform)
 * and its inverse.
 * 
 * @author Tom Nelson
 */
public class AffineTransformer implements BidirectionalTransformer, ShapeTransformer {

    protected AffineTransform inverse;

    /**
     * the AffineTransform to use. Initialize to identity
     * 
     */
    protected AffineTransform transform = new AffineTransform();
    
    /**
     * create an instance that does not transform points
     *
     */
    public AffineTransformer() {
        // nothing left to do
    }
    /**
     * Create an instance with the supplied transform
     */
    public AffineTransformer(AffineTransform transform) {
        if(transform != null) 
            this.transform = transform;
    }

    /**
     * @return Returns the transform.
     */
    public AffineTransform getTransform() {
        return transform;
    }
    /**
     * @param transform The transform to set.
     */
    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }
    
    /**
     * applies the inverse transform to the supplied point
     * @param p
     * @return
     */
    public Point2D inverseTransform(Point2D p) {

        return getInverse().transform(p, null);
    }
    
    public AffineTransform getInverse() {
        if(inverse == null) {
            try {
                inverse = transform.createInverse();
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }
        }
        return inverse;
    }
    
    /**
     * getter for scalex
     */
    public double getScaleX() {
        return transform.getScaleX();   
    }
    
    /**
     * getter for scaley
     */
    public double getScaleY() {
        return transform.getScaleY();
    }
    
    public double getScale() {
    		return Math.sqrt(transform.getDeterminant());
    }
    
    /**
     * getter for shear in x axis
     */
    public double getShearX() {
        return transform.getShearX();
    }
    
    /**
     * getter for shear in y axis
     */
    public double getShearY() {
        return transform.getShearY();
    }
    
    /**
     * get the translate x value
     */
    public double getTranslateX() {
        return transform.getTranslateX();
    }
    
    /**
     * get the translate y value
     */
    public double getTranslateY() {
        return transform.getTranslateY();
    }
    

    
    
    
    /**
     * applies the transform to the supplied point
     */
    public Point2D transform(Point2D p) {
        if(p == null) return null;
        return transform.transform(p, null);
    }
    
    /**
     * transform the supplied shape from graph coordinates to
     * screen coordinates
     * @return the GeneralPath of the transformed shape
     */
    public Shape transform(Shape shape) {
        GeneralPath newPath = new GeneralPath();
        float[] coords = new float[6];
        for(PathIterator iterator=shape.getPathIterator(null);
            iterator.isDone() == false;
            iterator.next()) {
            int type = iterator.currentSegment(coords);
            switch(type) {
            case PathIterator.SEG_MOVETO:
                Point2D p = transform(new Point2D.Float(coords[0], coords[1]));
                newPath.moveTo((float)p.getX(), (float)p.getY());
                break;
                
            case PathIterator.SEG_LINETO:
                p = transform(new Point2D.Float(coords[0], coords[1]));
                newPath.lineTo((float)p.getX(), (float) p.getY());
                break;
                
            case PathIterator.SEG_QUADTO:
                p = transform(new Point2D.Float(coords[0], coords[1]));
                Point2D q = transform(new Point2D.Float(coords[2], coords[3]));
                newPath.quadTo((float)p.getX(), (float)p.getY(), (float)q.getX(), (float)q.getY());
                break;
                
            case PathIterator.SEG_CUBICTO:
                p = transform(new Point2D.Float(coords[0], coords[1]));
                q = transform(new Point2D.Float(coords[2], coords[3]));
                Point2D r = transform(new Point2D.Float(coords[4], coords[5]));
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
     * transform the supplied shape from graph coordinates to
     * screen coordinates
     * @return the GeneralPath of the transformed shape
     */
    public Shape inverseTransform(Shape shape) {
        GeneralPath newPath = new GeneralPath();
        float[] coords = new float[6];
        for(PathIterator iterator=shape.getPathIterator(null);
            iterator.isDone() == false;
            iterator.next()) {
            int type = iterator.currentSegment(coords);
            switch(type) {
            case PathIterator.SEG_MOVETO:
                Point2D p = inverseTransform(new Point2D.Float(coords[0], coords[1]));
                newPath.moveTo((float)p.getX(), (float)p.getY());
                break;
                
            case PathIterator.SEG_LINETO:
                p = inverseTransform(new Point2D.Float(coords[0], coords[1]));
                newPath.lineTo((float)p.getX(), (float) p.getY());
                break;
                
            case PathIterator.SEG_QUADTO:
                p = inverseTransform(new Point2D.Float(coords[0], coords[1]));
                Point2D q = inverseTransform(new Point2D.Float(coords[2], coords[3]));
                newPath.quadTo((float)p.getX(), (float)p.getY(), (float)q.getX(), (float)q.getY());
                break;
                
            case PathIterator.SEG_CUBICTO:
                p = inverseTransform(new Point2D.Float(coords[0], coords[1]));
                q = inverseTransform(new Point2D.Float(coords[2], coords[3]));
                Point2D r = inverseTransform(new Point2D.Float(coords[4], coords[5]));
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
    
    public double getRotation() {    
        double[] unitVector = new double[]{0,0,1,0};
        double[] result = new double[4];

        transform.transform(unitVector, 0, result, 0, 2);

        double dy = Math.abs(result[3] - result[1]);
        double length = Point2D.distance(result[0], result[1], result[2], result[3]);
        double rotation = Math.asin(dy / length);        
        
        if (result[3] - result[1] > 0) {
            if (result[2] - result[0] < 0) {
                rotation = Math.PI - rotation;
            }
        } else {
            if (result[2] - result[0] > 0) {
                rotation = 2 * Math.PI - rotation;
            } else {
                rotation = rotation + Math.PI;
            }
        }

        return rotation;
    }

    @Override
    public String toString() {
        return "Transformer using "+transform;
    }
 
}
