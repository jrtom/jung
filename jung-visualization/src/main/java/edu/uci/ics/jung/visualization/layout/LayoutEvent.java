package edu.uci.ics.jung.visualization.layout;

import com.google.common.graph.Graph;

public class LayoutEvent<V> {

  V vertex;
  Graph<V> graph;

  public LayoutEvent(V vertex, Graph<V> graph) {
    this.vertex = vertex;
    this.graph = graph;
  }

  public V getVertex() {
    return vertex;
  }

  public void setVertex(V vertex) {
    this.vertex = vertex;
  }

  public Graph<V> getGraph() {
    return graph;
  }

  public void setGraph(Graph<V> graph) {
    this.graph = graph;
  }
}
