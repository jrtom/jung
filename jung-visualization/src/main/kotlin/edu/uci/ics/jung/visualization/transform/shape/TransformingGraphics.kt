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
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Rectangle
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.ImageObserver

/**
 * subclassed to pass certain operations thru the Function before the base class method is applied
 * This is useful when you want to apply non-affine transformations to the Graphics2D used to draw
 * elements of the graph.
 *
 * @author Tom Nelson
 */
open class TransformingGraphics(
  /** the Function to apply */
  protected var _transformer: BidirectionalTransformer,
  delegate: Graphics2D? = null
) : GraphicsDecorator(delegate) {

  /**
   * @return Returns the Function.
   */
  fun getTransformer(): BidirectionalTransformer = _transformer

  /**
   * @param transformer The Function to set.
   */
  fun setTransformer(transformer: BidirectionalTransformer) {
    this._transformer = transformer
  }

  /** transform the shape before letting the delegate draw it */
  override fun draw(s: Shape) {
    val shape = (_transformer as ShapeTransformer).transform(s)
    _delegate!!.draw(shape)
  }

  open fun draw(s: Shape, flatness: Float) {
    val shape = if (_transformer is ShapeFlatnessTransformer) {
      (_transformer as ShapeFlatnessTransformer).transform(s, flatness)
    } else {
      (_transformer as ShapeTransformer).transform(s)
    }
    _delegate!!.draw(shape)
  }

  /** transform the shape before letting the delegate fill it */
  override fun fill(s: Shape) {
    val shape = (_transformer as ShapeTransformer).transform(s)
    _delegate!!.fill(shape)
  }

  open fun fill(s: Shape, flatness: Float) {
    val shape = if (_transformer is ShapeFlatnessTransformer) {
      (_transformer as ShapeFlatnessTransformer).transform(s, flatness)
    } else {
      (_transformer as ShapeTransformer).transform(s)
    }
    _delegate!!.fill(shape)
  }

  override fun drawImage(img: Image, x: Int, y: Int, observer: ImageObserver): Boolean {
    var image: Image
    var drawX = x
    var drawY = y
    if (_transformer is ShapeFlatnessTransformer) {
      val r = Rectangle2D.Double(x.toDouble(), y.toDouble(), img.getWidth(observer).toDouble(), img.getHeight(observer).toDouble())
      val s = (_transformer as ShapeTransformer).transform(r).bounds2D
      image = img.getScaledInstance(s.width.toInt(), s.height.toInt(), Image.SCALE_SMOOTH)
      drawX = s.minX.toInt()
      drawY = s.minY.toInt()
    } else {
      image = img
    }
    return _delegate!!.drawImage(image, drawX, drawY, observer)
  }

  override fun drawImage(img: Image, at: AffineTransform, observer: ImageObserver): Boolean {
    var image: Image
    var drawAt = at
    val x = at.translateX.toInt()
    val y = at.translateY.toInt()
    if (_transformer is ShapeFlatnessTransformer) {
      val r = Rectangle2D.Double(x.toDouble(), y.toDouble(), img.getWidth(observer).toDouble(), img.getHeight(observer).toDouble())
      val s = (_transformer as ShapeTransformer).transform(r).bounds2D
      image = img.getScaledInstance(s.width.toInt(), s.height.toInt(), Image.SCALE_SMOOTH)
      drawAt = AffineTransform(at)
      drawAt.setToTranslation(s.minX, s.minY)
    } else {
      image = img
    }
    return _delegate!!.drawImage(image, drawAt, observer)
  }

  /** transform the shape before letting the delegate apply 'hit' with it */
  override fun hit(rect: Rectangle, s: Shape, onStroke: Boolean): Boolean {
    val shape = (_transformer as ShapeTransformer).transform(s)
    return _delegate!!.hit(rect, shape, onStroke)
  }

  override fun create(): Graphics = _delegate!!.create()

  override fun dispose() {
    _delegate!!.dispose()
  }
}
