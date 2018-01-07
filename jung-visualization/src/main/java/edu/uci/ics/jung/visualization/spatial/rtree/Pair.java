package edu.uci.ics.jung.visualization.spatial.rtree;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of two items. Used for pairs of lists during R*-Tree split
 *
 * @author Tom Nelson
 */
public class Pair<T> {

  private static final Logger log = LoggerFactory.getLogger(Pair.class);

  public final T left;
  public final T right;

  public static <T> Pair<T> of(T left, T right) {
    return new Pair(left, right);
  }

  public Pair(T left, T right) {
    Preconditions.checkArgument(left != right, "Attempt to create pair with 2 equal elements");
    this.left = left;
    this.right = right;
  }

  @Override
  public String toString() {
    return "Pair{" + "left=" + left + ", right=" + right + '}';
  }
}
