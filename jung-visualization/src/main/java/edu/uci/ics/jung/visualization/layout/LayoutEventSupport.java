package edu.uci.ics.jung.visualization.layout;

public interface LayoutEventSupport<V> {

  void addLayoutChangeListener(LayoutChangeListener<V> listener);

  void removeLayoutChangeListener(LayoutChangeListener<V> listener);
}
