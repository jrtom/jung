package edu.uci.ics.jung.layout.spatial

import com.google.common.graph.Graph
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.algorithms.FRBHIteratorLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.FRBHVisitorLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * Measures the time required to do the same work with each of 3 versions of the FRLayoutAlgorithm:
 *
 *  * FRLayoutAlgorithm - the JUNG legacy version
 *  * FRBHLayoutAlgorithm - modified to use a BarnesHutQuadTree to reduce the number of repulsion
 *    comparisons with a custom Iterator
 *  * FRBHVisitorLayoutAlgorithm - modified to use the BarnesHutQuadTree as a visitor during the
 *    repulsion step
 *
 * @author Tom Nelson
 */
class FRLayoutsTimingTest {

  private lateinit var graph: Graph<String>
  private lateinit var layoutModel: LayoutModel<String>

  /**
   * this runs again before each test. Build a simple graph, build a custom layout model (see below)
   * initialize the locations to be the same each time.
   */
  @Before
  fun setup() {
    graph = TestGraphs.getOneComponentGraph().asGraph()
    layoutModel =
      LoadingCacheLayoutModel.builder<String>().setGraph(graph).setSize(500, 500).build()
    layoutModel.setInitializer(RandomLocationTransformer(500.0, 500.0))
  }

  @Test
  fun testFRLayouts() {
    val layoutAlgorithmOne = FRLayoutAlgorithm<String>()
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmOne.setRandomSeed(0)
    doTest(layoutAlgorithmOne)
  }

  @Test
  fun testFRBH() {
    val layoutAlgorithmTwo = FRBHIteratorLayoutAlgorithm<String>()
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmTwo.setRandomSeed(0)
    doTest(layoutAlgorithmTwo)
  }

  @Test
  fun testFRBHVisitor() {
    val layoutAlgorithmThree = FRBHVisitorLayoutAlgorithm<String>()
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmThree.setRandomSeed(0)
    doTest(layoutAlgorithmThree)
  }

  private fun doTest(layoutAlgorithm: LayoutAlgorithm<String>) {
    val startTime = System.currentTimeMillis()
    layoutModel.accept(layoutAlgorithm)
    layoutModel
      .theFuture
      ?.thenRun {
        log.info(
          "elapsed time for {} was {}",
          layoutAlgorithm.javaClass.name,
          System.currentTimeMillis() - startTime
        )
      }
      ?.join()
  }

  companion object {
    private val log = LoggerFactory.getLogger(FRLayoutsTimingTest::class.java)
  }
}
