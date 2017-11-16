package edu.uci.ics.jung.layout;

import edu.uci.ics.jung.layout.model.AbstractPointModel;
import edu.uci.ics.jung.layout.model.PointModel;

/** Created by Tom Nelson */
public class TestPointModel extends AbstractPointModel<TestPointModel.Point>
    implements PointModel<TestPointModel.Point> {

  @Override
  public double getX(Point p) {
    return p.getX();
  }

  @Override
  public double getY(Point p) {
    return p.getY();
  }

  @Override
  public double getZ(Point point) {
    return 0;
  }

  @Override
  public void setLocation(Point p, double x, double y) {
    p.x = x;
    p.y = y;
  }

  @Override
  public void setLocation(Point p, double x, double y, double z) {
    setLocation(p, x, y);
  }

  @Override
  public void setLocation(Point p, Point from) {
    setLocation(p, from.x, from.y);
  }

  @Override
  public Point newPoint() {
    return new Point(0, 0);
  }

  @Override
  public Point newPoint(double x, double y) {
    return new Point(x, y);
  }

  @Override
  public Point newPoint(double x, double y, double z) {
    return newPoint(x, y);
  }

  @Override
  public void offset(Point p, double x, double y) {
    p.x += x;
    p.y += y;
  }

  @Override
  public void offset(Point p, double x, double y, double z) {
    offset(p, x, y);
  }

  public static class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
      this.x = x;
      this.y = y;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    @Override
    public String toString() {
      return "Point{" + "x=" + x + ", y=" + y + '}';
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
  }
}
