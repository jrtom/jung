/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform

import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import javax.swing.event.ChangeListener

/**
 * a complete decorator that wraps a MutableTransformer. Subclasses use this to allow them to only
 * declare methods they need to change.
 *
 * @author Tom Nelson
 */
abstract class MutableTransformerDecorator(
  delegate: MutableTransformer?
) : MutableTransformer {

  open var delegate: MutableTransformer = delegate ?: MutableAffineTransformer()

  override fun addChangeListener(l: ChangeListener) {
    delegate.addChangeListener(l)
  }

  override fun concatenate(transform: AffineTransform) {
    delegate.concatenate(transform)
  }

  override fun fireStateChanged() {
    delegate.fireStateChanged()
  }

  override fun getChangeListeners(): Array<ChangeListener> =
    delegate.getChangeListeners()

  override fun getScale(): Double = delegate.getScale()

  override fun getScaleX(): Double = delegate.getScaleX()

  override fun getScaleY(): Double = delegate.getScaleY()

  override fun getShearX(): Double = delegate.getShearX()

  override fun getShearY(): Double = delegate.getShearY()

  override fun getTransform(): AffineTransform = delegate.getTransform()

  override fun getTranslateX(): Double = delegate.getTranslateX()

  override fun getTranslateY(): Double = delegate.getTranslateY()

  override fun inverseTransform(p: Point2D): Point2D = delegate.inverseTransform(p)

  override fun inverseTransform(shape: Shape): Shape = delegate.inverseTransform(shape)

  override fun preConcatenate(transform: AffineTransform) {
    delegate.preConcatenate(transform)
  }

  override fun removeChangeListener(l: ChangeListener) {
    delegate.removeChangeListener(l)
  }

  override fun rotate(radians: Double, point: Point2D) {
    delegate.rotate(radians, point)
  }

  override fun scale(sx: Double, sy: Double, point: Point2D) {
    delegate.scale(sx, sy, point)
  }

  override fun setScale(sx: Double, sy: Double, point: Point2D) {
    delegate.setScale(sx, sy, point)
  }

  override fun setToIdentity() {
    delegate.setToIdentity()
  }

  override fun setTranslate(dx: Double, dy: Double) {
    delegate.setTranslate(dx, dy)
  }

  override fun shear(shx: Double, shy: Double, from: Point2D) {
    delegate.shear(shx, shy, from)
  }

  override fun transform(p: Point2D): Point2D? = delegate.transform(p)

  override fun transform(shape: Shape): Shape = delegate.transform(shape)

  override fun translate(dx: Double, dy: Double) {
    delegate.translate(dx, dy)
  }

  override fun getRotation(): Double = delegate.getRotation()

  override fun rotate(radians: Double, x: Double, y: Double) {
    delegate.rotate(radians, x, y)
  }

  override fun transform(x: Double, y: Double): Point2D? = delegate.transform(x, y)

  override fun inverseTransform(x: Double, y: Double): Point2D = delegate.inverseTransform(x, y)
}
