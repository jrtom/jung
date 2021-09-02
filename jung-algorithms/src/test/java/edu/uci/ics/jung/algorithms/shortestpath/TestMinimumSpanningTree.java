package edu.uci.ics.jung.algorithms.shortestpath;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import junit.framework.TestCase;

public class TestMinimumSpanningTree extends TestCase {

  public void testSimpleTree() {
    MutableNetwork<String, Integer> tree = NetworkBuilder.undirected().build();
    tree.addNode("A");
    tree.addEdge("A", "B0", 0);
    tree.addEdge("A", "B1", 1);

    Network<String, Integer> mst = MinimumSpanningTree.extractFrom(tree, e -> 1.0);

    assertEquals(tree.nodes(), mst.nodes());
    assertEquals(tree.edges(), mst.edges());
    assertEquals(tree, mst);
  }

  public void testDAG() {
    MutableNetwork<String, Integer> graph = NetworkBuilder.directed().build();
    graph.addNode("B0");
    graph.addEdge("A", "B0", 0);
    graph.addEdge("A", "B1", 1);

    Network<String, Integer> mst = MinimumSpanningTree.extractFrom(graph, e -> 1.0);

    assertEquals(graph.nodes(), mst.nodes());
    assertEquals(graph.edges(), mst.edges());

    // Add more tests.
    MutableNetwork<String, Integer> graph2 = NetworkBuilder.directed().build();
    graph2.addNode("B0");
    graph2.addNode("B2");
    graph2.addNode("B3");
    graph2.addEdge("A", "B1", 0);
    graph2.addEdge("A", "B2", 3);
    graph2.addEdge("A", "B3", 5);

    Network<String, Integer> mst2 = MinimumSpanningTree.extractFrom(graph2, e -> 1.0);

    // Test mutableNetwork.
    MutableNetwork<String, Integer> graph3 = NetworkBuilder.directed().build();

    assertEquals(graph2.nodes(), mst2.nodes());
    assertEquals(graph2.edges(), mst2.edges());
  }

  // TODO: add tests:
  // - cycle
  // - multiple components
  // - self-loops
}
