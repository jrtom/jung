/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 21, 2005
 */

package edu.uci.ics.jung.visualization.transform.shape

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.LensTransformSupport
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.TransformSupport
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.renderers.BasicRenderer
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.transform.AbstractLensSupport
import edu.uci.ics.jung.visualization.transform.LensTransformer

/**
 * Changes various visualization settings to activate or deactivate an examining lens for a jung
 * graph application.
 *
 * @author Tom Nelson
 */
open class MagnifyImageLensSupport<N : Any, E : Any>(
  vv: VisualizationViewer<N, E>,
  lensTransformer: LensTransformer,
  lensGraphMouse: ModalGraphMouse
) : AbstractLensSupport<N, E>(vv, lensGraphMouse) {

  protected val renderContext: RenderContext<N, E> = vv.getRenderContext()
  protected val lensGraphicsDecorator: GraphicsDecorator
  protected val savedGraphicsDecorator: GraphicsDecorator
  protected val renderer: Renderer<N, E>
  protected val transformingRenderer: Renderer<N, E>
  protected val pickSupport: NetworkElementAccessor<N, E>

  init {
    this.pickSupport = renderContext.getPickSupport()
    this.renderer = vv.getRenderer()
    this.transformingRenderer = BasicRenderer<N, E>()
    this.savedGraphicsDecorator = renderContext.getGraphicsContext()!!
    this._lensTransformer = lensTransformer

    var d = vv.getSize()
    if (d.width == 0 || d.height == 0) {
      d = vv.getPreferredSize()
    }
    this.lensGraphicsDecorator = MagnifyIconGraphics(lensTransformer)
  }

  override fun activate() {
    _lensTransformer.delegate =
      vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
    if (_lensPaintable == null) {
      _lensPaintable = LensPaintable(_lensTransformer)
    }
    if (_lensControls == null) {
      _lensControls = LensControls(_lensTransformer)
    }
    _lensTransformer.delegate =
      vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)
    vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.VIEW, _lensTransformer)
    renderContext.setGraphicsContext(lensGraphicsDecorator)
    vv.addPreRenderPaintable(_lensPaintable!!)
    vv.addPostRenderPaintable(_lensControls!!)
    vv.setGraphMouse(lensGraphMouse)
    vv.setToolTipText(instructions)
    vv.setTransformSupport(LensTransformSupport<N, E>())
    vv.repaint()
  }

  override fun deactivate() {
    renderContext.setPickSupport(pickSupport)
    vv.getRenderContext()
      .getMultiLayerTransformer()
      .setTransformer(Layer.VIEW, _lensTransformer.delegate)
    vv.removePreRenderPaintable(_lensPaintable!!)
    vv.removePostRenderPaintable(_lensControls!!)
    renderContext.setGraphicsContext(savedGraphicsDecorator)
    vv.setToolTipText(defaultToolTipText)
    vv.setGraphMouse(graphMouse)
    vv.setTransformSupport(TransformSupport<N, E>())
    vv.repaint()
  }

  companion object {
    val instructions: String =
      "<html><center>Mouse-Drag the Lens center to move it<p>" +
        "Mouse-Drag the Lens edge to resize it<p>" +
        "Ctrl+MouseWheel to change magnification</center></html>"
  }
}
