package edu.uci.ics.jung.visualization

import com.google.common.graph.GraphBuilder
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import org.junit.Test

class LayoutAlgorithmTransitionTest {

  @Test
  fun testTransition() {
    val graph = GraphBuilder.undirected().build<String>()
    graph.addNode("A")
    val model = LoadingCacheLayoutModel.builder<String>()
      .setGraph(graph)
      .setSize(100, 100)
      .build()

    model.set("A", 0.0, 0.0)
    val newLayoutAlgorithm = StaticLayoutAlgorithm<String>()
  }
}
