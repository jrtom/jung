/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform

import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.model.PolarPoint
import java.awt.Dimension
import java.awt.geom.Point2D
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * MagnifyTransformer wraps a MutableAffineTransformer and modifies the transform and
 * inverseTransform methods so that they create an enlarging projection of the graph points.
 *
 * <p>MagnifyTransformer uses an affine transform to cause translation, scaling, rotation, and
 * shearing while applying a separate magnification filter in its transform and inverseTransform
 * methods.
 *
 * @author Tom Nelson
 */
open class MagnifyTransformer : LensTransformer, MutableTransformer {

  /**
   * Create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component.
   *
   * @param d the size used for the lens
   */
  constructor(d: Dimension) : this(d, MutableAffineTransformer())

  constructor(lens: Lens) : this(lens, MutableAffineTransformer())

  /**
   * Create an instance with a possibly shared transform.
   *
   * @param d the size used for the lens
   * @param delegate the transformer to use
   */
  constructor(d: Dimension, delegate: MutableTransformer) : super(d, delegate)

  constructor(lens: Lens, delegate: MutableTransformer) : super(lens, delegate)

  /** override base class transform to project the fisheye effect */
  override fun transform(graphPoint: Point2D): Point2D? {
    if (graphPoint == null) {
      return null
    }
    val viewCenter = lens.getCenter()
    val viewRadius = lens.getRadius()
    val ratio = lens.getRatio()
    // transform the point from the graph to the view
    val viewPoint = delegate.transform(graphPoint)!!
    // calculate point from center
    var dx = viewPoint.x - viewCenter.x
    val dy = viewPoint.y - viewCenter.y
    // factor out ellipse
    dx *= ratio
    val pointFromCenter = Point.of(dx, dy)

    val polar = PolarPoint.cartesianToPolar(pointFromCenter)
    val theta = polar.theta
    var radius = polar.radius
    if (radius > viewRadius) {
      return viewPoint
    }

    val mag = lens.magnification.toDouble()
    radius *= mag

    radius = Math.min(radius, viewRadius)
    var projectedPoint = PolarPoint.polarToCartesian(theta, radius)
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y)
    return Point2D.Double(
      projectedPoint.x + viewCenter.x,
      projectedPoint.y + viewCenter.y
    )
  }

  /** override base class to un-project the fisheye effect */
  override fun inverseTransform(viewPoint: Point2D): Point2D {
    val viewCenter = lens.getCenter()
    val viewRadius = lens.getRadius()
    val ratio = lens.getRatio()
    var dx = viewPoint.x - viewCenter.x
    val dy = viewPoint.y - viewCenter.y
    // factor out ellipse
    dx *= ratio

    val pointFromCenter = Point.of(dx, dy)

    var polar = PolarPoint.cartesianToPolar(pointFromCenter)

    var radius = polar.radius
    if (radius > viewRadius) {
      return delegate.inverseTransform(viewPoint)
    }

    val mag = lens.magnification.toDouble()
    radius /= mag
    polar = polar.newRadius(radius)
    var projectedPoint = PolarPoint.polarToCartesian(polar)
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y)
    val translatedBack = Point2D.Double(
      projectedPoint.x + viewCenter.x,
      projectedPoint.y + viewCenter.y
    )
    return delegate.inverseTransform(translatedBack)
  }

  /**
   * Magnifies the point, without considering the Lens.
   *
   * @param graphPoint the point to transform via magnification
   * @return the transformed point
   */
  fun magnify(graphPoint: Point2D?): Point2D? {
    if (graphPoint == null) {
      return null
    }
    val viewCenter = lens.getCenter()
    val ratio = lens.getRatio()
    // transform the point from the graph to the view
    val viewPoint = graphPoint
    // calculate point from center
    var dx = viewPoint.x - viewCenter.x
    val dy = viewPoint.y - viewCenter.y
    // factor out ellipse
    dx *= ratio
    val pointFromCenter = Point.of(dx, dy)

    val polar = PolarPoint.cartesianToPolar(pointFromCenter)
    val theta = polar.theta
    var radius = polar.radius

    val mag = lens.magnification.toDouble()
    radius *= mag

    var projectedPoint = PolarPoint.polarToCartesian(theta, radius)
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y)
    return Point2D.Double(
      projectedPoint.x + viewCenter.x,
      projectedPoint.y + viewCenter.y
    )
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(MagnifyTransformer::class.java)
  }
}
