package edu.uci.ics.jung.algorithms.cluster

import edu.uci.ics.jung.graph.util.TestGraphs
import junit.framework.TestCase

class WeakComponentClustererTest : TestCase() {

  var graph = TestGraphs.getDemoGraph()

  fun testWeakComponent() {
    val clusterer = WeakComponentClusterer<String>()
    clusterer.apply(graph.asGraph())
  }
}
