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
    // fast-fail bounds check
    if (!p.inside(center.x - radius, center.y - radius, center.x + radius, center.y + radius)) {
      return false;
    }
    return center.distance(p) <= radius;
  }

  public boolean intersects(Rectangle r) {
    // quick fail with bounding box test
    if (r.maxX < center.x - radius) return false;
    if (r.maxY < center.y - radius) return false;
    if (r.x > center.x + radius) return false;
    if (r.y > center.y + radius) return false;
    // more expensive test
    return squaredDistance(center, r) < radius * radius;
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
