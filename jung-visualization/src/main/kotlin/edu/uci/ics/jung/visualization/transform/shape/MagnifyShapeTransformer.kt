/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform.shape

import edu.uci.ics.jung.layout.model.Point
import edu.uci.ics.jung.layout.model.PolarPoint
import edu.uci.ics.jung.visualization.transform.Lens
import edu.uci.ics.jung.visualization.transform.MagnifyTransformer
import edu.uci.ics.jung.visualization.transform.MutableTransformer
import java.awt.Dimension
import java.awt.Shape
import java.awt.geom.GeneralPath
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * MagnifyShapeTransformer extends MagnifyTransformer and adds implementations for methods in
 * ShapeTransformer. It modifies the shapes (Node, Edge, and Arrowheads) so that they are enlarged
 * by the magnify transformation.
 *
 * @author Tom Nelson
 */
open class MagnifyShapeTransformer : MagnifyTransformer, ShapeFlatnessTransformer {

  /**
   * @param d the size used for the lens
   */
  constructor(d: Dimension) : super(d)

  /**
   * @param d the size used for the lens
   * @param delegate the transformer to use
   */
  constructor(d: Dimension, delegate: MutableTransformer) : super(d, delegate)

  constructor(lens: Lens, delegate: MutableTransformer) : super(lens, delegate)

  /**
   * Transform the supplied shape with the overridden transform method so that the shape is
   * distorted by the magnify transform.
   *
   * @param shape a shape to transform
   * @return a GeneralPath for the transformed shape
   */
  override fun transform(shape: Shape): Shape = transform(shape, 0f)

