package edu.uci.ics.jung.visualization.spatial.rtree

import com.google.common.collect.Maps
import edu.uci.ics.jung.visualization.spatial.TreeNode
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.util.Random
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * @author Tom Nelson
 */
class RTreeTest {

  companion object {
    private val log = LoggerFactory.getLogger(RTreeTest::class.java)
  }

  val splitterContext: SplitterContext<String> =
    SplitterContext.of(RStarLeafSplitter(), RStarSplitter())
  private lateinit var rTree: RTree<String>
  private lateinit var r1: Rectangle2D
  private lateinit var r2: Rectangle2D
  private lateinit var r3: Rectangle2D
  private lateinit var r4: Rectangle2D
  private lateinit var r5: Rectangle2D
  private lateinit var r6: Rectangle2D
  private lateinit var r7: Rectangle2D
  private lateinit var r8: Rectangle2D
  val linkedMap: MutableMap<String, Rectangle2D> = Maps.newLinkedHashMap()

  @Before
  fun before() {
    rTree = RTree.create()
    r1 = Rectangle2D.Double(100.0, 100.0, 100.0, 100.0)
    r2 = Rectangle2D.Double(200.0, 200.0, 100.0, 100.0)
    r3 = Rectangle2D.Double(300.0, 300.0, 100.0, 100.0)
    r4 = Rectangle2D.Double(400.0, 400.0, 100.0, 100.0)
    r5 = Rectangle2D.Double(500.0, 500.0, 100.0, 100.0)
    r6 = Rectangle2D.Double(100.0, 300.0, 100.0, 100.0)
    r7 = Rectangle2D.Double(300.0, 100.0, 100.0, 100.0)
    r8 = Rectangle2D.Double(400.0, 100.0, 100.0, 100.0)

    val generator = Random(1001)
    // generate reusable random nodes
    for (i in 0 until 30) {
      val x = generator.nextDouble() * 500
      val y = generator.nextDouble() * 500
      val width = generator.nextDouble() * (600 - x)
      val height = generator.nextDouble() * (600 - y)
      val r = Rectangle2D.Double(x, y, width, height)
      linkedMap["N$i"] = r
    }
  }

  @Test
  fun testOne() {
    var rTree: RTree<String> = RTree.create()
    rTree = rTree.add(splitterContext, "A", Rectangle2D.Double(3.0, 3.0, 200.0, 100.0))
    rTree = rTree.add(splitterContext, "B", Rectangle2D.Double(400.0, 300.0, 100.0, 100.0))
    rTree = rTree.add(splitterContext, "C", Rectangle2D.Double(200.0, 300.0, 100.0, 100.0))

    rTree = rTree.add(splitterContext, "D", Rectangle2D.Double(400.0, 120.0, 100.0, 100.0))
    rTree = rTree.add(splitterContext, "E", Rectangle2D.Double(20.0, 500.0, 10.0, 100.0))
    rTree = rTree.add(splitterContext, "F", Rectangle2D.Double(5.0, 40.0, 100.0, 100.0))
    log.info("tree {} initial size is {}", rTree, rTree.count())
    for (i in 0 until 100) {
      val x = Math.random() * 500
      val y = Math.random() * 500
      val width = Math.random() * 50 + 50
      val height = Math.random() * 50 + 50
      val r = Rectangle2D.Double(x, y, width, height)
      rTree = rTree.add(splitterContext, "N$i", r)
      log.trace("tree:$rTree")
    }
    log.trace("root:{}")
    testAreas(rTree)

    log.info("after adding 100 'N' nodes, tree size is {}", rTree.count())

    for (i in 0 until 100) {
      val element = "N$i"
      rTree = rTree.remove(element)
      log.trace("tree size:{}", rTree.count())
    }
    log.info(
      "after removing all 'N' nodes, tree {} size is back to initial size of {}",
      rTree,
      rTree.count()
    )
  }

  @Test
  fun testAddOne() {
    rTree = rTree.add(splitterContext, "A", r1)
    Assert.assertTrue(rTree.getRoot().isPresent)
    val root = rTree.getRoot().get()
    Assert.assertTrue(root is LeafNode)
    Assert.assertTrue(root.size() == 1)

    testAreas(rTree)
  }

  @Test
  fun testAddTwo() {
    rTree = rTree.add(splitterContext, "A", r1)
    rTree = rTree.add(splitterContext, "B", r2)

    testAreas(rTree)
  }

  @Test
  fun testAddThree() {
    rTree = rTree.add(splitterContext, "A", r1)
    rTree = rTree.add(splitterContext, "B", r2)
    rTree = rTree.add(splitterContext, "C", r3)

    testAreas(rTree)
  }

