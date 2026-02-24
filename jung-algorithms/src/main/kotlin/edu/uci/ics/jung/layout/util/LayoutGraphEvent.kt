package edu.uci.ics.jung.layout.util

import com.google.common.graph.Graph
import edu.uci.ics.jung.layout.model.Point

/**
 * A LayoutEvent that also includes a reference to the Graph
 *
 * @param N
 */
class LayoutGraphEvent<N : Any> : LayoutEvent<N> {

  val graph: Graph<N>

  constructor(node: N, graph: Graph<N>, location: Point) : super(node, location) {
    this.graph = graph
  }

  constructor(layoutEvent: LayoutEvent<N>, graph: Graph<N>) : super(layoutEvent.node, layoutEvent.location) {
    this.graph = graph
  }
}
