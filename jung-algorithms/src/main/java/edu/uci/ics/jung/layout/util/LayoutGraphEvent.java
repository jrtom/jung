package edu.uci.ics.jung.layout.util;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.layout.model.Point;

/**
 * A LayoutEvent that also includes a reference to the Graph
 *
 * @param <N>
 */
public class LayoutGraphEvent<N> extends LayoutEvent<N> {

  final Graph<N> graph;

  public LayoutGraphEvent(N node, Graph<N> graph, Point location) {
    super(node, location);
    this.graph = graph;
  }

  public LayoutGraphEvent(LayoutEvent<N> layoutEvent, Graph<N> graph) {
    super(layoutEvent.getNode(), layoutEvent.location);
    this.graph = graph;
  }

  public Graph<N> getGraph() {
    return graph;
  }
}
