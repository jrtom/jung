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
import edu.uci.ics.jung.visualization.util.ChangeEventSupport
import edu.uci.ics.jung.visualization.util.DefaultChangeEventSupport
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import javax.swing.event.ChangeListener

/**
 * Provides methods to mutate the AffineTransform used by AffineTransformer base class to map points
 * from one coordinate system to another.
 *
 * @author Tom Nelson
 */
open class MutableAffineTransformer : AffineTransformer, MutableTransformer, ShapeTransformer, ChangeEventSupport {

  protected var changeSupport: ChangeEventSupport = DefaultChangeEventSupport(this)

  /** create an instance that does not transform points */
  constructor() : super()

  /**
   * Create an instance with the supplied transform
   *
   * @param transform the transform to use
   */
  constructor(transform: AffineTransform) : super(transform)

  override fun toString(): String = "MutableAffineTransformer using $_transform"

  /**
   * setter for the scale fires a PropertyChangeEvent with the AffineTransforms representing the
   * previous and new values for scale and offset
   *
   * @param scalex the amount to scale in the x direction
   * @param scaley the amount to scale in the y direction
   * @param from the point to transform
   */
  override fun scale(scalex: Double, scaley: Double, from: Point2D) {
    val xf = AffineTransform.getTranslateInstance(from.x, from.y)
    xf.scale(scalex, scaley)
    xf.translate(-from.x, -from.y)
    _inverse = null
    _transform.preConcatenate(xf)
    fireStateChanged()
  }

  /**
   * setter for the scale fires a PropertyChangeEvent with the AffineTransforms representing the
   * previous and new values for scale and offset
   *
   * @param scalex the amount to scale in the x direction
   * @param scaley the amount to scale in the y direction
   * @param from the point to transform
   */
  override fun setScale(scalex: Double, scaley: Double, from: Point2D) {
    _transform.setToIdentity()
    scale(scalex, scaley, from)
  }

  /**
   * shears the transform by passed parameters
   *
   * @param shx x value to shear
   * @param shy y value to shear
   * @param from the point to transform
   */
  override fun shear(shx: Double, shy: Double, from: Point2D) {
    _inverse = null
    val at = AffineTransform.getTranslateInstance(from.x, from.y)
    at.shear(shx, shy)
    at.translate(-from.x, -from.y)
    _transform.preConcatenate(at)
    fireStateChanged()
  }

  /**
   * Replace the Transform's translate x and y values with the passed values, leaving the scale
   * values unchanged.
   *
   * @param tx the x value of the translation
   * @param ty the y value of the translation
   */
  override fun setTranslate(tx: Double, ty: Double) {
    val scalex = _transform.scaleX.toFloat()
    val scaley = _transform.scaleY.toFloat()
    val shearx = _transform.shearX.toFloat()
    val sheary = _transform.shearY.toFloat()
    _inverse = null
    _transform.setTransform(scalex.toDouble(), sheary.toDouble(), shearx.toDouble(), scaley.toDouble(), tx, ty)
    fireStateChanged()
  }

  /**
   * Apply the passed values to the current Transform
   *
   * @param offsetx the x-value
   * @param offsety the y-value
   */
  override fun translate(offsetx: Double, offsety: Double) {
    _inverse = null
    _transform.translate(offsetx, offsety)
    fireStateChanged()
  }

  /**
   * preconcatenates the rotation at the supplied point with the current transform
   *
   * @param theta the angle by which to rotate the point
   * @param from the point to transform
   */
  override fun rotate(theta: Double, from: Point2D) {
    val rotate = AffineTransform.getRotateInstance(theta, from.x, from.y)
    _inverse = null
    _transform.preConcatenate(rotate)
    fireStateChanged()
  }

  /**
   * rotates the current transform at the supplied points
   *
   * @param radians angle by which to rotate the supplied coordinates
   * @param x the x coordinate of the point to transform
   * @param y the y coordinate of the point to transform
   */
  override fun rotate(radians: Double, x: Double, y: Double) {
    _inverse = null
    _transform.rotate(radians, x, y)
    fireStateChanged()
  }

  override fun concatenate(xform: AffineTransform) {
    _inverse = null
    _transform.concatenate(xform)
    fireStateChanged()
  }

  override fun preConcatenate(xform: AffineTransform) {
    _inverse = null
    _transform.preConcatenate(xform)
    fireStateChanged()
  }

  /**
   * Adds a `ChangeListener`.
   *
   * @param l the listener to be added
   */
  override fun addChangeListener(l: ChangeListener) {
    changeSupport.addChangeListener(l)
  }

  /**
   * Removes a ChangeListener.
   *
   * @param l the listener to be removed
   */
  override fun removeChangeListener(l: ChangeListener) {
    changeSupport.removeChangeListener(l)
  }

  /**
   * Returns an array of all the `ChangeListener`s added with addChangeListener().
   *
   * @return all of the `ChangeListener`s added or an empty array if no listeners have
   *     been added
   */
  override fun getChangeListeners(): Array<ChangeListener> =
    changeSupport.getChangeListeners()

  /**
   * Notifies all listeners that have registered interest for notification on this event type. The
   * event instance is lazily created.
   */
  override fun fireStateChanged() {
    changeSupport.fireStateChanged()
  }

  override fun setToIdentity() {
    _inverse = null
    _transform.setToIdentity()
    fireStateChanged()
  }
}
