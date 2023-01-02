package edu.uci.ics.jung.visualization;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.junit.Test;

/**
 * @author Tom Nelson
 */
public class ShapeTest {

  @Test
  public void testInside() {
    Rectangle2D r = new Rectangle2D.Double(0, 0, 500, 500);
    Point2D p = new Point2D.Double(0, 0);

    System.err.println(p + " is inside " + r + " :" + r.contains(p));

    p = new Point2D.Double(500, 500);
    System.err.println(p + " is inside " + r + " :" + r.contains(p));

    System.err.println(p + " is whatever " + r + " :" + r.intersects(0, 0, 1, 1));
  }
}
