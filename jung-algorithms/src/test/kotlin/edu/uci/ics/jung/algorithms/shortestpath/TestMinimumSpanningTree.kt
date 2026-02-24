package edu.uci.ics.jung.algorithms.shortestpath

import com.google.common.graph.NetworkBuilder
import junit.framework.TestCase

class TestMinimumSpanningTree : TestCase() {

  fun testSimpleTree() {
    val tree = NetworkBuilder.undirected().build<String, Int>()
    tree.addNode("A")
    tree.addEdge("A", "B0", 0)
    tree.addEdge("A", "B1", 1)

    val mst = MinimumSpanningTree.extractFrom(tree) { _: Int -> 1.0 }

    assertEquals(tree.nodes(), mst.nodes())
    assertEquals(tree.edges(), mst.edges())
    assertEquals(tree, mst)
  }

  fun testDAG() {
    val graph = NetworkBuilder.directed().build<String, Int>()
    graph.addNode("B0")
    graph.addEdge("A", "B0", 0)
    graph.addEdge("A", "B1", 1)

    val mst = MinimumSpanningTree.extractFrom(graph) { _: Int -> 1.0 }

    assertEquals(graph.nodes(), mst.nodes())
    assertEquals(graph.edges(), mst.edges())
  }

  // TODO: add tests:
  // - cycle
  // - multiple components
  // - self-loops
}
