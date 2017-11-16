package edu.uci.ics.jung.layout.model;

/**
 * @author Tom Nelson
 * @param <P>
 */
public abstract class AbstractPointModel<P> implements PointModel<P> {

  /**
   * @param from
   * @param to
   * @return squared distance between points
   */
  public double distanceSquared(P from, P to) {
    double deltaX = getX(to) - getX(from);
    double deltaY = getY(to) - getY(from);
    double deltaZ = getZ(to) - getZ(from);
    return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
  }

  /**
   * @param from
   * @param to
   * @return distance between points
   */
  public double distance(P from, P to) {
    return Math.sqrt(distanceSquared(from, to));
  }

  public double distance(P p) {
    return distance(p, newPoint());
  }
}
