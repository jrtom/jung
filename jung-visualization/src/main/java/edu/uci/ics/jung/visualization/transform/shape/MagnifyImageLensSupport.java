/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 21, 2005
 */

package edu.uci.ics.jung.visualization.transform.shape;

import java.awt.Dimension;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import edu.uci.ics.jung.visualization.picking.ViewLensShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.ReshapingEdgeRenderer;
import edu.uci.ics.jung.visualization.transform.AbstractLensSupport;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
/**
 * Changes various visualization settings to activate or deactivate an
 * examining lens for a jung graph application. 
 * 
 * @author Tom Nelson
 */
public class MagnifyImageLensSupport<V,E> extends AbstractLensSupport<V,E> {
    
    protected RenderContext<V,E> renderContext;
    protected GraphicsDecorator lensGraphicsDecorator;
    protected GraphicsDecorator savedGraphicsDecorator;
    protected Renderer<V,E> renderer;
    protected Renderer<V,E> transformingRenderer;
    protected GraphElementAccessor<V,E> pickSupport;
    protected Renderer.Edge<V,E> savedEdgeRenderer;
    protected Renderer.Edge<V,E> reshapingEdgeRenderer;

    static final String instructions = 
        "<html><center>Mouse-Drag the Lens center to move it<p>"+
        "Mouse-Drag the Lens edge to resize it<p>"+
        "Ctrl+MouseWheel to change magnification</center></html>";
    
    public MagnifyImageLensSupport(VisualizationViewer<V,E> vv) {
        this(vv, new MagnifyShapeTransformer(vv),
                new ModalLensGraphMouse());
    }
    
    public MagnifyImageLensSupport(VisualizationViewer<V,E> vv, LensTransformer lensTransformer,
            ModalGraphMouse lensGraphMouse) {
        super(vv, lensGraphMouse);
        this.renderContext = vv.getRenderContext();
        this.pickSupport = renderContext.getPickSupport();
        this.renderer = vv.getRenderer();
        this.transformingRenderer = new BasicRenderer<V,E>();
        this.savedGraphicsDecorator = renderContext.getGraphicsContext();
        this.lensTransformer = lensTransformer;
        this.savedEdgeRenderer = vv.getRenderer().getEdgeRenderer();
        this.reshapingEdgeRenderer = new ReshapingEdgeRenderer<V,E>();
        this.reshapingEdgeRenderer.setEdgeArrowRenderingSupport(savedEdgeRenderer.getEdgeArrowRenderingSupport());

        Dimension d = vv.getSize();
        if(d.width == 0 || d.height == 0) {
            d = vv.getPreferredSize();
        }
        lensTransformer.setViewRadius(d.width/5);
        this.lensGraphicsDecorator = new MagnifyIconGraphics(lensTransformer);
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
        vv.addPreRenderPaintable(lens);
        vv.addPostRenderPaintable(lensControls);
        vv.setGraphMouse(lensGraphMouse);
        vv.setToolTipText(instructions);
        vv.repaint();
    }
    
    public void deactivate() {
    	renderContext.setPickSupport(pickSupport);
    	vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.VIEW, lensTransformer.getDelegate());
        vv.removePreRenderPaintable(lens);
        vv.removePostRenderPaintable(lensControls);
        this.renderContext.setGraphicsContext(savedGraphicsDecorator);
        vv.getRenderer().setEdgeRenderer(savedEdgeRenderer);
        vv.setToolTipText(defaultToolTipText);
        vv.setGraphMouse(graphMouse);
        vv.repaint();
    }
}
