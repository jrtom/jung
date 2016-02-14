/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 15, 2005
 */

package edu.uci.ics.jung.visualization.control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer;

/**
 * A VisualizationViewer that can act as a satellite view for another
 * (master) VisualizationViewer. In this view, the full graph is always visible
 * and all mouse actions affect the graph in the master view.
 * 
 * A rectangular shape in the satellite view shows the visible bounds of
 * the master view. 
 * 
 * @author Tom Nelson 
 *
 * 
 */
@SuppressWarnings("serial")
public class SatelliteVisualizationViewer<V, E> 
	extends VisualizationViewer<V,E> {
    
    /**
     * the master VisualizationViewer that this is a satellite view for
     */
    protected VisualizationViewer<V,E> master;
    
    /**
     * @param master the master VisualizationViewer for which this is a satellite view
     * @param preferredSize the specified size of the component
     */
    public SatelliteVisualizationViewer(VisualizationViewer<V,E> master,
    		Dimension preferredSize) {
        super(master.getModel(), preferredSize);
        this.master = master;
        
        // create a graph mouse with custom plugins to affect the master view
        ModalGraphMouse gm = new ModalSatelliteGraphMouse();
        setGraphMouse(gm);
        
        // this adds the Lens to the satellite view
        addPreRenderPaintable(new ViewLens<V,E>(this, master));
        
        // get a copy of the current layout transform
        // it may have been scaled to fit the graph
        AffineTransform modelLayoutTransform =
            new AffineTransform(master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).getTransform());
        
        // I want no layout transformations in the satellite view
        // this resets the auto-scaling that occurs in the super constructor
        getRenderContext().getMultiLayerTransformer().setTransformer(Layer.LAYOUT, new MutableAffineTransformer(modelLayoutTransform));
        
        // make sure the satellite listens for changes in the master
        master.addChangeListener(this);
        
        // share the picked state of the master
        setPickedVertexState(master.getPickedVertexState());
        setPickedEdgeState(master.getPickedEdgeState());
    }

    /**
     * @return Returns the master.
     */
    public VisualizationViewer<V,E> getMaster() {
        return master;
    }
    
    /**
     * A four-sided shape that represents the visible part of the
     * master view and is drawn in the satellite view
     * 
     * @author Tom Nelson 
     *
     *
     */
    static class ViewLens<V,E> implements Paintable {

        VisualizationViewer<V,E> master;
        VisualizationViewer<V,E> vv;
        
        public ViewLens(VisualizationViewer<V,E> vv, VisualizationViewer<V,E> master) {
            this.vv = vv;
            this.master = master;
        }
        public void paint(Graphics g) {
            ShapeTransformer masterViewTransformer = 
            	master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);
            ShapeTransformer masterLayoutTransformer = master.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
            ShapeTransformer vvLayoutTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

            Shape lens = master.getBounds();
            lens = masterViewTransformer.inverseTransform(lens);
            lens = masterLayoutTransformer.inverseTransform(lens);
            lens = vvLayoutTransformer.transform(lens);
            Graphics2D g2d = (Graphics2D)g;
            Color old = g.getColor();
            Color lensColor = master.getBackground();
            vv.setBackground(lensColor.darker());
            g.setColor(lensColor);
            g2d.fill(lens);
            g.setColor(Color.gray);
            g2d.draw(lens);
            g.setColor(old);
        }

        public boolean useTransform() {
            return true;
        }
    }

}
