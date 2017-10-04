package edu.uci.ics.jung.algorithms.shortestpath;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import junit.framework.TestCase;

public class TestMinimumSpanningTree extends TestCase {

  public void testSimpleTree() {
    MutableCTreeNetwork<String, Integer> tree = TreeNetworkBuilder.builder().build();
    tree.addNode("A");
    tree.addEdge("A", "B0", 0);
    tree.addEdge("A", "B1", 1);

    Network<String, Integer> mst = MinimumSpanningTree.extractFrom(tree, e -> 1.0);

    assertEquals(tree.nodes(), mst.nodes());
    assertEquals(tree.edges(), mst.edges());
    assertEquals(tree.asGraph(), mst.asGraph());
  }

  public void testDAG() {
    MutableNetwork<String, Integer> graph = NetworkBuilder.directed().build();
    graph.addNode("B0");
    graph.addEdge("A", "B0", 0);
    graph.addEdge("A", "B1", 1);

    Network<String, Integer> mst = MinimumSpanningTree.extractFrom(graph, e -> 1.0);

    assertEquals(graph.nodes(), mst.nodes());
    assertEquals(graph.edges(), mst.edges());
  }

  // TODO: add tests:
  // - cycle
  // - multiple components
  // - self-loops
}
