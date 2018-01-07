package edu.uci.ics.jung.visualization.spatial.rtree;

import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Map;

/**
 * A comparator to compare along the x-axis, Nodes where the values are Rectangle2D First compare
 * the min x values, then the max x values
 *
 * @author Tom Nelson
 * @param <T>
 */
public class HorizontalEdgeNodeComparator<T> implements Comparator<Node<T>> {
  /**
   * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer
   * as the first argument is less than, equal to, or greater than the second.
   *
   * <p>
   *
   * <p>In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt>
   * designates the mathematical <i>signum</i> function, which is defined to return one of
   * <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i> is
   * negative, zero or positive.
   *
   * <p>
   *
   * <p>The implementor must ensure that <tt>sgn(compare(x, y)) == -sgn(compare(y, x))</tt> for all
   * <tt>x</tt> and <tt>y</tt>. (This implies that <tt>compare(x, y)</tt> must throw an exception if
   * and only if <tt>compare(y, x)</tt> throws an exception.)
   *
   * <p>
   *
   * <p>The implementor must also ensure that the relation is transitive: <tt>((compare(x, y)&gt;0)
   * &amp;&amp; (compare(y, z)&gt;0))</tt> implies <tt>compare(x, z)&gt;0</tt>.
   *
   * <p>
   *
   * <p>Finally, the implementor must ensure that <tt>compare(x, y)==0</tt> implies that
   * <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all <tt>z</tt>.
   *
   * <p>
   *
   * <p>It is generally the case, but <i>not</i> strictly required that <tt>(compare(x, y)==0) ==
   * (x.equals(y))</tt>. Generally speaking, any comparator that violates this condition should
   * clearly indicate this fact. The recommended language is "Note: this comparator imposes
   * orderings that are inconsistent with equals."
   *
   * @param left the first object to be compared.
   * @param right the second object to be compared.
   * @return a negative integer, zero, or a positive integer as the first argument is less than,
   *     equal to, or greater than the second.
   * @throws NullPointerException if an argument is null and this comparator does not permit null
   *     arguments
   * @throws ClassCastException if the arguments' types prevent them from being compared by this
   *     comparator.
   */
  public int compare(Rectangle2D left, Rectangle2D right) {
    if (left.getMinX() == right.getMinX()) {
      if (left.getMaxX() == right.getMaxX()) return 0;
      if (left.getMaxX() < right.getMaxX()) return -1;
      else return 1;
    } else {
      if (left.getMinX() < right.getMinX()) return -1;
      return 1;
    }
  }

  public int compare(Map.Entry<?, Rectangle2D> leftNode, Map.Entry<?, Rectangle2D> rightNode) {
    return compare(leftNode.getValue(), rightNode.getValue());
  }

  @Override
  public int compare(Node<T> leftNode, Node<T> rightNode) {
    return compare(leftNode.getBounds(), rightNode.getBounds());
  }
}
