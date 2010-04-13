/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * Created on Feb 2, 2005
 *
 */
package edu.uci.ics.jung.visualization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;
import edu.uci.ics.jung.visualization.transform.shape.Intersector;



/**
 * GraphZoomScrollPane is a Container for the Graph's VisualizationViewer
 * and includes custom horizontal and vertical scrollbars.
 * GraphZoomScrollPane listens for changes in the scale and
 * translation of the VisualizationViewer, and will update the
 * scrollbar positions and sizes accordingly. Changes in the
 * scrollbar positions will cause the corresponding change in
 * the translation component (offset) of the VisualizationViewer.
 * The scrollbars are modified so that they will allow panning
 * of the graph when the scale has been changed (e.g. zoomed-in
 * or zoomed-out).
 * 
 * The lower-right corner of this component is available to
 * use as a small button or menu.
 * 
 * samples.graph.GraphZoomScrollPaneDemo shows the use of this component.
 * 
 * @author Tom Nelson 
 *
 * 
 */
@SuppressWarnings("serial")
public class GraphZoomScrollPane extends JPanel {
    protected VisualizationViewer vv;
    protected JScrollBar horizontalScrollBar;
    protected JScrollBar verticalScrollBar;
    protected JComponent corner;
    protected boolean scrollBarsMayControlAdjusting = true;
    protected JPanel south;
    
