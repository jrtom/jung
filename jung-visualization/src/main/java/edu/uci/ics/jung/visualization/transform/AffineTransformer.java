/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
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
     * The AffineTransform to use; initialized to identity.
     * 
     */
    protected AffineTransform transform = new AffineTransform();
    
    /**
     * Create an instance that does not transform points.
     */
    public AffineTransformer() {
        // nothing left to do
    }

    /**
     * Create an instance with the supplied transform.
     * @param transform the transform to use
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
     * @param p the point to transform
     * @return the transformed point
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
     * @return the transform's x scale value
     */
    public double getScaleX() {
        return transform.getScaleX();   
    }
    
    /**
     * @return the transform's y scale value
     */
    public double getScaleY() {
        return transform.getScaleY();
    }

    /**
     * @return the transform's overall scale magnitude
     */
    public double getScale() {
    		return Math.sqrt(transform.getDeterminant());
    }
    
    /**
     * @return the transform's x shear value
     */
    public double getShearX() {
        return transform.getShearX();
    }
    
    /**
     * @return the transform's y shear value
     */
    public double getShearY() {
        return transform.getShearY();
    }
    
    /**
     * @return the transform's x translate value
     */
    public double getTranslateX() {
        return transform.getTranslateX();
    }
    
    /**
     * @return the transform's y translate value
     */
    public double getTranslateY() {
        return transform.getTranslateY();
    }
    
    /**
     * Applies the transform to the supplied point.
     * 
     * @param p the point to be transformed
     * @return the transformed point
     */
    public Point2D transform(Point2D p) {
        if(p == null) return null;
        return transform.transform(p, null);
    }
    
    /**
     * Transform the supplied shape from graph (layout) to screen (view) coordinates.
     * 
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
     * Transform the supplied shape from screen (view) to graph (layout) coordinates.
     * 
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
