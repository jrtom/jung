package edu.uci.ics.jung.layout.spatial

import edu.uci.ics.jung.layout.model.Point
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.junit.Assert
import org.junit.Test

class ShapesTest {

  @Test
  fun testCircleAgainstAwt() {
    for (i in 0 until 10000) {
      val cx = Math.random() * 500
      val cy = Math.random() * 500
      val radius = Math.random() * 20
      val c = Circle(Point.of(cx, cy), radius)
      val e2d = Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2)
      val rx = Math.random() * 500
      val ry = Math.random() * 500
      val w = Math.random() * 20
      val h = Math.random() * 20
      val r = Rectangle(rx, ry, w, h)
      val r2d = Rectangle2D.Double(rx, ry, w, h)

      Assert.assertEquals(c.intersects(r), e2d.intersects(r2d))
    }
  }

  @Test
  fun testPointAgainstAwt() {
    for (i in 0 until 10000) {
      val px = Math.random() * 500
      val py = Math.random() * 500
      val p = Point.of(px, py)
      val p2d = Point2D.Double(px, py)
      val rx = Math.random() * 500
      val ry = Math.random() * 500
      val w = Math.random() * 20
      val h = Math.random() * 20
      val r = Rectangle(rx, ry, w, h)
      val r2d = Rectangle2D.Double(rx, ry, w, h)

      Assert.assertEquals(p.inside(r), r2d.contains(p2d))
    }
  }
}
