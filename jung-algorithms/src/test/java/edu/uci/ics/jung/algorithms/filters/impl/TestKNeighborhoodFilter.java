package edu.uci.ics.jung.algorithms.filters.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import junit.framework.TestCase;

public class TestKNeighborhoodFilter extends TestCase {

  MutableGraph<Number> graph;

  // TODO: test multiple root nodes
  public void testDirected() {
    graph = GraphBuilder.directed().allowsSelfLoops(true).build();
    populateGraph(graph);
    MutableGraph<Number> expected = GraphBuilder.directed().allowsSelfLoops(true).build();
    expected.putEdge(0, 1);
    expected.putEdge(0, 2);
    expected.putEdge(2, 3);
    expected.putEdge(2, 4);
    expected.putEdge(3, 0);
    expected.putEdge(3, 3);

    Graph<Number> filtered =
        KNeighborhoodFilter.filterGraph(ImmutableGraph.copyOf(graph), ImmutableSet.of(0), 2);

    assertEquals(expected, filtered);
  }

  public void testUndirected() {
    graph = GraphBuilder.undirected().allowsSelfLoops(true).build();
    populateGraph(graph);
    MutableGraph<Number> expected = GraphBuilder.undirected().allowsSelfLoops(true).build();
    expected.putEdge(0, 1);
    expected.putEdge(0, 2);
    expected.putEdge(2, 3);
    expected.putEdge(2, 4);
    expected.putEdge(0, 3);
    expected.putEdge(3, 5);
    expected.putEdge(5, 0);
    expected.putEdge(5, 6);
    expected.putEdge(3, 3);

    Graph<Number> filtered =
        KNeighborhoodFilter.filterGraph(ImmutableGraph.copyOf(graph), ImmutableSet.of(0), 2);

    assertEquals(expected, filtered);
  }

  private void populateGraph(MutableGraph<Number> graph) {
    graph.putEdge(0, 1);
    graph.putEdge(0, 2);
    graph.putEdge(2, 3);
    graph.putEdge(2, 4);
    graph.putEdge(3, 5);
    graph.putEdge(5, 6);
    graph.putEdge(5, 0);
    graph.putEdge(3, 0);
    graph.putEdge(6, 7);
    graph.putEdge(3, 3);
    graph.addNode(8);
  }
}
