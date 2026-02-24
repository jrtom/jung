package edu.uci.ics.jung.visualization.sublayout

import com.google.common.collect.Sets
import com.google.common.graph.EndpointPair
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.visualization.picking.MultiPickedState
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser
import org.junit.Assert
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * @author Tom Nelson
 */
class GraphCollapserTest {

  val log = LoggerFactory.getLogger(GraphCollapserTest::class.java)

  @Test
  fun testCollapser() {
    val network: Network<Any, Any> = getDemoGraph()

    Assert.assertEquals(network.nodes(), Sets.newHashSet("A", "B", "C"))
    Assert.assertEquals(network.incidentNodes(0), EndpointPair.unordered("B", "A"))
    Assert.assertEquals(network.incidentNodes(1), EndpointPair.unordered("C", "A"))
    Assert.assertEquals(network.incidentNodes(2), EndpointPair.unordered("B", "C"))

    val collapser = GraphCollapser(network)
    val picker = MultiPickedState<Any>()
    picker.pick("B", true)
    picker.pick("C", true)

    val clusterGraph = collapser.getClusterGraph(network, picker.getPicked())
    @Suppress("UNCHECKED_CAST")
    val collapsed = collapser.collapse(network, clusterGraph) as Network<Any, Any>
    for (node in collapsed.nodes()) {
      if (node is Network<*, *>) {
        Assert.assertEquals(node.edges(), Sets.newHashSet(2))
      } else {
        Assert.assertEquals(node, "A")
      }
    }

    Assert.assertEquals(collapsed.edges(), Sets.newHashSet(0, 1))
    for (edge in collapsed.edges()) {
      Assert.assertEquals(collapsed.incidentNodes(edge).nodeU(), "A")
      Assert.assertTrue(collapsed.incidentNodes(edge).nodeV() is Network<*, *>)
    }

    val nodes = collapsed.nodes()
    picker.clear()
    for (node in collapsed.nodes()) {
      if (node is Network<*, *>) {
        picker.pick(node, true)
      }
    }
    @Suppress("UNCHECKED_CAST")
    val expanded = collapser.expand(network, collapsed, clusterGraph) as Network<Any, Any>
    Assert.assertEquals(network.nodes(), Sets.newHashSet("A", "B", "C"))
    Assert.assertEquals(expanded.incidentNodes(0), EndpointPair.unordered("B", "A"))
    Assert.assertEquals(expanded.incidentNodes(1), EndpointPair.unordered("C", "A"))
    Assert.assertEquals(expanded.incidentNodes(2), EndpointPair.unordered("B", "C"))
  }

  @Test
  fun testTwoConnectedClustersExpandOneThenTheOther() {
    val originalNetwork: Network<Any, Any> = getDemoGraph2()
    val collapser = GraphCollapser(originalNetwork)
    val picker = MultiPickedState<Any>()
    picker.pick("A", true)
    picker.pick("B", true)
    picker.pick("C", true)

    log.debug("originalNetwork:$originalNetwork")

    val clusterNodeOne = collapser.getClusterGraph(originalNetwork, picker.getPicked())
    val collapsedGraphOne = collapser.collapse(originalNetwork, clusterNodeOne)

    log.debug("collapsedGraphOne:$collapsedGraphOne")

    picker.clear()
    picker.pick("D", true)
    picker.pick("E", true)
    picker.pick("F", true)

    val clusterNodeTwo = collapser.getClusterGraph(collapsedGraphOne, picker.getPicked())
    val collapsedGraphTwo = collapser.collapse(collapsedGraphOne, clusterNodeTwo)

    log.debug("collapsedGraphTwo:$collapsedGraphTwo")

    val expanded = collapser.expand(originalNetwork, collapsedGraphTwo, clusterNodeTwo)

    Assert.assertEquals(expanded, collapsedGraphOne)

    val expandedAgain = collapser.expand(originalNetwork, expanded, clusterNodeOne)

    Assert.assertEquals(expandedAgain, originalNetwork)
  }

  companion object {
    private fun createEdge(
      g: MutableNetwork<String, Number>,
      v1Label: String,
      v2Label: String,
      weight: Int
    ) {
      g.addEdge(v1Label, v2Label, weight)
    }

    @JvmStatic
    fun getDemoGraph(): Network<Any, Any> {
      val g: MutableNetwork<String, Number> =
        NetworkBuilder.undirected().allowsParallelEdges(true).build()

      createEdge(g, "A", "B", 0)
      createEdge(g, "A", "C", 1)
      createEdge(g, "B", "C", 2)

      @Suppress("UNCHECKED_CAST")
      return g as Network<Any, Any>
    }

    @JvmStatic
    fun getDemoGraph2(): Network<Any, Any> {
      val g: MutableNetwork<String, Number> =
        NetworkBuilder.undirected().allowsParallelEdges(true).build()

      createEdge(g, "A", "B", 0)
      createEdge(g, "A", "C", 1)
      createEdge(g, "B", "C", 2)

      createEdge(g, "D", "E", 3)
      createEdge(g, "D", "F", 4)
      createEdge(g, "E", "F", 5)

      createEdge(g, "B", "D", 6)

      createEdge(g, "A", "G", 7)

      @Suppress("UNCHECKED_CAST")
      return g as Network<Any, Any>
    }
  }
}
