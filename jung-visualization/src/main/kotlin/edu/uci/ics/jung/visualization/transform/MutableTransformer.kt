/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Jul 3, 2005
 */

package edu.uci.ics.jung.visualization.transform

import edu.uci.ics.jung.visualization.transform.shape.ShapeTransformer
import edu.uci.ics.jung.visualization.util.ChangeEventSupport
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D

/**
 * Provides an API for the mutation of a Function and for adding listeners for changes on the
 * Function
 *
 * @author Tom Nelson
 */
interface MutableTransformer : ShapeTransformer, ChangeEventSupport {

  fun translate(dx: Double, dy: Double)

  fun setTranslate(dx: Double, dy: Double)

  fun scale(sx: Double, sy: Double, point: Point2D)

  fun setScale(sx: Double, sy: Double, point: Point2D)

  fun rotate(radians: Double, point: Point2D)

  fun rotate(radians: Double, x: Double, y: Double)

  fun shear(shx: Double, shy: Double, from: Point2D)

  fun concatenate(transform: AffineTransform)

  fun preConcatenate(transform: AffineTransform)

  fun getScaleX(): Double

  fun getScaleY(): Double

  fun getScale(): Double

  fun getTranslateX(): Double

  fun getTranslateY(): Double

  fun getShearX(): Double

  fun getShearY(): Double

  fun getTransform(): AffineTransform

  fun setToIdentity()

  fun getRotation(): Double
}
