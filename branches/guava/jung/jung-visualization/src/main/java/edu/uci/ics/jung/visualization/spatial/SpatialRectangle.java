package edu.uci.ics.jung.visualization.spatial;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.RenderContext;

public class SpatialRectangle<V,E> {
	
	protected Layout<V,E> layout;
	protected RenderContext<V,E> renderContext;
	protected Rectangle2D bounds;
	
	List<V> vertexList = new ArrayList<V>();
	List<E> edgeList = new ArrayList<E>();
	
	
	
	public void render(RenderContext<V, E> renderContext, Layout<V, E> layout) {
		
		// paint all the edges
        try {
        	for(E e : layout.getGraph().getEdges()) {

//		        renderEdge(
//		                renderContext,
//		                layout,
//		                e);
//		        renderEdgeLabel(
//		                renderContext,
//		                layout,
//		                e);
        	}
        } catch(ConcurrentModificationException cme) {
        	renderContext.getScreenDevice().repaint();
        }
		
		// paint all the vertices
        try {
        	for(V v : layout.getGraph().getVertices()) {

//		    	renderVertex(
//		                renderContext,
//                        layout,
//		                v);
//		    	renderVertexLabel(
//		                renderContext,
//                        layout,
//		                v);
        	}
        } catch(ConcurrentModificationException cme) {
            renderContext.getScreenDevice().repaint();
        }
	}
	

}
