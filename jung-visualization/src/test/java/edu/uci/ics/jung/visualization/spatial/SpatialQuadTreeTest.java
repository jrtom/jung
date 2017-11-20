package edu.uci.ics.jung.visualization.spatial;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.visualization.layout.AWTPointModel;
import java.awt.geom.Point2D;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test to make sure that a search for a node returns the same leaf that you get when you search for
 * the point location of that node
 *
 * @author Tom Nelson
 */
public class SpatialQuadTreeTest {

  private static final Logger log = LoggerFactory.getLogger(SpatialQuadTreeTest.class);

  int width = 600;
  int height = 600;

  /**
   * confirm that the quadtree cell for a node is the same as the quadtree cell for the node's
   * location
   */
  @Test
  public void testRandomPointsAndLocations() {
    // generate 100 random nodes in a graph at random locations in the layoutModel
    Graph<String> graph = TestGraphs.createChainPlusIsolates(0, 100).asGraph();
    LayoutModel<String, Point2D> layoutModel =
        new LoadingCacheLayoutModel(
            graph,
            new AWTPointModel(),
            width,
            height,
            new RandomLocationTransformer(
                new AWTPointModel(), width, height, 0, System.currentTimeMillis()));

    SpatialQuadTree tree = new SpatialQuadTree(layoutModel, width, height);
    for (String node : graph.nodes()) {
      tree.insert(node);
    }

    for (String node : graph.nodes()) {
      Point2D location = layoutModel.apply(node);
      SpatialQuadTree pointQuadTree = tree.getContainingQuadTreeLeaf(location);
      SpatialQuadTree nodeQuadTree = tree.getContainingQuadTreeLeaf(node);
      Assert.assertEquals(pointQuadTree, nodeQuadTree);
      log.info(
          "pointQuadTree level {} nodeQuadTree level {}",
          pointQuadTree.getLevel(),
          nodeQuadTree.getLevel());
    }
  }
}
