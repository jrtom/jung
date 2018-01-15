package edu.uci.ics.jung.layout.truth;

import static com.google.common.truth.Truth.assertWithMessage;
import static edu.uci.ics.jung.layout.truth.PointSubject.assertThat;
import static edu.uci.ics.jung.layout.truth.PointSubject.points;
import static edu.uci.ics.jung.layout.truth.Rectangle2DSubject.rectangles;

import edu.uci.ics.jung.layout.model.Point;
import java.awt.geom.Rectangle2D;
import org.junit.Test;

public class SubjectExtensionTest {

  @Test
  public void testPoints() {

    assertWithMessage("they were not close enough")
        .about(points())
        .that(Point.of(1.0, 1.0))
        .isWithin(0.1)
        .of(Point.of(1.0, .9));
    assertThat(Point.of(1.0, 1.0)).isWithin(0.1).of(Point.of(.9, 0.9));
    assertThat(Point.of(1.0, 0.8)).isNotWithin(0.1).of(Point.of(1.0, 1.0));
  }

  @Test
  public void testRectangle2Ds() {

    assertWithMessage("they were not close enough")
        .about(rectangles())
        .that(new Rectangle2D.Double(1.0, 1.0, 5, 5))
        .isWithin(0.1)
        .of(new Rectangle2D.Double(1.0, .9, 5, 5));
    Rectangle2DSubject.assertThat(new Rectangle2D.Double(1.0, 1.0, 5.0, 5.0))
        .isWithin(0.1)
        .of(new Rectangle2D.Double(1.0, 1.0, 5.0, 4.9));
    Rectangle2DSubject.assertThat(new Rectangle2D.Double(1.0, 0.8, 5, 5))
        .isNotWithin(0.1)
        .of(new Rectangle2D.Double(1.0, 1.0, 5, 5));
  }
}
