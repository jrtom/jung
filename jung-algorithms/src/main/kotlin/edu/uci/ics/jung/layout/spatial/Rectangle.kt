package edu.uci.ics.jung.layout.spatial

import com.google.common.base.Preconditions
import edu.uci.ics.jung.layout.model.Point

/**
 * Simple, immutable Rectangle class used for spatial data structures.
 *
 * @author Tom Nelson
 */
class Rectangle(
  val x: Double,
  val y: Double,
  val width: Double,
  val height: Double
) {
  val maxX: Double = x + width
  val maxY: Double = y + height

  init {
    Preconditions.checkArgument(width >= 0 && height >= 0, "width and height must be non-negative")
  }

  val centerX: Double
    get() = x + width / 2

  val centerY: Double
    get() = y + height / 2

  /**
   * fail-fast implementation to reduce computation
   *
   * @param other
   * @return
   */
  fun intersects(other: Rectangle): Boolean =
    maxX >= other.x && other.maxX >= x && maxY >= other.y && other.maxY >= y

  fun contains(p: Point): Boolean = contains(p.x, p.y)

  fun contains(ox: Double, oy: Double): Boolean =
    ox >= this.x && ox <= maxX && oy >= this.y && oy <= maxY
}
