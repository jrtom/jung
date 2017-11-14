package edu.uci.ics.jung.algorithms.layout;

/**
 * StaticLayout leaves the nodes in the locations specified in the LayoutModel, and has no other
 * behavior.
 *
 * @author Tom Nelson
 */
public class StaticLayoutAlgorithm<N, P> extends AbstractLayoutAlgorithm<N, P> {

  public StaticLayoutAlgorithm(DomainModel<P> domainModel) {
    super(domainModel);
  }

  public void visit(LayoutModel<N, P> layoutModel) {}

  public void initialize() {}

  public void reset() {}
}
