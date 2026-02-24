package edu.uci.ics.jung.layout.util

/**
 * interface for support to LayoutChangeListeners
 *
 * @param N
 */
interface LayoutEventSupport<N : Any> {

  fun addLayoutChangeListener(listener: LayoutChangeListener<N>)

  fun removeLayoutChangeListener(listener: LayoutChangeListener<N>)
}
