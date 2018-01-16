package edu.uci.ics.jung.visualization.sublayout;

import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.visualization.subLayout.TreeCollapser;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sanity checks. Make sure that you can collapse and expand a tree at a node and get back the
 * original tree. Confirm also when the tree is collapsed at the root
 *
 * @author Tom Nelson
 */
public class TreeCollapserTest {

  private static final Logger log = LoggerFactory.getLogger(TreeCollapserTest.class);

  MutableCTreeNetwork tree;

  @Before
  public void setup() {
    tree = createTree();
  }

  @Test
  public void testCollapseThenExpand() {

    log.info("original tree: {}", tree);
    Collection originalNodes = tree.nodes();
    Collection originalEdges = tree.edges();
    MutableCTreeNetwork subTree = TreeCollapser.collapse(tree, "V2");

    log.info("tree is now {}", tree);
    TreeCollapser.expand(tree, subTree);
    log.info("tree is now {}", tree);

    Assert.assertTrue(originalEdges.equals(tree.edges()));
    Assert.assertTrue(originalNodes.equals(tree.nodes()));
  }

  @Test
  public void testCollapseThenExpandRoot() {

    log.info("original tree: {}", tree);
    Collection originalNodes = tree.nodes();
    Collection originalEdges = tree.edges();
    MutableCTreeNetwork subTree = TreeCollapser.collapse(tree, "root");

    log.info("tree is now {}", tree);
    TreeCollapser.expand(tree, subTree);
    log.info("tree is now {}", tree);

    Assert.assertTrue(originalEdges.equals(tree.edges()));
    Assert.assertTrue(originalNodes.equals(tree.nodes()));
  }

  private MutableCTreeNetwork<String, Integer> createTree() {
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

    return tree;
  }
}
