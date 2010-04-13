/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package edu.uci.ics.jung.visualization.transform;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.event.ChangeListener;

/**
 * a complete decorator that wraps a MutableTransformer. Subclasses
 * use this to allow them to only declare methods they need to change.
 * 
 * @author Tom Nelson 
 *
 */
public abstract class MutableTransformerDecorator implements MutableTransformer {
	
	protected MutableTransformer delegate;
	
	public MutableTransformerDecorator(MutableTransformer delegate) {
		if(delegate == null) {
			delegate = new MutableAffineTransformer();
		}
		this.delegate = delegate;
	}
	
	/**
	 * @return Returns the delegate.
	 */
	public MutableTransformer getDelegate() {
		return delegate;
	}

	/**
	 * @param delegate The delegate to set.
	 */
	public void setDelegate(MutableTransformer delegate) {
		this.delegate = delegate;
	}



	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.utils.ChangeEventSupport#addChangeListener(javax.swing.event.ChangeListener)
	 */
	public void addChangeListener(ChangeListener l) {
		delegate.addChangeListener(l);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#concatenate(java.awt.geom.AffineTransform)
	 */
	public void concatenate(AffineTransform transform) {
		delegate.concatenate(transform);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.utils.ChangeEventSupport#fireStateChanged()
	 */
	public void fireStateChanged() {
		delegate.fireStateChanged();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.utils.ChangeEventSupport#getChangeListeners()
	 */
	public ChangeListener[] getChangeListeners() {
		return delegate.getChangeListeners();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#getScale()
	 */
	public double getScale() {
		return delegate.getScale();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#getScaleX()
	 */
	public double getScaleX() {
		return delegate.getScaleX();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#getScaleY()
	 */
	public double getScaleY() {
		return delegate.getScaleY();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#getShearX()
	 */
	public double getShearX() {
		return delegate.getShearX();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#getShearY()
	 */
	public double getShearY() {
		return delegate.getShearY();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#getTransform()
	 */
	public AffineTransform getTransform() {
		return delegate.getTransform();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#getTranslateX()
	 */
	public double getTranslateX() {
		return delegate.getTranslateX();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#getTranslateY()
	 */
	public double getTranslateY() {
		return delegate.getTranslateY();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.Transformer#inverseTransform(java.awt.geom.Point2D)
	 */
	public Point2D inverseTransform(Point2D p) {
		return delegate.inverseTransform(p);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer#inverseTransform(java.awt.Shape)
	 */
	public Shape inverseTransform(Shape shape) {
		return delegate.inverseTransform(shape);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.Transformer#isTransformed()
	 */
//	public boolean isTransformed() {
//		return delegate.isTransformed();
//	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#preConcatenate(java.awt.geom.AffineTransform)
	 */
	public void preConcatenate(AffineTransform transform) {
		delegate.preConcatenate(transform);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.utils.ChangeEventSupport#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	public void removeChangeListener(ChangeListener l) {
		delegate.removeChangeListener(l);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#rotate(double, java.awt.geom.Point2D)
	 */
	public void rotate(double radians, Point2D point) {
		delegate.rotate(radians, point);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#scale(double, double, java.awt.geom.Point2D)
	 */
	public void scale(double sx, double sy, Point2D point) {
		delegate.scale(sx, sy, point);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#setScale(double, double, java.awt.geom.Point2D)
	 */
	public void setScale(double sx, double sy, Point2D point) {
		delegate.setScale(sx, sy, point);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#setToIdentity()
	 */
	public void setToIdentity() {
		delegate.setToIdentity();
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#setTranslate(double, double)
	 */
	public void setTranslate(double dx, double dy) {
		delegate.setTranslate(dx, dy);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#shear(double, double, java.awt.geom.Point2D)
	 */
	public void shear(double shx, double shy, Point2D from) {
		delegate.shear(shx, shy, from);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.Transformer#transform(java.awt.geom.Point2D)
	 */
	public Point2D transform(Point2D p) {
		return delegate.transform(p);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer#transform(java.awt.Shape)
	 */
	public Shape transform(Shape shape) {
		return delegate.transform(shape);
	}

	/* (non-Javadoc)
	 * @see edu.uci.ics.jung.visualization.transform.MutableTransformer#translate(double, double)
	 */
	public void translate(double dx, double dy) {
		delegate.translate(dx, dy);
	}

    public double getRotation() {
        return delegate.getRotation();
    }

    public void rotate(double radians, double x, double y) {
        delegate.rotate(radians, x, y);
    }

}
