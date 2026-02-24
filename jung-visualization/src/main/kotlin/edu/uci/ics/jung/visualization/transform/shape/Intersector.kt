/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform.shape

import java.awt.Rectangle
import java.awt.geom.Line2D
import java.awt.geom.Point2D

open class Intersector {

  var rectangle: Rectangle
    protected set
  var line: Line2D? = null
    private set
  var points: MutableSet<Point2D> = HashSet()
    private set

  constructor(rectangle: Rectangle) {
    this.rectangle = rectangle
  }

  constructor(rectangle: Rectangle, line: Line2D) {
    this.rectangle = rectangle
    intersectLine(line)
  }

  fun intersectLine(line: Line2D) {
    this.line = line
    points.clear()
    val rx0 = rectangle.minX.toFloat()
    val ry0 = rectangle.minY.toFloat()
    val rx1 = rectangle.maxX.toFloat()
    val ry1 = rectangle.maxY.toFloat()

    val x1 = line.x1.toFloat()
    val y1 = line.y1.toFloat()
    val x2 = line.x2.toFloat()
    val y2 = line.y2.toFloat()

    val dy = y2 - y1
    val dx = x2 - x1

    if (dx != 0f) {
      val m = dy / dx
      val b = y1 - m * x1

      // base of rect where y == ry0
      var x = (ry0 - b) / m
      if (rx0 <= x && x <= rx1) {
        points.add(Point2D.Float(x, ry0))
      }

      // top where y == ry1
      x = (ry1 - b) / m
      if (rx0 <= x && x <= rx1) {
        points.add(Point2D.Float(x, ry1))
      }

      // left side, where x == rx0
      var y = m * rx0 + b
      if (ry0 <= y && y <= ry1) {
        points.add(Point2D.Float(rx0, y))
      }

      // right side, where x == rx1
      y = m * rx1 + b
      if (ry0 <= y && y <= ry1) {
        points.add(Point2D.Float(rx1, y))
      }
    } else {
      // base, where y == ry0
      var x = x1
      if (rx0 <= x && x <= rx1) {
        points.add(Point2D.Float(x, ry0))
      }

      // top, where y == ry1
      x = x2
      if (rx0 <= x && x <= rx1) {
        points.add(Point2D.Float(x, ry1))
      }
    }
  }

  override fun toString(): String = "Rectangle: $rectangle, points:$points"

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      val rectangle = Rectangle(0, 0, 10, 10)
      val line = Line2D.Float(4f, 4f, 5f, 5f)
      System.err.println("${Intersector(rectangle, line)}")
      System.err.println("${Intersector(rectangle, Line2D.Float(9f, 11f, 11f, 9f))}")
      System.err.println("${Intersector(rectangle, Line2D.Float(1f, 1f, 3f, 2f))}")
      System.err.println("${Intersector(rectangle, Line2D.Float(4f, 6f, 6f, 4f))}")
    }
  }
}
