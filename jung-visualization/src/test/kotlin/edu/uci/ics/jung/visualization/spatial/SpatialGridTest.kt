package edu.uci.ics.jung.visualization.spatial

import com.google.common.graph.GraphBuilder
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel
import java.awt.Rectangle
import java.util.Arrays
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * @author Tom Nelson
 */
class SpatialGridTest {

  companion object {
    private val log = LoggerFactory.getLogger(SpatialGridTest::class.java)
  }

  @Test
  fun testBoxLocations() {
    val graph = GraphBuilder.undirected().build<String>()
    val layoutModel = LoadingCacheLayoutModel.builder<String>()
      .setGraph(graph)
      .setSize(100, 100)
      .build()
    val spatial = SpatialGrid<String>(layoutModel, Rectangle(0, 0, 100, 100), 4, 4)
    log.trace("grid is " + spatial.getGrid())
    showBoxIndex(spatial, 10, 10)
    showBoxIndex(spatial, 49, 49)
    showBoxIndex(spatial, 50, 50)
    showBoxIndex(spatial, 99, 1)
    showBoxIndex(spatial, 70, 10)

    showVisibleTiles(spatial, Rectangle(0, 0, 100, 100))
    showVisibleTiles(spatial, Rectangle(25, 25, 10, 10))
    showVisibleTiles(spatial, Rectangle(25, 25, 30, 30))
    showVisibleTiles(spatial, Rectangle(99, 99, 30, 30))
  }

  private fun showBoxIndex(spatial: SpatialGrid<String>, x: Int, y: Int) {
    log.info(
      "spatial.getBoxIndex($x,$y):" + Arrays.toString(spatial.getBoxIndex(x.toDouble(), y.toDouble()))
    )
  }

  private fun showVisibleTiles(spatial: SpatialGrid<String>, r: Rectangle) {
    log.info("spatial.getVisibleTiles($r):" + spatial.getVisibleTiles(r))
  }
}
