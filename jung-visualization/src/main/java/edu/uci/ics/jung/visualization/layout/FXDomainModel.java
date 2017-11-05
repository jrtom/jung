package edu.uci.ics.jung.visualization.layout;

import javafx.geometry.Point2D;

/**
 * An implementation of DomainModel using javafx.geometry.Point2D
 *
 * @author Tom Nelson
 */
public class FXDomainModel implements DomainModel<Point2D> {

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
  @Override
  public void setLocation(Point2D p, double x, double y) {
    throw new UnsupportedOperationException(p + " is immutable");
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
    throw new UnsupportedOperationException(p + " is immutable");
  }

  /**
   * set the (x,y) coordinates of p to those of 'from'
   *
   * @param p
   * @param from source of new coordinates for P
   */
  @Override
  public void setLocation(Point2D p, Point2D from) {
    throw new UnsupportedOperationException(p + " is immutable");
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
    double a = p.getX() - q.getX();
    double b = p.getY() - q.getY();
    return a * a + b * b;
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
    return new Point2D(x, y);
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
    return new Point2D(x, y);
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
   * @throws UnsupportedOperationException for immutable Point2D
   */
  @Override
  public void offset(Point2D p, double x, double y) {
    throw new UnsupportedOperationException(p + " is immutable");
  }

  /**
   * offset the (x,y,z) coordinates of p by the passed values
   *
   * @param p
   * @param x
   * @param y
   * @param z ignored for Point2D
   * @throws UnsupportedOperationException for immutable Point2D
   */
  @Override
  public void offset(Point2D p, double x, double y, double z) {
    throw new UnsupportedOperationException(p + " is immutable");
  }
}
