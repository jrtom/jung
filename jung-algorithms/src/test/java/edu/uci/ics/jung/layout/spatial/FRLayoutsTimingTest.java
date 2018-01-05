package edu.uci.ics.jung.layout.spatial;

import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.FRBHLayoutAlgorithm;
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
 * This test makes a very small graph, sets initial locations for each node, and each time, it runs
 * a version of FRLayout. FRLayoutAlgorithm - the JUNG legacy version FRBHLayoutAlgorithm - modified
 * to use a BarnesHutQuadTree to reduce the number of repulsion comparisons with a custom Iterator
 * FRBHVisitorLayoutAlgorithm - modified to use the BarnesHutQuadTree as a visitor during the
 * repulsion step. The LayoutModel is subclassed so that no relax thread is started. A total of 200
 * steps of the layout relax is run. After all tests are run, the end values for both BarnesHut
 * versions are compared. The end values should be very close. The standard FRLayoutAlgorithm will
 * vary because force comparisions are approximated in the BarnesHut versions
 *
 * <p>The Iterator version of BarnesHut uses storage space to cache collections of 'nodes' (or force
 * vectors) to compare with. The Visitor version does not use that additional storage space, so it
 * should be better.
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
    FRBHLayoutAlgorithm layoutAlgorithmTwo = new FRBHLayoutAlgorithm();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmTwo.setRandomSeed(0);
    doTest(layoutAlgorithmTwo);
  }

  @Test
  public void testFRBHVisisor() {
    FRBHVisitorLayoutAlgorithm layoutAlgorithmThree = new FRBHVisitorLayoutAlgorithm();
    // using the same random seed each time for repeatable results from each test.
    layoutAlgorithmThree.setRandomSeed(0);
    doTest(layoutAlgorithmThree);
  }

  private void doTest(LayoutAlgorithm<String> layoutAlgorithm) {
    layoutModel
        .getLayoutStateChangeSupport()
        .addLayoutStateChangeListener(
            new LayoutModel.LayoutStateChangeListener() {
              long time = System.currentTimeMillis();

              @Override
              public void layoutStateChanged(LayoutModel.LayoutStateChangeEvent evt) {
                if (evt.active == false) {
                  long endTime = System.currentTimeMillis();
                  log.info(
                      "elapsed time for {} was {}",
                      layoutAlgorithm.getClass().getName(),
                      endTime - time);
                }
              }
            });
    layoutModel.accept(layoutAlgorithm);
  }
}
