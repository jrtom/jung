package edu.uci.ics.jung.visualization

import com.google.common.graph.NetworkBuilder
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm
import java.awt.Dimension
import junit.framework.TestCase

class BasicVisualizationServerTest : TestCase() {

  /*
   * Previously, a bug was introduced where the RenderContext in BasicVisualizationServer was reassigned, resulting
   * in data like pickedNodeState to be lost.
   */
  fun testRenderContextNotOverridden() {
    val graph = NetworkBuilder.directed().build<Any, Any>()
    val algorithm = CircleLayoutAlgorithm<Any>()

    val server = BasicVisualizationServer<Any, Any>(graph, algorithm, Dimension(600, 600))

    val pickedNodeState = server.getRenderContext().getPickedNodeState()
    assertNotNull(pickedNodeState)
  }
}
