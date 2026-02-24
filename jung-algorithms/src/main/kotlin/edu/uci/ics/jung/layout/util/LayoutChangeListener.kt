package edu.uci.ics.jung.layout.util

/**
 * interface for support for LayoutEvents
 *
 * @param N
 */
interface LayoutChangeListener<N : Any> {

  fun layoutChanged(evt: LayoutEvent<N>)

  fun layoutChanged(evt: LayoutNetworkEvent<N>)
}
