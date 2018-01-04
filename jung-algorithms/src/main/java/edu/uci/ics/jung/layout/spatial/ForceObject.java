package edu.uci.ics.jung.layout.spatial;

import edu.uci.ics.jung.layout.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Tom Nelson */
public class ForceObject<T> {

  private static final Logger log = LoggerFactory.getLogger(ForceObject.class);
  private static final double GRAVITATIONAL_CONSTANT = 6.67e-11f;

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
    this(element, Point.of(x, y), 1);
  }

  public ForceObject(T element, double x, double y, double mass) {
    this.element = element;
    p = Point.of(x, y);
    this.mass = mass;
  }

  public Point getForce() {
    return f;
  }

  public void resetForce() {
    this.f = Point.ORIGIN;
  }

  void addForceFrom(ForceObject other) {
    double EPS = 3E4; // softening parameter
    double dx = other.p.x - this.p.x;
    double dy = other.p.y - this.p.y;
    double dist = Math.sqrt(dx * dx + dy * dy);
    if (dist == 0) {
      log.error("got a zero distance comparing {} with {}", this, other);
    }
    double force = (this.mass * other.mass * GRAVITATIONAL_CONSTANT) / (dist * dist + EPS * EPS);
    log.trace(
        "force on {} from {} is {}, distance is {}", this.element, other.element, force, dist);

    this.f = f.add(force * dx / dist, force * dy / dist);
  }

  public ForceObject add(ForceObject other) {
    double totalMass = this.mass + other.mass;
    Point p =
        Point.of(
            (this.p.x * this.mass + other.p.x * other.mass) / totalMass,
            (this.p.y * this.mass + other.p.y * other.mass) / totalMass);

    ForceObject<String> forceObject = new ForceObject<>("force", p, totalMass);
    return forceObject;
  }

  public T getElement() {
    return element;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ForceObject<?> that = (ForceObject<?>) o;

    if (Double.compare(that.mass, mass) != 0) return false;
    if (p != null ? !p.equals(that.p) : that.p != null) return false;
    return element != null ? element.equals(that.element) : that.element == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = p != null ? p.hashCode() : 0;
    temp = Double.doubleToLongBits(mass);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (element != null ? element.hashCode() : 0);
    return result;
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
