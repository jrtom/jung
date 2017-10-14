package edu.uci.ics.jung.visualization.util;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.Layout;

/** Created by Tom Nelson */
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

  @Override
  public String toString() {
    return "LayoutMediator{" + "networkNodes=" + network.nodes() + ", layout=" + layout + '}';
  }
}
