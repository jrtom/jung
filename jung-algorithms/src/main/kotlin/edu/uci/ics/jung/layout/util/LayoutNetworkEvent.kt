package edu.uci.ics.jung.layout.util

import com.google.common.graph.Network
import edu.uci.ics.jung.layout.model.Point

/**
 * An LayoutEvent that also includes a reference to the Network
 *
 * @author Tom Nelson
 * @param N
 */
class LayoutNetworkEvent<N : Any> : LayoutEvent<N> {

  val network: Network<N, *>

  constructor(node: N, network: Network<N, *>, location: Point) : super(node, location) {
    this.network = network
  }

  constructor(layoutEvent: LayoutEvent<N>, network: Network<N, *>) : super(layoutEvent.node, layoutEvent.location) {
    this.network = network
  }
}
