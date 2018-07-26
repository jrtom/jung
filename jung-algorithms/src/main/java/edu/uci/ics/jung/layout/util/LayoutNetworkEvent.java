package edu.uci.ics.jung.layout.util;

import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.model.Point;

/**
 * An LayoutEvent that also includes a reference to the Network
 *
 * @author Tom Nelson
 * @param <N>
 */
public class LayoutNetworkEvent<N> extends LayoutEvent<N> {

  final Network<N, ?> network;

  public LayoutNetworkEvent(N node, Network<N, ?> network, Point location) {
    super(node, location);
    this.network = network;
  }

  public LayoutNetworkEvent(LayoutEvent<N> layoutEvent, Network<N, ?> network) {
    super(layoutEvent.getNode(), layoutEvent.location);
    this.network = network;
  }

  public Network<N, ?> getNetwork() {
    return this.network;
  }
}
