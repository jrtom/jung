package edu.uci.ics.jung.layout.util;

import com.google.common.graph.Graph;

/**
 * A LayoutEvent that also includes a reference to the Graph
 *
 * @param <N>
 * @param <P>
 */
public class LayoutGraphEvent<N, P> extends LayoutEvent<N, P> {

  final Graph<N> graph;

  public LayoutGraphEvent(N node, Graph<N> graph, P location) {
    super(node, location);
    this.graph = graph;
  }

  public LayoutGraphEvent(LayoutEvent<N, P> layoutEvent, Graph<N> graph) {
    super(layoutEvent.getNode(), layoutEvent.location);
    this.graph = graph;
  }

  public Graph<N> getGraph() {
    return graph;
  }
}
