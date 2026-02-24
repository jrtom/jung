/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform

import com.google.common.base.Preconditions
import java.awt.Dimension
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.geom.RectangularShape
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * LensTransformer wraps a MutableAffineTransformer and modifies the transform and inverseTransform
 * methods so that they create a projection of the graph points within an elliptical lens.
 *
 * <p>LensTransformer uses an affine transform to cause translation, scaling, rotation, and shearing
 * while applying a possibly non-affine filter in its transform and inverseTransform methods.
 *
 * @author Tom Nelson
 */
open class Lens(d: Dimension) {

  /** the area affected by the transform */
  var lensShape: RectangularShape = Ellipse2D.Float()

  var magnification: Float = 0.7f
    set(value) {
      log.trace("setmagnification to {}", value)
      field = value
    }

  init {
    setSize(d)
  }

  /**
   * @param d the size used for the lens
   */
  fun setSize(d: Dimension) {
    Preconditions.checkNotNull(d)
    Preconditions.checkArgument(d.width > 0, "width must be > 0")
    Preconditions.checkArgument(d.height > 0, "height must be > 0")
    val width = d.width / 1.5f
    val height = d.height / 1.5f
    lensShape.setFrame(
      ((d.width - width) / 2).toDouble(),
      ((d.height - height) / 2).toDouble(),
      width.toDouble(),
      height.toDouble()
    )
  }

  fun getCenter(): Point2D =
    Point2D.Double(lensShape.centerX, lensShape.centerY)

  fun setCenter(viewCenter: Point2D) {
    val width = lensShape.width
    val height = lensShape.height
    lensShape.setFrame(
      viewCenter.x - width / 2,
      viewCenter.y - height / 2,
      width,
      height
    )
  }

  fun getRadius(): Double = lensShape.height / 2

  fun setRadius(viewRadius: Double) {
    val x = lensShape.centerX
    val y = lensShape.centerY
    val viewRatio = getRatio()
    lensShape.setFrame(
      x - viewRadius / viewRatio,
      y - viewRadius,
      2 * viewRadius / viewRatio,
      2 * viewRadius
    )
  }

  /**
   * @return the ratio between the lens height and lens width
   */
  fun getRatio(): Double = lensShape.height / lensShape.width

  fun getDistanceFromCenter(p: Point2D): Double {
    var dx = lensShape.centerX - p.x
    val dy = lensShape.centerY - p.y
    dx *= getRatio()
    return Math.sqrt(dx * dx + dy * dy)
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(Lens::class.java)
  }
}
