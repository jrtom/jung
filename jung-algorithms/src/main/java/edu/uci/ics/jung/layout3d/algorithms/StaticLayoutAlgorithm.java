package edu.uci.ics.jung.layout3d.algorithms;

import edu.uci.ics.jung.layout.algorithms.AbstractLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;

/**
 * StaticLayout leaves the nodes in the locations specified in the LayoutModel, and has no other
 * behavior.
 *
 * @author Tom Nelson
 */
public class StaticLayoutAlgorithm<N, P> extends AbstractLayoutAlgorithm<N, P> {

  public StaticLayoutAlgorithm(PointModel<P> pointModel) {
    super(pointModel);
  }

  public void visit(LayoutModel<N, P> layoutModel) {}

  public void initialize() {}

  public void reset() {}
}
