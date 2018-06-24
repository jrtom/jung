package edu.uci.ics.jung.layout.spatial;

import edu.uci.ics.jung.layout.model.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.junit.Assert;
import org.junit.Test;

public class ShapesTest {

  @Test
  public void testCircleAgainstAwt() {
    for (int i = 0; i < 10000; i++) {
      double cx = Math.random() * 500;
      double cy = Math.random() * 500;
      double radius = Math.random() * 20;
      Circle c = new Circle(Point.of(cx, cy), radius);
      Ellipse2D e2d = new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2);
      double rx = Math.random() * 500;
      double ry = Math.random() * 500;
      double w = Math.random() * 20;
      double h = Math.random() * 20;
      Rectangle r = new Rectangle(rx, ry, w, h);
      Rectangle2D r2d = new Rectangle2D.Double(rx, ry, w, h);

      Assert.assertEquals(c.intersects(r), e2d.intersects(r2d));
    }
  }

  @Test
  public void testPointAgainstAwt() {
    for (int i = 0; i < 10000; i++) {
      double px = Math.random() * 500;
      double py = Math.random() * 500;
      Point p = Point.of(px, py);
      Point2D p2d = new Point2D.Double(px, py);
      double rx = Math.random() * 500;
      double ry = Math.random() * 500;
      double w = Math.random() * 20;
      double h = Math.random() * 20;
      Rectangle r = new Rectangle(rx, ry, w, h);
      Rectangle2D r2d = new Rectangle2D.Double(rx, ry, w, h);

      Assert.assertEquals(p.inside(r), r2d.contains(p2d));
    }
  }
}
