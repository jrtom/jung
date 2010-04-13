/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package edu.uci.ics.jung.visualization.transform.shape;

import java.awt.Dimension;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.picking.ViewLensShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.ReshapingEdgeRenderer;
import edu.uci.ics.jung.visualization.transform.AbstractLensSupport;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.LensTransformer;

/**
 * Uses a LensTransformer to use in the view
 * transform. This one will distort Vertex shapes.
 * 
 * @author Tom Nelson 
 *
 *
 */
public class ViewLensSupport<V,E> extends AbstractLensSupport<V,E>
    implements LensSupport {
    
    protected RenderContext<V,E> renderContext;
    protected GraphicsDecorator lensGraphicsDecorator;
    protected GraphicsDecorator savedGraphicsDecorator;
    protected GraphElementAccessor<V,E> pickSupport;
    protected Renderer.Edge<V,E> savedEdgeRenderer;
    protected Renderer.Edge<V,E> reshapingEdgeRenderer;

    public ViewLensSupport(VisualizationViewer<V,E> vv, 
    		LensTransformer lensTransformer,
            ModalGraphMouse lensGraphMouse) {
        super(vv, lensGraphMouse);
        this.renderContext = vv.getRenderContext();
        this.pickSupport = renderContext.getPickSupport();
        this.savedGraphicsDecorator = renderContext.getGraphicsContext();
        this.lensTransformer = lensTransformer;
        Dimension d = vv.getSize();
        lensTransformer.setViewRadius(d.width/5);
        this.lensGraphicsDecorator = new TransformingFlatnessGraphics(lensTransformer);
        this.savedEdgeRenderer = vv.getRenderer().getEdgeRenderer();
        this.reshapingEdgeRenderer = new ReshapingEdgeRenderer<V,E>();
        this.reshapingEdgeRenderer.setEdgeArrowRenderingSupport(savedEdgeRenderer.getEdgeArrowRenderingSupport());

    }
    public void activate() {
    	lensTransformer.setDelegate(vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW));
        if(lens == null) {
            lens = new Lens(lensTransformer);
        }
        if(lensControls == null) {
            lensControls = new LensControls(lensTransformer);
        }
        renderContext.setPickSupport(new ViewLensShapePickSupport<V,E>(vv));
        lensTransformer.setDelegate(vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW));
        vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.VIEW, lensTransformer);
        this.renderContext.setGraphicsContext(lensGraphicsDecorator);
        vv.getRenderer().setEdgeRenderer(reshapingEdgeRenderer);
        vv.prependPreRenderPaintable(lens);
        vv.addPostRenderPaintable(lensControls);
        vv.setGraphMouse(lensGraphMouse);
        vv.setToolTipText(instructions);
        vv.repaint();
    }

    public void deactivate() {
//    	savedViewTransformer.setTransform(lensTransformer.getDelegate().getTransform());
//        vv.setViewTransformer(savedViewTransformer);
    	renderContext.setPickSupport(pickSupport);
        vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.VIEW, lensTransformer.getDelegate());
        vv.removePreRenderPaintable(lens);
        vv.removePostRenderPaintable(lensControls);
        this.renderContext.setGraphicsContext(savedGraphicsDecorator);
        vv.setRenderContext(renderContext);
        vv.setToolTipText(defaultToolTipText);
        vv.setGraphMouse(graphMouse);
        vv.getRenderer().setEdgeRenderer(savedEdgeRenderer);
        vv.repaint();
    }
}