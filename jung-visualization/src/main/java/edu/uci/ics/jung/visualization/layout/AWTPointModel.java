package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.layout.model.PointModel;
import java.awt.geom.Point2D;

/**
 * An implementation of PointModel using java.awt.geom.Point2D
 *
 * @author Tom Nelson
 */
public class AWTPointModel implements PointModel<Point2D> {

  /**
   * @param p
   * @return the x value of the passed Point2D
   */
  public double getX(Point2D p) {
    return p.getX();
  }

  /**
   * @param p
   * @return the y value of the passed Point2D
   */
  public double getY(Point2D p) {
    return p.getY();
  }

  /**
   * @param p
   * @return zero for Point2D
   */
  public double getZ(Point2D p) {
    return 0;
  }

  /**
   * set the x and y attributes for the passed Point2D
   *
   * @param p
   * @param x
   * @param y
   */
  public void setLocation(Point2D p, double x, double y) {
    p.setLocation(x, y);
  }

  /**
   * set the x, y, z attributes of the passed P
   *
   * @param p
   * @param x
   * @param y
   * @param z ignored for Point2D
   */
  @Override
  public void setLocation(Point2D p, double x, double y, double z) {
    p.setLocation(x, y);
  }

  /**
   * set the (x,y) coordinates of p to those of 'from'
   *
   * @param p
   * @param from source of new coordinates for P
   */
  @Override
  public void setLocation(Point2D p, Point2D from) {
    p.setLocation(from);
  }

  /**
   * returns the distance squared between p and q
   *
   * @param p
   * @param q
   * @return
   */
  @Override
  public double distanceSquared(Point2D p, Point2D q) {
    return p.distanceSq(q);
  }

  /**
   * create and return a new Point2D at the origin
   *
   * @return
   */
  @Override
  public Point2D newPoint() {
    return new Point2D.Double(0, 0);
  }

  /**
   * create and return a new Point2D with (x, y)
   *
   * @param x
   * @param y
   * @return
   */
  @Override
  public Point2D newPoint(double x, double y) {
    return new Point2D.Double(x, y);
  }

  /**
   * create and return a new P with (x, y, z)
   *
   * @param x
   * @param y
   * @param z ignored for Point2D
   * @return
   */
  @Override
  public Point2D newPoint(double x, double y, double z) {
    return new Point2D.Double(x, y);
  }

  /**
   * returns the distance between p and the origin
   *
   * @param p
   * @return
   */
  @Override
  public double distance(Point2D p) {
    return p.distance(0, 0);
  }

  /**
   * offset the (x,y) coordinates of p by the passed values
   *
   * @param p
   * @param x
   * @param y
   */
  @Override
  public void offset(Point2D p, double x, double y) {
    p.setLocation(p.getX() + x, p.getY() + y);
  }

  /**
   * offset the (x,y,z) coordinates of p by the passed values
   *
   * @param p
   * @param x
   * @param y
   * @param z ignored for Point2D
   */
  @Override
  public void offset(Point2D p, double x, double y, double z) {
    p.setLocation(p.getX() + x, p.getY() + y);
  }
}
