package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.layout.model.LayoutModel;

/**
 * LayoutAlgorithm is a visitor to the LayoutModel. When it visits, it runs the algorithm to place
 * the graph nodes at locations.
 *
 * @author Tom Nelson.
 */
public interface LayoutAlgorithm<N> {

  /**
   * visit the passed layoutModel and set its locations
   *
   * @param layoutModel the mediator between the container for nodes (the Graph) and the mapping
   *     from Node to Point
   */
  void visit(LayoutModel<N> layoutModel);
}
