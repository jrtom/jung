/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Apr 16, 2005
 */

package edu.uci.ics.jung.visualization.transform

import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.GeneralPath
import java.awt.geom.NoninvertibleTransformException
import java.awt.geom.PathIterator
import java.awt.geom.Point2D

/**
 * Provides methods to map points from one coordinate system to another, by delegating to a wrapped
 * AffineTransform (uniform) and its inverse.
 *
 * @author Tom Nelson
 */
open class AffineTransformer : BidirectionalTransformer, ShapeTransformer {

  /** cached inverse, cleared when transform changes */
  protected var _inverse: AffineTransform? = null

  /** The AffineTransform to use; initialized to identity. */
  protected open var _transform: AffineTransform = AffineTransform()

  open fun getTransform(): AffineTransform = _transform
  open fun setTransform(value: AffineTransform) { _transform = value }

  /** Create an instance that does not transform points. */
  constructor()

  /**
   * Create an instance with the supplied transform.
   *
   * @param transform the transform to use
   */
  constructor(transform: AffineTransform?) {
    if (transform != null) {
      this._transform = transform
    }
  }

  /**
   * applies the inverse transform to the supplied point
   *
   * @param p the point to transform
   * @return the transformed point
   */
  override fun inverseTransform(p: Point2D): Point2D =
    getInverse().transform(p, null)

  override fun inverseTransform(x: Double, y: Double): Point2D =
    inverseTransform(Point2D.Double(x, y))

  fun getInverse(): AffineTransform {
    if (_inverse == null) {
      try {
        _inverse = _transform.createInverse()
      } catch (e: NoninvertibleTransformException) {
        e.printStackTrace()
      }
    }
    return _inverse!!
  }

  /** @return the transform's x scale value */
  fun getScaleX(): Double = _transform.scaleX

  /** @return the transform's y scale value */
  fun getScaleY(): Double = _transform.scaleY

  /** @return the transform's overall scale magnitude */
  fun getScale(): Double = Math.sqrt(_transform.determinant)

  /** @return the transform's x shear value */
  fun getShearX(): Double = _transform.shearX

  /** @return the transform's y shear value */
  fun getShearY(): Double = _transform.shearY

  /** @return the transform's x translate value */
  fun getTranslateX(): Double = _transform.translateX

  /** @return the transform's y translate value */
  fun getTranslateY(): Double = _transform.translateY

  /**
   * Applies the transform to the supplied point.
   *
   * @param p the point to be transformed
   * @return the transformed point
   */
  override fun transform(p: Point2D): Point2D? {
    return _transform.transform(p, null)
  }

  override fun transform(x: Double, y: Double): Point2D? =
    transform(Point2D.Double(x, y))

  /**
   * Transform the supplied shape from graph (layout) to screen (view) coordinates.
   *
   * @return the GeneralPath of the transformed shape
   */
  override fun transform(shape: Shape): Shape {
    val newPath = GeneralPath()
    val coords = FloatArray(6)
    val iterator = shape.getPathIterator(null)
    while (!iterator.isDone) {
      val type = iterator.currentSegment(coords)
      when (type) {
        PathIterator.SEG_MOVETO -> {
          val p = transform(coords[0].toDouble(), coords[1].toDouble())!!
          newPath.moveTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_LINETO -> {
          val p = transform(coords[0].toDouble(), coords[1].toDouble())!!
          newPath.lineTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_QUADTO -> {
          val p = transform(coords[0].toDouble(), coords[1].toDouble())!!
          val q = transform(coords[2].toDouble(), coords[3].toDouble())!!
          newPath.quadTo(p.x.toFloat(), p.y.toFloat(), q.x.toFloat(), q.y.toFloat())
        }
        PathIterator.SEG_CUBICTO -> {
          val p = transform(coords[0].toDouble(), coords[1].toDouble())!!
          val q = transform(coords[2].toDouble(), coords[3].toDouble())!!
          val r = transform(coords[4].toDouble(), coords[5].toDouble())!!
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

  /**
   * Transform the supplied shape from screen (view) to graph (layout) coordinates.
   *
   * @return the GeneralPath of the transformed shape
   */
  override fun inverseTransform(shape: Shape): Shape {
    val newPath = GeneralPath()
    val coords = FloatArray(6)
    val iterator = shape.getPathIterator(null)
    while (!iterator.isDone) {
      val type = iterator.currentSegment(coords)
      when (type) {
        PathIterator.SEG_MOVETO -> {
          val p = inverseTransform(coords[0].toDouble(), coords[1].toDouble())
          newPath.moveTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_LINETO -> {
          val p = inverseTransform(coords[0].toDouble(), coords[1].toDouble())
          newPath.lineTo(p.x.toFloat(), p.y.toFloat())
        }
        PathIterator.SEG_QUADTO -> {
          val p = inverseTransform(coords[0].toDouble(), coords[1].toDouble())
          val q = inverseTransform(coords[2].toDouble(), coords[3].toDouble())
          newPath.quadTo(p.x.toFloat(), p.y.toFloat(), q.x.toFloat(), q.y.toFloat())
        }
        PathIterator.SEG_CUBICTO -> {
          val p = inverseTransform(coords[0].toDouble(), coords[1].toDouble())
          val q = inverseTransform(coords[2].toDouble(), coords[3].toDouble())
          val r = inverseTransform(coords[4].toDouble(), coords[5].toDouble())
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

  fun getRotation(): Double {
    val unitVector = doubleArrayOf(0.0, 0.0, 1.0, 0.0)
    val result = DoubleArray(4)

    _transform.transform(unitVector, 0, result, 0, 2)

    val dy = Math.abs(result[3] - result[1])
    val length = Point2D.distance(result[0], result[1], result[2], result[3])
    var rotation = Math.asin(dy / length)

    if (result[3] - result[1] > 0) {
      if (result[2] - result[0] < 0) {
        rotation = Math.PI - rotation
      }
    } else {
      if (result[2] - result[0] > 0) {
        rotation = 2 * Math.PI - rotation
      } else {
        rotation += Math.PI
      }
    }

    return rotation
  }

  override fun toString(): String = "Transformer using $_transform"
}
