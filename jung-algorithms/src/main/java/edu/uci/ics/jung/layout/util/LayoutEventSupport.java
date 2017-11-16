package edu.uci.ics.jung.layout.util;

/**
 * interface for support to LayoutChangeListeners
 *
 * @param <V>
 * @param <P>
 */
public interface LayoutEventSupport<V, P> {

  void addLayoutChangeListener(LayoutChangeListener<V, P> listener);

  void removeLayoutChangeListener(LayoutChangeListener<V, P> listener);
}
