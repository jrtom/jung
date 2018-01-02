package edu.uci.ics.jung.layout.model;

import edu.uci.ics.jung.layout.spatial.Circle;
import edu.uci.ics.jung.layout.spatial.Rectangle;

/** @author Tom Nelson */
public class Point {

  public final double x;
  public final double y;
  public static final Point ORIGIN = new Point();

  public static Point of(double x, double y) {
    return new Point(x, y);
  }

  private Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  private Point() {
    this.x = 0;
    this.y = 0;
  }

  public Point add(double dx, double dy) {
    return new Point(x + dx, y + dy);
  }

  public double distanceSquared(Point other) {
    return distanceSquared(other.x, other.y);
  }

  public double distanceSquared(double ox, double oy) {
    double dx = x - ox;
    double dy = y - oy;
    return dx * dx + dy * dy;
  }

  public boolean inside(Circle c) {
    // fast-fail bounds check
    if (!inside(
        c.center.x - c.radius,
        c.center.y - c.radius,
        c.center.x + c.radius,
        c.center.y + c.radius)) {
      return false;
    }
    return c.center.distance(this) <= c.radius;
  }

  public boolean inside(Rectangle r) {
    return inside(r.x, r.y, r.maxX, r.maxY);
  }

  public boolean inside(double minX, double minY, double maxX, double maxY) {
    if (x < minX || maxX < x || y < minY || maxY < y) {
      return false;
    }
    return true;
  }

  public double length() {
    return Math.sqrt(x * x + y * y);
  }

  public double distance(Point other) {
    return Math.sqrt(distanceSquared(other));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point point = (Point) o;

    if (Double.compare(point.x, x) != 0) return false;
    return Double.compare(point.y, y) == 0;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "Point{" + "x=" + x + ", y=" + y + '}';
  }
}
