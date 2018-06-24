package edu.uci.ics.jung.visualization.spatial.rtree;

import edu.uci.ics.jung.visualization.spatial.TreeNode;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for R-Tree nodes Includes static functions for area, union, margin, overlap
 *
 * @author Tom Nelson
 */
public interface Node<T> extends TreeNode, Bounded {

  Logger log = LoggerFactory.getLogger(Node.class);

  public static final int M = 10;
  public static final int m = (int) (M * .4); // m is 40% of M

  String asString(String margin);

  T getPickedObject(Point2D p);

  int size();

  void setParent(Node<T> node);

  Optional<Node<T>> getParent();

  Node<T> add(SplitterContext<T> splitterContext, T element, Rectangle2D bounds);

  boolean isLeafChildren();

  int count();

  Set<LeafNode<T>> getContainingLeafs(Set<LeafNode<T>> containingLeafs, double x, double y);

  Set<LeafNode<T>> getContainingLeafs(Set<LeafNode<T>> containingLeafs, Point2D p);

  LeafNode<T> getContainingLeaf(T element);

  Node<T> remove(T element);

  Node<T> recalculateBounds();

  Collection<Shape> collectGrids(Collection<Shape> list);

  Set<T> getVisibleElements(Set<T> visibleElements, Shape shape);

  static String asString(List<Shape> rectangles) {
    StringBuilder sb = new StringBuilder();
    for (Shape r : rectangles) {
      sb.append(asString(r.getBounds()));
      sb.append("\n");
    }
    return sb.toString();
  }

  static String asString(Rectangle2D r) {
    return "["
        + (int) r.getX()
        + ","
        + (int) r.getY()
        + ","
        + (int) r.getWidth()
        + ","
        + (int) r.getHeight()
        + "]";
  }

  static <T> String asString(Map.Entry<T, Rectangle2D> entry) {
    return entry.getKey() + "->" + asString(entry.getValue());
  }

  static <T> String asString(Node<T> node, String margin) {
    StringBuilder s = new StringBuilder();
    s.append(margin);
    s.append("bounds=");
    s.append(asString(node.getBounds()));
    s.append('\n');

    s.append(node.asString(margin + marginIncrement));
    return s.toString();
  }

  String marginIncrement = "   ";

  static <T> Rectangle2D entryBoundingBox(Collection<Map.Entry<T, Rectangle2D>> entries) {
    Rectangle2D boundingBox = null;
    for (Map.Entry<T, Rectangle2D> entry : entries) {
      Rectangle2D rectangle = entry.getValue();
      if (boundingBox == null) {
        boundingBox = rectangle;
      } else {
        boundingBox = boundingBox.createUnion(rectangle);
      }
    }
    return boundingBox;
  }

  static <T> Rectangle2D nodeBoundingBox(Collection<Node<T>> nodes) {
    Rectangle2D boundingBox = null;
    for (Node<T> node : nodes) {
      Rectangle2D rectangle = node.getBounds();
      if (boundingBox == null) {
        boundingBox = rectangle;
      } else {
        boundingBox = boundingBox.createUnion(rectangle);
      }
    }
    return boundingBox;
  }

  static Rectangle2D boundingBox(Collection<Rectangle2D> rectangles) {
    Rectangle2D boundingBox = null;
    for (Rectangle2D rectangle : rectangles) {
      if (boundingBox == null) {
        boundingBox = rectangle;
      } else {
        boundingBox = boundingBox.createUnion(rectangle);
      }
    }
    return boundingBox;
  }

  static double area(Collection<Rectangle2D> rectangles) {
    return area(boundingBox(rectangles));
  }

  static <T> double nodeArea(Collection<Node<T>> nodes) {
    return area(nodeBoundingBox(nodes));
  }

  static <T> double entryArea(Collection<Map.Entry<T, Rectangle2D>> entries) {
    return area(entryBoundingBox(entries));
  }

  static <T> double entryArea(
      Collection<Map.Entry<T, Rectangle2D>> left, Collection<Map.Entry<T, Rectangle2D>> right) {
    return entryArea(left) + entryArea(right);
  }

  static <T> double nodeArea(Collection<Node<T>> left, Collection<Node<T>> right) {
    return nodeArea(left) + nodeArea(right);
  }

  static double area(Collection<Rectangle2D> left, Collection<Rectangle2D> right) {
    return area(left) + area(right);
  }

  static double area(Rectangle2D r) {
    double area = r.getWidth() * r.getHeight();
    return area < 0 ? -area : area;
  }

  static double area(Rectangle2D left, Rectangle2D right) {
    return area(left) + area(right);
  }

  static double margin(Collection<Rectangle2D> rectangles) {
    return margin(boundingBox(rectangles));
  }

  static double margin(Rectangle2D r) {
    double width = r.getMaxX() - r.getMinX();
    double height = r.getMaxY() - r.getMinY();
    return 2 * (width + height);
  }

  static double margin(Rectangle2D left, Rectangle2D right) {
    return margin(left) + margin(right);
  }

  static double margin(Collection<Rectangle2D> left, Collection<Rectangle2D> right) {
    return margin(left) + margin(right);
  }

  static <T> double nodeMargin(Collection<Node<T>> left, Collection<Node<T>> right) {
    return margin(nodeBoundingBox(left)) + margin(nodeBoundingBox(right));
  }

  static <T> double entryMargin(
      Collection<Map.Entry<T, Rectangle2D>> left, Collection<Map.Entry<T, Rectangle2D>> right) {
    return margin(entryBoundingBox(left)) + margin(entryBoundingBox(right));
  }

  static <T> double entryOverlap(
      Collection<Map.Entry<T, Rectangle2D>> left, Collection<Map.Entry<T, Rectangle2D>> right) {
    return overlap(entryBoundingBox(left), entryBoundingBox(right));
  }

  static <T> double nodeOverlap(Collection<Node<T>> left, Collection<Node<T>> right) {
    return overlap(nodeBoundingBox(left), nodeBoundingBox(right));
  }

  static double overlap(Collection<Rectangle2D> left, Collection<Rectangle2D> right) {
    return overlap(boundingBox(left), boundingBox(right));
  }

  static double overlap(Rectangle2D left, Rectangle2D right) {
    return area(left.createIntersection(right));
  }

  static Rectangle2D union(Collection<? extends Bounded> boundedItems) {
    Rectangle2D union = null;
    for (Bounded r : boundedItems) {
      if (union == null) {
        union = r.getBounds();
      } else {
        union = r.getBounds().createUnion(union);
      }
    }
    return union;
  }

  static double width(Collection<? extends Bounded> boundedItems) {
    double min = 600;
    double max = 0;
    for (Bounded b : boundedItems) {
      min = Math.min(b.getBounds().getMinX(), min);
      max = Math.max(b.getBounds().getMaxX(), max);
    }
    return max - min;
  }
}
