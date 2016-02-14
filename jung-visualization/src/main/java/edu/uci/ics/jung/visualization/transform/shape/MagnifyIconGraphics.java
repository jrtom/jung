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

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;


/**
 * Subclassed to apply a magnification transform to an icon.
 *  
 * @author Tom Nelson 
 *
 *
 */
public class MagnifyIconGraphics extends TransformingFlatnessGraphics {
    
    public MagnifyIconGraphics(BidirectionalTransformer transformer) {
        this(transformer, null);
    }
    
    public MagnifyIconGraphics(BidirectionalTransformer Function, Graphics2D delegate) {
        super(Function, delegate);
    }
    
    public void draw(Icon icon, Component c, Shape clip, int x, int y) {
    	
    	if(transformer instanceof MagnifyShapeTransformer) {
    		MagnifyShapeTransformer mst = (MagnifyShapeTransformer)transformer;
    		int w = icon.getIconWidth();
    		int h = icon.getIconHeight();
    		Rectangle2D r = new Rectangle2D.Double(x-w/2,y-h/2,w,h);
    		Shape lens = mst.getLensShape();
    		if(lens.intersects(r)) {
    			// magnify the whole icon
    			Rectangle2D s = mst.magnify(r).getBounds2D();
    			if(lens.intersects(s)) {
    				clip = mst.transform(clip);
    				double sx = s.getWidth()/r.getWidth();
    				double sy = s.getHeight()/r.getHeight();

    				AffineTransform old = delegate.getTransform();
    				AffineTransform xform = new AffineTransform(old);
    				xform.translate(s.getMinX(), s.getMinY());
    				xform.scale(sx, sy);
    				xform.translate(-s.getMinX(), -s.getMinY());
    				Shape oldClip = delegate.getClip();
    				delegate.clip(clip);
    				delegate.setTransform(xform);
    				icon.paintIcon(c, delegate, (int)s.getMinX(), (int)s.getMinY());
    				delegate.setTransform(old);
    				delegate.setClip(oldClip);
    			} else {
    				// clip out the lens so the small icon doesn't get drawn
    				// inside of it
    				Shape oldClip = delegate.getClip();
    				Area viewBounds = new Area(oldClip);
    				viewBounds.subtract(new Area(lens));
    				delegate.setClip(viewBounds);
    				icon.paintIcon(c, delegate, (int)r.getMinX(),(int)r.getMinY());
    				delegate.setClip(oldClip);
    			}

    		} else {
    			icon.paintIcon(c, delegate, (int)r.getMinX(),(int)r.getMinY());
    		}
    	}
    }
}
