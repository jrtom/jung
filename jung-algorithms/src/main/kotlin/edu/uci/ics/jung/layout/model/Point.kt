package edu.uci.ics.jung.layout.model

import edu.uci.ics.jung.layout.spatial.Circle
import edu.uci.ics.jung.layout.spatial.Rectangle
import java.util.Objects

/**
 * Simple, immutable Point class used for Graph layout
 *
 * @author Tom Nelson
 */
class Point private constructor(
  val x: Double,
  val y: Double
) {

  companion object {
    @JvmField
    val ORIGIN = Point(0.0, 0.0)

    @JvmStatic
    fun of(x: Double, y: Double): Point = Point(x, y)
  }

  /**
   * @param other the Point with values to add
   * @return a new Point with the sum of this Point and other Point's values
   */
  fun add(other: Point): Point = add(other.x, other.y)

  /**
   * @param dx change in x
   * @param dy change in y
   * @return a new Point with the sum of this Point and the passed values
   */
  fun add(dx: Double, dy: Double): Point = Point(x + dx, y + dy)

  /**
   * @param other the Point to measure against
   * @return the square of the distance between this Point and the passed Point
   */
  fun distanceSquared(other: Point): Double = distanceSquared(other.x, other.y)

  /**
   * @param ox coordinate of another location
   * @param oy coordinate of another location
   * @return the square of the distance between this Point and the passed location
   */
  fun distanceSquared(ox: Double, oy: Double): Double {
    val dx = x - ox
    val dy = y - oy
    return dx * dx + dy * dy
  }

  /**
   * @param c a Circle to compare against
   * @return true if this Point is within the passed Circle, false otherwise
   */
  fun inside(c: Circle): Boolean =
    // fast-fail bounds check first
    inside(
      c.center.x - c.radius,
      c.center.y - c.radius,
      c.center.x + c.radius,
      c.center.y + c.radius
    ) &&
      // more expensive test last
      c.center.distance(this) <= c.radius

  /**
   * @param r a Rectangle to compare against
   * @return true if this Point is inside the passed Rectangle, false otherwise
   */
  fun inside(r: Rectangle): Boolean = inside(r.x, r.y, r.maxX, r.maxY)

  /**
   * @param minX min coordinate of a rectangular space
   * @param minY min coordinate of a rectangular space
   * @param maxX max coordinate of a rectangular space
   * @param maxY max coordinate of a rectangular space
   * @return true if this Point is within the passed rectangular space, false otherwise
   */
  fun inside(minX: Double, minY: Double, maxX: Double, maxY: Double): Boolean =
    x >= minX && maxX >= x && y >= minY && maxY >= y

  /**
   * @return the distance between this Point and the origin.
   */
  fun length(): Double = Math.sqrt(x * x + y * y)

  /**
   * @param other a Point to consider
   * @return the distance between this Point and the passed Point
   */
  fun distance(other: Point): Double = Math.sqrt(distanceSquared(other))

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Point) return false
    return java.lang.Double.compare(other.x, x) == 0 && java.lang.Double.compare(other.y, y) == 0
  }

  override fun hashCode(): Int = Objects.hash(x, y)

  override fun toString(): String = "Point{x=$x, y=$y}"
}
