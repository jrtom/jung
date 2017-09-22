package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Network;

public class LayoutEvent<V, E> {

  V vertex;
  Network<V, E> graph;

  public LayoutEvent(V vertex, Network<V, E> graph) {
    this.vertex = vertex;
    this.graph = graph;
  }

  public V getVertex() {
    return vertex;
  }

  public void setVertex(V vertex) {
    this.vertex = vertex;
  }

  public Network<V, E> getGraph() {
    return graph;
  }

  public void setGraph(Network<V, E> graph) {
    this.graph = graph;
  }
}
