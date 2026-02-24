/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 11, 2005
 */

package edu.uci.ics.jung.visualization.transform.shape

import java.awt.Component
import java.awt.Graphics2D
import java.awt.Shape
import javax.swing.CellRendererPane
import javax.swing.Icon

/**
 * an extension of Graphics2DWrapper that adds enhanced methods for drawing icons and components
 *
 * @see TransformingGraphics as an example subclass
 * @author Tom Nelson
 */
open class GraphicsDecorator(
  _delegate: Graphics2D? = null
) : Graphics2DWrapper(_delegate) {

  open fun draw(icon: Icon, c: Component, clip: Shape, x: Int, y: Int) {
    val w = icon.iconWidth
    val h = icon.iconHeight
    icon.paintIcon(c, _delegate, x - w / 2, y - h / 2)
  }

  open fun draw(
    c: Component,
    rendererPane: CellRendererPane,
    x: Int,
    y: Int,
    w: Int,
    h: Int,
    shouldValidate: Boolean
  ) {
    rendererPane.paintComponent(_delegate, c, c.parent, x, y, w, h, shouldValidate)
  }
}
