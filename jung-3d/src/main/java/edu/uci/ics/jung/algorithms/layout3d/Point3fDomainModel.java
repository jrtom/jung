package edu.uci.ics.jung.algorithms.layout3d;

import edu.uci.ics.jung.visualization.layout.DomainModel;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

public class Point3fDomainModel implements DomainModel<Point3f> {

  public Point3f[] newPointArray(int size) {
    return new Point3f[size];
  }

  public double getX(Point3f p) {
    return p.getX();
  }

  public double getY(Point3f p) {
    return p.getY();
  }

  @Override
  public double getZ(Point3f p) {
    return p.getZ();
  }

  public void setLocation(Point3f p, double x, double y) {
    p.set((float) x, (float) y, 0);
  }

  @Override
  public void setLocation(Point3f p, double x, double y, double z) {
    p.set((float) x, (float) y, (float) z);
  }

  public void setLocation(Point3f p, Point3f from) {
    p.set(from);
  }

  @Override
  public double distanceSquared(Point3f p, Point3f q) {
    return p.distanceSquared(q);
  }

  @Override
  public Point3f newPoint(double x, double y) {
    return new Point3f((float) x, (float) y, 0);
  }

  @Override
  public Point3f newPoint(double x, double y, double z) {
    return new Point3f((float) x, (float) y, (float) z);
  }

  @Override
  public double distance(Point3f in) {
    return new Vector3d(in).length();
    //    return in.distance(new Point3d(0, 0, 0));
  }

  @Override
  public void offset(Point3f p, double x, double y) {
    p.set(p.getX() + (float) x, p.getY() + (float) y, 0);
  }

  @Override
  public void offset(Point3f p, double x, double y, double z) {
    p.set(p.getX() + (float) x, p.getY() + (float) y, p.getZ() + (float) z);
  }
}
