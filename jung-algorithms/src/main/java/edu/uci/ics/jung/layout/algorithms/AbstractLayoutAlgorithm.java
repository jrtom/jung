package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.layout.model.PointModel;

/**
 * AbstractLayoutAlgorithm holds a reference to a PointModel
 *
 * @author Tom Nelson
 */
public abstract class AbstractLayoutAlgorithm<N, P> implements LayoutAlgorithm<N, P> {

  /** the model to abstract the point system being used (AWT, FX, etc) */
  protected final PointModel<P> pointModel;

  //  protected P origin;

  /**
   * create an instance using the passed pointModel
   *
   * @param pointModel
   */
  protected AbstractLayoutAlgorithm(PointModel<P> pointModel) {
    this.pointModel = pointModel;
    //    this.origin = pointModel.newPoint();
  }

  //  /**
  //   * @param from
  //   * @param to
  //   * @return squared distance between points
  //   */
  //  protected double distanceSquared(P from, P to) {
  //    double deltaX = pointModel.getX(to) - pointModel.getX(from);
  //    double deltaY = pointModel.getY(to) - pointModel.getY(from);
  //    double deltaZ = pointModel.getZ(to) - pointModel.getZ(from);
  //    return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
  //  }
  //
  //  /**
  //   * @param from
  //   * @param to
  //   * @return distance between points
  //   */
  //  protected double distance(P from, P to) {
  //    return Math.sqrt(distanceSquared(from, to));
  //  }
  //
  //  protected double distance(P p) {
  //    return distance(p, origin);
  //  }

  public void reset() {}
}
