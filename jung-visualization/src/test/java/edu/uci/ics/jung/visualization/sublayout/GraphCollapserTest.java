package edu.uci.ics.jung.visualization.sublayout;

import com.google.common.collect.Sets;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Nelson
 */
public class GraphCollapserTest {

  Logger log = LoggerFactory.getLogger(GraphCollapserTest.class);

  @Test
  public void testCollapser() {
    Network network = getDemoGraph();

    Assert.assertEquals(network.nodes(), Sets.newHashSet("A", "B", "C"));
    Assert.assertEquals(network.incidentNodes(0), EndpointPair.unordered("B", "A"));
    Assert.assertEquals(network.incidentNodes(1), EndpointPair.unordered("C", "A"));
    Assert.assertEquals(network.incidentNodes(2), EndpointPair.unordered("B", "C"));

    GraphCollapser collapser = new GraphCollapser(network);
    MultiPickedState picker = new MultiPickedState();
    picker.pick("B", true);
    picker.pick("C", true);

    Network clusterGraph = collapser.getClusterGraph(network, picker.getPicked());
    Network collapsed = collapser.collapse(network, clusterGraph);
    for (Object node : collapsed.nodes()) {
      if (node instanceof Network) {
        Assert.assertEquals(((Network) node).edges(), Sets.newHashSet(2));
      } else {
        Assert.assertEquals(node, "A");
      }
    }

    Assert.assertEquals(collapsed.edges(), Sets.newHashSet(0, 1));
    for (Object edge : collapsed.edges()) {
      Assert.assertEquals(collapsed.incidentNodes(edge).nodeU(), "A");
      Assert.assertTrue(collapsed.incidentNodes(edge).nodeV() instanceof Network);
    }

    Collection nodes = collapsed.nodes();
    picker.clear();
    for (Object node : collapsed.nodes()) {
      if (node instanceof Network) {
        picker.pick(node, true);
      }
    }
    Network expanded = collapser.expand(network, collapsed, clusterGraph);
    Assert.assertEquals(network.nodes(), Sets.newHashSet("A", "B", "C"));
    Assert.assertEquals(expanded.incidentNodes(0), EndpointPair.unordered("B", "A"));
    Assert.assertEquals(expanded.incidentNodes(1), EndpointPair.unordered("C", "A"));
    Assert.assertEquals(expanded.incidentNodes(2), EndpointPair.unordered("B", "C"));
  }

  @Test
  public void testTwoConnectedClustersExpandOneThenTheOther() {
    Network originalNetwork = getDemoGraph2();
    GraphCollapser collapser = new GraphCollapser(originalNetwork);
    MultiPickedState picker = new MultiPickedState();
    picker.pick("A", true);
    picker.pick("B", true);
    picker.pick("C", true);

    log.debug("originalNetwork:" + originalNetwork);

    Network clusterNodeOne = collapser.getClusterGraph(originalNetwork, picker.getPicked());
    Network collapsedGraphOne = collapser.collapse(originalNetwork, clusterNodeOne);

    log.debug("collapsedGraphOne:" + collapsedGraphOne);

    picker.clear();
    picker.pick("D", true);
    picker.pick("E", true);
    picker.pick("F", true);

    Network clusterNodeTwo = collapser.getClusterGraph(collapsedGraphOne, picker.getPicked());
    Network collapsedGraphTwo = collapser.collapse(collapsedGraphOne, clusterNodeTwo);

    log.debug("collapsedGraphTwo:" + collapsedGraphTwo);

    Network expanded = collapser.expand(originalNetwork, collapsedGraphTwo, clusterNodeTwo);

    Assert.assertEquals(expanded, collapsedGraphOne);

    Network expandedAgain = collapser.expand(originalNetwork, expanded, clusterNodeOne);

    Assert.assertEquals(expandedAgain, originalNetwork);
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

  public static Network<String, Number> getDemoGraph2() {
    MutableNetwork<String, Number> g =
        NetworkBuilder.undirected().allowsParallelEdges(true).build();

    createEdge(g, "A", "B", 0);
    createEdge(g, "A", "C", 1);
    createEdge(g, "B", "C", 2);

    createEdge(g, "D", "E", 3);
    createEdge(g, "D", "F", 4);
    createEdge(g, "E", "F", 5);

    createEdge(g, "B", "D", 6);

    createEdge(g, "A", "G", 7);

    return g;
  }
}
