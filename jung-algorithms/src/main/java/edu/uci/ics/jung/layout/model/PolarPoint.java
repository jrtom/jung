/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.model;

/**
 * Represents a point in polar coordinates: distance and angle from the origin. Includes conversions
 * between polar and Cartesian coordinates (Point2D).
 *
 * @author Tom Nelson
 */
public class PolarPoint {
  private final double theta;
  private final double radius;

  public static PolarPoint ORIGIN = new PolarPoint();

  public static PolarPoint of(double theta, double radius) {
    return new PolarPoint(theta, radius);
  }

  /** Creates a new instance with radius and angle each 0. */
  private PolarPoint() {
    this(0, 0);
  }

  /**
   * Creates a new instance with the specified radius and angle.
   *
   * @param theta the angle of the point to create
   * @param radius the distance from the origin of the point to create
   */
  private PolarPoint(double theta, double radius) {
    this.theta = theta;
    this.radius = radius;
  }

  /** @return the angle for this point */
  public double getTheta() {
    return theta;
  }

  /** @return the radius for this point */
  public double getRadius() {
    return radius;
  }

  public PolarPoint setRadius(double radius) {
    return PolarPoint.of(theta, radius);
  }

  /**
   * @param polar the input location to convert
   * @return the result of converting <code>polar</code> to Cartesian coordinates.
   */
  public static Point polarToCartesian(PolarPoint polar) {
    return polarToCartesian(polar.getTheta(), polar.getRadius());
  }

  /**
   * @param theta the angle of the input location
   * @param radius the distance from the origin of the input location
   * @return the result of converting <code>(theta, radius)</code> to Cartesian coordinates.
   */
  public static Point polarToCartesian(double theta, double radius) {
    return Point.of(radius * Math.cos(theta), radius * Math.sin(theta));
  }

  /**
   * @param point the input location
   * @return the result of converting <code>point</code> to polar coordinates.
   */
  public static PolarPoint cartesianToPolar(Point point) {
    return cartesianToPolar(point.x, point.y);
  }

  /**
   * @param x the x coordinate of the input location
   * @param y the y coordinate of the input location
   * @return the result of converting <code>(x, y)</code> to polar coordinates.
   */
  public static PolarPoint cartesianToPolar(double x, double y) {
    double theta = Math.atan2(y, x);
    double radius = Math.sqrt(x * x + y * y);
    return new PolarPoint(theta, radius);
  }

  @Override
  public String toString() {
    return "PolarPoint[" + radius + "," + theta + "]";
  }
}
