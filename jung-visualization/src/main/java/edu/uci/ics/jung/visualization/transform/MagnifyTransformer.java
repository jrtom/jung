/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.visualization.transform;

import edu.uci.ics.jung.algorithms.layout.DomainModel;
import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.visualization.layout.AWTDomainModel;
import java.awt.Component;
import java.awt.geom.Point2D;

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

  protected DomainModel<Point2D> domainModel = new AWTDomainModel();
  /**
   * Create an instance, setting values from the passed component and registering to listen for
   * layoutSize changes on the component.
   *
   * @param component the component used for rendering
   */
  public MagnifyTransformer(Component component) {
    this(component, new MutableAffineTransformer());
  }

  /**
   * Create an instance with a possibly shared transform.
   *
   * @param component the component used for rendering
   * @param delegate the transformer to use
   */
  public MagnifyTransformer(Component component, MutableTransformer delegate) {
    super(component, delegate);
    this.magnification = 3.f;
  }

  /** override base class transform to project the fisheye effect */
  public Point2D transform(Point2D graphPoint) {
    if (graphPoint == null) {
      return null;
    }
    Point2D viewCenter = getViewCenter();
    double viewRadius = getViewRadius();
    double ratio = getRatio();
    // transform the point from the graph to the view
    Point2D viewPoint = delegate.transform(graphPoint);
    // calculate point from center
    double dx = viewPoint.getX() - viewCenter.getX();
    double dy = viewPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;
    Point2D pointFromCenter = new Point2D.Double(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(domainModel, pointFromCenter);
    double theta = polar.getTheta();
    double radius = polar.getRadius();
    if (radius > viewRadius) {
      return viewPoint;
    }

    double mag = magnification;
    radius *= mag;

    radius = Math.min(radius, viewRadius);
    Point2D projectedPoint = PolarPoint.polarToCartesian(domainModel, theta, radius);
    projectedPoint.setLocation(projectedPoint.getX() / ratio, projectedPoint.getY());
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.getX() + viewCenter.getX(), projectedPoint.getY() + viewCenter.getY());
    return translatedBack;
  }

  /** override base class to un-project the fisheye effect */
  public Point2D inverseTransform(Point2D viewPoint) {

    Point2D viewCenter = getViewCenter();
    double viewRadius = getViewRadius();
    double ratio = getRatio();
    double dx = viewPoint.getX() - viewCenter.getX();
    double dy = viewPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;

    Point2D pointFromCenter = new Point2D.Double(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(domainModel, pointFromCenter);

    double radius = polar.getRadius();
    if (radius > viewRadius) {
      return delegate.inverseTransform(viewPoint);
    }

    double mag = magnification;
    radius /= mag;
    polar.setRadius(radius);
    Point2D projectedPoint = PolarPoint.polarToCartesian(domainModel, polar);
    projectedPoint.setLocation(projectedPoint.getX() / ratio, projectedPoint.getY());
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.getX() + viewCenter.getX(), projectedPoint.getY() + viewCenter.getY());
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
    Point2D viewCenter = getViewCenter();
    double ratio = getRatio();
    // transform the point from the graph to the view
    Point2D viewPoint = graphPoint;
    // calculate point from center
    double dx = viewPoint.getX() - viewCenter.getX();
    double dy = viewPoint.getY() - viewCenter.getY();
    // factor out ellipse
    dx *= ratio;
    Point2D pointFromCenter = new Point2D.Double(dx, dy);

    PolarPoint polar = PolarPoint.cartesianToPolar(domainModel, pointFromCenter);
    double theta = polar.getTheta();
    double radius = polar.getRadius();

    double mag = magnification;
    radius *= mag;

    //        radius = Math.min(radius, viewRadius);
    Point2D projectedPoint = PolarPoint.polarToCartesian(domainModel, theta, radius);
    projectedPoint.setLocation(projectedPoint.getX() / ratio, projectedPoint.getY());
    Point2D translatedBack =
        new Point2D.Double(
            projectedPoint.getX() + viewCenter.getX(), projectedPoint.getY() + viewCenter.getY());
    return translatedBack;
  }
}
