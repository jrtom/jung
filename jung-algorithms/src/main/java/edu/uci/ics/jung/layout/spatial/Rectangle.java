package edu.uci.ics.jung.layout.spatial;

import com.google.common.base.Preconditions;
import edu.uci.ics.jung.layout.model.Point;

/**
 * Simple, immutable Rectangle class used for spatial data structures.
 *
 * @author Tom Nelson
 */
public class Rectangle {

  public final double x;
  public final double y;
  public final double width;
  public final double height;
  public final double maxX;
  public final double maxY;

  public Rectangle(double x, double y, double width, double height) {
    Preconditions.checkArgument(width >= 0 && height >= 0, "width and height must be non-negative");
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.maxX = x + width;
    this.maxY = y + height;
  }

  public double getCenterX() {
    return x + width / 2;
  }

  public double getCenterY() {
    return y + height / 2;
  }

  /**
   * fail-fast implementation to reduce computation
   *
   * @param other
   * @return
   */
  public boolean intersects(Rectangle other) {
    return maxX >= other.x && other.maxX >= x && maxY >= other.y && other.maxY >= y;
  }

  public boolean contains(Point p) {
    return contains(p.x, p.y);
  }

  public boolean contains(double ox, double oy) {
    return ox >= this.x && ox <= maxX && oy >= this.y && oy <= maxY;
  }

  public Rectangle add(double newX, double newY) {
    double x1 = Math.min(x, newX);
    double x2 = Math.max(maxX, newX);
    double y1 = Math.min(y, newY);
    double y2 = Math.max(maxY, newY);
    return new Rectangle(x1, y1, x2 - x1, y2 - y1);
  }

  @Override
  public String toString() {
    return "Rectangle{"
        + "x="
        + x
        + ", y="
        + y
        + ", width="
        + width
        + ", height="
        + height
        + ", maxX="
        + maxX
        + ", maxY="
        + maxY
        + '}';
  }
}
