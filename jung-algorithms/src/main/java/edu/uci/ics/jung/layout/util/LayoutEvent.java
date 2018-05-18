package edu.uci.ics.jung.layout.util;

import edu.uci.ics.jung.layout.model.Point;

/**
 * an event with information about a node and its (new?) location
 *
 * @author Tom Nelson
 * @param <N>
 */
public class LayoutEvent<N> {

  final N node;
  final Point location;

  public LayoutEvent(N node, Point location) {
    this.node = node;
    this.location = location;
  }

  public N getNode() {
    return node;
  }

  public Point getLocation() {
    return location;
  }
}
