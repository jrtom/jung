/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform;

import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.model.PolarPoint;
import java.awt.*;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MagnifyTransformer wraps a MutableAffineTransformer and modifies the transform and
 * inverseTransform methods so that they create an enlarging projection of the graph points.
 *
 * <p>MagnifyTransformer uses an affine transform to cause translation, scaling, rotation, and
 * shearing while applying a separate magnification filter in its transform and inverseTransform
 * methods.
 *
 * @author Tom Nelson
 */
public class MagnifyTransformer extends LensTransformer implements MutableTransformer {

  private static final Logger log = LoggerFactory.getLogger(MagnifyTransformer.class);

  /**
   * Create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component.
   *
   * @param d the size used for the lens
   */
  public MagnifyTransformer(Dimension d) {
    this(d, new MutableAffineTransformer());
  }

  public MagnifyTransformer(Lens lens) {
    this(lens, new MutableAffineTransformer());
  }

  /**
   * Create an instance with a possibly shared transform.
   *
   * @param d the size used for the lens
   * @param delegate the transformer to use
   */
  public MagnifyTransformer(Dimension d, MutableTransformer delegate) {
    super(d, delegate);
  }

  public MagnifyTransformer(Lens lens, MutableTransformer delegate) {
    super(lens, delegate);
  }

  /** override base class transform to project the fisheye effect */
  public Point2D transform(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    Point2D viewCenter = lens.getCenter();
    double viewRadius = lens.getRadius();
    double ratio = lens.getRatio();
    // transform the point from the graph to the view
    Point2D viewPoint = delegate.transform(graphPoint);
    // calculate point from center
    double dx = viewPoint.getX() - viewCenter.getX();
    double dy = viewPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;
    Point pointFromCenter = Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
    double theta = polar.theta;
    double radius = polar.radius;
    if (radius > viewRadius) {
      return viewPoint;
    }

    double mag = lens.getMagnification();
    radius *= mag;

    radius = Math.min(radius, viewRadius);
    Point projectedPoint = PolarPoint.polarToCartesian(theta, radius);
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y);
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.x + viewCenter.getX(), projectedPoint.y + viewCenter.getY());
    return translatedBack;
  }

  /** override base class to un-project the fisheye effect */
  public Point2D inverseTransform(Point2D viewPoint) {

    Point2D viewCenter = lens.getCenter();
    double viewRadius = lens.getRadius();
    double ratio = lens.getRatio();
    double dx = viewPoint.getX() - viewCenter.getX();
    double dy = viewPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;

    Point pointFromCenter = Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);

    double radius = polar.radius;
    if (radius > viewRadius) {
      return delegate.inverseTransform(viewPoint);
    }

    double mag = lens.getMagnification();
    radius /= mag;
    polar = polar.newRadius(radius);
    Point projectedPoint = PolarPoint.polarToCartesian(polar);
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y);
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.x + viewCenter.getX(), projectedPoint.y + viewCenter.getY());
    return delegate.inverseTransform(translatedBack);
  }

  /**
   * Magnifies the point, without considering the Lens.
   *
   * @param graphPoint the point to transform via magnification
   * @return the transformed point
   */
  public Point2D magnify(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    Point2D viewCenter = lens.getCenter();
    double ratio = lens.getRatio();
    // transform the point from the graph to the view
    Point2D viewPoint = graphPoint;
    // calculate point from center
    double dx = viewPoint.getX() - viewCenter.getX();
    double dy = viewPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;
    Point pointFromCenter = Point.of(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(pointFromCenter);
    double theta = polar.theta;
    double radius = polar.radius;

    double mag = lens.getMagnification();
    radius *= mag;

    //        radius = Math.min(radius, viewRadius);
    Point projectedPoint = PolarPoint.polarToCartesian(theta, radius);
    projectedPoint = Point.of(projectedPoint.x / ratio, projectedPoint.y);
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.x + viewCenter.getX(), projectedPoint.y + viewCenter.getY());
    return translatedBack;
  }
}
