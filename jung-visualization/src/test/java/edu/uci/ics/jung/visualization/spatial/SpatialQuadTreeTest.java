package edu.uci.ics.jung.visualization.spatial;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.util.NetworkNodeAccessor;
import edu.uci.ics.jung.layout.util.RadiusNetworkNodeAccessor;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.visualization.layout.AWTPointModel;
import java.awt.geom.Point2D;
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
public class SpatialQuadTreeTest {

  private static final Logger log = LoggerFactory.getLogger(SpatialQuadTreeTest.class);

  int width = 600;
  int height = 600;
  Graph<String> graph;
  LayoutModel<String, Point2D> layoutModel;
  SpatialQuadTree<String> tree;

  @Before
  public void setup() {
    // generate 100 random nodes in a graph at random locations in the layoutModel
    graph = TestGraphs.createChainPlusIsolates(0, 100).asGraph();
    layoutModel =
        new LoadingCacheLayoutModel(
            graph,
            new AWTPointModel(),
            width,
            height,
            new RandomLocationTransformer(
                new AWTPointModel(), width, height, 0, System.currentTimeMillis()));

    tree = new SpatialQuadTree(layoutModel, width, height);
    for (String node : graph.nodes()) {
      tree.insert(node);
    }
  }

  /**
   * confirm that the quadtree cell for a node is the same as the quadtree cell for the node's
   * location
   */
  @Test
  public void testRandomPointsAndLocations() {
    for (String node : graph.nodes()) {
      Point2D location = layoutModel.apply(node);
      SpatialQuadTree pointQuadTree = tree.getContainingQuadTreeLeaf(location);
      SpatialQuadTree nodeQuadTree = tree.getContainingQuadTreeLeaf(node);
      Assert.assertEquals(pointQuadTree, nodeQuadTree);
      log.debug(
          "pointQuadTree level {} nodeQuadTree level {}",
          pointQuadTree.getLevel(),
          nodeQuadTree.getLevel());
    }
  }

  /**
   * test that the closest node for a random point is the same one returned for the
   * RadiusNetworkNodeAccessor and for the SpatialQuadTree Test with 1000 randomly generated points
   */
  @Test
  public void testClosestNodes() {
    final int COUNT = 10000;
    NetworkNodeAccessor<String, Point2D> slowWay =
        new RadiusNetworkNodeAccessor<String, Point2D>(
            graph, new AWTPointModel(), Double.MAX_VALUE);

    // look for nodes closest to COUNT random locations
    for (int i = 0; i < COUNT; i++) {
      double x = Math.random() * layoutModel.getWidth();
      double y = Math.random() * layoutModel.getHeight();
      // use the slowWay
      String winnerOne = slowWay.getNode(layoutModel, x, y, 0);
      // use the quadtree
      String winnerTwo = tree.getClosestNode(x, y);

      log.debug("{} and {} should be the same...", winnerOne, winnerTwo);

      if (!winnerOne.equals(winnerTwo)) {
        log.warn(
            "the radius distanceSq from winnerOne {} at {} to {},{} is {}",
            winnerOne,
            layoutModel.apply(winnerOne),
            x,
            y,
            layoutModel.apply(winnerOne).distanceSq(x, y));
        log.warn(
            "the radius distanceSq from winnerTwo {} at {} to {},{} is {}",
            winnerTwo,
            layoutModel.apply(winnerTwo),
            x,
            y,
            layoutModel.apply(winnerTwo).distanceSq(x, y));

        log.warn(
            "the cell for winnerOne {} is {}",
            winnerOne,
            tree.getContainingQuadTreeLeaf(winnerOne));
        log.warn(
            "the cell for winnerTwo {} is {}",
            winnerTwo,
            tree.getContainingQuadTreeLeaf(winnerTwo));
        log.warn(
            "the cell for the search point {},{} is {}",
            x,
            y,
            tree.getContainingQuadTreeLeaf(x, y));
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
    final int COUNT = 10000;
    NetworkNodeAccessor<String, Point2D> slowWay =
        new RadiusNetworkNodeAccessor<String, Point2D>(
            graph, new AWTPointModel(), Double.MAX_VALUE);

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
      String winnerOne = slowWay.getNode(layoutModel, xs[i], ys[i], 0);
    }
    long end = System.currentTimeMillis();
    log.info("radius way took {}", end - start);
    start = System.currentTimeMillis();
    for (int i = 0; i < COUNT; i++) {
      // use the quadtree
      String winnerTwo = tree.getClosestNode(xs[i], ys[i]);
    }
    end = System.currentTimeMillis();
    log.info("spatial way took {}", end - start);
  }
}
