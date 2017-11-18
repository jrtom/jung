package edu.uci.ics.jung.layout.util;

import com.google.common.graph.Network;

/**
 * An LayoutEvent that also includes a reference to the Network
 *
 * @author Tom Nelson
 * @param <N>
 * @param <P>
 */
public class LayoutNetworkEvent<N, P> extends LayoutEvent<N, P> {

  final Network<N, ?> network;

  public LayoutNetworkEvent(N vertex, Network<N, ?> network, P location) {
    super(vertex, location);
    this.network = network;
  }

  public LayoutNetworkEvent(LayoutEvent<N, P> layoutEvent, Network<N, ?> network) {
    super(layoutEvent.getNode(), layoutEvent.location);
    this.network = network;
  }

  public Network<N, ?> getNetwork() {
    return this.network;
  }
}
