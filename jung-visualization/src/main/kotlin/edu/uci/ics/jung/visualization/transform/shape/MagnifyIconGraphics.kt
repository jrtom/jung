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
import java.awt.Component
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Rectangle2D
import javax.swing.Icon

/**
 * Subclassed to apply a magnification transform to an icon.
 *
 * @author Tom Nelson
 */
open class MagnifyIconGraphics : TransformingFlatnessGraphics {

  constructor(_transformer: BidirectionalTransformer) : this(_transformer, null)

  constructor(_transformer: BidirectionalTransformer, delegate: Graphics2D?) : super(_transformer, delegate)

  override fun draw(icon: Icon, c: Component, clip: Shape, x: Int, y: Int) {
    if (_transformer is MagnifyShapeTransformer) {
      val mst = _transformer as MagnifyShapeTransformer
      val w = icon.iconWidth
      val h = icon.iconHeight
      val r = Rectangle2D.Double((x - w / 2).toDouble(), (y - h / 2).toDouble(), w.toDouble(), h.toDouble())
      val lens = mst.lens.lensShape
      if (lens.intersects(r)) {
        // magnify the whole icon
        val s = mst.magnify(r).bounds2D
        if (lens.intersects(s)) {
          val transformedClip = mst.transform(clip)
          val sx = s.width / r.width
          val sy = s.height / r.height

          val old = _delegate!!.transform
          val xform = AffineTransform(old)
          xform.translate(s.minX, s.minY)
          xform.scale(sx, sy)
          xform.translate(-s.minX, -s.minY)
          val oldClip = _delegate!!.clip
          _delegate!!.clip(transformedClip)
          _delegate!!.transform = xform
          icon.paintIcon(c, _delegate, s.minX.toInt(), s.minY.toInt())
          _delegate!!.transform = old
          _delegate!!.clip = oldClip
        } else {
          // clip out the lens so the small icon doesn't get drawn
          // inside of it
          val oldClip = _delegate!!.clip
          val viewBounds = Area(oldClip)
          viewBounds.subtract(Area(lens))
          _delegate!!.clip = viewBounds
          icon.paintIcon(c, _delegate, r.minX.toInt(), r.minY.toInt())
          _delegate!!.clip = oldClip
        }
      } else {
        icon.paintIcon(c, _delegate, r.minX.toInt(), r.minY.toInt())
      }
    }
  }
}
