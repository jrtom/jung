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
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * HyperbolicTransformer wraps a MutableAffineTransformer and modifies the transform and
 * inverseTransform methods so that they create a fisheye projection of the graph points, with
 * points near the center spread out and points near the edges collapsed onto the circumference of
 * an ellipse.
 *
 * <p>HyperbolicTransformer is not an affine transform, but it uses an affine transform to cause
 * translation, scaling, rotation, and shearing while applying a non-affine hyperbolic filter in its
 * transform and inverseTransform methods.
 *
 * @author Tom Nelson
 */
open class HyperbolicTransformer : LensTransformer, MutableTransformer {

  /**
   * Create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component.
   *
   * @param d the size used for the lens
   */
  constructor(d: Dimension) : this(d, MutableAffineTransformer())

  /**
   * create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component
   *
   * @param d the size used for the lens
   */
  constructor(d: Dimension, delegate: MutableTransformer) : super(d, delegate)

  /**
   * Create an instance with a possibly shared transform.
   *
   * @param lens a lens created elsewhere, but on the same component
   */
  constructor(lens: Lens, delegate: MutableTransformer) : super(lens, delegate)

  /** override base class transform to project the fisheye effect */
  override fun transform(graphPoint: Point2D): Point2D? {
    if (graphPoint == null) {
      return null
    }
    val lensEllipse = lens.lensShape as Ellipse2D
    if (lensEllipse.contains(graphPoint)) {
      log.trace("lens {} contains graphPoint{}", lensEllipse, graphPoint)
    } else {
      log.trace("lens {} does not contain graphPoint {}", lensEllipse, graphPoint)
    }
    val viewCenter = lens.getCenter()
    val viewRadius = lens.getRadius()
    val ratio = lens.getRatio()
    // transform the point from the graph to the view
    val viewPoint = delegate.transform(graphPoint)!!
    if (lensEllipse.contains(viewPoint)) {
      log.trace("lens {} contains viewPoint {}", lensEllipse, viewPoint)
    } else {
      log.trace("lens {} does not contain viewPoint {}", lensEllipse, viewPoint)
    }

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
      log.trace("outside point radius {} > viewRadius {}", radius, viewRadius)
      return viewPoint
    } else {
      log.trace("inside point radius {} >= viewRadius {}", radius, viewRadius)
    }

    val mag = Math.tan(Math.PI / 2 * lens.magnification)
    radius *= mag

    radius = Math.min(radius, viewRadius)
    radius /= viewRadius
    radius *= Math.PI / 2
    radius = Math.abs(Math.atan(radius))
    radius *= viewRadius
    var projectedPoint = PolarPoint.polarToCartesian(theta, radius)
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y)
    return Point2D.Double(
      projectedPoint.x + viewCenter.x,
      projectedPoint.y + viewCenter.y
    )
  }

  /** override base class to un-project the fisheye effect */
  override fun inverseTransform(viewPoint: Point2D): Point2D {
    val lensEllipse = lens.lensShape as Ellipse2D
    if (lensEllipse.contains(viewPoint)) {
      log.trace("lens {} contains viewPoint{}", lensEllipse, viewPoint)
    } else {
      log.trace("lens {} does not contain viewPoint {}", lensEllipse, viewPoint)
    }

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
      log.trace("outside point radius {} > viewRadius {}", radius, viewRadius)
    } else {
      log.trace("inside point radius {} <= viewRadius {}", radius, viewRadius)
    }

    if (radius > viewRadius) {
      return delegate.inverseTransform(viewPoint)
    }

    radius /= viewRadius
    radius = Math.abs(Math.tan(radius))
    radius /= Math.PI / 2
    radius *= viewRadius
    val mag = Math.tan(Math.PI / 2 * lens.magnification)
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

  companion object {
    private val log: Logger = LoggerFactory.getLogger(HyperbolicTransformer::class.java)
  }
}
