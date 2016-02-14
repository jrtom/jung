/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.renderers;

import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.JComponent;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

public class BasicVertexRenderer<V,E> implements Renderer.Vertex<V,E> {

    public void paintVertex(RenderContext<V,E> rc, Layout<V,E> layout, V v) {
    	Graph<V,E> graph = layout.getGraph();
        if (rc.getVertexIncludePredicate().apply(Context.<Graph<V,E>,V>getInstance(graph,v))) {
        	paintIconForVertex(rc, v, layout);
        }
    }
    
    /**
     * Returns the vertex shape in view coordinates.
     * @param rc the render context used for rendering the vertex
     * @param v the vertex whose shape is to be returned
     * @param layout the layout algorithm that provides coordinates for the vertex
     * @param coords the x and y view coordinates
     * @return the vertex shape in view coordinates
     */
    protected Shape prepareFinalVertexShape(RenderContext<V,E> rc, V v, 
    		Layout<V,E> layout, int[] coords) {

        // get the shape to be rendered
        Shape shape = rc.getVertexShapeTransformer().apply(v);
        Point2D p = layout.apply(v);
        p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
        float x = (float)p.getX();
        float y = (float)p.getY();
        coords[0] = (int)x;
        coords[1] = (int)y;
        // create a transform that translates to the location of
        // the vertex to be rendered
        AffineTransform xform = AffineTransform.getTranslateInstance(x,y);
        // transform the vertex shape with xtransform
        shape = xform.createTransformedShape(shape);
        return shape;
    }
    
    /**
     * Paint <code>v</code>'s icon on <code>g</code> at <code>(x,y)</code>.
     * 
     * @param rc the render context used for rendering the vertex
     * @param v the vertex to be painted
     * @param layout the layout algorithm that provides coordinates for the vertex
     */
    protected void paintIconForVertex(RenderContext<V,E> rc, V v, Layout<V,E> layout) {
        GraphicsDecorator g = rc.getGraphicsContext();
        boolean vertexHit = true;
        int[] coords = new int[2];
        Shape shape = prepareFinalVertexShape(rc, v, layout, coords);
        vertexHit = vertexHit(rc, shape);

        if (vertexHit) {
        	if(rc.getVertexIconTransformer() != null) {
        		Icon icon = rc.getVertexIconTransformer().apply(v);
        		if(icon != null) {
        		
           			g.draw(icon, rc.getScreenDevice(), shape, coords[0], coords[1]);

        		} else {
        			paintShapeForVertex(rc, v, shape);
        		}
        	} else {
        		paintShapeForVertex(rc, v, shape);
        	}
        }
    }
    
    protected boolean vertexHit(RenderContext<V,E> rc, Shape s) {
        JComponent vv = rc.getScreenDevice();
        Rectangle deviceRectangle = null;
        if(vv != null) {
            Dimension d = vv.getSize();
            deviceRectangle = new Rectangle(
                    0,0,
                    d.width,d.height);
        }
        MutableTransformer vt = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW);
        if(vt instanceof MutableTransformerDecorator) {
        	vt = ((MutableTransformerDecorator)vt).getDelegate();
        }
        return vt.transform(s).intersects(deviceRectangle);
    }

    protected void paintShapeForVertex(RenderContext<V,E> rc, V v, Shape shape) {
        GraphicsDecorator g = rc.getGraphicsContext();
        Paint oldPaint = g.getPaint();
        Paint fillPaint = rc.getVertexFillPaintTransformer().apply(v);
        if(fillPaint != null) {
            g.setPaint(fillPaint);
            g.fill(shape);
            g.setPaint(oldPaint);
        }
        Paint drawPaint = rc.getVertexDrawPaintTransformer().apply(v);
        if(drawPaint != null) {
        	g.setPaint(drawPaint);
        	Stroke oldStroke = g.getStroke();
        	Stroke stroke = rc.getVertexStrokeTransformer().apply(v);
        	if(stroke != null) {
        		g.setStroke(stroke);
        	}
        	g.draw(shape);
        	g.setPaint(oldPaint);
        	g.setStroke(oldStroke);
        }
    }
}
