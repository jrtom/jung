package edu.uci.ics.jung.algorithms.layout;

/**
 * LayoutAlgorithm is a visitor to the LayoutModel. When it visits, it runs the algorithm to place
 * the graph nodes at locations.
 *
 * @author Tom Nelson.
 */
public interface LayoutAlgorithm<N, P> {

  /**
   * visit the passed layoutModel and set its locations
   *
   * @param layoutModel
   */
  void visit(LayoutModel<N, P> layoutModel);

  void reset(); // remove?
}
