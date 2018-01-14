package edu.uci.ics.jung.layout.model;

import edu.uci.ics.jung.layout.spatial.Circle;
import edu.uci.ics.jung.layout.spatial.Rectangle;
import java.util.Objects;

/**
 * Simple, immutable Point class used for Graph layout
 *
 * @author Tom Nelson
 */
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

  /**
   * @param other the Point with values to add
   * @return a new Point with the sum of this Point and other Point's values
   */
  public Point add(Point other) {
    return add(other.x, other.y);
  }

  /**
   * @param dx change in x
   * @param dy change in y
   * @return a new Point with the sum of this Point and the passed values
   */
  public Point add(double dx, double dy) {
    return new Point(x + dx, y + dy);
  }

  /**
   * @param other the Point to measure against
   * @return the square of the distance between this Point and the passed Point
   */
  public double distanceSquared(Point other) {
    return distanceSquared(other.x, other.y);
  }

  /**
   * @param ox coordinate of another location
   * @param oy coordinate of another location
   * @return the square of the distance between this Point and the passed location
   */
  public double distanceSquared(double ox, double oy) {
    double dx = x - ox;
    double dy = y - oy;
    return dx * dx + dy * dy;
  }

  /**
   * @param c a Circle to compare against
   * @return true if this Point is within the passed Circle, false otherwise
   */
  public boolean inside(Circle c) {
    // fast-fail bounds check first
    return inside(
            c.center.x - c.radius,
            c.center.y - c.radius,
            c.center.x + c.radius,
            c.center.y + c.radius)
        &&
        // more expensive test last
        c.center.distance(this) <= c.radius;
  }

  /**
   * @param r a Rectangle to compare against
   * @return true if this Point is inside the passed Rectangle, false otherwise
   */
  public boolean inside(Rectangle r) {
    return inside(r.x, r.y, r.maxX, r.maxY);
  }

  /**
   * @param minX min coordinate of a rectangular space
   * @param minY min coordinate of a rectangular space
   * @param maxX max coordinate of a rectangular space
   * @param maxY max coordinate of a rectangular space
   * @return true if this Point is within the passed rectangular space, false otherwise
   */
  public boolean inside(double minX, double minY, double maxX, double maxY) {
    return x >= minX && maxX >= x && y >= minY && maxY >= y;
  }

  /** @return the distance between this Point and the origin. */
  public double length() {
    return Math.sqrt(x * x + y * y);
  }

  /**
   * @param other a Point to consider
   * @return the distance between this Point and the passed Point
   */
  public double distance(Point other) {
    return Math.sqrt(distanceSquared(other));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Point)) {
      return false;
    }

    Point other = (Point) o;

    return (Double.compare(other.x, x) == 0 && Double.compare(other.y, y) == 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    return "Point{" + "x=" + x + ", y=" + y + '}';
  }
}
