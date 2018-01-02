package edu.uci.ics.jung.layout.spatial;

import edu.uci.ics.jung.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class ForceObject<T> {

  private static final Logger log = LoggerFactory.getLogger(ForceObject.class);
  private static final double GRAVITY = 6.67e-11f;

  /** location of p */
  public final Point p;

  /** force vector */
  protected Point f;

  /** mass */
  protected double mass;

  T element;

  public ForceObject(T element, Point p, double mass) {
    this.element = element;
    this.p = p;
    this.f = Point.ORIGIN;
    this.mass = mass;
  }

  public ForceObject(T element, Point p) {
    this(element, p, 1);
  }

  public ForceObject(T element, double x, double y) {
    this(element, Point.of(x, y), 1000);
  }

  public ForceObject(T element, double x, double y, double mass) {
    this.element = element;
    p = Point.of(x, y);
    this.mass = mass;
  }

  public Point getForce() {
    return f;
  }

  private void addForceFrom(ForceObject other) {
    double EPS = 3E4; // softening parameter
    double dx = other.p.x - p.x;
    double dy = other.p.y - p.y;
    double dist = Math.sqrt(dx * dx + dy * dy);
    double force = (mass * other.mass * GRAVITY) / (dist * dist + EPS * EPS);
    this.f = f.add(force * dx / dist, force * dy / dist);
  }

  public ForceObject add(ForceObject other) {
    double totalMass = this.mass + other.mass;
    Point p =
        Point.of(
            (this.p.x * this.mass + other.p.x * other.mass) / totalMass,
            (this.p.y * this.mass + other.p.y * other.mass) / totalMass);

    ForceObject<String> forceObject = new ForceObject<>("force", p, totalMass);
    forceObject.addForceFrom(other);
    return forceObject;
  }

  public T getElement() {
    return element;
  }

  @Override
  public String toString() {
    return "ForceObject{"
        + "element="
        + element
        + ", p="
        + p
        + ", mass="
        + mass
        + ", force="
        + f
        + '}';
  }
}
