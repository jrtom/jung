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

import edu.uci.ics.jung.visualization.VisualizationServer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.control.ModalGraphMouse
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Paint
import java.awt.geom.RectangularShape

/**
 * A class to make it easy to add an examining lens to a jung graph application. See
 * HyperbolicTransformerDemo, ViewLensSupport and LayoutLensSupport for examples of how to use it.
 *
 * @author Tom Nelson
 */
abstract class AbstractLensSupport<N : Any, E : Any>(
  protected val vv: VisualizationViewer<N, E>,
  protected val lensGraphMouse: ModalGraphMouse
) : LensSupport {

  protected var graphMouse: VisualizationViewer.GraphMouse = vv.getGraphMouse()!!
  protected lateinit var _lensTransformer: LensTransformer
  protected var _lensPaintable: LensPaintable? = null
  protected var _lensControls: LensControls? = null
  protected val defaultToolTipText: String? = vv.getToolTipText()

  override fun activate(state: Boolean) {
    if (state) {
      activate()
    } else {
      deactivate()
    }
  }

  override fun getLensTransformer(): LensTransformer = _lensTransformer

  /**
   * @return the hyperbolicGraphMouse.
   */
  override fun getGraphMouse(): ModalGraphMouse = lensGraphMouse

  /**
   * the background for the hyperbolic projection
   *
   * @author Tom Nelson
   */
  open class LensPaintable(lensTransformer: LensTransformer) : VisualizationServer.Paintable {
    val lensShape: RectangularShape = lensTransformer.lens.lensShape
    var paint: Paint = Color.decode("0xdddddd")

    override fun paint(g: Graphics) {
      val g2d = g as Graphics2D
      g2d.paint = paint
      g2d.fill(lensShape)
    }

    override fun useTransform(): Boolean = true
  }

  /**
   * the background for the hyperbolic projection
   *
   * @author Tom Nelson
   */
  open class LensControls(lensTransformer: LensTransformer) : VisualizationServer.Paintable {
    val lensShape: RectangularShape = lensTransformer.lens.lensShape
    var paint: Paint = Color.gray

    override fun paint(g: Graphics) {
      val g2d = g as Graphics2D
      g2d.paint = paint
      g2d.draw(lensShape)
      val centerX = Math.round(lensShape.centerX).toInt()
      val centerY = Math.round(lensShape.centerY).toInt()
      g.drawOval(centerX - 10, centerY - 10, 20, 20)
    }

    override fun useTransform(): Boolean = true
  }

  companion object {
    @JvmField
    val instructions: String =
      "<html><center>Mouse-Drag the Lens center to move it<p>" +
        "Mouse-Drag the Lens edge to resize it<p>" +
        "Ctrl+MouseWheel to change magnification</center></html>"
  }
}
