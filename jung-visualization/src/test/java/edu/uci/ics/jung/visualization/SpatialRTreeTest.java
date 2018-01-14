package edu.uci.ics.jung.visualization;

import com.google.common.graph.Graph;
import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.util.NetworkNodeAccessor;
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import edu.uci.ics.jung.visualization.spatial.TreeNode;
import java.awt.*;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test to make sure that a search for a node returns the same leaf that you get when you search for
 * the point location of that node
 *
 * @author Tom Nelson
 */
public class SpatialRTreeTest {

  private static final Logger log = LoggerFactory.getLogger(SpatialRTreeTest.class);

  int width = 600;
  int height = 600;
  Graph<String> graph;
  LayoutModel<String> layoutModel;
  Spatial<String> tree;

  @Before
  public void setup() {
    // generate 100 random nodes in a graph at random locations in the layoutModel
    Network<String, Number> network = TestGraphs.createChainPlusIsolates(0, 5000);
    graph = network.asGraph();

    VisualizationServer<String, Number> vv =
        new BasicVisualizationServer(network, new StaticLayoutAlgorithm(), new Dimension(600, 600));

    tree = vv.getNodeSpatial();
    layoutModel = vv.getModel().getLayoutModel();
  }

  /**
   * confirm that the quadtree cell for a node is the same as the quadtree cell for the node's
   * location
   */
  @Test
  public void testRandomPointsAndLocations() {
    for (String node : graph.nodes()) {
      Point location = layoutModel.apply(node);
      Collection<? extends TreeNode> pointQuadTrees =
          tree.getContainingLeafs(location.x, location.y);
      TreeNode nodeQuadTree = tree.getContainingLeaf(node);
      Assert.assertTrue(pointQuadTrees.contains(nodeQuadTree));
    }
  }

  /**
   * test that the closest node for a random point is the same one returned for the
   * RadiusNetworkNodeAccessor and for the SpatialQuadTree Test with 1000 randomly generated points
   */
  @Test
  public void testClosestNodes() {
    final int COUNT = 10000;
    NetworkNodeAccessor<String> slowWay = new RadiusNetworkNodeAccessor<>(Double.MAX_VALUE);

    // look for nodes closest to COUNT random locations
    for (int i = 0; i < COUNT; i++) {
      double x = Math.random() * layoutModel.getWidth();
      double y = Math.random() * layoutModel.getHeight();
      // use the slowWay
      String winnerOne = slowWay.getNode(layoutModel, x, y);
      // use the quadtree
      String winnerTwo = tree.getClosestElement(x, y);

      log.trace("{} and {} should be the same...", winnerOne, winnerTwo);

      if (!winnerOne.equals(winnerTwo)) {
        log.warn(
            "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
            winnerOne,
            layoutModel.apply(winnerOne),
            x,
            y,
            layoutModel.apply(winnerOne).distanceSquared(x, y));
        log.warn(
            "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
            winnerTwo,
            layoutModel.apply(winnerTwo),
            x,
            y,
            layoutModel.apply(winnerTwo).distanceSquared(x, y));

        log.warn("the cell for winnerOne {} is {}", winnerOne, tree.getContainingLeaf(winnerOne));
        log.warn("the cell for winnerTwo {} is {}", winnerTwo, tree.getContainingLeaf(winnerTwo));
        log.warn("the cell for the search point {},{} is {}", x, y, tree.getContainingLeafs(x, y));
      }
      Assert.assertEquals(winnerOne, winnerTwo);
    }
  }

  /**
   * a simple performance measure to compare using the RadiusNetworkNodeAccessor and the
   * SpatialQuadTree. Not really a test, it just outputs elapsed time
   */
  @Test
  public void comparePerformance() {
    final int COUNT = 1000;
    NetworkNodeAccessor<String> slowWay = new RadiusNetworkNodeAccessor<>(Double.MAX_VALUE);

    // generate the points first so both tests use the same points
    double[] xs = new double[COUNT];
    double[] ys = new double[COUNT];
    for (int i = 0; i < COUNT; i++) {
      xs[i] = Math.random() * layoutModel.getWidth();
      ys[i] = Math.random() * layoutModel.getHeight();
    }
    long start = System.currentTimeMillis();
    // look for nodes closest to 10000 random locations
    for (int i = 0; i < COUNT; i++) {
      // use the RadiusNetworkNodeAccessor
      String winnerOne = slowWay.getNode(layoutModel, xs[i], ys[i]);
    }
    long end = System.currentTimeMillis();
    log.info("radius way took {}", end - start);
    start = System.currentTimeMillis();
    for (int i = 0; i < COUNT; i++) {
      // use the rtree
      String winnerTwo = tree.getClosestElement(xs[i], ys[i]);
    }
    end = System.currentTimeMillis();
    log.info("spatial way took {}", end - start);
  }
}
