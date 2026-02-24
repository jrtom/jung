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

import java.awt.Color
import java.awt.Composite
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.Image
import java.awt.Paint
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.RenderingHints.Key
import java.awt.Shape
import java.awt.Stroke
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ImageObserver
import java.awt.image.RenderedImage
import java.awt.image.renderable.RenderableImage
import java.text.AttributedCharacterIterator

/**
 * a complete wrapping of Graphics2D, useful as a base class. Contains no additional methods, other
 * than direct calls to the _delegate.
 *
 * @see GraphicsDecorator as an example subclass that adds additional methods.
 * @author Tom Nelson
 */
open class Graphics2DWrapper(
  protected var _delegate: Graphics2D? = null
) {

  open fun setDelegate(_delegate: Graphics2D) {
    this._delegate = _delegate
  }

  open fun getDelegate(): Graphics2D? = _delegate

  open fun addRenderingHints(hints: Map<*, *>) {
    _delegate!!.addRenderingHints(hints)
  }

  open fun clearRect(x: Int, y: Int, width: Int, height: Int) {
    _delegate!!.clearRect(x, y, width, height)
  }

  open fun clip(s: Shape) {
    _delegate!!.clip(s)
  }

  open fun clipRect(x: Int, y: Int, width: Int, height: Int) {
    _delegate!!.clipRect(x, y, width, height)
  }

  open fun copyArea(x: Int, y: Int, width: Int, height: Int, dx: Int, dy: Int) {
    _delegate!!.copyArea(x, y, width, height, dx, dy)
  }

  open fun create(): Graphics = _delegate!!.create()

  open fun create(x: Int, y: Int, width: Int, height: Int): Graphics =
    _delegate!!.create(x, y, width, height)

  open fun dispose() {
    _delegate!!.dispose()
  }

  open fun draw(s: Shape) {
    _delegate!!.draw(s)
  }

  open fun draw3DRect(x: Int, y: Int, width: Int, height: Int, raised: Boolean) {
    _delegate!!.draw3DRect(x, y, width, height, raised)
  }

  open fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) {
    _delegate!!.drawArc(x, y, width, height, startAngle, arcAngle)
  }

  open fun drawBytes(data: ByteArray, offset: Int, length: Int, x: Int, y: Int) {
    _delegate!!.drawBytes(data, offset, length, x, y)
  }

  open fun drawChars(data: CharArray, offset: Int, length: Int, x: Int, y: Int) {
    _delegate!!.drawChars(data, offset, length, x, y)
  }

  open fun drawGlyphVector(g: GlyphVector, x: Float, y: Float) {
    _delegate!!.drawGlyphVector(g, x, y)
  }

  open fun drawImage(img: BufferedImage, op: BufferedImageOp, x: Int, y: Int) {
    _delegate!!.drawImage(img, op, x, y)
  }

  open fun drawImage(img: Image, xform: AffineTransform, obs: ImageObserver): Boolean =
    _delegate!!.drawImage(img, xform, obs)

  open fun drawImage(img: Image, x: Int, y: Int, bgcolor: Color, observer: ImageObserver): Boolean =
    _delegate!!.drawImage(img, x, y, bgcolor, observer)

  open fun drawImage(img: Image, x: Int, y: Int, observer: ImageObserver): Boolean =
    _delegate!!.drawImage(img, x, y, observer)

  open fun drawImage(
    img: Image, x: Int, y: Int, width: Int, height: Int,
    bgcolor: Color, observer: ImageObserver
  ): Boolean =
    _delegate!!.drawImage(img, x, y, width, height, bgcolor, observer)

  open fun drawImage(
    img: Image, x: Int, y: Int, width: Int, height: Int,
    observer: ImageObserver
  ): Boolean =
    _delegate!!.drawImage(img, x, y, width, height, observer)

  open fun drawImage(
    img: Image,
    dx1: Int, dy1: Int, dx2: Int, dy2: Int,
    sx1: Int, sy1: Int, sx2: Int, sy2: Int,
    bgcolor: Color, observer: ImageObserver
  ): Boolean =
    _delegate!!.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer)

  open fun drawImage(
    img: Image,
    dx1: Int, dy1: Int, dx2: Int, dy2: Int,
    sx1: Int, sy1: Int, sx2: Int, sy2: Int,
    observer: ImageObserver
  ): Boolean =
    _delegate!!.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)

  open fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
    _delegate!!.drawLine(x1, y1, x2, y2)
  }

  open fun drawOval(x: Int, y: Int, width: Int, height: Int) {
    _delegate!!.drawOval(x, y, width, height)
  }

  open fun drawPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
    _delegate!!.drawPolygon(xPoints, yPoints, nPoints)
  }

  open fun drawPolygon(p: Polygon) {
    _delegate!!.drawPolygon(p)
  }

  open fun drawPolyline(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
    _delegate!!.drawPolyline(xPoints, yPoints, nPoints)
  }

  open fun drawRect(x: Int, y: Int, width: Int, height: Int) {
    _delegate!!.drawRect(x, y, width, height)
  }

  open fun drawRenderableImage(img: RenderableImage, xform: AffineTransform) {
    _delegate!!.drawRenderableImage(img, xform)
  }

  open fun drawRenderedImage(img: RenderedImage, xform: AffineTransform) {
    _delegate!!.drawRenderedImage(img, xform)
  }

  open fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
    _delegate!!.drawRoundRect(x, y, width, height, arcWidth, arcHeight)
  }

  open fun drawString(iterator: AttributedCharacterIterator, x: Float, y: Float) {
    _delegate!!.drawString(iterator, x, y)
  }

  open fun drawString(iterator: AttributedCharacterIterator, x: Int, y: Int) {
    _delegate!!.drawString(iterator, x, y)
  }

  open fun drawString(s: String, x: Float, y: Float) {
    _delegate!!.drawString(s, x, y)
  }

  open fun drawString(str: String, x: Int, y: Int) {
    _delegate!!.drawString(str, x, y)
  }

  override fun equals(other: Any?): Boolean = _delegate == other

  open fun fill(s: Shape) {
    _delegate!!.fill(s)
  }

  open fun fill3DRect(x: Int, y: Int, width: Int, height: Int, raised: Boolean) {
    _delegate!!.fill3DRect(x, y, width, height, raised)
  }

  open fun fillArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) {
    _delegate!!.fillArc(x, y, width, height, startAngle, arcAngle)
  }

  open fun fillOval(x: Int, y: Int, width: Int, height: Int) {
    _delegate!!.fillOval(x, y, width, height)
  }

  open fun fillPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) {
    _delegate!!.fillPolygon(xPoints, yPoints, nPoints)
  }

  open fun fillPolygon(p: Polygon) {
    _delegate!!.fillPolygon(p)
  }

  open fun fillRect(x: Int, y: Int, width: Int, height: Int) {
    _delegate!!.fillRect(x, y, width, height)
  }

  open fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) {
    _delegate!!.fillRoundRect(x, y, width, height, arcWidth, arcHeight)
  }

  @Suppress("deprecation", "DEPRECATION")
  protected fun finalize() {
    _delegate!!.finalize()
  }

  open fun getBackground(): Color = _delegate!!.background

  open fun getClip(): Shape = _delegate!!.clip

  open fun getClipBounds(): Rectangle = _delegate!!.clipBounds

  open fun getClipBounds(r: Rectangle): Rectangle = _delegate!!.getClipBounds(r)

  @Suppress("deprecation", "DEPRECATION")
  open fun getClipRect(): Rectangle = _delegate!!.clipRect

  open fun getColor(): Color = _delegate!!.color

  open fun getComposite(): Composite = _delegate!!.composite

  open fun getDeviceConfiguration(): GraphicsConfiguration = _delegate!!.deviceConfiguration

  open fun getFont(): Font = _delegate!!.font

  open fun getFontMetrics(): FontMetrics = _delegate!!.fontMetrics

  open fun getFontMetrics(f: Font): FontMetrics = _delegate!!.getFontMetrics(f)

  open fun getFontRenderContext(): FontRenderContext = _delegate!!.fontRenderContext

  open fun getPaint(): Paint = _delegate!!.paint

  open fun getRenderingHint(hintKey: Key): Any = _delegate!!.getRenderingHint(hintKey)

  open fun getRenderingHints(): RenderingHints = _delegate!!.renderingHints

  open fun getStroke(): Stroke = _delegate!!.stroke

  open fun getTransform(): AffineTransform = _delegate!!.transform

  override fun hashCode(): Int = _delegate.hashCode()

  open fun hit(rect: Rectangle, s: Shape, onStroke: Boolean): Boolean =
    _delegate!!.hit(rect, s, onStroke)

  open fun hitClip(x: Int, y: Int, width: Int, height: Int): Boolean =
    _delegate!!.hitClip(x, y, width, height)

  open fun rotate(theta: Double, x: Double, y: Double) {
    _delegate!!.rotate(theta, x, y)
  }

  open fun rotate(theta: Double) {
    _delegate!!.rotate(theta)
  }

  open fun scale(sx: Double, sy: Double) {
    _delegate!!.scale(sx, sy)
  }

  open fun setBackground(color: Color) {
    _delegate!!.background = color
  }

  open fun setClip(x: Int, y: Int, width: Int, height: Int) {
    _delegate!!.setClip(x, y, width, height)
  }

  open fun setClip(clip: Shape) {
    _delegate!!.clip = clip
  }

  open fun setColor(c: Color) {
    _delegate!!.color = c
  }

  open fun setComposite(comp: Composite) {
    _delegate!!.composite = comp
  }

  open fun setFont(font: Font) {
    _delegate!!.font = font
  }

  open fun setPaint(paint: Paint) {
    _delegate!!.paint = paint
  }

  open fun setPaintMode() {
    _delegate!!.setPaintMode()
  }

  open fun setRenderingHint(hintKey: Key, hintValue: Any) {
    _delegate!!.setRenderingHint(hintKey, hintValue)
  }

  open fun setRenderingHints(hints: Map<*, *>) {
    _delegate!!.setRenderingHints(hints)
  }

  open fun setStroke(s: Stroke) {
    _delegate!!.stroke = s
  }

  open fun setTransform(tx: AffineTransform) {
    _delegate!!.transform = tx
  }

  open fun setXORMode(c1: Color) {
    _delegate!!.setXORMode(c1)
  }

  open fun shear(shx: Double, shy: Double) {
    _delegate!!.shear(shx, shy)
  }

  override fun toString(): String = _delegate.toString()

  open fun transform(tx: AffineTransform) {
    _delegate!!.transform(tx)
  }

  open fun translate(tx: Double, ty: Double) {
    _delegate!!.translate(tx, ty)
  }

  open fun translate(x: Int, y: Int) {
    _delegate!!.translate(x, y)
  }
}
