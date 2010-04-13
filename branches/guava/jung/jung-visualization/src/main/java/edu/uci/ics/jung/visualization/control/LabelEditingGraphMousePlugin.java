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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/** 
 * 
 * 
 * @author Tom Nelson
 */
public class LabelEditingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener {

	/**
	 * the picked Vertex, if any
	 */
    protected V vertex;
    
    /**
     * the picked Edge, if any
     */
    protected E edge;
    
    /**
	 * create an instance with default settings
	 */
	public LabelEditingGraphMousePlugin() {
	    this(InputEvent.BUTTON1_MASK);
	}

	/**
	 * create an instance with overides
	 * @param selectionModifiers for primary selection
	 * @param addToSelectionModifiers for additional selection
	 */
    public LabelEditingGraphMousePlugin(int selectionModifiers) {
        super(selectionModifiers);
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }
    
	/**
	 * For primary modifiers (default, MouseButton1):
	 * pick a single Vertex or Edge that
     * is under the mouse pointer. If no Vertex or edge is under
     * the pointer, unselect all picked Vertices and edges, and
     * set up to draw a rectangle for multiple selection
     * of contained Vertices.
     * For additional selection (default Shift+MouseButton1):
     * Add to the selection, a single Vertex or Edge that is
     * under the mouse pointer. If a previously picked Vertex
     * or Edge is under the pointer, it is un-picked.
     * If no vertex or Edge is under the pointer, set up
     * to draw a multiple selection rectangle (as above)
     * but do not unpick previously picked elements.
	 * 
	 * @param e the event
	 */
    @SuppressWarnings("unchecked")
    public void mouseClicked(MouseEvent e) {
    	if(e.getModifiers() == modifiers && e.getClickCount() == 2) {
    		VisualizationViewer<V,E> vv = (VisualizationViewer)e.getSource();
    		GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
    		if(pickSupport != null) {
    			Transformer<V,String> vs = vv.getRenderContext().getVertexLabelTransformer();
    			if(vs instanceof MapTransformer) {
    				Map<V,String> map = ((MapTransformer)vs).getMap();
    				Layout<V,E> layout = vv.getGraphLayout();
    				// p is the screen point for the mouse event
    				Point2D p = e.getPoint();

    				V vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
    				if(vertex != null) {
    					String newLabel = vs.transform(vertex);
    					newLabel = JOptionPane.showInputDialog("New Vertex Label for "+vertex);
    					if(newLabel != null) {
    						map.put(vertex, newLabel);
    						vv.repaint();
    					}
    					return;
    				}
    			}
    			Transformer<E,String> es = vv.getRenderContext().getEdgeLabelTransformer();
    			if(es instanceof MapTransformer) {
    				Map<E,String> map = ((MapTransformer)es).getMap();
    				Layout<V,E> layout = vv.getGraphLayout();
    				// p is the screen point for the mouse event
    				Point2D p = e.getPoint();
    				// take away the view transform
    				Point2D ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, p);
    				E edge = pickSupport.getEdge(layout, ip.getX(), ip.getY());
    				if(edge != null) {
    					String newLabel = JOptionPane.showInputDialog("New Edge Label for "+edge);
    					if(newLabel != null) {
    						map.put(edge, newLabel);
    						vv.repaint();
    					}
    					return;
    				}
    			}
    		}
     		e.consume();
     	}
    }

    /**
	 * If the mouse is dragging a rectangle, pick the
	 * Vertices contained in that rectangle
	 * 
	 * clean up settings from mousePressed
	 */
    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {
    }
    
    /**
	 * If the mouse is over a picked vertex, drag all picked
	 * vertices with the mouse.
	 * If the mouse is not over a Vertex, draw the rectangle
	 * to select multiple Vertices
	 * 
	 */

    public void mousePressed(MouseEvent e) {
    }

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
