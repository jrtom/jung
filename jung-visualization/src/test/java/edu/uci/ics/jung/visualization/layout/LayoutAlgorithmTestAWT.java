package edu.uci.ics.jung.visualization.layout;

import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.immutable.KKLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.immutable.TreeLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.PointModel;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by tanelso on 11/1/17. */
public class LayoutAlgorithmTestAWT {

  private static final Logger log = LoggerFactory.getLogger(LayoutAlgorithmTestAWT.class);

  Graph<String> graph;
  LoadingCacheLayoutModel<String, Point2D> layoutModel;
  PointModel<Point2D> pointModel = new AWTPointModel();

  @Test
  public void testLayoutAlgorithms() {
    graph = TestGraphs.getDemoGraph().asGraph();
    layoutModel = new LoadingCacheLayoutModel<>(graph, pointModel, 500, 500, 0);
    testLayoutAlgorithm(new SpringLayoutAlgorithm<String, Point2D>(pointModel));
    testLayoutAlgorithm(new KKLayoutAlgorithm<String, Point2D>(pointModel));
    //            testLayoutAlgorithm(new ISOMLayoutAlgorithm<String, Point2D>(pointModel));
    testLayoutAlgorithm(new CircleLayoutAlgorithm<String, Point2D>(pointModel));
  }

  @Test
  public void testTreeLayoutAlgorithms() {
    graph = createTree().asGraph();
    layoutModel = new LoadingCacheLayoutModel<>(graph, pointModel, 500, 500, 0);
    testLayoutAlgorithm(new TreeLayoutAlgorithm<String, Point2D>(pointModel));
  }

  private void testLayoutAlgorithm(LayoutAlgorithm<String, Point2D> layoutAlgorithm) {
    layoutModel.accept(layoutAlgorithm);
    testUniqueLocations();
  }

  private void testUniqueLocations() {
    Set<Point2D> locations = Sets.newHashSet();
    Collection<String> nodes = layoutModel.getGraph().nodes();
    for (String node : nodes) {
      Point2D p = layoutModel.get(node);
      locations.add(layoutModel.get(node));
    }
    // make sure that the algorithm as provided unique locations for all nodes
    Assert.assertEquals(nodes.size(), locations.size());
  }

  private CTreeNetwork<String, Integer> createTree() {
    MutableCTreeNetwork<String, Integer> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(27).build();

    tree.addNode("root");

    int edgeId = 0;
    tree.addEdge("root", "V0", edgeId++);
    tree.addEdge("V0", "V1", edgeId++);
    tree.addEdge("V0", "V2", edgeId++);
    tree.addEdge("V1", "V4", edgeId++);
    tree.addEdge("V2", "V3", edgeId++);
    tree.addEdge("V2", "V5", edgeId++);
    tree.addEdge("V4", "V6", edgeId++);
    tree.addEdge("V4", "V7", edgeId++);
    tree.addEdge("V3", "V8", edgeId++);
    tree.addEdge("V6", "V9", edgeId++);
    tree.addEdge("V4", "V10", edgeId++);

    tree.addEdge("root", "A0", edgeId++);
    tree.addEdge("A0", "A1", edgeId++);
    tree.addEdge("A0", "A2", edgeId++);
    tree.addEdge("A0", "A3", edgeId++);

    tree.addEdge("root", "B0", edgeId++);
    tree.addEdge("B0", "B1", edgeId++);
    tree.addEdge("B0", "B2", edgeId++);
    tree.addEdge("B1", "B4", edgeId++);
    tree.addEdge("B2", "B3", edgeId++);
    tree.addEdge("B2", "B5", edgeId++);
    tree.addEdge("B4", "B6", edgeId++);
    tree.addEdge("B4", "B7", edgeId++);
    tree.addEdge("B3", "B8", edgeId++);
    tree.addEdge("B6", "B9", edgeId++);

    return tree;
  }
}
