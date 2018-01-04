package edu.uci.ics.jung.layout.model;

import edu.uci.ics.jung.layout.spatial.Circle;
import edu.uci.ics.jung.layout.spatial.Rectangle;

/** @author Tom Nelson */
public class Point {

  public final double x;
  public final double y;
  public static final Point ORIGIN = new Point(0, 0);

  public static Point of(double x, double y) {
    return new Point(x, y);
  }

  private Point(double x, double y) {
    this.x = x;
    this.y = y;
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
  public String toString() {
    return "Point{" + "x=" + x + ", y=" + y + '}';
  }
}
