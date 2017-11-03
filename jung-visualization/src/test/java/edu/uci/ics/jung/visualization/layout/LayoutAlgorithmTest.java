package edu.uci.ics.jung.visualization.layout;

import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.graph.util.TestGraphs;
import java.util.Collection;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Created by tanelso on 11/1/17. */
public class LayoutAlgorithmTest {

  private static final Logger log = LoggerFactory.getLogger(LayoutAlgorithmTest.class);

  Graph<String> graph;
  LoadingCacheLayoutModel<String, TestDomainModel.Point> layoutModel;
  DomainModel<TestDomainModel.Point> domainModel = new TestDomainModel();

  @Test
  public void testLayoutAlgorithms() {
    graph = TestGraphs.getDemoGraph().asGraph();
    layoutModel = new LoadingCacheLayoutModel<>(graph, domainModel, 500, 500);
    testLayoutAlgorithm(new SpringLayoutAlgorithm<String, TestDomainModel.Point>(domainModel));
    testLayoutAlgorithm(new KKLayoutAlgorithm<String, TestDomainModel.Point>(domainModel));
    //        testLayoutAlgorithm(new ISOMLayoutAlgorithm<String, TestDomainModel.Point>(domainModel));
    testLayoutAlgorithm(new CircleLayoutAlgorithm<String, TestDomainModel.Point>(domainModel));
  }

  @Test
  public void testTreeLayoutAlgorithms() {
    graph = createTree().asGraph();
    layoutModel = new LoadingCacheLayoutModel<>(graph, domainModel, 500, 500);
    testLayoutAlgorithm(new TreeLayoutAlgorithm<String, TestDomainModel.Point>(domainModel));
  }

  private void testLayoutAlgorithm(LayoutAlgorithm<String, TestDomainModel.Point> layoutAlgorithm) {
    layoutModel.accept(layoutAlgorithm);
    testUniqueLocations();
  }

  private void testUniqueLocations() {
    Set<TestDomainModel.Point> locations = Sets.newHashSet();
    Collection<String> nodes = layoutModel.getGraph().nodes();
    for (String node : nodes) {
      TestDomainModel.Point p = layoutModel.get(node);
      locations.add(layoutModel.get(node));
    }
    // make sure that the algorithm as provided unique locations for all nodes
    Assert.assertEquals(locations.size(), nodes.size());
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
