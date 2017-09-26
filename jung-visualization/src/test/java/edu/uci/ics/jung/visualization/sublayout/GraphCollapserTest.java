package edu.uci.ics.jung.visualization.sublayout;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import java.util.Collection;
import org.junit.Test;

/** Created by tanelso on 9/26/17. */
public class GraphCollapserTest {

  @Test
  public void testCollapser() {
    Network network = getDemoGraph();
    System.err.println("nodes " + network.nodes());

    System.err.println("edges " + network.edges());
    for (Object edge : network.edges()) {
      System.err.println("edge " + edge + " " + network.incidentNodes(edge));
    }

    GraphCollapser collapser = new GraphCollapser(network);
    MultiPickedState picker = new MultiPickedState();
    picker.pick("B", true);
    picker.pick("C", true);

    Network clusterGraph = collapser.getClusterGraph(network, picker.getPicked());
    Network collapsed = collapser.collapse(network, clusterGraph);
    System.err.println("nodes " + collapsed.nodes());

    System.err.println("edges " + collapsed.edges());
    for (Object edge : collapsed.edges()) {
      System.err.println("edge " + edge + " " + collapsed.incidentNodes(edge));
    }

    Collection nodes = collapsed.nodes();
    picker.clear();
    for (Object node : collapsed.nodes()) {
      if (node instanceof Network) {
        picker.pick(node, true);
      }
    }
    Network expanded = collapser.expand(network, clusterGraph);
    System.err.println("nodes " + expanded.nodes());

    System.err.println("edges " + expanded.edges());
    for (Object edge : expanded.edges()) {
      System.err.println("edge " + edge + " " + expanded.incidentNodes(edge));
    }
  }

  @Test
  public void biggerGraph() {
    Network network = TestGraphs.getOneComponentGraph();
    System.err.println("nodes " + network.nodes());

    System.err.println("edges " + network.edges());
    for (Object edge : network.edges()) {
      System.err.println("edge " + edge + " " + network.incidentNodes(edge));
    }

    GraphCollapser collapser = new GraphCollapser(network);
    MultiPickedState picker = new MultiPickedState();
    picker.pick("2", true);
    picker.pick("3", true);

    Network clusterGraph = collapser.getClusterGraph(network, picker.getPicked());
    Network collapsed = collapser.collapse(network, clusterGraph);
    System.err.println("nodes " + collapsed.nodes());

    System.err.println("edges " + collapsed.edges());
    for (Object edge : collapsed.edges()) {
      System.err.println("edge " + edge + " " + collapsed.incidentNodes(edge));
    }

    Collection nodes = collapsed.nodes();
    picker.clear();
    for (Object node : collapsed.nodes()) {
      if (node instanceof Network) {
        picker.pick(node, true);
      }
    }
    Network expanded = collapser.expand(network, clusterGraph);
    System.err.println("nodes " + expanded.nodes());

    System.err.println("edges " + expanded.edges());
    for (Object edge : expanded.edges()) {
      System.err.println("edge " + edge + " " + expanded.incidentNodes(edge));
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
