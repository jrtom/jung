package edu.uci.ics.jung.graph

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.collect.ImmutableList
import com.google.common.graph.ElementOrder
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.MutableNetwork
import edu.uci.ics.jung.graph.event.NetworkEvent
import edu.uci.ics.jung.graph.event.NetworkEventListener
import java.util.Collections.synchronizedList
import java.util.Optional
import java.util.function.Supplier

/**
 * A decorator class for graphs which generates events
 *
 * @author Joshua O'Madadhain
 */
// TODO: Add tests in similar fashion to CTreeTest and AbstractCTreeTest
class ObservableNetwork<N : Any, E : Any>(
  private val delegate: MutableNetwork<N, E>
) : MutableNetwork<N, E> {

  private val listenerList: MutableList<NetworkEventListener<N, E>> =
    synchronizedList(ArrayList())

  init {
    checkNotNull(delegate, "delegate")
  }

  /**
   * Adds [l] as a listener to this graph.
   *
   * @param l the listener to add
   */
  fun addGraphEventListener(l: NetworkEventListener<N, E>) {
    listenerList.add(checkNotNull(l, "l"))
  }

  /**
   * Removes [l] as a listener to this graph.
   *
   * @param l the listener to remove
   */
  fun removeGraphEventListener(l: NetworkEventListener<N, E>) {
    listenerList.remove(checkNotNull(l, "l"))
  }

  protected fun fireGraphEvent(evtSupplier: Supplier<NetworkEvent<N, E>>) {
    checkNotNull(evtSupplier, "evtSupplier")
    if (listenerList.isEmpty()) return

    val evt = checkNotNull(evtSupplier.get(), "evt")

    for (listener in listenerList) {
      listener.handleGraphEvent(evt)
    }
  }

  override fun addEdge(v1: N, v2: N, e: E): Boolean {
    val state = delegate.addEdge(v1, v2, e)
    if (state) {
      fireGraphEvent(Supplier { NetworkEvent.Edge(delegate, NetworkEvent.Type.EDGE_ADDED, e) })
    }
    return state
  }

  override fun addEdge(endpoints: EndpointPair<N>, edge: E): Boolean {
    val state = delegate.addEdge(endpoints, edge)
    if (state) {
      fireGraphEvent(Supplier { NetworkEvent.Edge(delegate, NetworkEvent.Type.EDGE_ADDED, edge) })
    }
    return state
  }

  override fun addNode(node: N): Boolean {
    val state = delegate.addNode(node)
    if (state) {
      fireGraphEvent(Supplier { NetworkEvent.Node(delegate, NetworkEvent.Type.NODE_ADDED, node) })
    }
    return state
  }

  override fun removeEdge(edge: E): Boolean {
    val state = delegate.removeEdge(edge)
    if (state) {
      fireGraphEvent(Supplier { NetworkEvent.Edge(delegate, NetworkEvent.Type.EDGE_REMOVED, edge) })
    }
    return state
  }

  override fun removeNode(node: N): Boolean {
    // remove all incident edges first, so that the appropriate events will
    // be fired (otherwise they'll be removed inside delegate.removeNode
    // and the events will not be fired)
    val incidentEdges = ImmutableList.copyOf(delegate.incidentEdges(node))
    for (e in incidentEdges) {
      this.removeEdge(e)
    }

    val state = delegate.removeNode(node)
    if (state) {
      fireGraphEvent(Supplier { NetworkEvent.Node(delegate, NetworkEvent.Type.NODE_REMOVED, node) })
    }
    return state
  }

  override fun adjacentEdges(edge: E): Set<E> = delegate.adjacentEdges(edge)
  override fun adjacentNodes(node: N): Set<N> = delegate.adjacentNodes(node)
  override fun allowsParallelEdges(): Boolean = delegate.allowsParallelEdges()
  override fun allowsSelfLoops(): Boolean = delegate.allowsSelfLoops()
  override fun asGraph(): Graph<N> = delegate.asGraph()
  override fun degree(node: N): Int = delegate.degree(node)
  override fun edgeOrder(): ElementOrder<E> = delegate.edgeOrder()
  override fun edges(): Set<E> = delegate.edges()
  override fun edgesConnecting(nodeU: N, nodeV: N): Set<E> = delegate.edgesConnecting(nodeU, nodeV)
  override fun edgesConnecting(endpoints: EndpointPair<N>): Set<E> = delegate.edgesConnecting(endpoints)
  override fun edgeConnecting(nodeU: N, nodeV: N): Optional<E> = delegate.edgeConnecting(nodeU, nodeV)
  override fun edgeConnecting(endpoints: EndpointPair<N>): Optional<E> = delegate.edgeConnecting(endpoints)
  override fun edgeConnectingOrNull(nodeU: N, nodeV: N): E? = delegate.edgeConnectingOrNull(nodeU, nodeV)
  override fun edgeConnectingOrNull(endpoints: EndpointPair<N>): E? = delegate.edgeConnectingOrNull(endpoints)
  override fun hasEdgeConnecting(nodeU: N, nodeV: N): Boolean = delegate.hasEdgeConnecting(nodeU, nodeV)
  override fun hasEdgeConnecting(endpoints: EndpointPair<N>): Boolean = delegate.hasEdgeConnecting(endpoints)
  override fun inDegree(node: N): Int = delegate.inDegree(node)
  override fun inEdges(node: N): Set<E> = delegate.inEdges(node)
  override fun incidentEdges(node: N): Set<E> = delegate.incidentEdges(node)
  override fun incidentNodes(edge: E): EndpointPair<N> = delegate.incidentNodes(edge)
  override fun isDirected(): Boolean = delegate.isDirected()
  override fun nodeOrder(): ElementOrder<N> = delegate.nodeOrder()
  override fun nodes(): Set<N> = delegate.nodes()
  override fun outDegree(node: N): Int = delegate.outDegree(node)
  override fun outEdges(node: N): Set<E> = delegate.outEdges(node)
  override fun predecessors(node: N): Set<N> = delegate.predecessors(node)
  override fun successors(node: N): Set<N> = delegate.successors(node)
}
