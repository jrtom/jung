package edu.uci.ics.jung.visualization

import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.junit.Test

/**
 * @author Tom Nelson
 */
class ShapeTest {

  @Test
  fun testInside() {
    val r = Rectangle2D.Double(0.0, 0.0, 500.0, 500.0)
    var p: Point2D = Point2D.Double(0.0, 0.0)

    System.err.println("$p is inside $r :${r.contains(p)}")

    p = Point2D.Double(500.0, 500.0)
    System.err.println("$p is inside $r :${r.contains(p)}")

    System.err.println("$p is whatever $r :${r.intersects(0.0, 0.0, 1.0, 1.0)}")
  }
}
