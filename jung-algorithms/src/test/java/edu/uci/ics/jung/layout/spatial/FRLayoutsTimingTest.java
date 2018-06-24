package edu.uci.ics.jung.layout.spatial;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.FRBHIteratorLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.FRBHVisitorLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Measures the time required to do the same work with each of 3 versions of the FRLayoutAlgorithm:
 *
 * <ul>
 *   <li>FRLayoutAlgorithm - the JUNG legacy version
 *   <li>FRBHLayoutAlgorithm - modified to use a BarnesHutQuadTree to reduce the number of repulsion
 *       comparisons with a custom Iterator
 *   <li>FRBHVisitorLayoutAlgorithm - modified to use the BarnesHutQuadTree as a visitor during the
 *       repulsion step
 * </ul>
 *
 * @author Tom Nelson
 */
public class FRLayoutsTimingTest {

  private static final Logger log = LoggerFactory.getLogger(FRLayoutsTimingTest.class);
  Graph<String> graph;
  LayoutModel<String> layoutModel;

  /**
   * this runs again before each test. Build a simple graph, build a custom layout model (see below)
   * initialize the locations to be the same each time.
   */
  @Before
  public void setup() {
    graph = TestGraphs.getOneComponentGraph().asGraph();
    layoutModel =
        LoadingCacheLayoutModel.<String>builder().setGraph(graph).setSize(500, 500).build();
    layoutModel.setInitializer(new RandomLocationTransformer<>(500, 500));
  }

  @Test
  public void testFRLayouts() {
    FRLayoutAlgorithm layoutAlgorithmOne = new FRLayoutAlgorithm();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmOne.setRandomSeed(0);
    doTest(layoutAlgorithmOne);
  }

  @Test
  public void testFRBH() {
    FRBHIteratorLayoutAlgorithm layoutAlgorithmTwo = new FRBHIteratorLayoutAlgorithm();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmTwo.setRandomSeed(0);
    doTest(layoutAlgorithmTwo);
  }

  @Test
  public void testFRBHVisitor() {
    FRBHVisitorLayoutAlgorithm layoutAlgorithmThree = new FRBHVisitorLayoutAlgorithm();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmThree.setRandomSeed(0);
    doTest(layoutAlgorithmThree);
  }

  private void doTest(LayoutAlgorithm<String> layoutAlgorithm) {
    long startTime = System.currentTimeMillis();
    layoutModel.accept(layoutAlgorithm);
    layoutModel
        .getTheFuture()
        .thenRun(
            () ->
                log.info(
                    "elapsed time for {} was {}",
                    layoutAlgorithm.getClass().getName(),
                    System.currentTimeMillis() - startTime))
        .join();
  }
}
