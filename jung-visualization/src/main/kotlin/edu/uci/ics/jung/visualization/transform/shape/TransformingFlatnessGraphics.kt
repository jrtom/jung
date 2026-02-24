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

import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer
import java.awt.Graphics2D
import java.awt.Shape

/**
 * subclassed to pass certain operations thru the Function before the base class method is applied
 * This is useful when you want to apply non-affine transformations to the Graphics2D used to draw
 * elements of the graph.
 *
 * @author Tom Nelson
 */
open class TransformingFlatnessGraphics(
  _transformer: BidirectionalTransformer,
  delegate: Graphics2D? = null
) : TransformingGraphics(_transformer, delegate) {

  @Suppress("unused")
  private var flatness: Float = 0f

  override fun draw(s: Shape, flatness: Float) {
    val shape = if (_transformer is ShapeFlatnessTransformer) {
      (_transformer as ShapeFlatnessTransformer).transform(s, flatness)
    } else {
      (_transformer as ShapeTransformer).transform(s)
    }
    _delegate!!.draw(shape)
  }

  override fun fill(s: Shape, flatness: Float) {
    val shape = if (_transformer is HyperbolicTransformer) {
      (_transformer as HyperbolicShapeTransformer).transform(s, flatness)
    } else {
      (_transformer as ShapeTransformer).transform(s)
    }
    _delegate!!.fill(shape)
  }
}
