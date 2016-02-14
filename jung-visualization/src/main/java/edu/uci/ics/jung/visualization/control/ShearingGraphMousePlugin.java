/*
 * Copyright (c) 2005, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

/** 
 * ShearingGraphMousePlugin allows the user to drag with the mouse
 * to shear the transform either in the horizontal or vertical direction.
 * By default, the control or meta key must be depressed to activate
 * shearing. 
 * 
 * 
 * @author Tom Nelson
 */
public class ShearingGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

    private static int mask = MouseEvent.CTRL_MASK;
    
    static {
        if(System.getProperty("os.name").startsWith("Mac")) {
            mask = MouseEvent.META_MASK;
        }
    }
	/**
	 * create an instance with default modifier values
	 */
	public ShearingGraphMousePlugin() {
	    this(MouseEvent.BUTTON1_MASK | mask);
	}

	/**
	 * create an instance with passed modifier values
	 * @param modifiers the mouse modifiers to use
	 */
	public ShearingGraphMousePlugin(int modifiers) {
	    super(modifiers);
	    Dimension cd = Toolkit.getDefaultToolkit().getBestCursorSize(16,16);
        BufferedImage cursorImage = 
        		new BufferedImage(cd.width,cd.height,BufferedImage.TYPE_INT_ARGB);
        Graphics g = cursorImage.createGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.addRenderingHints(Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g.setColor(new Color(0,0,0,0));
        g.fillRect(0,0,16,16);
        
        int left = 0;
        int top = 0;
        int right = 15;
        int bottom = 15;
        
        g.setColor(Color.white);
        g2.setStroke(new BasicStroke(3));
        g.drawLine(left+2,top+5,right-2,top+5);
        g.drawLine(left+2,bottom-5,right-2,bottom-5);
        g.drawLine(left+2,top+5,left+4,top+3);
        g.drawLine(left+2,top+5,left+4,top+7);
        g.drawLine(right-2,bottom-5,right-4,bottom-3);
        g.drawLine(right-2,bottom-5,right-4,bottom-7);

        g.setColor(Color.black);
        g2.setStroke(new BasicStroke(1));
        g.drawLine(left+2,top+5,right-2,top+5);
        g.drawLine(left+2,bottom-5,right-2,bottom-5);
        g.drawLine(left+2,top+5,left+4,top+3);
        g.drawLine(left+2,top+5,left+4,top+7);
        g.drawLine(right-2,bottom-5,right-4,bottom-3);
        g.drawLine(right-2,bottom-5,right-4,bottom-7);
        g.dispose();
        cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(), "RotateCursor");
	}

	public void mousePressed(MouseEvent e) {
	    VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>)e.getSource();
	    boolean accepted = checkModifiers(e);
	    down = e.getPoint();
	    if(accepted) {
	        vv.setCursor(cursor);
	    }
	}
    
    public void mouseReleased(MouseEvent e) {
        VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>)e.getSource();
        down = null;
        vv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void mouseDragged(MouseEvent e) {
        if(down == null) return;
        VisualizationViewer<?, ?> vv = (VisualizationViewer<?, ?>)e.getSource();
        boolean accepted = checkModifiers(e);
        if(accepted) {
            MutableTransformer modelTransformer = 
            	vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
            vv.setCursor(cursor);
            Point2D q = down;
            Point2D p = e.getPoint();
            float dx = (float) (p.getX()-q.getX());
            float dy = (float) (p.getY()-q.getY());

            Dimension d = vv.getSize();
            float shx = 2.f*dx/d.height;
            float shy = 2.f*dy/d.width;
            Point2D center = vv.getCenter();
            if(p.getX() < center.getX()) {
                shy = -shy;
            }
            if(p.getY() < center.getY()) {
                shx = -shx;
            }
            modelTransformer.shear(shx, shy, center);
            down.x = e.getX();
            down.y = e.getY();
        
            e.consume();
        }
    }

    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
}
