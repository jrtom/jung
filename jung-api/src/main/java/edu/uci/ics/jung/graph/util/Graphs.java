package edu.uci.ics.jung.graph.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import java.util.Set;

/**
 * Provides specialized implementations of <code>GraphDecorator</code>. Currently these wrapper
 * types include "synchronized" and "unmodifiable".
 *
 * <p>The methods of this class may each throw a <code>NullPointerException</code> if the graphs or
 * class objects provided to them are null.
 *
 * @author Tom Nelson
 */
public class Graphs {

  public static boolean isSelfLoop(EndpointPair<?> endpoints) {
    checkNotNull(endpoints, "endpoints");
    return endpoints.nodeU().equals(endpoints.nodeV());
  }

  public static <E> boolean isSelfLoop(Network<?, E> network, E edge) {
    checkNotNull(network, "network");
    checkNotNull(edge, "edge");
    return isSelfLoop(network.incidentNodes(edge));
  }

  public static <N, E> MutableNetwork<N, E> synchronizedNetwork(MutableNetwork<N, E> delegate) {
    return new SynchronizedNetwork<>(delegate);
  }

  private static class SynchronizedNetwork<N, E> extends AbstractNetwork<N, E>
      implements MutableNetwork<N, E> {

    private final MutableNetwork<N, E> delegate;

    private SynchronizedNetwork(MutableNetwork<N, E> delegate) {
      this.delegate = checkNotNull(delegate, "delegate");
    }

    @Override
    public synchronized Set<E> adjacentEdges(E arg0) {
      return delegate.adjacentEdges(arg0);
    }

    @Override
    public synchronized Set<N> adjacentNodes(N arg0) {
      return delegate.adjacentNodes(arg0);
    }

    @Override
    public synchronized boolean allowsParallelEdges() {
      return delegate.allowsParallelEdges();
    }

    @Override
    public synchronized boolean allowsSelfLoops() {
      return delegate.allowsSelfLoops();
    }

    @Override
    public synchronized Graph<N> asGraph() {
      return delegate.asGraph();
    }

    @Override
    public synchronized int degree(N arg0) {
      return delegate.degree(arg0);
    }

    @Override
    public synchronized ElementOrder<E> edgeOrder() {
      return delegate.edgeOrder();
    }

    @Override
    public synchronized Set<E> edges() {
      return delegate.edges();
    }

    @Override
    public synchronized Set<E> edgesConnecting(N arg0, N arg1) {
      return delegate.edgesConnecting(arg0, arg1);
    }

    @Override
    public synchronized int inDegree(N arg0) {
      return delegate.inDegree(arg0);
    }

    @Override
    public synchronized Set<E> inEdges(N arg0) {
      return delegate.inEdges(arg0);
    }

    @Override
    public synchronized Set<E> incidentEdges(N arg0) {
      return delegate.incidentEdges(arg0);
    }

    @Override
    public synchronized EndpointPair<N> incidentNodes(E arg0) {
      return delegate.incidentNodes(arg0);
    }

    @Override
    public synchronized boolean isDirected() {
      return delegate.isDirected();
    }

    @Override
    public synchronized ElementOrder<N> nodeOrder() {
      return delegate.nodeOrder();
    }

    @Override
    public synchronized Set<N> nodes() {
      return delegate.nodes();
    }

    @Override
    public synchronized int outDegree(N arg0) {
      return delegate.outDegree(arg0);
    }

    @Override
    public synchronized Set<E> outEdges(N arg0) {
      return delegate.outEdges(arg0);
    }

    @Override
    public synchronized Set<N> predecessors(N arg0) {
      return delegate.predecessors(arg0);
    }

    @Override
    public synchronized Set<N> successors(N arg0) {
      return delegate.successors(arg0);
    }

    @Override
    public synchronized boolean addEdge(N arg0, N arg1, E arg2) {
      return delegate.addEdge(arg0, arg1, arg2);
    }

    @Override
    public synchronized boolean addEdge(EndpointPair<N> endpoints, E edge) {
      return delegate.addEdge(endpoints, edge);
    }

    @Override
    public synchronized boolean addNode(N arg0) {
      return delegate.addNode(arg0);
    }

    @Override
    public synchronized boolean removeEdge(E arg0) {
      return delegate.removeEdge(arg0);
    }

    @Override
    public synchronized boolean removeNode(N arg0) {
      return delegate.removeNode(arg0);
    }
  }
}
