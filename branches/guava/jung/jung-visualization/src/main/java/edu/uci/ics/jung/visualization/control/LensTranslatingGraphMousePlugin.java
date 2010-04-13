/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/** 
 * Extends TranslatingGraphMousePlugin and adds the capability 
 * to drag and resize the viewing
 * lens in the graph view. Mouse1 in the center moves the lens,
 * mouse1 on the edge resizes the lens. The default mouse button and
 * modifiers can be overridden in the constructor.
 * 
 * 
 * @author Tom Nelson
 */
public class LensTranslatingGraphMousePlugin extends TranslatingGraphMousePlugin
implements MouseListener, MouseMotionListener {
    
    protected boolean dragOnLens;
    protected boolean dragOnEdge;
    protected double edgeOffset;
    /**
     * create an instance with default modifiers
     */
    public LensTranslatingGraphMousePlugin() {
        this(MouseEvent.BUTTON1_MASK);
    }
    
    /**
     * create an instance with passed modifer value
     * @param modifiers the mouse event modifier to activate this function
     */
    public LensTranslatingGraphMousePlugin(int modifiers) {
        super(modifiers);
    }
    
    /**
     * Check the event modifiers. Set the 'down' point for later
     * use. If this event satisfies the modifiers, change the cursor
     * to the system 'move cursor'
     * @param e the event
     */
    public void mousePressed(MouseEvent e) {
        VisualizationViewer vv = (VisualizationViewer)e.getSource();
        MutableTransformer vt = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
        if(vt instanceof LensTransformer) {
        	vt = ((LensTransformer)vt).getDelegate();
        }
        Point2D p = vt.inverseTransform(e.getPoint());
        boolean accepted = checkModifiers(e);
        if(accepted) {
            vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            testViewCenter(vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT), p);
            testViewCenter(vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW), p);
            vv.repaint();
        }
        super.mousePressed(e);
    }
    
    /**
     * called to change the location of the lens
     * @param transformer
     * @param point
     */
    private void setViewCenter(MutableTransformer transformer, Point2D point) {
        if(transformer instanceof LensTransformer) {
            LensTransformer ht =
                (LensTransformer)transformer;
            ht.setViewCenter(point);
        }
    }
    
    /**
     * called to change the radius of the lens
     * @param transformer
     * @param point
     */
    private void setViewRadius(MutableTransformer transformer, Point2D point) {
        if(transformer instanceof LensTransformer) {
            LensTransformer ht =
                (LensTransformer)transformer;
            double distanceFromCenter = ht.getDistanceFromCenter(point);
            ht.setViewRadius(distanceFromCenter+edgeOffset);
        }
    }
    
    /**
     * called to set up translating the lens center or changing the size
     * @param transformer
     * @param point
     */
    private void testViewCenter(MutableTransformer transformer, Point2D point) {
        if(transformer instanceof LensTransformer) {
            LensTransformer ht =
                (LensTransformer)transformer;
            double distanceFromCenter = ht.getDistanceFromCenter(point);
            if(distanceFromCenter < 10) {
                ht.setViewCenter(point);
                dragOnLens = true;
            } else if(Math.abs(distanceFromCenter - ht.getViewRadius()) < 10) {
                edgeOffset = ht.getViewRadius() - distanceFromCenter;
                ht.setViewRadius(distanceFromCenter+edgeOffset);
                dragOnEdge = true;
            }
        }
    }
    
    /**
     * unset the 'down' point and change the cursoe back to the system
     * default cursor
     */
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        dragOnLens = false;
        dragOnEdge = false;
        edgeOffset = 0;
    }
    
    /**
     * check the modifiers. If accepted, move or resize the lens according
     * to the dragging of the mouse pointer
     * @param e the event
     */
    public void mouseDragged(MouseEvent e) {
        VisualizationViewer vv = (VisualizationViewer)e.getSource();
        MutableTransformer vt = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
        if(vt instanceof LensTransformer) {
        	vt = ((LensTransformer)vt).getDelegate();
        }
        Point2D p = vt.inverseTransform(e.getPoint());
        boolean accepted = checkModifiers(e);

        if(accepted ) {
            MutableTransformer modelTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
            vv.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            if(dragOnLens) {
                setViewCenter(modelTransformer, p);
                setViewCenter(vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW), p);
                e.consume();
                vv.repaint();

            } else if(dragOnEdge) {

                setViewRadius(modelTransformer, p);
                setViewRadius(vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW), p);
                e.consume();
                vv.repaint();
                
            } else {
            	
            	MutableTransformer mt = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
                Point2D iq = vt.inverseTransform(down);
                iq = mt.inverseTransform(iq);
                Point2D ip = vt.inverseTransform(e.getPoint());
                ip = mt.inverseTransform(ip);
                float dx = (float) (ip.getX()-iq.getX());
                float dy = (float) (ip.getY()-iq.getY());
                
                modelTransformer.translate(dx, dy);
                down.x = e.getX();
                down.y = e.getY();
            }
        }
    }
}