    /**
     * Create an instance of the GraphZoomScrollPane to contain the
     * VisualizationViewer
     * @param vv
     */
    public GraphZoomScrollPane(VisualizationViewer vv) {
        super(new BorderLayout());
        this.vv = vv;
        addComponentListener(new ResizeListener());        
        Dimension d = vv.getGraphLayout().getSize();
        verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL, 0, d.height, 0, d.height);
        horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL, 0, d.width, 0, d.width);
        verticalScrollBar.addAdjustmentListener(new VerticalAdjustmentListenerImpl());
        horizontalScrollBar.addAdjustmentListener(new HorizontalAdjustmentListenerImpl());
        verticalScrollBar.setUnitIncrement(20);
        horizontalScrollBar.setUnitIncrement(20);
        // respond to changes in the VisualizationViewer's transform
        // and set the scroll bar parameters appropriately
        vv.addChangeListener(
                new ChangeListener(){
            public void stateChanged(ChangeEvent evt) {
                VisualizationViewer vv = 
                    (VisualizationViewer)evt.getSource();
                setScrollBars(vv);
            }
        });
        add(vv);
        add(verticalScrollBar, BorderLayout.EAST);
        south = new JPanel(new BorderLayout());
        south.add(horizontalScrollBar);
        setCorner(new JPanel());
        add(south, BorderLayout.SOUTH);
    }
    
    /**
     * listener for adjustment of the horizontal scroll bar.
     * Sets the translation of the VisualizationViewer
     */
    class HorizontalAdjustmentListenerImpl implements AdjustmentListener {
        int previous = 0;
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int hval = e.getValue();
            float dh = previous - hval;
            previous = hval;
            if(dh != 0 && scrollBarsMayControlAdjusting) {
                // get the uniform scale of all transforms
                float layoutScale = (float) vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale();
                dh *= layoutScale;
                AffineTransform at = AffineTransform.getTranslateInstance(dh, 0);
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).preConcatenate(at);
            }
        }
    }
    
    /**
     * Listener for adjustment of the vertical scroll bar.
     * Sets the translation of the VisualizationViewer
     */
    class VerticalAdjustmentListenerImpl implements AdjustmentListener {
        int previous = 0;
        public void adjustmentValueChanged(AdjustmentEvent e) {
            JScrollBar sb = (JScrollBar)e.getSource();
            BoundedRangeModel m = sb.getModel();
            int vval = m.getValue();
            float dv = previous - vval;
            previous = vval;
            if(dv != 0 && scrollBarsMayControlAdjusting) {
            
                // get the uniform scale of all transforms
                float layoutScale = (float) vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getScale();
                dv *= layoutScale;
                AffineTransform at = AffineTransform.getTranslateInstance(0, dv);
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).preConcatenate(at);
            }
        }
    }
    
    /**
     * use the supplied vv characteristics to set the position and
     * dimensions of the scroll bars. Called in response to
     * a ChangeEvent from the VisualizationViewer
     * @param xform the transform of the VisualizationViewer
     */
    private void setScrollBars(VisualizationViewer vv) {
        Dimension d = vv.getGraphLayout().getSize();
        Rectangle2D vvBounds = vv.getBounds();
        
        // a rectangle representing the layout
        Rectangle layoutRectangle = 
            new Rectangle(0,0,d.width,d.height);
            		//-d.width/2, -d.height/2, 2*d.width, 2*d.height);
        
        BidirectionalTransformer viewTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
        BidirectionalTransformer layoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

        Point2D h0 = new Point2D.Double(vvBounds.getMinX(), vvBounds.getCenterY());
        Point2D h1 = new Point2D.Double(vvBounds.getMaxX(), vvBounds.getCenterY());
        Point2D v0 = new Point2D.Double(vvBounds.getCenterX(), vvBounds.getMinY());
        Point2D v1 = new Point2D.Double(vvBounds.getCenterX(), vvBounds.getMaxY());
        
        h0 = viewTransformer.inverseTransform(h0);
        h0 = layoutTransformer.inverseTransform(h0);
        h1 = viewTransformer.inverseTransform(h1);
        h1 = layoutTransformer.inverseTransform(h1);
        v0 = viewTransformer.inverseTransform(v0);
        v0 = layoutTransformer.inverseTransform(v0);
        v1 = viewTransformer.inverseTransform(v1);
        v1 = layoutTransformer.inverseTransform(v1);
        
        scrollBarsMayControlAdjusting = false;
        setScrollBarValues(layoutRectangle, h0, h1, v0, v1);
        scrollBarsMayControlAdjusting = true;
    }
    
    @SuppressWarnings("unchecked")
    protected void setScrollBarValues(Rectangle rectangle, 
            Point2D h0, Point2D h1, 
            Point2D v0, Point2D v1) {
        boolean containsH0 = rectangle.contains(h0);
        boolean containsH1 = rectangle.contains(h1);
        boolean containsV0 = rectangle.contains(v0);
        boolean containsV1 = rectangle.contains(v1);
        
        // horizontal scrollbar:
        
        Intersector intersector = new Intersector(rectangle, new Line2D.Double(h0, h1));
        
        int min = 0;
        int ext;
        int val = 0;
        int max;
        
        Set points = intersector.getPoints();
        Point2D first = null;
        Point2D second = null;
        
        Point2D[] pointArray = (Point2D[])points.toArray(new Point2D[points.size()]);
        if(pointArray.length > 1) {
            first = pointArray[0];
            second = pointArray[1];
        } else if(pointArray.length > 0) {
            first = second = pointArray[0];
        }
        
        if(first != null && second != null) {
            // correct direction of intersect points
            if((h0.getX() - h1.getX()) * (first.getX() - second.getX()) < 0) {
                // swap them
                Point2D temp = first;
                first = second;
                second = temp;
            }

            if(containsH0 && containsH1) {
                max = (int)first.distance(second);
                val = (int)first.distance(h0);
                ext = (int)h0.distance(h1);
                
            } else if(containsH0) {
                max = (int)first.distance(second);
                val = (int)first.distance(h0);
                ext = (int)h0.distance(second);
                
            } else if(containsH1) {
                max = (int) first.distance(second);
                val = 0;
                ext = (int) first.distance(h1);
                
            } else {
                max = ext = rectangle.width;
                val = min;
            }
            horizontalScrollBar.setValues(val, ext+1, min, max);
        }
        
        // vertical scroll bar
        min = val = 0;
        
        intersector.intersectLine(new Line2D.Double(v0, v1));
        points = intersector.getPoints();
        
        pointArray = (Point2D[])points.toArray(new Point2D[points.size()]);
        if(pointArray.length > 1) {
            first = pointArray[0];
            second = pointArray[1];
        } else if(pointArray.length > 0) {
            first = second = pointArray[0];
        }
        
        if(first != null && second != null) {
            
            // arrange for direction
            if((v0.getY() - v1.getY()) * (first.getY() - second.getY()) < 0) {
                // swap them
                Point2D temp = first;
                first = second;
                second = temp;
            }
            
            if(containsV0 && containsV1) {
                max = (int)first.distance(second);
                val = (int)first.distance(v0);
                ext = (int)v0.distance(v1);
                
            } else if(containsV0) {
                max = (int)first.distance(second);
                val = (int)first.distance(v0);
                ext = (int)v0.distance(second);
                
            } else if(containsV1) {
                max = (int) first.distance(second);
                val = 0;
                ext = (int) first.distance(v1);
                
            } else {
                max = ext = rectangle.height;
                val = min;
            }
            verticalScrollBar.setValues(val, ext+1, min, max);
        }
    }

    /**
     * Listener to adjust the scroll bar parameters when the window
     * is resized
     */
	protected class ResizeListener extends ComponentAdapter {

		public void componentHidden(ComponentEvent e) {
		}

		public void componentResized(ComponentEvent e) {
		    setScrollBars(vv);
		}	
		public void componentShown(ComponentEvent e) {
		}
	}

    /**
     * @return Returns the corner component.
     */
    public JComponent getCorner() {
        return corner;
    }

    /**
     * @param corner The cornerButton to set.
     */
    public void setCorner(JComponent corner) {
        this.corner = corner;
        corner.setPreferredSize(new Dimension(verticalScrollBar.getPreferredSize().width,
                horizontalScrollBar.getPreferredSize().height));
        south.add(this.corner, BorderLayout.EAST);
    }

    public JScrollBar getHorizontalScrollBar() {
        return horizontalScrollBar;
    }

    public JScrollBar getVerticalScrollBar() {
        return verticalScrollBar;
    }
}
