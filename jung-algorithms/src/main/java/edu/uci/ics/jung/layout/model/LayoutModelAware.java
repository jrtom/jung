package edu.uci.ics.jung.layout.model;

import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;

/** @author Tom Nelson */
public interface LayoutModelAware<N, E, P> {

  Network<N, E> getNetwork();

  void setLayoutAlgorithm(LayoutAlgorithm<N, P> layoutAlgorithm);

  LayoutAlgorithm<N, P> getLayoutAlgorithm();

  LayoutModel<N, P> getLayoutModel();
}
