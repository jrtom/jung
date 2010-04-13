/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * 
 */
package edu.uci.ics.jung.visualization.annotations;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.transform.AffineTransformer;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * handles the selection of annotations, and the support for the
 * tools to draw them at specific layers.
 * 
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 */
public class AnnotationManager {
	
    protected AnnotationRenderer annotationRenderer = new AnnotationRenderer();
	protected AnnotationPaintable lowerAnnotationPaintable;
	protected AnnotationPaintable upperAnnotationPaintable;
	
	protected RenderContext<?,?> rc;
	protected AffineTransformer transformer;

	public AnnotationManager(RenderContext<?,?> rc) {
		this.rc = rc;
		this.lowerAnnotationPaintable = new AnnotationPaintable(rc, annotationRenderer);
		this.upperAnnotationPaintable = new AnnotationPaintable(rc, annotationRenderer);
		
		MutableTransformer mt = rc.getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
		if(mt instanceof AffineTransformer) {
			transformer = (AffineTransformer)mt;
		} else if(mt instanceof LensTransformer) {
			transformer = (AffineTransformer)((LensTransformer)mt).getDelegate();
		}

	}
	
	public AnnotationPaintable getAnnotationPaintable(Annotation.Layer layer) {
		if(layer == Annotation.Layer.LOWER) {
			return this.lowerAnnotationPaintable;
		}
		if(layer == Annotation.Layer.UPPER) {
			return this.upperAnnotationPaintable;
		}
		return null;
	}
	
	public void add(Annotation.Layer layer, Annotation<?> annotation) {
		if(layer == Annotation.Layer.LOWER) {
			this.lowerAnnotationPaintable.add(annotation);
		}
		if(layer == Annotation.Layer.UPPER) {
			this.upperAnnotationPaintable.add(annotation);
		}
	}
	
	public void remove(Annotation<?> annotation) {
		this.lowerAnnotationPaintable.remove(annotation);
		this.upperAnnotationPaintable.remove(annotation);
	}
	
	protected AnnotationPaintable getLowerAnnotationPaintable() {
		return lowerAnnotationPaintable;
	}
	
	protected AnnotationPaintable getUpperAnnotationPaintable() {
		return upperAnnotationPaintable;
	}
	
	@SuppressWarnings("unchecked")
    public Annotation getAnnotation(Point2D p) {
		Set<Annotation> annotations = new HashSet<Annotation>(lowerAnnotationPaintable.getAnnotations());
		annotations.addAll(upperAnnotationPaintable.getAnnotations());
		return getAnnotation(p, annotations);
	}
	
	@SuppressWarnings("unchecked")
    public Annotation getAnnotation(Point2D p, Collection<Annotation> annotations) {
		double closestDistance = Double.MAX_VALUE;
		Annotation closestAnnotation = null;
		for(Annotation annotation : annotations) {
			Object ann = annotation.getAnnotation();
			if(ann instanceof Shape) {
				Point2D ip = rc.getMultiLayerTransformer().inverseTransform(p);
				Shape shape = (Shape)ann;
				if(shape.contains(ip)) {
					
					Rectangle2D shapeBounds = shape.getBounds2D();
					Point2D shapeCenter = new Point2D.Double(shapeBounds.getCenterX(), shapeBounds.getCenterY());
					double distanceSq = shapeCenter.distanceSq(ip);
					if(distanceSq < closestDistance) {
						closestDistance = distanceSq;
						closestAnnotation = annotation;
					}
				}
			} else if(ann instanceof String) {
				
				Point2D ip = rc.getMultiLayerTransformer().inverseTransform(Layer.VIEW, p);
				Point2D ap = annotation.getLocation();
				String label = (String)ann;
				Component component = prepareRenderer(rc, annotationRenderer, label);
				
				AffineTransform base = new AffineTransform(transformer.getTransform());
				double rotation = transformer.getRotation();
				// unrotate the annotation
				AffineTransform unrotate =
					AffineTransform.getRotateInstance(-rotation, ap.getX(), ap.getY());
				base.concatenate(unrotate);
				
				Dimension d = component.getPreferredSize();
				Rectangle2D componentBounds = new Rectangle2D.Double(ap.getX(), ap.getY(), d.width, d.height);
				
				Shape componentBoundsShape = base.createTransformedShape(componentBounds);
				Point2D componentCenter = new Point2D.Double(componentBoundsShape.getBounds().getCenterX(),
						componentBoundsShape.getBounds().getCenterY());
				if(componentBoundsShape.contains(ip)) {
					double distanceSq = componentCenter.distanceSq(ip);
					if(distanceSq < closestDistance) {
						closestDistance = distanceSq;
						closestAnnotation = annotation;
					}
				}
				
			}
		}
		return closestAnnotation;
	}
	
	public Component prepareRenderer(RenderContext<?,?> rc, AnnotationRenderer annotationRenderer, Object value) {
		return annotationRenderer.getAnnotationRendererComponent(rc.getScreenDevice(), value);
	}


	
	

}
