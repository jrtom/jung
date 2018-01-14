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

import java.util.Objects;

/**
 * Immutable Point. Represents a point in polar coordinates: distance and angle from the origin.
 * Includes conversions between polar and Cartesian coordinates (Point).
 *
 * @author Tom Nelson
 */
public class PolarPoint {
  public final double theta;
  public final double radius;

  public static PolarPoint ORIGIN = new PolarPoint(0, 0);

  public static PolarPoint of(double theta, double radius) {
    return new PolarPoint(theta, radius);
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

  public PolarPoint newRadius(double radius) {
    return PolarPoint.of(theta, radius);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PolarPoint)) {
      return false;
    }

    PolarPoint other = (PolarPoint) o;

    return (Double.compare(other.theta, theta) == 0 && Double.compare(other.radius, radius) == 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(theta, radius);
  }

  /**
   * @param polar the input location to convert
   * @return the result of converting <code>polar</code> to Cartesian coordinates.
   */
  public static Point polarToCartesian(PolarPoint polar) {
    return polarToCartesian(polar.theta, polar.radius);
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
