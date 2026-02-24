package edu.uci.ics.jung.layout.spatial

import edu.uci.ics.jung.layout.model.Point

/**
 * unused at this time
 *
 * @author Tom Nelson
 */
class Circle(
  val center: Point,
  val radius: Double
) {

  fun contains(p: Point): Boolean =
    p.inside(center.x - radius, center.y - radius, center.x + radius, center.y + radius) &&
      center.distance(p) <= radius

  fun intersects(r: Rectangle): Boolean =
    // quick fail with bounding box test first
    r.maxX >= center.x - radius &&
      r.maxY >= center.y - radius &&
      r.x <= center.x + radius &&
      r.y <= center.y + radius &&
      // more expensive test last
      squaredDistance(center, r) < radius * radius

  private fun squaredDistance(p: Point, r: Rectangle): Double {
    var distSq = 0.0
    val cx = p.x
    if (cx < r.x) {
      distSq += (r.x - cx) * (r.x - cx)
    }
    if (cx > r.maxX) {
      distSq += (cx - r.maxX) * (cx - r.maxX)
    }
    val cy = p.y
    if (cy < r.y) {
      distSq += (r.y - cy) * (r.y - cy)
    }
    if (cy > r.maxY) {
      distSq += (cy - r.maxY) * (cy - r.maxY)
    }
    return distSq
  }
}
