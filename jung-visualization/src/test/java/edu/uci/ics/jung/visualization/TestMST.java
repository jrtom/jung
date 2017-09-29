package edu.uci.ics.jung.visualization;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningTree;
import edu.uci.ics.jung.graph.util.TreeUtils;
import org.junit.Test;

/** Created by Tom Nelson */
public class TestMST {

  @Test
  public void testFromGraph() {
    Network<String, Number> network = getDemoGraph();
    System.err.println("network:" + network);
    Network<String, Number> tree = MinimumSpanningTree.extractFrom(network, e -> 1.0);
    System.err.println("tree:" + tree);
    System.err.println("tree roots are " + TreeUtils.<String>roots(tree.asGraph()));
    Layout<String> layout = new TreeLayout<String>(tree.asGraph());
    for (String node : network.nodes()) {
      System.err.println(node + " is at " + layout.apply(node));
      System.err.println(node + " has successors " + tree.successors(node));
      System.err.println(node + " has predecessors " + tree.predecessors(node));
    }
  }

  private static void createEdge(
      MutableNetwork<String, Number> g, String v1Label, String v2Label, int weight) {
    g.addEdge(v1Label, v2Label, weight);
  }

  public static Network<String, Number> getDemoGraph() {
    MutableNetwork<String, Number> g =
        NetworkBuilder.undirected().allowsParallelEdges(true).build();

    createEdge(g, "A", "B", 0);
    createEdge(g, "A", "C", 1);
    createEdge(g, "B", "C", 2);

    return g;
  }
}
