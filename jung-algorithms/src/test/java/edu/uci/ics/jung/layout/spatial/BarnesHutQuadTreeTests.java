package edu.uci.ics.jung.layout.spatial;

import edu.uci.ics.jung.layout.model.Point;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test of basic construction of the BarnesHutQuadTree, including an edge case with forceObjects at
 * the same location
 *
 * @author Tom Nelson
 */
public class BarnesHutQuadTreeTests {

  private static final Logger log = LoggerFactory.getLogger(BarnesHutQuadTreeTests.class);
  private BarnesHutQuadTree<String> tree;

  @Before
  public void setup() {
    tree = new BarnesHutQuadTree<>(500, 500);
  }

  /**
   * test that edge case where all force objects are at the same location results in the correct
   * tree
   */
  @Test
  public void testOne() {
    ForceObject<String> forceObjectOne = new ForceObject("A", Point.of(10, 10));
    ForceObject<String> forceObjectTwo = new ForceObject("B", Point.of(10, 10));
    ForceObject<String> forceObjectThree = new ForceObject("C", Point.of(10, 10));
    tree.insert(forceObjectOne);
    tree.insert(forceObjectTwo);
    tree.insert(forceObjectThree);

    log.info("tree: {}", tree);
    ForceObject<String> expectedForceObject = new ForceObject("force", Point.of(10, 10), 3);
    Assert.assertTrue(tree.getRoot().forceObject.equals(expectedForceObject));
  }

  /** test a simple construction */
  @Test
  public void testTwo() {
    ForceObject<String> forceObjectA = new ForceObject<>("A", Point.of(200, 100));
    ForceObject<String> forceObjectB = new ForceObject<>("B", Point.of(100, 200));
    ForceObject<String> forceObjectC = new ForceObject<>("C", Point.of(100, 100));
    ForceObject<String> forceObjectD = new ForceObject<>("D", Point.of(500, 100));
    tree.insert(forceObjectA);
    tree.insert(forceObjectB);
    tree.insert(forceObjectC);
    tree.insert(forceObjectD);

    log.info("tree: {}", tree);
    Assert.assertTrue(tree.getRoot() != null);
    Node<String> root = tree.getRoot();
    Assert.assertTrue(root.isLeaf() == false);
    Node<String> NW = root.NW;
    Assert.assertTrue(NW.forceObject.equals(forceObjectA.add(forceObjectB).add(forceObjectC)));
    Assert.assertTrue(NW.isLeaf() == false);
    Assert.assertTrue(NW.NW.forceObject.equals(forceObjectC));
    Assert.assertTrue(NW.NE.forceObject.equals(forceObjectA));
    Assert.assertTrue(NW.SW.forceObject.equals(forceObjectB));
    Assert.assertTrue(NW.SE.forceObject == null);
    Assert.assertTrue(root.NE.forceObject.equals(forceObjectD));
  }
}
