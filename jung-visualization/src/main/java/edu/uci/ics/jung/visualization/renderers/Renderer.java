/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.visualization.renderers;

import java.awt.Dimension;

/**
 * The interface for drawing vertices, edges, and their labels.
 * Implementations of this class can set specific renderers for
 * each element, allowing custom control of each.
 */
public interface Renderer<V,E> {

	void render();
	void renderVertex(V v);
	void renderVertexLabel(V v);
	void renderEdge(E e);
	void renderEdgeLabel(E e);
    void setVertexRenderer(Renderer.Vertex<V> r);
    void setEdgeRenderer(Renderer.Edge<V,E> r);
    void setVertexLabelRenderer(Renderer.VertexLabel<V> r);
    void setEdgeLabelRenderer(Renderer.EdgeLabel<V,E> r);
    Renderer.VertexLabel<V> getVertexLabelRenderer();
    Renderer.Vertex<V> getVertexRenderer();
    Renderer.Edge<V,E> getEdgeRenderer();
    Renderer.EdgeLabel<V,E> getEdgeLabelRenderer();

	interface Vertex<V> {
		void paintVertex(V v);
		@SuppressWarnings("rawtypes")
		class NOOP implements Vertex {
			public void paintVertex(Object v) {}
		};
	}
    
	interface Edge<V,E> {
		void paintEdge(E e);
		EdgeArrowRenderingSupport<V, E> getEdgeArrowRenderingSupport();
		void setEdgeArrowRenderingSupport(EdgeArrowRenderingSupport<V, E> edgeArrowRenderingSupport);
		@SuppressWarnings("rawtypes")
		class NOOP implements Edge {
			public void paintEdge(Object e) {}
			public EdgeArrowRenderingSupport getEdgeArrowRenderingSupport(){return null;}
			public void setEdgeArrowRenderingSupport(EdgeArrowRenderingSupport edgeArrowRenderingSupport){}
		}
	}
	
	interface VertexLabel<V> {
		void labelVertex(V v, String label);
		Position getPosition();
		void setPosition(Position position);
		void setPositioner(Positioner positioner);
		Positioner getPositioner();
		@SuppressWarnings("rawtypes")
		class NOOP implements VertexLabel {
			public void labelVertex(Object v, String label) {}
			public Position getPosition() { return Position.CNTR; }
			public void setPosition(Position position) {}
			public Positioner getPositioner() {
				return new Positioner() {
					public Position getPosition(float x, float y, Dimension d) {
						return Position.CNTR;
					}};
			}
			public void setPositioner(Positioner positioner) {
			}
		}
		enum Position { N, NE, E, SE, S, SW, W, NW, CNTR, AUTO }
	    interface Positioner {
	    	Position getPosition(float x, float y, Dimension d);
	    }

	}
	
	interface EdgeLabel<V,E> {
		void labelEdge(E e, String label);
		@SuppressWarnings("rawtypes")
		class NOOP implements EdgeLabel {
			public void labelEdge(Object e, String label) {}
		}
	}
}
