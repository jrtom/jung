/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization.renderers;

import java.util.ConcurrentModificationException;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.RenderContext;

/**
 * The default implementation of the Renderer used by the
 * VisualizationViewer. Default Vertex and Edge Renderers
 * are supplied, or the user may set custom values. The
 * Vertex and Edge renderers are used in the renderVertex
 * and renderEdge methods, which are called in the render
 * loop of the VisualizationViewer.
 * 
 * @author Tom Nelson
 */
public class BasicRenderer<V,E> implements Renderer<V, E> {
	
    Renderer.Vertex<V,E> vertexRenderer = new BasicVertexRenderer<V,E>();
    Renderer.VertexLabel<V,E> vertexLabelRenderer = new BasicVertexLabelRenderer<V,E>();
    Renderer.Edge<V,E> edgeRenderer = new BasicEdgeRenderer<V,E>();
    Renderer.EdgeLabel<V,E> edgeLabelRenderer = new BasicEdgeLabelRenderer<V,E>();
    
	public void render(RenderContext<V, E> renderContext, Layout<V, E> layout) {
		
		// paint all the edges
        try {
        	for(E e : layout.getGraph().getEdges()) {

		        renderEdge(
		                renderContext,
		                layout,
		                e);
		        renderEdgeLabel(
		                renderContext,
		                layout,
		                e);
        	}
        } catch(ConcurrentModificationException cme) {
        	renderContext.getScreenDevice().repaint();
        }
		
		// paint all the vertices
        try {
        	for(V v : layout.getGraph().getVertices()) {

		    	renderVertex(
		                renderContext,
                        layout,
		                v);
		    	renderVertexLabel(
		                renderContext,
                        layout,
		                v);
        	}
        } catch(ConcurrentModificationException cme) {
            renderContext.getScreenDevice().repaint();
        }
	}

    public void renderVertex(RenderContext<V,E> rc, Layout<V,E> layout, V v) {
        vertexRenderer.paintVertex(rc, layout, v);
    }
    
    public void renderVertexLabel(RenderContext<V,E> rc, Layout<V,E> layout, V v) {
        vertexLabelRenderer.labelVertex(rc, layout, v, rc.getVertexLabelTransformer().transform(v));
    }
    
    public void renderEdge(RenderContext<V,E> rc, Layout<V,E> layout, E e) {
    	edgeRenderer.paintEdge(rc, layout, e);
    }
    
    public void renderEdgeLabel(RenderContext<V,E> rc, Layout<V,E> layout, E e) {
    	edgeLabelRenderer.labelEdge(rc, layout, e, rc.getEdgeLabelTransformer().transform(e));
    }
    
    public void setVertexRenderer(Renderer.Vertex<V,E> r) {
    	this.vertexRenderer = r;
    }

    public void setEdgeRenderer(Renderer.Edge<V,E> r) {
    	this.edgeRenderer = r;
    }

	/**
	 * @return the edgeLabelRenderer
	 */
	public Renderer.EdgeLabel<V, E> getEdgeLabelRenderer() {
		return edgeLabelRenderer;
	}

	/**
	 * @param edgeLabelRenderer the edgeLabelRenderer to set
	 */
	public void setEdgeLabelRenderer(Renderer.EdgeLabel<V, E> edgeLabelRenderer) {
		this.edgeLabelRenderer = edgeLabelRenderer;
	}

	/**
	 * @return the vertexLabelRenderer
	 */
	public Renderer.VertexLabel<V, E> getVertexLabelRenderer() {
		return vertexLabelRenderer;
	}

	/**
	 * @param vertexLabelRenderer the vertexLabelRenderer to set
	 */
	public void setVertexLabelRenderer(
			Renderer.VertexLabel<V, E> vertexLabelRenderer) {
		this.vertexLabelRenderer = vertexLabelRenderer;
	}

	/**
	 * @return the edgeRenderer
	 */
	public Renderer.Edge<V, E> getEdgeRenderer() {
		return edgeRenderer;
	}

	/**
	 * @return the vertexRenderer
	 */
	public Renderer.Vertex<V, E> getVertexRenderer() {
		return vertexRenderer;
	}


}