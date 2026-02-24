/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 21, 2005
 */

package edu.uci.ics.jung.visualization.transform

import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.LensTransformSupport
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse
import edu.uci.ics.jung.visualization.control.TransformSupport
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A class to make it easy to add an examining lens to a jung graph application. See
 * HyperbolicTransformerDemo for an example of how to use it.
 *
 * @author Tom Nelson
 */
open class LayoutLensSupport<N : Any, E : Any> : AbstractLensSupport<N, E>, LensSupport {

  protected var pickSupport: NetworkElementAccessor<N, E>

  constructor(vv: VisualizationViewer<N, E>) : this(
    vv,
    HyperbolicTransformer(
      Lens(vv.getModel().getLayoutSize()),
      vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
    ),
    ModalLensGraphMouse()
  )

  constructor(vv: VisualizationViewer<N, E>, lens: Lens) : this(
    vv,
    HyperbolicTransformer(
      lens,
      vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)
    ),
    ModalLensGraphMouse()
  )

  /**
   * Create an instance with the specified parameters.
   *
   * @param vv the visualization viewer used for rendering
   * @param lensTransformer the lens transformer to use
   * @param lensGraphMouse the lens input handler
   */
  constructor(
    vv: VisualizationViewer<N, E>,
    lensTransformer: LensTransformer,
    lensGraphMouse: ModalGraphMouse
  ) : super(vv, lensGraphMouse) {
    this._lensTransformer = lensTransformer
    this.pickSupport = vv.getPickSupport()
  }

  override fun activate() {
    if (_lensPaintable == null) {
      _lensPaintable = LensPaintable(_lensTransformer)
    }
    if (_lensControls == null) {
      _lensControls = LensControls(_lensTransformer)
    }
    vv.getRenderContext().getMultiLayerTransformer().setTransformer(Layer.LAYOUT, _lensTransformer)
    vv.prependPreRenderPaintable(_lensPaintable!!)
    vv.addPostRenderPaintable(_lensControls!!)
    vv.setGraphMouse(lensGraphMouse)
    vv.setToolTipText(instructions)
    vv.setTransformSupport(LensTransformSupport<N, E>())
    vv.repaint()
  }

  override fun deactivate() {
    vv.removePreRenderPaintable(_lensPaintable!!)
    vv.removePostRenderPaintable(_lensControls!!)
    vv.getRenderContext()
      .getMultiLayerTransformer()
      .setTransformer(Layer.LAYOUT, _lensTransformer.delegate)
    vv.setToolTipText(defaultToolTipText)
    vv.setGraphMouse(graphMouse)
    vv.setTransformSupport(TransformSupport<N, E>())
    vv.repaint()
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(LayoutLensSupport::class.java)
  }
}
