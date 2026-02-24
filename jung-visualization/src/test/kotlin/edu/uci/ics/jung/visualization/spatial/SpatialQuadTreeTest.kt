package edu.uci.ics.jung.visualization.spatial

import com.google.common.graph.Graph
import edu.uci.ics.jung.graph.util.TestGraphs
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor
import edu.uci.ics.jung.layout.util.RandomLocationTransformer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * test to make sure that a search for a node returns the same leaf that you get when you search for
 * the point location of that node
 *
 * @author Tom Nelson
 */
class SpatialQuadTreeTest {

  companion object {
    private val log = LoggerFactory.getLogger(SpatialQuadTreeTest::class.java)
  }

  val width = 600
  val height = 600
  lateinit var graph: Graph<String>
  lateinit var layoutModel: LayoutModel<String>
  lateinit var tree: SpatialQuadTree<String>

  @Before
  fun setup() {
    // generate 100 random nodes in a graph at random locations in the layoutModel
    graph = TestGraphs.createChainPlusIsolates(0, 100).asGraph()
    layoutModel = LoadingCacheLayoutModel.builder<String>()
      .setGraph(graph)
      .setSize(width, height)
      .setInitializer(
        RandomLocationTransformer(width.toDouble(), height.toDouble(), System.currentTimeMillis())
      )
      .build()

    tree = SpatialQuadTree(layoutModel, width.toDouble(), height.toDouble())
    for (node in graph.nodes()) {
      tree.insert(node)
    }
  }

  /**
   * confirm that the quadtree cell for a node is the same as the quadtree cell for the node's
   * location
   */
  @Test
  fun testRandomPointsAndLocations() {
    for (node in graph.nodes()) {
      val location = layoutModel.apply(node)
      val pointQuadTree = tree.getContainingQuadTreeLeaf(location.x, location.y)
      val nodeQuadTree = tree.getContainingQuadTreeLeaf(node) as SpatialQuadTree<*>
      Assert.assertEquals(pointQuadTree, nodeQuadTree)
      log.debug(
        "pointQuadTree level {} nodeQuadTree level {}",
        pointQuadTree!!.getLevel(),
        nodeQuadTree.getLevel()
      )
    }
  }

  /**
   * test that the closest node for a random point is the same one returned for the
   * RadiusNetworkNodeAccessor and for the SpatialQuadTree Test with 1000 randomly generated points
   */
  @Test
  fun testClosestNodes() {
    val COUNT = 10000
    val slowWay = RadiusNetworkNodeAccessor<String>(Double.MAX_VALUE)

    // look for nodes closest to COUNT random locations
    for (i in 0 until COUNT) {
      val x = Math.random() * layoutModel.width
      val y = Math.random() * layoutModel.height
      // use the slowWay
      val winnerOne = slowWay.getNode(layoutModel, x, y)
      // use the quadtree
      val winnerTwo = tree.getClosestElement(x, y)

      log.trace("{} and {} should be the same...", winnerOne, winnerTwo)

      if (winnerOne != winnerTwo) {
        log.warn(
          "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
          winnerOne,
          layoutModel.apply(winnerOne!!),
          x,
          y,
          layoutModel.apply(winnerOne).distanceSquared(x, y)
        )
        log.warn(
          "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
          winnerTwo,
          layoutModel.apply(winnerTwo!!),
          x,
          y,
          layoutModel.apply(winnerTwo).distanceSquared(x, y)
        )

        log.warn(
          "the cell for winnerOne {} is {}",
          winnerOne,
          tree.getContainingQuadTreeLeaf(winnerOne!!)
        )
        log.warn(
          "the cell for winnerTwo {} is {}",
          winnerTwo,
          tree.getContainingQuadTreeLeaf(winnerTwo!!)
        )
        log.warn(
          "the cell for the search point {},{} is {}",
          x,
          y,
          tree.getContainingQuadTreeLeaf(x, y)
        )
      }
      Assert.assertEquals(winnerOne, winnerTwo)
    }
  }

  /**
   * a simple performance measure to compare using the RadiusNetworkNodeAccessor and the
   * SpatialQuadTree. Not really a test, it just outputs elapsed time
   */
  @Test
  fun comparePerformance() {
    val COUNT = 100000
    val slowWay = RadiusNetworkNodeAccessor<String>(Double.MAX_VALUE)

    // generate the points first so both tests use the same points
    val xs = DoubleArray(COUNT)
    val ys = DoubleArray(COUNT)
    for (i in 0 until COUNT) {
      xs[i] = Math.random() * layoutModel.width
      ys[i] = Math.random() * layoutModel.height
    }
    var start = System.currentTimeMillis()
    // look for nodes closest to 10000 random locations
    for (i in 0 until COUNT) {
      // use the RadiusNetworkNodeAccessor
      val winnerOne = slowWay.getNode(layoutModel, xs[i], ys[i])
    }
    var end = System.currentTimeMillis()
    log.info("radius way took {}", end - start)
    start = System.currentTimeMillis()
    for (i in 0 until COUNT) {
      // use the quadtree
      val winnerTwo = tree.getClosestElement(xs[i], ys[i])
    }
    end = System.currentTimeMillis()
    log.info("spatial way took {}", end - start)
  }
}
