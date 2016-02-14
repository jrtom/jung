/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * 
 */
package edu.uci.ics.jung.visualization.annotations;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.transform.AffineTransformer;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/**
 * handles the actual drawing of annotations
 * 
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 */
public class AnnotationPaintable implements Paintable {
	
    @SuppressWarnings("rawtypes")
	protected Set<Annotation> annotations = new HashSet<Annotation>();
    protected AnnotationRenderer annotationRenderer;

	protected RenderContext<?,?> rc;
	protected AffineTransformer transformer;
	
	public AnnotationPaintable(RenderContext<?,?> rc, AnnotationRenderer annotationRenderer) {
		this.rc = rc;
		this.annotationRenderer = annotationRenderer;
		MutableTransformer mt = rc.getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
		if(mt instanceof AffineTransformer) {
			transformer = (AffineTransformer)mt;
		} else if(mt instanceof LensTransformer) {
			transformer = (AffineTransformer)((LensTransformer)mt).getDelegate();
		}
	}
	
    public void add(Annotation<?> annotation) {
		annotations.add(annotation);
	}
	
    public void remove(Annotation<?> annotation) {
		annotations.remove(annotation);
	}
	
    /**
	 * @return the annotations
	 */
    @SuppressWarnings("rawtypes")
	public Set<Annotation> getAnnotations() {
		return Collections.unmodifiableSet(annotations);
	}

    public void paint(Graphics g) {
    	Graphics2D g2d = (Graphics2D)g;
        Color oldColor = g.getColor();
        for(Annotation<?> annotation : annotations) {
        	Object ann = annotation.getAnnotation();
        	if(ann instanceof Shape) {
            	Shape shape = (Shape)ann;
            	Paint paint = annotation.getPaint();
            	Shape s = transformer.transform(shape);
            	g2d.setPaint(paint);
            	if(annotation.isFill()) {
            		g2d.fill(s);
            	} else {
            		g2d.draw(s);
            	}
        	} else if(ann instanceof String) {
            	Point2D p = annotation.getLocation();
            	String label = (String)ann;
                Component component = prepareRenderer(rc, annotationRenderer, label);
                component.setForeground((Color)annotation.getPaint());
                if(annotation.isFill()) {
                	((JComponent)component).setOpaque(true);
                	component.setBackground((Color)annotation.getPaint());
                	component.setForeground(Color.black);
                }
                Dimension d = component.getPreferredSize();
                AffineTransform old = g2d.getTransform();
                AffineTransform base = new AffineTransform(old);
                AffineTransform xform = transformer.getTransform();

                double rotation = transformer.getRotation();
                // unrotate the annotation
                AffineTransform unrotate = 
                	AffineTransform.getRotateInstance(-rotation, p.getX(), p.getY());
                base.concatenate(xform);
                base.concatenate(unrotate);
                g2d.setTransform(base);
                rc.getRendererPane().paintComponent(g, component, rc.getScreenDevice(), 
                        (int)p.getX(), (int)p.getY(),
                        d.width, d.height, true);
                g2d.setTransform(old);
        	}
        }
        g.setColor(oldColor);
    }
    
	public Component prepareRenderer(RenderContext<?,?> rc, AnnotationRenderer annotationRenderer, Object value) {
		return annotationRenderer.getAnnotationRendererComponent(rc.getScreenDevice(), value);
	}

    public boolean useTransform() {
        return true;
    }
}
