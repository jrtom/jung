package edu.uci.ics.jung.layout.util;

/**
 * interface for support to LayoutChangeListeners
 *
 * @param <N>
 * @param <P>
 */
public interface LayoutEventSupport<N, P> {

  void addLayoutChangeListener(LayoutChangeListener<N, P> listener);

  void removeLayoutChangeListener(LayoutChangeListener<N, P> listener);
}
