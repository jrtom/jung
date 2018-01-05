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
import java.util.Set;

/**
 * A decorator class for graphs which generates events
 *
 * @author Joshua O'Madadhain
 */
// TODO: Add tests in similar fashion to CTreeTest and AbstractCTreeTest
public class ObservableNetwork<N, E> implements MutableNetwork<N, E> {

  private List<NetworkEventListener<N, E>> listenerList =
      synchronizedList(new ArrayList<NetworkEventListener<N, E>>());
  private MutableNetwork<N, E> delegate;

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

  protected void fireGraphEvent(NetworkEvent<N, E> evt) {
    checkNotNull(evt, "evt");
    for (NetworkEventListener<N, E> listener : listenerList) {
      listener.handleGraphEvent(evt);
    }
  }

  @Override
  public boolean addEdge(N n1, N n2, E e) {
    checkNotNull(n1, "n1");
    checkNotNull(n2, "n2");
    checkNotNull(e, "e");
    boolean state = delegate.addEdge(n1, n2, e);
    if (state) {
      NetworkEvent<N, E> evt = new NetworkEvent.Edge<>(delegate, NetworkEvent.Type.EDGE_ADDED, e);
      fireGraphEvent(evt);
    }
    return state;
  }

  @Override
  public boolean addNode(N node) {
    checkNotNull(node, "node");
    boolean state = delegate.addNode(node);
    if (state) {
      NetworkEvent<N, E> evt =
          new NetworkEvent.Node<>(delegate, NetworkEvent.Type.NODE_ADDED, node);
      fireGraphEvent(evt);
    }
    return state;
  }

  @Override
  public boolean removeEdge(E edge) {
    checkNotNull(edge, "edge");
    boolean state = delegate.removeEdge(edge);
    if (state) {
      NetworkEvent<N, E> evt =
          new NetworkEvent.Edge<>(delegate, NetworkEvent.Type.EDGE_REMOVED, edge);
      fireGraphEvent(evt);
    }
    return state;
  }

  @Override
  public boolean removeNode(N node) {
    checkNotNull(node, "node");
    // remove all incident edges first, so that the appropriate events will
    // be fired (otherwise they'll be removed inside {@code
    // delegate.removeNode}
    // and the events will not be fired)
    List<E> incidentEdges = ImmutableList.copyOf(delegate.incidentEdges(node));
    for (E e : incidentEdges) {
      this.removeEdge(e);
    }

    boolean state = delegate.removeNode(node);
    if (state) {
      NetworkEvent<N, E> evt =
          new NetworkEvent.Node<>(delegate, NetworkEvent.Type.NODE_REMOVED, node);
      fireGraphEvent(evt);
    }
    return state;
  }

  @Override
  public Set<E> adjacentEdges(E edge) {
    checkNotNull(edge, "edge");
    return delegate.adjacentEdges(edge);
  }

  @Override
  public Set<N> adjacentNodes(N node) {
    checkNotNull(node, "node");
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
    checkNotNull(node, "node");
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
    checkNotNull(nodeU, "nodeU");
    checkNotNull(nodeV, "nodeV");
    return delegate.edgesConnecting(nodeU, nodeV);
  }

  @Override
  public int inDegree(N node) {
    checkNotNull(node, "node");
    return delegate.inDegree(node);
  }

  @Override
  public Set<E> inEdges(N node) {
    checkNotNull(node, "node");
    return delegate.inEdges(node);
  }

  @Override
  public Set<E> incidentEdges(N node) {
    checkNotNull(node, "node");
    return delegate.incidentEdges(node);
  }

  @Override
  public EndpointPair<N> incidentNodes(E edge) {
    checkNotNull(edge, "edge");
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
    checkNotNull(node, "node");
    return delegate.outDegree(node);
  }

  @Override
  public Set<E> outEdges(N node) {
    checkNotNull(node, "node");
    return delegate.outEdges(node);
  }

  @Override
  public Set<N> predecessors(N node) {
    checkNotNull(node, "node");
    return delegate.predecessors(node);
  }

  @Override
  public Set<N> successors(N node) {
    checkNotNull(node, "node");
    return delegate.successors(node);
  }
}
