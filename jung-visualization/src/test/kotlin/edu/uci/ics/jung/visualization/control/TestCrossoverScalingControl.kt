package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.visualization.BasicVisualizationServer
import edu.uci.ics.jung.visualization.VisualizationServer
import java.awt.Dimension
import java.awt.geom.Point2D
import junit.framework.TestCase

class TestCrossoverScalingControl : TestCase() {

  lateinit var sc: CrossoverScalingControl
  lateinit var vv: VisualizationServer<*, *>

  var crossover = 0f
  var scale = 0f

  override fun setUp() {
    sc = CrossoverScalingControl()
    val network = TestGraphs.getDemoGraph()
    vv = BasicVisualizationServer(network, FRLayoutAlgorithm(), Dimension(600, 600))
  }

  fun testCrossover() {
    crossover = 2.0f
    scale = .5f
    sc.crossover = crossover.toDouble()
    sc.scale(vv, scale, Point2D.Double())
  }

  fun testCrossover2() {
    crossover = 2.0f
    scale = 1.5f
    sc.crossover = crossover.toDouble()
    sc.scale(vv, scale, Point2D.Double())
  }

  fun testCrossover3() {
    crossover = 2.0f
    scale = 2.5f
    sc.crossover = crossover.toDouble()
    sc.scale(vv, scale, Point2D.Double())
  }

  fun testCrossover4() {
    crossover = 0.5f
    scale = 2.5f
    sc.crossover = crossover.toDouble()
    sc.scale(vv, scale, Point2D.Double())
  }

  fun testCrossover5() {
    crossover = 0.5f
    scale = .3f
    sc.crossover = crossover.toDouble()
    sc.scale(vv, scale, Point2D.Double())
  }
}
