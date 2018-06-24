/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform.shape;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.LensTransformSupport;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.TransformSupport;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.AbstractLensSupport;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import java.awt.*;

/**
 * Uses a LensTransformer to use in the view transform. This one will distort Node shapes.
 *
 * @author Tom Nelson
 */
public class ViewLensSupport<N, E> extends AbstractLensSupport<N, E> implements LensSupport {

  protected RenderContext<N, E> renderContext;
  protected GraphicsDecorator lensGraphicsDecorator;
  protected GraphicsDecorator savedGraphicsDecorator;
  protected NetworkElementAccessor<N, E> pickSupport;
  protected Renderer.Edge<N, E> savedEdgeRenderer;
  protected Renderer.Edge<N, E> reshapingEdgeRenderer;

  public ViewLensSupport(
      VisualizationViewer<N, E> vv,
      LensTransformer lensTransformer,
      ModalGraphMouse lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensTransformer = lensTransformer;
    LayoutModel layoutModel = vv.getModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    lensTransformer.getLens().setSize(d);

    this.lensGraphicsDecorator = new TransformingFlatnessGraphics(lensTransformer);
    this.savedEdgeRenderer = vv.getRenderer().getEdgeRenderer();
  }

  public void activate() {
    lensTransformer.setDelegate(
        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW));
    if (lensPaintable == null) {
      lensPaintable = new LensPaintable(lensTransformer);
    }
    if (lensControls == null) {
      lensControls = new LensControls(lensTransformer);
    }
    vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.VIEW, lensTransformer);
    this.renderContext.setGraphicsContext(lensGraphicsDecorator);
    vv.prependPreRenderPaintable(lensPaintable);
    vv.addPostRenderPaintable(lensControls);
    vv.setGraphMouse(lensGraphMouse);
    vv.setToolTipText(instructions);
    vv.setTransformSupport(new LensTransformSupport());
    vv.repaint();
  }

  public void deactivate() {
    vv.getRenderContext()
        .getMultiLayerTransformer()
        .setTransformer(Layer.VIEW, lensTransformer.getDelegate());
    vv.removePreRenderPaintable(lensPaintable);
    vv.removePostRenderPaintable(lensControls);
    this.renderContext.setGraphicsContext(savedGraphicsDecorator);
    vv.setRenderContext(renderContext);
    vv.setToolTipText(defaultToolTipText);
    vv.setGraphMouse(graphMouse);
    vv.setTransformSupport(new TransformSupport());
    vv.repaint();
  }
}
