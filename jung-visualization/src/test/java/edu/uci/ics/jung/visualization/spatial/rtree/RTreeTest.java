package edu.uci.ics.jung.visualization.spatial.rtree;

import com.google.common.collect.Maps;
import edu.uci.ics.jung.visualization.spatial.TreeNode;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class RTreeTest {

  private static final Logger log = LoggerFactory.getLogger(RTreeTest.class);

  SplitterContext<String> splitterContext =
      SplitterContext.of(new RStarLeafSplitter(), new RStarSplitter());
  private RTree<String> rTree;
  private Rectangle2D r1;
  private Rectangle2D r2;
  private Rectangle2D r3;
  private Rectangle2D r4;
  private Rectangle2D r5;
  private Rectangle2D r6;
  private Rectangle2D r7;
  private Rectangle2D r8;
  Map<String, Rectangle2D> linkedMap = Maps.newLinkedHashMap();

  @Before
  public void before() {
    rTree = RTree.create();
    r1 = new Rectangle2D.Double(100, 100, 100, 100);
    r2 = new Rectangle2D.Double(200, 200, 100, 100);
    r3 = new Rectangle2D.Double(300, 300, 100, 100);
    r4 = new Rectangle2D.Double(400, 400, 100, 100);
    r5 = new Rectangle2D.Double(500, 500, 100, 100);
    r6 = new Rectangle2D.Double(100, 300, 100, 100);
    r7 = new Rectangle2D.Double(300, 100, 100, 100);
    r8 = new Rectangle2D.Double(400, 100, 100, 100);

    Random generator = new Random(1001);
    // generate reusable random nodes
    for (int i = 0; i < 30; i++) {

      double x = generator.nextDouble() * 500;
      double y = generator.nextDouble() * 500;
      double width = generator.nextDouble() * (600 - x);
      double height = generator.nextDouble() * (600 - y);
      Rectangle2D r = new Rectangle2D.Double(x, y, width, height);
      linkedMap.put("N" + i, r);
    }
  }

  @Test
  public void testOne() {

    RTree<String> rTree = RTree.create();
    rTree = rTree.add(splitterContext, "A", new Rectangle2D.Double(3, 3, 200, 100));
    rTree = rTree.add(splitterContext, "B", new Rectangle2D.Double(400, 300, 100, 100));
    rTree = rTree.add(splitterContext, "C", new Rectangle2D.Double(200, 300, 100, 100));

    rTree = rTree.add(splitterContext, "D", new Rectangle2D.Double(400, 120, 100, 100));
    rTree = rTree.add(splitterContext, "E", new Rectangle2D.Double(20, 500, 10, 100));
    rTree = rTree.add(splitterContext, "F", new Rectangle2D.Double(5, 40, 100, 100));
    log.info("tree {} initial size is {}", rTree, rTree.count());
    for (int i = 0; i < 100; i++) {
      double x = Math.random() * 500;
      double y = Math.random() * 500;
      double width = Math.random() * 50 + 50;
      double height = Math.random() * 50 + 50;
      Rectangle2D r = new Rectangle2D.Double(x, y, width, height);
      rTree = rTree.add(splitterContext, "N" + i, r);
      log.trace("tree:" + rTree);
    }
    log.trace("root:{}");
    testAreas(rTree);

    log.info("after adding 100 'N' nodes, tree size is {}", rTree.count());

    for (int i = 0; i < 100; i++) {
      String element = "N" + i;
      rTree = rTree.remove(element);
      log.trace("tree size:{}", rTree.count());
    }
    log.info(
        "after removing all 'N' nodes, tree {} size is back to initial size of {}",
        rTree,
        rTree.count());
  }

  @Test
  public void testAddOne() {
    rTree = rTree.add(splitterContext, "A", r1);
    //    Assert.assertTrue(rTree.level == 0);
    Assert.assertTrue(rTree.getRoot().isPresent());
    Node<String> root = rTree.getRoot().get();
    Assert.assertTrue(root instanceof LeafNode);
    Assert.assertTrue(root.size() == 1);

    testAreas(rTree);
  }

  @Test
  public void testAddTwo() {
    rTree = rTree.add(splitterContext, "A", r1);
    rTree = rTree.add(splitterContext, "B", r2);

    testAreas(rTree);
  }

  @Test
  public void testAddThree() {
    rTree = rTree.add(splitterContext, "A", r1);
    rTree = rTree.add(splitterContext, "B", r2);
    rTree = rTree.add(splitterContext, "C", r3);

    testAreas(rTree);
  }

  @Test
  public void testAddFour() {
    rTree = rTree.add(splitterContext, "A", r1);
    rTree = rTree.add(splitterContext, "B", r2);
    rTree = rTree.add(splitterContext, "C", r3);
    rTree = rTree.add(splitterContext, "D", r4);

    testAreas(rTree);
  }

  @Test
  public void testAddFive() {
    rTree = rTree.add(splitterContext, "A", r1);
    rTree = rTree.add(splitterContext, "B", r2);
    rTree = rTree.add(splitterContext, "C", r3);
    rTree = rTree.add(splitterContext, "D", r4);
    rTree = rTree.add(splitterContext, "E", r5);

    testAreas(rTree);
  }

  @Test
  public void testAddSix() {
    rTree = rTree.add(splitterContext, "A", r1);
    rTree = rTree.add(splitterContext, "B", r2);
    rTree = rTree.add(splitterContext, "C", r3);
    rTree = rTree.add(splitterContext, "D", r4);
    rTree = rTree.add(splitterContext, "E", r5);
    rTree = rTree.add(splitterContext, "F", r6);

    testAreas(rTree);
  }

  @Test
  public void testAddSeven() {
    rTree = rTree.add(splitterContext, "A", r1);
    rTree = rTree.add(splitterContext, "B", r2);
    rTree = rTree.add(splitterContext, "C", r3);
    rTree = rTree.add(splitterContext, "D", r4);
    rTree = rTree.add(splitterContext, "E", r5);
    rTree = rTree.add(splitterContext, "F", r6);
    rTree = rTree.add(splitterContext, "G", r7);

    testAreas(rTree);
  }

  @Test
  public void testAddEight() {
    rTree = rTree.add(splitterContext, "A", r1);
    log.trace("rtree:{}", rTree);
    rTree = rTree.add(splitterContext, "B", r2);
    log.trace("rtree:{}", rTree);
    rTree = rTree.add(splitterContext, "C", r3);
    log.trace("rtree:{}", rTree);
    rTree = rTree.add(splitterContext, "D", r4);
    log.trace("rtree:{}", rTree);
    rTree = rTree.add(splitterContext, "E", r5);
    log.trace("rtree:{}", rTree);
    rTree = rTree.add(splitterContext, "F", r6);
    log.trace("rtree:{}", rTree);
    rTree = rTree.add(splitterContext, "G", r7);
    log.trace("rtree:{}", rTree);
    rTree = rTree.add(splitterContext, "H", r8);
    log.trace("rtree:{}", rTree);

    testAreas(rTree);
  }

  @Test
  public void testFindingElementsByLocation() {
    rTree = rTree.add(splitterContext, "A", r1);
    rTree = rTree.add(splitterContext, "B", r2);
    rTree = rTree.add(splitterContext, "C", r3);
    rTree = rTree.add(splitterContext, "D", r4);
    rTree = rTree.add(splitterContext, "F", r5);
    rTree = rTree.add(splitterContext, "G", r6);
    rTree = rTree.add(splitterContext, "H", r7);

    Point2D p = new Point2D.Double(r4.getX() + r4.getWidth() / 2, r4.getY() + r4.getHeight() / 2);
    Object found = rTree.getPickedObject(p);
    Assert.assertEquals(found, "D");

    p = new Point2D.Double(r7.getX() + r7.getWidth() / 2, r7.getY() + r7.getHeight() / 2);
    found = rTree.getPickedObject(p);
    Assert.assertEquals(found, "H");

    p = new Point2D.Double(r1.getX() + r1.getWidth() / 2, r1.getY() + r1.getHeight() / 2);
    found = rTree.getPickedObject(p);
    Assert.assertEquals(found, "A");
  }

  // make sure the rectangle area in rTree is the same as the union of the areas
  // in its children (elements or children)

  private void testAreas(RTree<String> rTree) {
    if (rTree.getRoot().isPresent()) {
      TreeNode root = rTree.getRoot().get();
      testAreas(root);
    }
  }

  private void testAreas(TreeNode rootNode) {

    Rectangle2D rootBounds = rootNode.getBounds();
    if (rootNode instanceof InnerNode) {
      InnerNode innerNode = (InnerNode) rootNode;
      Assert.assertTrue(closeEnough(rootBounds, Node.union(innerNode.getChildren())));
    }

    for (TreeNode rt : rootNode.getChildren()) {
      testAreas(rt);
    }
  }

  private boolean closeEnough(Rectangle2D left, Rectangle2D right) {
    return left.equals(right)
        || (closeEnough(left.getMinX(), right.getMinX())
            && closeEnough(left.getMinY(), right.getMinY())
            && closeEnough(left.getMaxX(), right.getMaxX())
            && closeEnough(left.getMaxY(), right.getMaxY()));
  }

  private final double CLOSE_ENOUGH = 0.001;

  private boolean closeEnough(double left, double right) {
    return Math.abs(left - right) < CLOSE_ENOUGH;
  }
}
