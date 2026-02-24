package edu.uci.ics.jung.graph.event

import java.util.EventListener

/** An interface for classes that listen for graph events. */
interface NetworkEventListener<N : Any, E : Any> : EventListener {
  /**
   * Method called by the process generating a graph event to which this instance is listening. The
   * implementor of this interface is responsible for deciding what behavior is appropriate.
   *
   * @param evt the graph event to be handled
   */
  fun handleGraphEvent(evt: NetworkEvent<N, E>)
}
