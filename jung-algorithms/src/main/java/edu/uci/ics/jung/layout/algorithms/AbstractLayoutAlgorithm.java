package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;

/**
 * AbstractLayoutAlgorithm holds a reference to a PointModel
 *
 * @author Tom Nelson
 */
public abstract class AbstractLayoutAlgorithm<N, P> implements LayoutAlgorithm<N, P> {

  /** the model to abstract the point system being used (AWT, FX, etc) */
  protected PointModel<P> pointModel;

  public void visit(LayoutModel<N, P> layoutModel) {
    this.pointModel = layoutModel.getPointModel();
  }

  public void reset() {}
}
