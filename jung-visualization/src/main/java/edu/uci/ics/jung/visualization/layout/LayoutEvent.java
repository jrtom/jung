package edu.uci.ics.jung.visualization.layout;

import edu.uci.ics.jung.graph.Graph;

public class LayoutEvent<V,E> {
	
	V vertex;
	Graph<V,E> graph;

	public LayoutEvent(V vertex, Graph<V, E> graph) {
		this.vertex = vertex;
		this.graph = graph;
	}
	public V getVertex() {
		return vertex;
	}
	public void setVertex(V vertex) {
		this.vertex = vertex;
	}
	public Graph<V, E> getGraph() {
		return graph;
	}
	public void setGraph(Graph<V, E> graph) {
		this.graph = graph;
	}
}
