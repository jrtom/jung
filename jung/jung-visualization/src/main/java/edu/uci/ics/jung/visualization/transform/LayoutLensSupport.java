/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 21, 2005
 */

package edu.uci.ics.jung.visualization.transform;

import java.awt.Dimension;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import edu.uci.ics.jung.visualization.picking.LayoutLensShapePickSupport;
/**
 * A class to make it easy to add an 
 * examining lens to a jung graph application. See HyperbolicTransformerDemo
 * for an example of how to use it.
 * 
 * @author Tom Nelson
 *
 *
 */
public class LayoutLensSupport<V,E> extends AbstractLensSupport<V,E> 
    implements LensSupport {

	protected GraphElementAccessor<V,E> pickSupport;
	
    public LayoutLensSupport(VisualizationViewer<V,E> vv) {
        this(vv, new HyperbolicTransformer(vv, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
                new ModalLensGraphMouse());
    }
    /**
     * create the base class, setting common members and creating
     * a custom GraphMouse
     * @param vv the VisualizationViewer to work on
     */
    public LayoutLensSupport(VisualizationViewer<V,E> vv, LensTransformer lensTransformer,
            ModalGraphMouse lensGraphMouse) {
        super(vv, lensGraphMouse);
        this.lensTransformer = lensTransformer;
        this.pickSupport = vv.getPickSupport();

        Dimension d = vv.getSize();
        if(d.width <= 0 || d.height <= 0) {
            d = vv.getPreferredSize();
        }
        lensTransformer.setViewRadius(d.width/5);
   }
    
    public void activate() {
        if(lens == null) {
            lens = new Lens(lensTransformer);
        }
        if(lensControls == null) {
            lensControls = new LensControls(lensTransformer);
        }
        vv.getRenderContext().setPickSupport(new LayoutLensShapePickSupport<V,E>(vv));
        vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.LAYOUT, lensTransformer);
        vv.prependPreRenderPaintable(lens);
        vv.addPostRenderPaintable(lensControls);
        vv.setGraphMouse(lensGraphMouse);
        vv.setToolTipText(instructions);
        vv.repaint();
    }
    
    public void deactivate() {
        if(lensTransformer != null) {
            vv.removePreRenderPaintable(lens);
            vv.removePostRenderPaintable(lensControls);
            vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.LAYOUT, lensTransformer.getDelegate());
        }
        vv.getRenderContext().setPickSupport(pickSupport);
        vv.setToolTipText(defaultToolTipText);
        vv.setGraphMouse(graphMouse);
        vv.repaint();
    }
}