  @Test
  fun testAddFour() {
    rTree = rTree.add(splitterContext, "A", r1)
    rTree = rTree.add(splitterContext, "B", r2)
    rTree = rTree.add(splitterContext, "C", r3)
    rTree = rTree.add(splitterContext, "D", r4)

    testAreas(rTree)
  }

  @Test
  fun testAddFive() {
    rTree = rTree.add(splitterContext, "A", r1)
    rTree = rTree.add(splitterContext, "B", r2)
    rTree = rTree.add(splitterContext, "C", r3)
    rTree = rTree.add(splitterContext, "D", r4)
    rTree = rTree.add(splitterContext, "E", r5)

    testAreas(rTree)
  }

  @Test
  fun testAddSix() {
    rTree = rTree.add(splitterContext, "A", r1)
    rTree = rTree.add(splitterContext, "B", r2)
    rTree = rTree.add(splitterContext, "C", r3)
    rTree = rTree.add(splitterContext, "D", r4)
    rTree = rTree.add(splitterContext, "E", r5)
    rTree = rTree.add(splitterContext, "F", r6)

    testAreas(rTree)
  }

  @Test
  fun testAddSeven() {
    rTree = rTree.add(splitterContext, "A", r1)
    rTree = rTree.add(splitterContext, "B", r2)
    rTree = rTree.add(splitterContext, "C", r3)
    rTree = rTree.add(splitterContext, "D", r4)
    rTree = rTree.add(splitterContext, "E", r5)
    rTree = rTree.add(splitterContext, "F", r6)
    rTree = rTree.add(splitterContext, "G", r7)

    testAreas(rTree)
  }

  @Test
  fun testAddEight() {
    rTree = rTree.add(splitterContext, "A", r1)
    log.trace("rtree:{}", rTree)
    rTree = rTree.add(splitterContext, "B", r2)
    log.trace("rtree:{}", rTree)
    rTree = rTree.add(splitterContext, "C", r3)
    log.trace("rtree:{}", rTree)
    rTree = rTree.add(splitterContext, "D", r4)
    log.trace("rtree:{}", rTree)
    rTree = rTree.add(splitterContext, "E", r5)
    log.trace("rtree:{}", rTree)
    rTree = rTree.add(splitterContext, "F", r6)
    log.trace("rtree:{}", rTree)
    rTree = rTree.add(splitterContext, "G", r7)
    log.trace("rtree:{}", rTree)
    rTree = rTree.add(splitterContext, "H", r8)
    log.trace("rtree:{}", rTree)

    testAreas(rTree)
  }

  @Test
  fun testFindingElementsByLocation() {
    rTree = rTree.add(splitterContext, "A", r1)
    rTree = rTree.add(splitterContext, "B", r2)
    rTree = rTree.add(splitterContext, "C", r3)
    rTree = rTree.add(splitterContext, "D", r4)
    rTree = rTree.add(splitterContext, "F", r5)
    rTree = rTree.add(splitterContext, "G", r6)
    rTree = rTree.add(splitterContext, "H", r7)

    var p = Point2D.Double(r4.x + r4.width / 2, r4.y + r4.height / 2)
    var found = rTree.getPickedObject(p)
    Assert.assertEquals(found, "D")

    p = Point2D.Double(r7.x + r7.width / 2, r7.y + r7.height / 2)
    found = rTree.getPickedObject(p)
    Assert.assertEquals(found, "H")

    p = Point2D.Double(r1.x + r1.width / 2, r1.y + r1.height / 2)
    found = rTree.getPickedObject(p)
    Assert.assertEquals(found, "A")
  }

  // make sure the rectangle area in rTree is the same as the union of the areas
  // in its children (elements or children)

  private fun testAreas(rTree: RTree<String>) {
    if (rTree.getRoot().isPresent) {
      val root = rTree.getRoot().get()
      testAreas(root)
    }
  }

  private fun testAreas(rootNode: TreeNode) {
    val rootBounds = rootNode.getBounds()
    if (rootNode is InnerNode<*>) {
      Assert.assertTrue(closeEnough(rootBounds, Node.union(rootNode.getChildren())!!))
    }

    val children = rootNode.getChildren()
    if (children != null) {
      for (rt in children) {
        testAreas(rt)
      }
    }
  }

  private fun closeEnough(left: Rectangle2D, right: Rectangle2D): Boolean {
    return left == right ||
      (closeEnough(left.minX, right.minX) &&
        closeEnough(left.minY, right.minY) &&
        closeEnough(left.maxX, right.maxX) &&
        closeEnough(left.maxY, right.maxY))
  }

  private val CLOSE_ENOUGH = 0.001

  private fun closeEnough(left: Double, right: Double): Boolean {
    return Math.abs(left - right) < CLOSE_ENOUGH
  }
}
