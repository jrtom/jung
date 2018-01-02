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

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.LensTransformSupport;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.TransformSupport;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.renderers.BasicRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.transform.AbstractLensSupport;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import java.awt.*;

/**
 * Changes various visualization settings to activate or deactivate an examining lens for a jung
 * graph application.
 *
 * @author Tom Nelson
 */
public class MagnifyImageLensSupport<N, E> extends AbstractLensSupport<N, E> {

  protected RenderContext<N, E> renderContext;
  protected GraphicsDecorator lensGraphicsDecorator;
  protected GraphicsDecorator savedGraphicsDecorator;
  protected Renderer<N, E> renderer;
  protected Renderer<N, E> transformingRenderer;
  protected NetworkElementAccessor<N, E> pickSupport;

  static final String instructions =
      "<html><center>Mouse-Drag the Lens center to move it<p>"
          + "Mouse-Drag the Lens edge to resize it<p>"
          + "Ctrl+MouseWheel to change magnification</center></html>";

  public MagnifyImageLensSupport(
      VisualizationViewer<N, E> vv,
      LensTransformer lensTransformer,
      ModalGraphMouse lensGraphMouse) {
    super(vv, lensGraphMouse);
    this.renderContext = vv.getRenderContext();
    this.pickSupport = renderContext.getPickSupport();
    this.renderer = vv.getRenderer();
    this.transformingRenderer = new BasicRenderer<>();
    this.savedGraphicsDecorator = renderContext.getGraphicsContext();
    this.lensTransformer = lensTransformer;

    Dimension d = vv.getSize();
    if (d.width == 0 || d.height == 0) {
      d = vv.getPreferredSize();
    }
    this.lensGraphicsDecorator = new MagnifyIconGraphics(lensTransformer);
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
    lensTransformer.setDelegate(
        vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW));
    vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.VIEW, lensTransformer);
    this.renderContext.setGraphicsContext(lensGraphicsDecorator);
    vv.addPreRenderPaintable(lensPaintable);
    vv.addPostRenderPaintable(lensControls);
    vv.setGraphMouse(lensGraphMouse);
    vv.setToolTipText(instructions);
    vv.setTransformSupport(new LensTransformSupport<>());
    vv.repaint();
  }

  public void deactivate() {
    renderContext.setPickSupport(pickSupport);
    vv.getRenderContext()
        .getMultiLayerTransformer()
        .setTransformer(Layer.VIEW, lensTransformer.getDelegate());
    vv.removePreRenderPaintable(lensPaintable);
    vv.removePostRenderPaintable(lensControls);
    this.renderContext.setGraphicsContext(savedGraphicsDecorator);
    vv.setToolTipText(defaultToolTipText);
    vv.setGraphMouse(graphMouse);
    vv.setTransformSupport(new TransformSupport<>());
    vv.repaint();
  }
}
