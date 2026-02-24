/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform.shape

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.RenderContext
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.LensTransformSupport
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.TransformSupport
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import edu.uci.ics.jung.visualization.renderers.Renderer
import edu.uci.ics.jung.visualization.transform.AbstractLensSupport
import edu.uci.ics.jung.visualization.transform.LensSupport
import edu.uci.ics.jung.visualization.transform.LensTransformer
import java.awt.Dimension

/**
 * Uses a LensTransformer to use in the view transform. This one will distort Node shapes.
 *
 * @author Tom Nelson
 */
open class ViewLensSupport<N : Any, E : Any>(
  vv: VisualizationViewer<N, E>,
  lensTransformer: LensTransformer,
  lensGraphMouse: ModalGraphMouse
) : AbstractLensSupport<N, E>(vv, lensGraphMouse), LensSupport {

  protected val renderContext: RenderContext<N, E> = vv.getRenderContext()
  protected val lensGraphicsDecorator: GraphicsDecorator
  protected val savedGraphicsDecorator: GraphicsDecorator
  protected val pickSupport: NetworkElementAccessor<N, E>
  protected val savedEdgeRenderer: Renderer.Edge<N, E>

  init {
    this.pickSupport = renderContext.getPickSupport()
    this.savedGraphicsDecorator = renderContext.getGraphicsContext()!!
    this._lensTransformer = lensTransformer
    val layoutModel = vv.getModel().getLayoutModel()
    val d = Dimension(layoutModel.width, layoutModel.height)
    lensTransformer.lens.setSize(d)

    this.lensGraphicsDecorator = TransformingFlatnessGraphics(lensTransformer)
    this.savedEdgeRenderer = vv.getRenderer().getEdgeRenderer()
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
    vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.VIEW, _lensTransformer)
    renderContext.setGraphicsContext(lensGraphicsDecorator)
    vv.prependPreRenderPaintable(_lensPaintable!!)
    vv.addPostRenderPaintable(_lensControls!!)
    vv.setGraphMouse(lensGraphMouse)
    vv.setToolTipText(instructions)
    vv.setTransformSupport(LensTransformSupport<N, E>())
    vv.repaint()
  }

  override fun deactivate() {
    vv.getRenderContext()
      .getMultiLayerTransformer()
      .setTransformer(Layer.VIEW, _lensTransformer.delegate)
    vv.removePreRenderPaintable(_lensPaintable!!)
    vv.removePostRenderPaintable(_lensControls!!)
    renderContext.setGraphicsContext(savedGraphicsDecorator)
    vv.setRenderContext(renderContext)
    vv.setToolTipText(defaultToolTipText)
    vv.setGraphMouse(graphMouse)
    vv.setTransformSupport(TransformSupport<N, E>())
    vv.repaint()
  }
}
