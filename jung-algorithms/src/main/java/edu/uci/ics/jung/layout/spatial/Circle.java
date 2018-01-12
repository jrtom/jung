package edu.uci.ics.jung.layout.spatial;

import edu.uci.ics.jung.layout.model.Point;

/**
 * unused at this time
 *
 * @author Tom Nelson
 */
public class Circle {

  public final Point center;
  public final double radius;

  public Circle(Point center, double radius) {
    this.center = center;
    this.radius = radius;
  }

  public boolean contains(Point p) {
    return p.inside(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
        && center.distance(p) <= radius;
  }

  public boolean intersects(Rectangle r) {
    // quick fail with bounding box test first
    return r.maxX >= center.x - radius
        && r.maxY >= center.y - radius
        && r.x <= center.x + radius
        && r.y <= center.y + radius
        &&
        // more expensive test last
        squaredDistance(center, r) < radius * radius;
  }

  private double squaredDistance(Point p, Rectangle r) {
    double distSq = 0;
    double cx = p.x;
    if (cx < r.x) {
      distSq += (r.x - cx) * (r.x - cx);
    }
    if (cx > r.maxX) {
      distSq += (cx - r.maxX) * (cx - r.maxX);
    }
    double cy = p.y;
    if (cy < r.y) {
      distSq += (r.y - cy) * (r.y - cy);
    }
    if (cy > r.maxY) {
      distSq += (cy - r.maxY) * (cy - r.maxY);
    }
    return distSq;
  }
}
