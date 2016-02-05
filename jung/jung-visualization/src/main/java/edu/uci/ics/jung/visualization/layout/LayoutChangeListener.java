package edu.uci.ics.jung.visualization.layout;

public interface LayoutChangeListener<V, E> {
	
	void layoutChanged(LayoutEvent<V,E> evt);

}
