package edu.uci.ics.jung.visualization.spatial;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;

/**
 * break into several rectangular areas, each of which will have a reference Graph
 * @author tanelso
 *
 * @param <V>
 * @param <E>
 */
public class FastRenderingLayout<V,E> implements Layout<V,E> {
	
	protected Layout<V,E> layout;
	protected Graph<V,E> graph;
	
	protected Rectangle2D[][] grid;
	
	public FastRenderingLayout(Layout<V,E> layout) {
		this.layout = layout;
	}

	public Graph<V, E> getGraph() {
		return layout.getGraph();
	}

	public Dimension getSize() {
		return layout.getSize();
	}

	public void initialize() {
		layout.initialize();
	}

	public boolean isLocked(V v) {
		return layout.isLocked(v);
	}

	public void lock(V v, boolean state) {
		layout.lock(v, state);
	}

	public void reset() {
		layout.reset();
	}

	public void setGraph(Graph<V, E> graph) {
//		layout.setGraph(new FastRenderingGraph(graph));
	}

	public void setInitializer(Function<V, Point2D> initializer) {
		layout.setInitializer(initializer);
	}

	public void setLocation(V v, Point2D location) {
		layout.setLocation(v, location);
	}

	public void setSize(Dimension d) {
		layout.setSize(d);
	}

	public Point2D apply(V arg0) {
		return layout.apply(arg0);
	}

}
