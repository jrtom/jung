/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.model

import java.util.Objects

/**
 * Immutable Point. Represents a point in polar coordinates: distance and angle from the origin.
 * Includes conversions between polar and Cartesian coordinates (Point).
 *
 * @author Tom Nelson
 */
class PolarPoint private constructor(
  val theta: Double,
  val radius: Double
) {

  companion object {
    @JvmField
    val ORIGIN = PolarPoint(0.0, 0.0)

    @JvmStatic
    fun of(theta: Double, radius: Double): PolarPoint = PolarPoint(theta, radius)

    /**
     * @param polar the input location to convert
     * @return the result of converting `polar` to Cartesian coordinates.
     */
    @JvmStatic
    fun polarToCartesian(polar: PolarPoint): Point = polarToCartesian(polar.theta, polar.radius)

    /**
     * @param theta the angle of the input location
     * @param radius the distance from the origin of the input location
     * @return the result of converting `(theta, radius)` to Cartesian coordinates.
     */
    @JvmStatic
    fun polarToCartesian(theta: Double, radius: Double): Point =
      Point.of(radius * Math.cos(theta), radius * Math.sin(theta))

    /**
     * @param point the input location
     * @return the result of converting `point` to polar coordinates.
     */
    @JvmStatic
    fun cartesianToPolar(point: Point): PolarPoint = cartesianToPolar(point.x, point.y)

    /**
     * @param x the x coordinate of the input location
     * @param y the y coordinate of the input location
     * @return the result of converting `(x, y)` to polar coordinates.
     */
    @JvmStatic
    fun cartesianToPolar(x: Double, y: Double): PolarPoint {
      val theta = Math.atan2(y, x)
      val radius = Math.sqrt(x * x + y * y)
      return PolarPoint(theta, radius)
    }
  }

  fun newRadius(radius: Double): PolarPoint = of(theta, radius)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PolarPoint) return false
    return java.lang.Double.compare(other.theta, theta) == 0 && java.lang.Double.compare(other.radius, radius) == 0
  }

  override fun hashCode(): Int = Objects.hash(theta, radius)

  override fun toString(): String = "PolarPoint[$radius,$theta]"
}
