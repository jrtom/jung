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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport;

/**
 *
 * Provides methods to mutate the AffineTransform used by AffineTransformer
 * base class to map points from one coordinate system to
 * another.
 * 
 * 
 * @author Tom Nelson
 *
 * 
 */
public class MutableAffineTransformer extends AffineTransformer 
implements MutableTransformer, ShapeTransformer, ChangeEventSupport {

    protected ChangeEventSupport changeSupport =
        new DefaultChangeEventSupport(this);
    
    /**
     * create an instance that does not transform points
     *
     */
    public MutableAffineTransformer() {
        // nothing left to do
    }
    /**
     * Create an instance with the supplied transform
     */
    public MutableAffineTransformer(AffineTransform transform) {
        super(transform);
    }

    public String toString() {
        return "MutableAffineTransformer using "+transform;
    }
    /**
     * setter for the scale
     * fires a PropertyChangeEvent with the AffineTransforms representing
     * the previous and new values for scale and offset
     * @param scalex
     * @param scaley
     */
    public void scale(double scalex, double scaley, Point2D from) {
        AffineTransform xf = AffineTransform.getTranslateInstance(from.getX(),from.getY());
        xf.scale(scalex, scaley);
        xf.translate(-from.getX(), -from.getY());
        inverse = null;
        transform.preConcatenate(xf);
        fireStateChanged();
    }
    
    /**
     * setter for the scale
     * fires a PropertyChangeEvent with the AffineTransforms representing
     * the previous and new values for scale and offset
     * @param scalex
     * @param scaley
     */
    public void setScale(double scalex, double scaley, Point2D from) {
        transform.setToIdentity();
        scale(scalex, scaley, from);
    }
    
    /**
     * shears the transform by passed parameters
     * @param shx x value to shear
     * @param shy y value to shear
     */
    public void shear(double shx, double shy, Point2D from) {
        inverse = null;
        AffineTransform at = 
            AffineTransform.getTranslateInstance(from.getX(), from.getY());
        at.shear(shx, shy);
        at.translate(-from.getX(), -from.getY());
        transform.preConcatenate(at);
        fireStateChanged();
    }
    
    /**
     * replace the Transform's translate x and y values
     * with the passed values, leaving the scale values
     * unchanged
     * @param tx the x value 
     * @param ty the y value
     */
    public void setTranslate(double tx, double ty) {
        float scalex = (float) transform.getScaleX();
        float scaley = (float) transform.getScaleY();
        float shearx = (float) transform.getShearX();
        float sheary = (float) transform.getShearY();
        inverse = null;
        transform.setTransform(scalex, 
                sheary, 
                shearx, 
                scaley,
                tx, ty);
        fireStateChanged();
    }
    
    /**
     * Apply the passed values to the current Transform
     * @param offsetx the x-value
     * @param offsety the y-value
     */
    public void translate(double offsetx, double offsety) {
        inverse = null;
        transform.translate(offsetx, offsety);
        fireStateChanged();
    }
    
    /**
     * preconcatenates the rotation at the supplied point with the current transform
     */
    public void rotate(double theta, Point2D from) {
        AffineTransform rotate = 
            AffineTransform.getRotateInstance(theta, from.getX(), from.getY());
        inverse = null;
        transform.preConcatenate(rotate);

        fireStateChanged();
    }
    
    /**
     * rotates the current transform at the supplied points
     */
    public void rotate(double radians, double x, double y) {
        inverse = null;
        transform.rotate(radians, x, y);
        fireStateChanged();
    }
    
    public void concatenate(AffineTransform xform) {
        inverse = null;
        transform.concatenate(xform);
        fireStateChanged();
        
    }
    public void preConcatenate(AffineTransform xform) {
        inverse = null;
        transform.preConcatenate(xform);
        fireStateChanged();
    }   

    
    /**
     * Adds a <code>ChangeListener</code>.
     * @param l the listener to be added
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    /**
     * Removes a ChangeListener.
     * @param l the listener to be removed
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    /**
     * Returns an array of all the <code>ChangeListener</code>s added
     * with addChangeListener().
     *
     * @return all of the <code>ChangeListener</code>s added or an empty
     *         array if no listeners have been added
     */
    public ChangeListener[] getChangeListeners() {
        return changeSupport.getChangeListeners();
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created.
     * @see EventListenerList
     */
    public void fireStateChanged() {
        changeSupport.fireStateChanged();
    }
    
    public void setToIdentity() {
        inverse = null;
        transform.setToIdentity();
        fireStateChanged();
    }
}
