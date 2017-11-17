package edu.uci.ics.jung.layout.model;

/**
 * PointModel is the interface for interaction with various Point classes in a rendering framework.
 *
 * <p>It provides an API to work with whatever Point type is injected in the LayoutAlgorithm
 *
 * @param <P>
 * @author Tom Nelson
 */
public interface PointModel<P> {

  /**
   * return the x value of the passed P
   *
   * @param p
   * @return
   */
  double getX(P p);

  /**
   * return the y value of the passed P
   *
   * @param p
   * @return
   */
  double getY(P p);

  /**
   * return the z value of the passed P
   *
   * @param p
   * @return
   */
  double getZ(P p);

  /**
   * set the x and y attributes for the passed P
   *
   * @param p
   * @param x
   * @param y
   */
  void setLocation(P p, double x, double y);

  /**
   * set the x, y, z attributes of the passed P
   *
   * @param p
   * @param x
   * @param y
   * @param z
   */
  void setLocation(P p, double x, double y, double z);

  /**
   * set the (x,y) coordinates of p to those of 'from'
   *
   * @param p
   * @param from source of new coordinates for P
   */
  void setLocation(P p, P from);

  /**
   * returns the distance squared between p and q
   *
   * @param p
   * @param q
   * @return
   */
  double distanceSquared(P p, P q);

  /**
   * create a new point at the origin
   *
   * @return
   */
  P newPoint();

  /**
   * create and return a new P with (x, y)
   *
   * @param x
   * @param y
   * @return
   */
  P newPoint(double x, double y);

  /**
   * create and return a new P with (x, y, z)
   *
   * @param x
   * @param y
   * @param z
   * @return
   */
  P newPoint(double x, double y, double z);

  /**
   * returns the distance between p and the origin
   *
   * @param p
   * @return
   */
  double distance(P p);

  /**
   * offset the (x,y) coordinates of p by the passed values
   *
   * @param p
   * @param x
   * @param y
   */
  void offset(P p, double x, double y);

  /**
   * offset the (x,y,z) coordinates of p by the passed values
   *
   * @param p
   * @param x
   * @param y
   * @param z
   */
  void offset(P p, double x, double y, double z);
}