  override fun transform(shape: Shape, flatness: Float): Shape {
    if (log.isTraceEnabled) {
      log.trace("transform {}", shape)
    }
    val newPath = GeneralPath()
    val coords = FloatArray(6)
    val iterator = if (flatness == 0f) {
      shape.getPathIterator(null)
    } else {
      shape.getPathIterator(null, flatness.toDouble())
    }
    while (!iterator.isDone) {
      val type = iterator.currentSegment(coords)
      when (type) {
        PathIterator.SEG_MOVETO -> {
          val p = _transform(Point2D.Float(coords[0], coords[1]))!!
          newPath.moveTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_LINETO -> {
          val p = _transform(Point2D.Float(coords[0], coords[1]))!!
          newPath.lineTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_QUADTO -> {
          val p = _transform(Point2D.Float(coords[0], coords[1]))!!
          val q = _transform(Point2D.Float(coords[2], coords[3]))!!
          newPath.quadTo(p.x.toFloat(), p.y.toFloat(), q.x.toFloat(), q.y.toFloat())
        }
        PathIterator.SEG_CUBICTO -> {
          val p = _transform(Point2D.Float(coords[0], coords[1]))!!
          val q = _transform(Point2D.Float(coords[2], coords[3]))!!
          val r = _transform(Point2D.Float(coords[4], coords[5]))!!
          newPath.curveTo(
            p.x.toFloat(), p.y.toFloat(),
            q.x.toFloat(), q.y.toFloat(),
            r.x.toFloat(), r.y.toFloat()
          )
        }
        PathIterator.SEG_CLOSE -> newPath.closePath()
      }
      iterator.next()
    }
    return newPath
  }

  override fun inverseTransform(shape: Shape): Shape {
    if (log.isTraceEnabled) {
      log.trace("inverseTransform {}", shape)
    }
    val newPath = GeneralPath()
    val coords = FloatArray(6)
    val iterator = shape.getPathIterator(null)
    while (!iterator.isDone) {
      val type = iterator.currentSegment(coords)
      when (type) {
        PathIterator.SEG_MOVETO -> {
          val p = _inverseTransform(Point2D.Float(coords[0], coords[1]))
          newPath.moveTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_LINETO -> {
          val p = _inverseTransform(Point2D.Float(coords[0], coords[1]))
          newPath.lineTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_QUADTO -> {
          val p = _inverseTransform(Point2D.Float(coords[0], coords[1]))
          val q = _inverseTransform(Point2D.Float(coords[2], coords[3]))
          newPath.quadTo(p.x.toFloat(), p.y.toFloat(), q.x.toFloat(), q.y.toFloat())
        }
        PathIterator.SEG_CUBICTO -> {
          val p = _inverseTransform(Point2D.Float(coords[0], coords[1]))
          val q = _inverseTransform(Point2D.Float(coords[2], coords[3]))
          val r = _inverseTransform(Point2D.Float(coords[4], coords[5]))
          newPath.curveTo(
            p.x.toFloat(), p.y.toFloat(),
            q.x.toFloat(), q.y.toFloat(),
            r.x.toFloat(), r.y.toFloat()
          )
        }
        PathIterator.SEG_CLOSE -> newPath.closePath()
      }
      iterator.next()
    }
    return newPath
  }

  private fun _transform(graphPoint: Point2D?): Point2D? {
    if (graphPoint == null) {
      return null
    }
    val viewCenter = lens.getCenter()
    val viewRadius = lens.getRadius()
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
  private fun _inverseTransform(viewPoint: Point2D): Point2D {
    val invViewPoint = delegate.inverseTransform(viewPoint)
    val viewCenter = lens.getCenter()
    val viewRadius = lens.getRadius()
    val ratio = lens.getRatio()
    var dx = invViewPoint.x - viewCenter.x
    val dy = invViewPoint.y - viewCenter.y
    // factor out ellipse
    dx *= ratio

    val pointFromCenter = Point.of(dx, dy)

    var polar = PolarPoint.cartesianToPolar(pointFromCenter)

    var radius = polar.radius
    if (radius > viewRadius) {
      return invViewPoint
    }

    val mag = lens.magnification.toDouble()
    radius /= mag
    polar = polar.newRadius(radius)
    var projectedPoint = PolarPoint.polarToCartesian(polar)
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y)
    return Point2D.Double(
      projectedPoint.x + viewCenter.x,
      projectedPoint.y + viewCenter.y
    )
  }

  /**
   * Magnify the shape, without considering the Lens.
   *
   * @param shape the shape to magnify
   * @return the transformed shape
   */
  fun magnify(shape: Shape): Shape = magnify(shape, 0f)

  fun magnify(shape: Shape, flatness: Float): Shape {
    val newPath = GeneralPath()
    val coords = FloatArray(6)
    val iterator = if (flatness == 0f) {
      shape.getPathIterator(null)
    } else {
      shape.getPathIterator(null, flatness.toDouble())
    }
    while (!iterator.isDone) {
      val type = iterator.currentSegment(coords)
      when (type) {
        PathIterator.SEG_MOVETO -> {
          val p = magnify(Point2D.Float(coords[0], coords[1]))!!
          newPath.moveTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_LINETO -> {
          val p = magnify(Point2D.Float(coords[0], coords[1]))!!
          newPath.lineTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_QUADTO -> {
          val p = magnify(Point2D.Float(coords[0], coords[1]))!!
          val q = magnify(Point2D.Float(coords[2], coords[3]))!!
          newPath.quadTo(p.x.toFloat(), p.y.toFloat(), q.x.toFloat(), q.y.toFloat())
        }
        PathIterator.SEG_CUBICTO -> {
          val p = magnify(Point2D.Float(coords[0], coords[1]))!!
          val q = magnify(Point2D.Float(coords[2], coords[3]))!!
          val r = magnify(Point2D.Float(coords[4], coords[5]))!!
          newPath.curveTo(
            p.x.toFloat(), p.y.toFloat(),
            q.x.toFloat(), q.y.toFloat(),
            r.x.toFloat(), r.y.toFloat()
          )
        }
        PathIterator.SEG_CLOSE -> newPath.closePath()
      }
      iterator.next()
    }
    return newPath
  }

  companion object {
    private val log: Logger = LoggerFactory.getLogger(MagnifyShapeTransformer::class.java)
  }
}
