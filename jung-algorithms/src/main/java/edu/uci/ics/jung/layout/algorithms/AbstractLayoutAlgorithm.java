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

  /**
   * create an instance using the passed pointModel
   *
   * @param pointModel
   */
  protected AbstractLayoutAlgorithm(PointModel<P> pointModel) {
    this.pointModel = pointModel;
  }

  public void reset() {}
}
