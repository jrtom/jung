package edu.uci.ics.jung.graph.event;

import com.google.common.graph.Network;

/**
 * @author tom nelson
 * @param <V> the node type
 * @param <E> the edge type
 */
public abstract class NetworkEvent<V, E> {

  protected Network<V, E> source;
  protected Type type;

  /**
   * Creates an instance with the specified {@code source} graph and {@code Type} (node/edge
   * addition/removal).
   *
   * @param source the graph whose event this is
   * @param type the type of event this is
   */
  public NetworkEvent(Network<V, E> source, Type type) {
    this.source = source;
    this.type = type;
  }

  /** Types of graph events. */
  public static enum Type {
    NODE_ADDED,
    NODE_REMOVED,
    EDGE_ADDED,
    EDGE_REMOVED
  }

  /** An event type pertaining to graph nodes. */
  public static class Node<V, E> extends NetworkEvent<V, E> {
    protected V node;

    /**
     * Creates a graph event for the specified graph, node, and type.
     *
     * @param source the graph whose event this is
     * @param type the type of event this is
     * @param node the node involved in this event
     */
    public Node(Network<V, E> source, Type type, V node) {
      super(source, type);
      this.node = node;
    }

    /** @return the node associated with this event */
    public V getNode() {
      return node;
    }

    @Override
    public String toString() {
      return "GraphEvent type:" + type + " for " + node;
    }
  }

  /** An event type pertaining to graph edges. */
  public static class Edge<V, E> extends NetworkEvent<V, E> {
    protected E edge;

    /**
     * Creates a graph event for the specified graph, edge, and type.
     *
     * @param source the graph whose event this is
     * @param type the type of event this is
     * @param edge the edge involved in this event
     */
    public Edge(Network<V, E> source, Type type, E edge) {
      super(source, type);
      this.edge = edge;
    }

    /** @return the edge associated with this event. */
    public E getEdge() {
      return edge;
    }

    @Override
    public String toString() {
      return "GraphEvent type:" + type + " for " + edge;
    }
  }

  /** @return the source */
  public Network<V, E> getSource() {
    return source;
  }

  /** @return the type */
  public Type getType() {
    return type;
  }
}
