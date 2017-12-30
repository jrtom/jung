package edu.uci.ics.jung.layout.model;

/** @author Tom Nelson */
public class Point {

  public final double x;
  public final double y;

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Point() {
    this.x = 0;
    this.y = 0;
  }

  public double distanceSquared(Point other) {
    return distanceSquared(other.x, other.y);
  }

  public double distanceSquared(double ox, double oy) {
    double dx = x - ox;
    double dy = y - oy;
    return dx * dx + dy * dy;
  }

  public double length() {
    return Math.sqrt(x * x + y * y);
  }

  public double distance(Point other) {
    return Math.sqrt(distanceSquared(other));
  }
}
