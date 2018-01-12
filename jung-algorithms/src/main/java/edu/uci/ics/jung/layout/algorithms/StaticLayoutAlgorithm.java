package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.layout.model.LayoutModel;

/**
 * StaticLayout leaves the nodes in the locations specified in the LayoutModel, and has no other
 * behavior.
 *
 * @author Tom Nelson
 */
public class StaticLayoutAlgorithm<N> implements LayoutAlgorithm<N> {

  /**
   * a no-op, as the Node locations are unchanged from where they are in the layoutModel
   *
   * @param layoutModel the mediator between the container for nodes (the Graph) and the mapping
   *     from Node to Point
   */
  @Override
  public void visit(LayoutModel<N> layoutModel) {}
}
