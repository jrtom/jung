package edu.uci.ics.jung.layout.util

import edu.uci.ics.jung.layout.model.Point

/**
 * an event with information about a node and its (new?) location
 *
 * @author Tom Nelson
 * @param N
 */
open class LayoutEvent<N : Any>(
  val node: N,
  val location: Point
)
