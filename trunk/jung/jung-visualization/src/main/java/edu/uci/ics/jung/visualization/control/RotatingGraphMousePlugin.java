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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
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
 * RotatingGraphMouse provides the abiity to rotate the graph using
 * the mouse. By default, it is activated by mouse button one drag
 * with the shift key pressed. The modifiers can be overridden so that
 * a different mouse/key combination activates the rotation
 * 
 * @author Tom Nelson
 */
public class RotatingGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

	/**
	 * create an instance with default modifier values
	 */
	public RotatingGraphMousePlugin() {
	    this(MouseEvent.BUTTON1_MASK | MouseEvent.SHIFT_MASK);
	}

	/**
	 * create an instance with passed zoom in/out values
	 * @param modifiers the event modifiers to trigger rotation
	 */
	public RotatingGraphMousePlugin(int modifiers) {
	    super(modifiers);
	    Dimension cd = Toolkit.getDefaultToolkit().getBestCursorSize(16,16);
        BufferedImage cursorImage = 
        		new BufferedImage(cd.width,cd.height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = cursorImage.createGraphics();
        g.addRenderingHints(Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g.setColor(new Color(0,0,0,0));
        g.fillRect(0,0,16,16);

        int left = 0;
        int top = 0;
        int right = 15;
        int bottom = 15;
        
        g.setColor(Color.white);
        g.setStroke(new BasicStroke(3));
        // top bent line
        g.drawLine(left+2,top+6,right/2+1,top);
        g.drawLine(right/2+1,top,right-2,top+5);
        // bottom bent line
        g.drawLine(left+2,bottom-6,right/2,bottom);
        g.drawLine(right/2,bottom,right-2,bottom-6);
        // top arrow
        g.drawLine(left+2,top+6,left+5,top+6);
        g.drawLine(left+2,top+6,left+2,top+3);
        // bottom arrow
        g.drawLine(right-2,bottom-6,right-6,bottom-6);
        g.drawLine(right-2, bottom-6,right-2,bottom-3);

        
        g.setColor(Color.black);
        g.setStroke(new BasicStroke(1));
        // top bent line
        g.drawLine(left+2,top+6,right/2+1,top);
        g.drawLine(right/2+1,top,right-2,top+5);
        // bottom bent line
        g.drawLine(left+2,bottom-6,right/2,bottom);
        g.drawLine(right/2,bottom,right-2,bottom-6);
        // top arrow
        g.drawLine(left+2,top+6,left+5,top+6);
        g.drawLine(left+2,top+6,left+2,top+3);
        // bottom arrow
        g.drawLine(right-2,bottom-6,right-6,bottom-6);
        g.drawLine(right-2, bottom-6,right-2,bottom-3);

        g.dispose();
        
        cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(), "RotateCursor");
	}

    /**
     * save the 'down' point and check the modifiers. If the
     * modifiers are accepted, set the cursor to the 'hand' cursor
	 * @param e the event
	 */
    public void mousePressed(MouseEvent e) {
        VisualizationViewer vv = (VisualizationViewer)e.getSource();
           boolean accepted = checkModifiers(e);
           down = e.getPoint();
          if(accepted) {
               vv.setCursor(cursor);
           }
    }
    
	/**
     * unset the down point and change the cursor back to the default
	 */
    public void mouseReleased(MouseEvent e) {
        VisualizationViewer vv = (VisualizationViewer)e.getSource();
        down = null;
        vv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * check the modifiers. If accepted, use the mouse drag motion
     * to rotate the graph
	 */
    public void mouseDragged(MouseEvent e) {
        if(down == null) return;
        VisualizationViewer vv = (VisualizationViewer)e.getSource();
        boolean accepted = checkModifiers(e);
        if(accepted) {
            MutableTransformer modelTransformer =
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
            // rotate
            vv.setCursor(cursor);
            
            Point2D center = vv.getCenter();
            Point2D q = down;
            Point2D p = e.getPoint();
            Point2D v1 = new Point2D.Double(center.getX()-p.getX(), center.getY()-p.getY());
            Point2D v2 = new Point2D.Double(center.getX()-q.getX(), center.getY()-q.getY());
            double theta = angleBetween(v1, v2);
            modelTransformer.rotate(theta, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, center));
            down.x = e.getX();
            down.y = e.getY();
        
            e.consume();
        }
    }
    
    /**
     * Returns the angle between two vectors from the origin
     * to points v1 and v2.
     * @param v1
     * @param v2
     * @return
     */
    protected double angleBetween(Point2D v1, Point2D v2) {
        double x1 = v1.getX();
        double y1 = v1.getY();
        double x2 = v2.getX();
        double y2 = v2.getY();
        // cross product for direction
        double cross = x1*y2 - x2*y1;
        int cw = 1;
        if(cross > 0) {
            cw = -1;
        } 
        // dot product for angle
        double angle = 
            cw*Math.acos( ( x1*x2 + y1*y2 ) / 
                ( Math.sqrt( x1*x1 + y1*y1 ) * 
                        Math.sqrt( x2*x2 + y2*y2 ) ) );
        if(Double.isNaN(angle)) {
            angle = 0;
        }
        return angle;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
}
