package edu.uci.ics.jung.visualization.layout

import com.google.common.collect.Sets
import com.google.common.graph.Graph
import edu.uci.ics.jung.graph.CTreeNetwork
import edu.uci.ics.jung.graph.MutableCTreeNetwork
import edu.uci.ics.jung.graph.TreeNetworkBuilder
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.TreeLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import org.junit.Assert
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * @author Tom Nelson
 */
class LayoutAlgorithmTestAWT {

  companion object {
    private val log = LoggerFactory.getLogger(LayoutAlgorithmTestAWT::class.java)
  }

  lateinit var graph: Graph<String>
  lateinit var layoutModel: LoadingCacheLayoutModel<String>

  @Test
  fun testLayoutAlgorithms() {
    graph = TestGraphs.getDemoGraph().asGraph()
    layoutModel = LoadingCacheLayoutModel.builder<String>()
      .setGraph(graph)
      .setSize(500, 500)
      .build()
    testLayoutAlgorithm(SpringLayoutAlgorithm())
    testLayoutAlgorithm(KKLayoutAlgorithm())
    testLayoutAlgorithm(CircleLayoutAlgorithm())
  }

  @Test
  fun testTreeLayoutAlgorithms() {
    graph = createTree().asGraph()
    layoutModel = LoadingCacheLayoutModel.builder<String>()
      .setGraph(graph)
      .setSize(500, 500)
      .build()
    testLayoutAlgorithm(TreeLayoutAlgorithm())
  }

  private fun testLayoutAlgorithm(layoutAlgorithm: LayoutAlgorithm<String>) {
    layoutModel.accept(layoutAlgorithm)
    testUniqueLocations()
  }

  private fun testUniqueLocations() {
    val locations = Sets.newHashSet<Any>()
    val nodes = layoutModel.graph.nodes()
    for (node in nodes) {
      val p = layoutModel.get(node)
      locations.add(layoutModel.get(node))
    }
    // make sure that the algorithm has provided unique locations for all nodes
    Assert.assertEquals(nodes.size, locations.size)
  }

  private fun createTree(): CTreeNetwork<String, Int> {
    val tree: MutableCTreeNetwork<String, Int> = TreeNetworkBuilder.builder().expectedNodeCount(27).build()

    tree.addNode("root")

    var edgeId = 0
    tree.addEdge("root", "V0", edgeId++)
    tree.addEdge("V0", "V1", edgeId++)
    tree.addEdge("V0", "V2", edgeId++)
    tree.addEdge("V1", "V4", edgeId++)
    tree.addEdge("V2", "V3", edgeId++)
    tree.addEdge("V2", "V5", edgeId++)
    tree.addEdge("V4", "V6", edgeId++)
    tree.addEdge("V4", "V7", edgeId++)
    tree.addEdge("V3", "V8", edgeId++)
    tree.addEdge("V6", "V9", edgeId++)
    tree.addEdge("V4", "V10", edgeId++)

    tree.addEdge("root", "A0", edgeId++)
    tree.addEdge("A0", "A1", edgeId++)
    tree.addEdge("A0", "A2", edgeId++)
    tree.addEdge("A0", "A3", edgeId++)

    tree.addEdge("root", "B0", edgeId++)
    tree.addEdge("B0", "B1", edgeId++)
    tree.addEdge("B0", "B2", edgeId++)
    tree.addEdge("B1", "B4", edgeId++)
    tree.addEdge("B2", "B3", edgeId++)
    tree.addEdge("B2", "B5", edgeId++)
    tree.addEdge("B4", "B6", edgeId++)
    tree.addEdge("B4", "B7", edgeId++)
    tree.addEdge("B3", "B8", edgeId++)
    tree.addEdge("B6", "B9", edgeId++)

    return tree
  }
}
