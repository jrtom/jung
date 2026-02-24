/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform

import java.awt.Dimension
import java.awt.geom.Point2D
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
abstract class LensTransformer : MutableTransformerDecorator, MutableTransformer {

  var lens: Lens
    protected set

  /**
   * @param d the size used for the lens
   */
  constructor(d: Dimension) : this(Lens(d))

  /**
   * Create an instance with a possibly shared lens.
   *
   * @param lens
   */
  constructor(lens: Lens) : super(MutableAffineTransformer()) {
    this.lens = lens
  }

  /**
   * @param d the size used for the lens
   * @param delegate the transformer to use
   */
  constructor(d: Dimension, delegate: MutableTransformer) : this(Lens(d), delegate)

  /**
   * @param lens
   * @param delegate the transformer to use
   */
  constructor(lens: Lens, delegate: MutableTransformer) : super(delegate) {
    this.lens = lens
  }

  override fun setToIdentity() {
    delegate.setToIdentity()
  }

  /** override base class transform to project the fisheye effect */
  abstract override fun transform(p: Point2D): Point2D?

  override fun transform(x: Double, y: Double): Point2D? =
    transform(Point2D.Double(x, y))

  /** override base class to un-project the fisheye effect */
  abstract override fun inverseTransform(p: Point2D): Point2D

  override fun inverseTransform(x: Double, y: Double): Point2D =
    inverseTransform(Point2D.Double(x, y))

  companion object {
    private val log: Logger = LoggerFactory.getLogger(LensTransformer::class.java)
  }
}
