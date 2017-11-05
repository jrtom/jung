package edu.uci.ics.jung.algorithms.layout;

/**
 * AbstractLayoutAlgorithm holds a reference to a DomainModel
 *
 * @author Tom Nelson
 */
public abstract class AbstractLayoutAlgorithm<N, P> implements LayoutAlgorithm<N, P> {

  /** the model to abstract the point system being used (AWT, FX, etc) */
  protected final DomainModel<P> domainModel;

  /**
   * create an instance using the passed domainModel
   *
   * @param domainModel
   */
  protected AbstractLayoutAlgorithm(DomainModel<P> domainModel) {
    this.domainModel = domainModel;
  }
}
