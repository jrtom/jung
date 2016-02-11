package edu.uci.ics.jung.visualization.layout;

public interface LayoutEventSupport<V, E> {
	
	void addLayoutChangeListener(LayoutChangeListener<V,E> listener);
	
	void removeLayoutChangeListener(LayoutChangeListener<V,E> listener);

}
