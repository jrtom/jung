package edu.uci.ics.jung.visualization.util;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;

/** Created by Tom Nelson on 9/22/17. */
public class LayoutMediator<N, E> {

  private final Network<N, E> network;

  private final Layout<N> layout;

  public LayoutMediator(Network<N, E> network, Layout<N> layout) {
    this.network = network;
    this.layout = layout;
  }

  public Network<N, E> getNetwork() {
    return network;
  }

  public Layout<N> getLayout() {
    return layout;
  }
}
