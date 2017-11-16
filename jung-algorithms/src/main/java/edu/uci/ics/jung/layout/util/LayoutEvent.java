package edu.uci.ics.jung.layout.util;

/**
 * an event with information about a node and its (new?) location
 *
 * @author Tom Nelson
 * @param <N>
 * @param <P>
 */
public class LayoutEvent<N, P> {

  final N node;
  final P location;

  public LayoutEvent(N node, P location) {
    this.node = node;
    this.location = location;
  }

  public N getNode() {
    return node;
  }

  public P getLocation() {
    return location;
  }
}
