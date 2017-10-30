package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Network;

public class LayoutEvent<V, E> {

  final V vertex;
  final Network<V, E> graph;

  public LayoutEvent(V vertex, Network<V, E> graph) {
    this.vertex = vertex;
    this.graph = graph;
  }

  public V getVertex() {
    return vertex;
  }

  public Network<V, E> getGraph() {
    return graph;
  }
}
