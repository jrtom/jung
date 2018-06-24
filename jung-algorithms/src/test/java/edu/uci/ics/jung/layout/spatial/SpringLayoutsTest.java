package edu.uci.ics.jung.layout.spatial;

import com.google.common.collect.Maps;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import edu.uci.ics.jung.layout.algorithms.IterativeLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.SpringBHIteratorLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.SpringBHVisitorLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test makes a very small graph, sets initial locations for each node, and each time, it runs
 * a version of FRLayout.
 *
 * <ul>
 *   <li>SpringLayoutAlgorithm - the JUNG legacy version
 *   <li>SpringBHLayoutAlgorithm - modified to use a BarnesHutQuadTree to reduce the number of
 *       repulsion comparisons with a custom Iterator
 *   <li>SpringBHVisitorLayoutAlgorithm - modified to use the BarnesHutQuadTree as a visitor during
 *       the repulsion step.
 * </ul>
 *
 * <p>The LayoutModel is subclassed so that no relax thread is started. A total of 200 steps of the
 * layout relax is run. After all tests are run, the end values for both BarnesHut versions are
 * compared. The end values should be very close. The standard FRLayoutAlgorithm will vary because
 * force comparisons are approximated in the BarnesHut versions
 *
 * <p>The Iterator version of BarnesHut uses storage space to cache collections of 'nodes' (or force
 * vectors) to compare with. The Visitor version does not use that additional storage space, so it
 * should be better.
 *
 * @author Tom Nelson
 */
public class SpringLayoutsTest {

  private static final Logger log = LoggerFactory.getLogger(SpringLayoutsTest.class);
  MutableGraph<String> graph;
  LayoutModel<String> layoutModel;
  static Map<String, Point> mapOne = Maps.newHashMap();
  static Map<String, Point> mapTwo = Maps.newHashMap();
  static Map<String, Point> mapThree = Maps.newHashMap();

  /**
   * this runs again before each test. Build a simple graph, build a custom layout model (see below)
   * initialize the locations to be the same each time.
   */
  @Before
  public void setup() {
    graph = GraphBuilder.directed().build();
    graph.putEdge("A", "B");
    graph.putEdge("B", "C");
    graph.putEdge("C", "A");
    graph.putEdge("D", "C");

    layoutModel =
        new TestLayoutModel<String>(
            LoadingCacheLayoutModel.<String>builder().setGraph(graph).setSize(500, 500), 30);
    layoutModel.set("A", Point.of(200, 100));
    layoutModel.set("B", Point.of(100, 200));
    layoutModel.set("C", Point.of(100, 100));
    layoutModel.set("D", Point.of(500, 100));
    for (String node : graph.nodes()) {
      log.debug("node {} starts at {}", node, layoutModel.apply(node));
    }
  }

  @Test
  public void testSpringLayoutAlgorithm() {
    SpringLayoutAlgorithm layoutAlgorithmOne = new SpringLayoutAlgorithm();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmOne.setRandomSeed(0);
    doTest(layoutAlgorithmOne, mapOne);
  }

  @Test
  public void testSpringBHLayoutAlgorithm() {
    SpringBHIteratorLayoutAlgorithm layoutAlgorithmTwo = new SpringBHIteratorLayoutAlgorithm();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmTwo.setRandomSeed(0);
    doTest(layoutAlgorithmTwo, mapTwo);
  }

  @Test
  public void testSpringBHVisitorLayoutAlgorithm() {
    SpringBHVisitorLayoutAlgorithm layoutAlgorithmThree = new SpringBHVisitorLayoutAlgorithm();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmThree.setRandomSeed(0);
    doTest(layoutAlgorithmThree, mapThree);
  }

  /**
   * check to see if mapTwo and mapThree (the ones that used the BarnesHut optimization) returned
   * similar results
   */
  @AfterClass
  public static void check() {
    log.debug("mapOne:{}", mapOne);
    log.debug("mapTwo:{}", mapTwo);
    log.debug("mapThree:{}", mapThree);
    Assert.assertTrue(
        "the compared maps are not close enough: mapTwo:" + mapTwo + ", mapThree:" + mapThree,
        closeEnough(mapTwo, mapThree));
  }

  private void doTest(LayoutAlgorithm<String> layoutAlgorithm, Map<String, Point> map) {
    log.debug("for {}", layoutAlgorithm.getClass());
    layoutModel.accept(layoutAlgorithm);
    for (String node : graph.nodes()) {
      map.put(node, layoutModel.apply(node));
      log.debug("node {} placed at {}", node, layoutModel.apply(node));
    }
  }

  private static boolean closeEnough(Map<String, Point> left, Map<String, Point> right) {
    if (left.keySet().equals(right.keySet())) {
      for (String key : left.keySet()) {
        Point leftPoint = left.get(key);
        Point rightPoint = right.get(key);
        if (Math.abs(leftPoint.x - rightPoint.x) > 0.001) return false;
        if (Math.abs(leftPoint.y - rightPoint.y) > 0.001) return false;
      }
      return true;
    }
    return false;
  }

  /**
   * a LoadingCacheLayoutModel that will not start a relax thread, but will 'step' the layout the
   * number of times requested in a passed parameter
   *
   * @param <T>
   */
  private static class TestLayoutModel<T> extends LoadingCacheLayoutModel<T> {

    // how many steps
    private int steps;

    public TestLayoutModel(Builder<T, ?> builder, int steps) {
      super(builder);
      this.steps = steps;
    }

    @Override
    public void accept(LayoutAlgorithm<T> layoutAlgorithm) {
      layoutAlgorithm.visit(this);
      if (layoutAlgorithm instanceof IterativeLayoutAlgorithm) {
        IterativeLayoutAlgorithm iterativeLayoutAlgorithm =
            (IterativeLayoutAlgorithm) layoutAlgorithm;
        for (int i = 0; i < steps; i++) {
          iterativeLayoutAlgorithm.step();
        }
      }
    }
  }
}
