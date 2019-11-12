package edu.uci.ics.jung.graph;

import static com.google.common.base.Preconditions.checkNotNull;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A decorator class for graphs which generates events
 *
 * @author Joshua O'Madadhain
 */
// TODO: Add tests in similar fashion to CTreeTest and AbstractCTreeTest
public class ObservableNetwork<N, E> implements MutableNetwork<N, E> {

  List<NetworkEventListener<N, E>> listenerList =
      synchronizedList(new ArrayList<NetworkEventListener<N, E>>());
  MutableNetwork<N, E> delegate;

  /**
   * Creates a new instance based on the provided {@code delegate}.
   *
   * @param delegate the graph on which this class operates
   */
  public ObservableNetwork(MutableNetwork<N, E> delegate) {
    this.delegate = checkNotNull(delegate, "delegate");
  }

  /**
   * Adds {@code l} as a listener to this graph.
   *
   * @param l the listener to add
   */
  public void addGraphEventListener(NetworkEventListener<N, E> l) {
    listenerList.add(checkNotNull(l, "l"));
  }

  /**
   * Removes {@code l} as a listener to this graph.
   *
   * @param l the listener to remove
   */
  public void removeGraphEventListener(NetworkEventListener<N, E> l) {
    listenerList.remove(checkNotNull(l, "l"));
  }

  protected void fireGraphEvent(Supplier<NetworkEvent<N, E>> evtSupplier) {
    checkNotNull(evtSupplier, "evtSupplier");
    if (listenerList.isEmpty()) return;

    NetworkEvent<N, E> evt = evtSupplier.get();
    checkNotNull(evt, "evt");

    for (NetworkEventListener<N, E> listener : listenerList) {
      listener.handleGraphEvent(evt);
    }
  }

  @Override
  public boolean addEdge(N v1, N v2, E e) {
    boolean state = delegate.addEdge(v1, v2, e);
    if (state) {
      fireGraphEvent(() -> new NetworkEvent.Edge<>(delegate, NetworkEvent.Type.EDGE_ADDED, e));
    }
    return state;
  }

  @Override
  public boolean addNode(N node) {
    boolean state = delegate.addNode(node);
    if (state) {
      fireGraphEvent(() -> new NetworkEvent.Node<>(delegate, NetworkEvent.Type.NODE_ADDED, node));
    }
    return state;
  }

  @Override
  public boolean removeEdge(E edge) {
    boolean state = delegate.removeEdge(edge);
    if (state) {
      fireGraphEvent(
          () -> new NetworkEvent.Edge<N, E>(delegate, NetworkEvent.Type.EDGE_REMOVED, (E) edge));
    }
    return state;
  }

  @Override
  public boolean removeNode(N node) {
    // remove all incident edges first, so that the appropriate events will
    // be fired (otherwise they'll be removed inside {@code
    // delegate.removeNode}
    // and the events will not be fired)
    List<E> incident_edges = ImmutableList.copyOf(delegate.incidentEdges(node));
    for (E e : incident_edges) {
      this.removeEdge(e);
    }

    boolean state = delegate.removeNode(node);
    if (state) {
      fireGraphEvent(
          () -> new NetworkEvent.Node<>(delegate, NetworkEvent.Type.NODE_REMOVED, (N) node));
    }
    return state;
  }

  @Override
  public Set<E> adjacentEdges(E edge) {
    return delegate.adjacentEdges(edge);
  }

  @Override
  public Set<N> adjacentNodes(N node) {
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
  public Graph<N> asGraph() {
    return delegate.asGraph();
  }

  @Override
  public int degree(N node) {
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
  public Set<E> edgesConnecting(N nodeU, N nodeV) {
    return delegate.edgesConnecting(nodeU, nodeV);
  }

  @Override
  public Optional<E> edgeConnecting(N nodeU, N nodeV) {
    return delegate.edgeConnecting(nodeU, nodeV);
  }

  @Override
  public E edgeConnectingOrNull(N nodeU, N nodeV) {
    return delegate.edgeConnectingOrNull(nodeU, nodeV);
  }

  @Override
  public boolean hasEdgeConnecting(N nodeU, N nodeV) {
    return delegate.hasEdgeConnecting(nodeU, nodeV);
  }

  @Override
  public int inDegree(N node) {
    return delegate.inDegree(node);
  }

  @Override
  public Set<E> inEdges(N node) {
    return delegate.inEdges(node);
  }

  @Override
  public Set<E> incidentEdges(N node) {
    return delegate.incidentEdges(node);
  }

  @Override
  public EndpointPair<N> incidentNodes(E edge) {
    return delegate.incidentNodes(edge);
  }

  @Override
  public boolean isDirected() {
    return delegate.isDirected();
  }

  @Override
  public ElementOrder<N> nodeOrder() {
    return delegate.nodeOrder();
  }

  @Override
  public Set<N> nodes() {
    return delegate.nodes();
  }

  @Override
  public int outDegree(N node) {
    return delegate.outDegree(node);
  }

  @Override
  public Set<E> outEdges(N node) {
    return delegate.outEdges(node);
  }

  @Override
  public Set<N> predecessors(N node) {
    return delegate.predecessors(node);
  }

  @Override
  public Set<N> successors(N node) {
    return delegate.successors(node);
  }
}
