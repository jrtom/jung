package edu.uci.ics.jung.graph.event

import com.google.common.graph.Network

/**
 * @author tom nelson
 * @param <N> the node type
 * @param <E> the edge type
 */
abstract class NetworkEvent<N : Any, E : Any>(
  val source: Network<N, E>,
  val type: Type
) {

  /** Types of graph events. */
  enum class Type {
    NODE_ADDED,
    NODE_REMOVED,
    EDGE_ADDED,
    EDGE_REMOVED
  }

  /** An event type pertaining to graph nodes. */
  class Node<N : Any, E : Any>(
    source: Network<N, E>,
    type: Type,
    val node: N
  ) : NetworkEvent<N, E>(source, type) {

    override fun toString(): String = "GraphEvent type:$type for $node"
  }

  /** An event type pertaining to graph edges. */
  class Edge<N : Any, E : Any>(
    source: Network<N, E>,
    type: Type,
    val edge: E
  ) : NetworkEvent<N, E>(source, type) {

    override fun toString(): String = "GraphEvent type:$type for $edge"
  }
}
