package edu.uci.ics.jung.algorithms.layout3d;

import edu.uci.ics.jung.visualization.layout.DomainModel;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Point3dDomainModel implements DomainModel<Point3d> {

  public Point3d[] newPointArray(int size) {
    return new Point3d[size];
  }

  public double getX(Point3d p) {
    return p.getX();
  }

  public double getY(Point3d p) {
    return p.getY();
  }

  @Override
  public double getZ(Point3d p) {
    return p.getZ();
  }

  public void setLocation(Point3d p, double x, double y) {
    p.set(x, y, 0);
  }

  @Override
  public void setLocation(Point3d p, double x, double y, double z) {
    p.set(x, y, z);
  }

  public void setLocation(Point3d p, Point3d from) {
    p.set(from);
  }

  @Override
  public double distanceSquared(Point3d p, Point3d q) {
    return p.distanceSquared(q);
  }

  @Override
  public Point3d newPoint(double x, double y) {
    return new Point3d(x, y, 0);
  }

  @Override
  public Point3d newPoint(double x, double y, double z) {
    return new Point3d(x, y, z);
  }

  @Override
  public double distance(Point3d in) {
    return new Vector3d(in).length();
    //    return in.distance(new Point3d(0, 0, 0));
  }

  @Override
  public void offset(Point3d p, double x, double y) {
    p.set(p.getX() + x, p.getY() + y, 0);
  }

  @Override
  public void offset(Point3d p, double x, double y, double z) {
    p.set(p.getX() + x, p.getY() + y, p.getZ() + z);
  }
}
