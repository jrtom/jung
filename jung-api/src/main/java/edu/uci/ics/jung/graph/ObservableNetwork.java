package edu.uci.ics.jung.graph;

import static java.util.Collections.synchronizedList;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.graph.event.NetworkEvent;
import edu.uci.ics.jung.graph.event.NetworkEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A decorator class for graphs which generates events
 *
 * @author Joshua O'Madadhain
 */
public class ObservableNetwork<V, E>
    implements MutableNetwork<V, E> { // extends MutableNetworkDecorator<V, E> {

  List<NetworkEventListener<V, E>> listenerList =
      synchronizedList(new ArrayList<NetworkEventListener<V, E>>());
  MutableNetwork<V, E> delegate;

  /**
   * Creates a new instance based on the provided {@code delegate}.
   *
   * @param delegate the graph on which this class operates
   */
  public ObservableNetwork(MutableNetwork<V, E> delegate) {
    this.delegate = delegate;
  }

  /**
   * Adds {@code l} as a listener to this graph.
   *
   * @param l the listener to add
   */
  public void addGraphEventListener(NetworkEventListener<V, E> l) {
    listenerList.add(l);
  }

  /**
   * Removes {@code l} as a listener to this graph.
   *
   * @param l the listener to remove
   */
  public void removeGraphEventListener(NetworkEventListener<V, E> l) {
    listenerList.remove(l);
  }

  protected void fireGraphEvent(NetworkEvent<V, E> evt) {
    for (NetworkEventListener<V, E> listener : listenerList) {
      listener.handleGraphEvent(evt);
    }
  }

  @Override
  public boolean addEdge(V v1, V v2, E e) {
    boolean state = delegate.addEdge(v1, v2, e);
    if (state) {
      NetworkEvent<V, E> evt =
          new NetworkEvent.Edge<V, E>(delegate, NetworkEvent.Type.EDGE_ADDED, e);
      fireGraphEvent(evt);
    }
    return state;
  }

  @Override
  public boolean addNode(V vertex) {
    boolean state = delegate.addNode(vertex);
    if (state) {
      NetworkEvent<V, E> evt =
          new NetworkEvent.Node<V, E>(delegate, NetworkEvent.Type.VERTEX_ADDED, vertex);
      fireGraphEvent(evt);
    }
    return state;
  }

  @Override
  public boolean removeEdge(E edge) {
    boolean state = delegate.removeEdge(edge);
    if (state) {
      NetworkEvent<V, E> evt =
          new NetworkEvent.Edge<V, E>(delegate, NetworkEvent.Type.EDGE_REMOVED, (E) edge);
      fireGraphEvent(evt);
    }
    return state;
  }

  @Override
  public boolean removeNode(V vertex) {
    // remove all incident edges first, so that the appropriate events will
    // be fired (otherwise they'll be removed inside {@code
    // delegate.removeNode}
    // and the events will not be fired)
    List<E> incident_edges = ImmutableList.copyOf(delegate.incidentEdges(vertex));
    for (E e : incident_edges) {
      this.removeEdge(e);
    }

    boolean state = delegate.removeNode(vertex);
    if (state) {
      NetworkEvent<V, E> evt =
          new NetworkEvent.Node<V, E>(delegate, NetworkEvent.Type.VERTEX_REMOVED, (V) vertex);
      fireGraphEvent(evt);
    }
    return state;
  }

  @Override
  public Set<E> adjacentEdges(E edge) {
    return delegate.adjacentEdges(edge);
  }

  @Override
  public Set<V> adjacentNodes(V node) {
    return delegate.adjacentNodes(node);
  }

  @Override
  public boolean allowsParallelEdges() {
    return delegate.allowsParallelEdges();
  }

  @Override
  public boolean allowsSelfLoops() {
    return delegate.allowsSelfLoops();
  }

  @Override
  public Graph<V> asGraph() {
    return delegate.asGraph();
  }

  @Override
  public int degree(V node) {
    return delegate.degree(node);
  }

  @Override
  public ElementOrder<E> edgeOrder() {
    return delegate.edgeOrder();
  }

  @Override
  public Set<E> edges() {
    return delegate.edges();
  }

  @Override
  public Set<E> edgesConnecting(V nodeU, V nodeV) {
    return delegate.edgesConnecting(nodeU, nodeV);
  }

  @Override
  public int inDegree(V node) {
    return delegate.inDegree(node);
  }

  @Override
  public Set<E> inEdges(V node) {
    return delegate.inEdges(node);
  }

  @Override
  public Set<E> incidentEdges(V node) {
    return delegate.incidentEdges(node);
  }

  @Override
  public EndpointPair<V> incidentNodes(E edge) {
    return delegate.incidentNodes(edge);
  }

  @Override
  public boolean isDirected() {
    return delegate.isDirected();
  }

  @Override
  public ElementOrder<V> nodeOrder() {
    return delegate.nodeOrder();
  }

  @Override
  public Set<V> nodes() {
    return delegate.nodes();
  }

  @Override
  public int outDegree(V node) {
    return delegate.outDegree(node);
  }

  @Override
  public Set<E> outEdges(V node) {
    return delegate.outEdges(node);
  }

  @Override
  public Set<V> predecessors(V node) {
    return delegate.predecessors(node);
  }

  @Override
  public Set<V> successors(V node) {
    return delegate.successors(node);
  }
}
