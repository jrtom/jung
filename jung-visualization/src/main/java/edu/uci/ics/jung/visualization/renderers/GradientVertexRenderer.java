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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

/**
 * A renderer that will fill vertex shapes with a GradientPaint
 * @author Tom Nelson
 *
 * @param <V> the vertex type
 * @param <V> the edge type
 */
public class GradientVertexRenderer<V,E> implements Renderer.Vertex<V,E> {
	
	Color colorOne;
	Color colorTwo;
	Color pickedColorOne;
	Color pickedColorTwo;
	PickedState<V> pickedState;
	boolean cyclic;
	

    public GradientVertexRenderer(Color colorOne, Color colorTwo, boolean cyclic) {
		this.colorOne = colorOne;
		this.colorTwo = colorTwo;
		this.cyclic = cyclic;
	}


	public GradientVertexRenderer(Color colorOne, Color colorTwo, Color pickedColorOne, Color pickedColorTwo, PickedState<V> pickedState, boolean cyclic) {
		this.colorOne = colorOne;
		this.colorTwo = colorTwo;
		this.pickedColorOne = pickedColorOne;
		this.pickedColorTwo = pickedColorTwo;
		this.pickedState = pickedState;
		this.cyclic = cyclic;
	}


	public void paintVertex(RenderContext<V,E> rc, Layout<V,E> layout, V v) {
		Graph<V,E> graph = layout.getGraph();
        if (rc.getVertexIncludePredicate().apply(Context.<Graph<V,E>,V>getInstance(graph,v))) {
            boolean vertexHit = true;
            // get the shape to be rendered
            Shape shape = rc.getVertexShapeTransformer().apply(v);
            
            Point2D p = layout.apply(v);
            p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);

            float x = (float)p.getX();
            float y = (float)p.getY();

            // create a transform that translates to the location of
            // the vertex to be rendered
            AffineTransform xform = AffineTransform.getTranslateInstance(x,y);
            // transform the vertex shape with xtransform
            shape = xform.createTransformedShape(shape);
            
            vertexHit = vertexHit(rc, shape);
                //rc.getViewTransformer().transform(shape).intersects(deviceRectangle);

            if (vertexHit) {
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
        return rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(s).intersects(deviceRectangle);
    }

    protected void paintShapeForVertex(RenderContext<V,E> rc, V v, Shape shape) {
        GraphicsDecorator g = rc.getGraphicsContext();
        Paint oldPaint = g.getPaint();
        Rectangle r = shape.getBounds();
        float y2 = (float)r.getMaxY();
        if(cyclic) {
        	y2 = (float)(r.getMinY()+r.getHeight()/2);
        }
        
        Paint fillPaint = null;
        if(pickedState != null && pickedState.isPicked(v)) {
        	fillPaint = new GradientPaint((float)r.getMinX(), (float)r.getMinY(), pickedColorOne,
            		(float)r.getMinX(), y2, pickedColorTwo, cyclic);
        } else {
        	fillPaint = new GradientPaint((float)r.getMinX(), (float)r.getMinY(), colorOne,
        		(float)r.getMinX(), y2, colorTwo, cyclic);
        }
        if(fillPaint != null) {
            g.setPaint(fillPaint);
            g.fill(shape);
            g.setPaint(oldPaint);
        }
        Paint drawPaint = rc.getVertexDrawPaintTransformer().apply(v);
        if(drawPaint != null) {
            g.setPaint(drawPaint);
        }
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
