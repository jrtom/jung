package edu.uci.ics.jung.layout.spatial

import edu.uci.ics.jung.layout.model.Point
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * Test of basic construction of the BarnesHutQuadTree, including an edge case with forceObjects at
 * the same location
 *
 * @author Tom Nelson
 */
class BarnesHutQuadTreeTests {

  private lateinit var tree: BarnesHutQuadTree<String>

  @Before
  fun setup() {
    tree = BarnesHutQuadTree(500.0, 500.0)
  }

  /**
   * test that edge case where all force objects are at the same location results in the correct
   * tree
   */
  @Test
  fun testOne() {
    val forceObjectOne = ForceObject<String>("A", Point.of(10.0, 10.0))
    val forceObjectTwo = ForceObject<String>("B", Point.of(10.0, 10.0))
    val forceObjectThree = ForceObject<String>("C", Point.of(10.0, 10.0))
    tree.insert(forceObjectOne)
    tree.insert(forceObjectTwo)
    tree.insert(forceObjectThree)

    log.info("tree: {}", tree)
    val expectedForceObject = ForceObject("force", Point.of(10.0, 10.0), 3.0)
    Assert.assertTrue(tree.getRoot().forceObject == expectedForceObject)
  }

  /** test a simple construction */
  @Test
  fun testTwo() {
    val forceObjectA = ForceObject<String>("A", Point.of(200.0, 100.0))
    val forceObjectB = ForceObject<String>("B", Point.of(100.0, 200.0))
    val forceObjectC = ForceObject<String>("C", Point.of(100.0, 100.0))
    val forceObjectD = ForceObject<String>("D", Point.of(500.0, 100.0))
    tree.insert(forceObjectA)
    tree.insert(forceObjectB)
    tree.insert(forceObjectC)
    tree.insert(forceObjectD)

    log.info("tree: {}", tree)
    Assert.assertTrue(tree.getRoot() != null)
    val root = tree.getRoot()
    Assert.assertTrue(root.isLeaf == false)
    val NW = root.NW!!
    Assert.assertTrue(NW.forceObject == forceObjectA.add(forceObjectB).add(forceObjectC))
    Assert.assertTrue(NW.isLeaf == false)
    Assert.assertTrue(NW.NW!!.forceObject == forceObjectC)
    Assert.assertTrue(NW.NE!!.forceObject == forceObjectA)
    Assert.assertTrue(NW.SW!!.forceObject == forceObjectB)
    Assert.assertTrue(NW.SE!!.forceObject == null)
    Assert.assertTrue(root.NE!!.forceObject == forceObjectD)
  }

  companion object {
    private val log = LoggerFactory.getLogger(BarnesHutQuadTreeTests::class.java)
  }
}
