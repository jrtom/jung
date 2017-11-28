package edu.uci.ics.jung.layout3d;

import edu.uci.ics.jung.layout.model.PointModel;

/** @author Tom Nelson */
public class TestPointModel implements PointModel<TestPointModel.Point> {

  @Override
  public double getX(Point p) {
    return p.getX();
  }

  @Override
  public double getY(Point p) {
    return p.getY();
  }

  @Override
  public double getZ(Point p) {
    return p.getZ();
  }

  @Override
  public void setLocation(Point p, double x, double y, double z) {
    p.x = x;
    p.y = y;
    p.z = z;
  }

  @Override
  public void setLocation(Point p, double x, double y) {
    setLocation(p, x, y, 0);
  }

  @Override
  public void setLocation(Point p, Point from) {
    setLocation(p, from.x, from.y, from.z);
  }

  @Override
  public Point newPoint() {
    return new Point(0, 0, 0);
  }

  @Override
  public Point newPoint(double x, double y) {
    return new Point(x, y, 0);
  }

  @Override
  public Point newPoint(double x, double y, double z) {
    return new Point(x, y, z);
  }

  @Override
  public void offset(Point p, double x, double y, double z) {
    p.x += x;
    p.y += y;
    p.z += z;
  }

  @Override
  public void offset(Point p, double x, double y) {
    offset(p, x, y, 0);
  }

  public static class Point {
    private double x;
    private double y;
    private double z;

    public Point(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public double getZ() {
      return z;
    }

    @Override
    public String toString() {
      return "Point{" + "x=" + x + ", y=" + y + ", z=" + z + "}";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Point point = (Point) o;

      if (Double.compare(point.x, x) != 0) return false;
      if (Double.compare(point.y, y) != 0) return false;
      return Double.compare(point.z, z) == 0;
    }

    @Override
    public int hashCode() {
      int result;
      long temp;
      temp = Double.doubleToLongBits(x);
      result = (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(y);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(z);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      return result;
    }
  }
}
