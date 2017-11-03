package edu.uci.ics.jung.visualization.layout;

/**
 * interface for support for LayoutEvents
 *
 * @param <N>
 * @param <P>
 */
public interface LayoutChangeListener<N, P> {

  void layoutChanged(LayoutEvent<N, P> evt);

  void layoutChanged(LayoutNetworkEvent<N, P> evt);
}
