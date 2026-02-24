package edu.uci.ics.jung.graph.util

import com.google.common.base.Preconditions.checkNotNull
import com.google.common.graph.AbstractNetwork
import com.google.common.graph.ElementOrder
import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network

/**
 * Provides specialized implementations of `GraphDecorator`. Currently these wrapper
 * types include "synchronized" and "unmodifiable".
 *
 * The methods of this class may each throw a `NullPointerException` if the graphs or
 * class objects provided to them are null.
 *
 * @author Tom Nelson
 */
object Graphs {

  @JvmStatic
  fun isSelfLoop(endpoints: EndpointPair<*>): Boolean {
    checkNotNull(endpoints, "endpoints")
    return endpoints.nodeU() == endpoints.nodeV()
  }

  @JvmStatic
  fun <E : Any> isSelfLoop(network: Network<*, E>, edge: E): Boolean {
    checkNotNull(network, "network")
    checkNotNull(edge, "edge")
    return isSelfLoop(network.incidentNodes(edge))
  }

  @JvmStatic
  fun <N : Any, E : Any> synchronizedNetwork(delegate: MutableNetwork<N, E>): MutableNetwork<N, E> {
    return SynchronizedNetwork(delegate)
  }

  private class SynchronizedNetwork<N : Any, E : Any>(
    private val delegate: MutableNetwork<N, E>
  ) : AbstractNetwork<N, E>(), MutableNetwork<N, E> {

    init {
      checkNotNull(delegate, "delegate")
    }

    @Synchronized override fun adjacentEdges(edge: E): Set<E> = delegate.adjacentEdges(edge)
    @Synchronized override fun adjacentNodes(node: N): Set<N> = delegate.adjacentNodes(node)
    @Synchronized override fun allowsParallelEdges(): Boolean = delegate.allowsParallelEdges()
    @Synchronized override fun allowsSelfLoops(): Boolean = delegate.allowsSelfLoops()
    @Synchronized override fun asGraph(): Graph<N> = delegate.asGraph()
    @Synchronized override fun degree(node: N): Int = delegate.degree(node)
    @Synchronized override fun edgeOrder(): ElementOrder<E> = delegate.edgeOrder()
    @Synchronized override fun edges(): Set<E> = delegate.edges()
    @Synchronized override fun edgesConnecting(nodeU: N, nodeV: N): Set<E> = delegate.edgesConnecting(nodeU, nodeV)
    @Synchronized override fun inDegree(node: N): Int = delegate.inDegree(node)
    @Synchronized override fun inEdges(node: N): Set<E> = delegate.inEdges(node)
    @Synchronized override fun incidentEdges(node: N): Set<E> = delegate.incidentEdges(node)
    @Synchronized override fun incidentNodes(edge: E): EndpointPair<N> = delegate.incidentNodes(edge)
    @Synchronized override fun isDirected(): Boolean = delegate.isDirected()
    @Synchronized override fun nodeOrder(): ElementOrder<N> = delegate.nodeOrder()
    @Synchronized override fun nodes(): Set<N> = delegate.nodes()
    @Synchronized override fun outDegree(node: N): Int = delegate.outDegree(node)
    @Synchronized override fun outEdges(node: N): Set<E> = delegate.outEdges(node)
    @Synchronized override fun predecessors(node: N): Set<N> = delegate.predecessors(node)
    @Synchronized override fun successors(node: N): Set<N> = delegate.successors(node)
    @Synchronized override fun addEdge(nodeU: N, nodeV: N, edge: E): Boolean = delegate.addEdge(nodeU, nodeV, edge)
    @Synchronized override fun addEdge(endpoints: EndpointPair<N>, edge: E): Boolean = delegate.addEdge(endpoints, edge)
    @Synchronized override fun addNode(node: N): Boolean = delegate.addNode(node)
    @Synchronized override fun removeEdge(edge: E): Boolean = delegate.removeEdge(edge)
    @Synchronized override fun removeNode(node: N): Boolean = delegate.removeNode(node)
  }
}
