/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
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
	
	public MutableTransformer getDelegate() {
		return delegate;
	}

	public void setDelegate(MutableTransformer delegate) {
		this.delegate = delegate;
	}

	public void addChangeListener(ChangeListener l) {
		delegate.addChangeListener(l);
	}

	public void concatenate(AffineTransform transform) {
		delegate.concatenate(transform);
	}

	public void fireStateChanged() {
		delegate.fireStateChanged();
	}

	public ChangeListener[] getChangeListeners() {
		return delegate.getChangeListeners();
	}

	public double getScale() {
		return delegate.getScale();
	}

	public double getScaleX() {
		return delegate.getScaleX();
	}

	public double getScaleY() {
		return delegate.getScaleY();
	}

	public double getShearX() {
		return delegate.getShearX();
	}

	public double getShearY() {
		return delegate.getShearY();
	}

	public AffineTransform getTransform() {
		return delegate.getTransform();
	}

	public double getTranslateX() {
		return delegate.getTranslateX();
	}

	public double getTranslateY() {
		return delegate.getTranslateY();
	}

	public Point2D inverseTransform(Point2D p) {
		return delegate.inverseTransform(p);
	}

	public Shape inverseTransform(Shape shape) {
		return delegate.inverseTransform(shape);
	}

	public void preConcatenate(AffineTransform transform) {
		delegate.preConcatenate(transform);
	}

	public void removeChangeListener(ChangeListener l) {
		delegate.removeChangeListener(l);
	}

	public void rotate(double radians, Point2D point) {
		delegate.rotate(radians, point);
	}

	public void scale(double sx, double sy, Point2D point) {
		delegate.scale(sx, sy, point);
	}

	public void setScale(double sx, double sy, Point2D point) {
		delegate.setScale(sx, sy, point);
	}

	public void setToIdentity() {
		delegate.setToIdentity();
	}

	public void setTranslate(double dx, double dy) {
		delegate.setTranslate(dx, dy);
	}

	public void shear(double shx, double shy, Point2D from) {
		delegate.shear(shx, shy, from);
	}

	public Point2D transform(Point2D p) {
		return delegate.transform(p);
	}

	public Shape transform(Shape shape) {
		return delegate.transform(shape);
	}

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
