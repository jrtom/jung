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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import javax.swing.JComponent;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.transform.LensTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.shape.TransformingGraphics;

/**
 * uses a flatness argument to break edges into
 * smaller segments. This produces a more detailed
 * transformation of the edge shape
 * 
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 * @param <V> the vertex type
 * @param <V> the edge type
 */
public class ReshapingEdgeRenderer<V,E> extends BasicEdgeRenderer<V,E>
	implements Renderer.Edge<V,E> {

    /**
     * Draws the edge <code>e</code>, whose endpoints are at <code>(x1,y1)</code>
     * and <code>(x2,y2)</code>, on the graphics context <code>g</code>.
     * The <code>Shape</code> provided by the <code>EdgeShapeFunction</code> instance
     * is scaled in the x-direction so that its width is equal to the distance between
     * <code>(x1,y1)</code> and <code>(x2,y2)</code>.
     */
    protected void drawSimpleEdge(RenderContext<V,E> rc, Layout<V,E> layout, E e) {
        
    	TransformingGraphics g = (TransformingGraphics)rc.getGraphicsContext();
        Graph<V,E> graph = layout.getGraph();
        Pair<V> endpoints = graph.getEndpoints(e);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();
        Point2D p1 = layout.apply(v1);
        Point2D p2 = layout.apply(v2);
        p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
        p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
        float x1 = (float) p1.getX();
        float y1 = (float) p1.getY();
        float x2 = (float) p2.getX();
        float y2 = (float) p2.getY();
        
        float flatness = 0;
        MutableTransformer transformer = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW);
        if(transformer instanceof LensTransformer) {
            LensTransformer ht = (LensTransformer)transformer;
            RectangularShape lensShape = ht.getLensShape();
            if(lensShape.contains(x1,y1) || lensShape.contains(x2,y2)) {
                flatness = .05f;
            }
        }

        boolean isLoop = v1.equals(v2);
        Shape s2 = rc.getVertexShapeTransformer().apply(v2);
        Shape edgeShape = rc.getEdgeShapeTransformer().apply(e);
        
        boolean edgeHit = true;
        boolean arrowHit = true;
        Rectangle deviceRectangle = null;
        JComponent vv = rc.getScreenDevice();
        if(vv != null) {
            Dimension d = vv.getSize();
            deviceRectangle = new Rectangle(0,0,d.width,d.height);
        }

        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);
        
        if(isLoop) {
            // this is a self-loop. scale it is larger than the vertex
            // it decorates and translate it so that its nadir is
            // at the center of the vertex.
            Rectangle2D s2Bounds = s2.getBounds2D();
            xform.scale(s2Bounds.getWidth(),s2Bounds.getHeight());
            xform.translate(0, -edgeShape.getBounds2D().getWidth()/2);
        } else {
            // this is a normal edge. Rotate it to the angle between
            // vertex endpoints, then scale it to the distance between
            // the vertices
            float dx = x2-x1;
            float dy = y2-y1;
            float thetaRadians = (float) Math.atan2(dy, dx);
            xform.rotate(thetaRadians);
            float dist = (float) Math.sqrt(dx*dx + dy*dy);
            xform.scale(dist, 1.0);
        }
        
        edgeShape = xform.createTransformedShape(edgeShape);
        
        MutableTransformer vt = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW);
        if(vt instanceof LensTransformer) {
        	vt = ((LensTransformer)vt).getDelegate();
        }
        edgeHit = vt.transform(edgeShape).intersects(deviceRectangle);

        if(edgeHit == true) {
            
            Paint oldPaint = g.getPaint();
            
            // get Paints for filling and drawing
            // (filling is done first so that drawing and label use same Paint)
            Paint fill_paint = rc.getEdgeFillPaintTransformer().apply(e); 
            if (fill_paint != null)
            {
                g.setPaint(fill_paint);
                g.fill(edgeShape, flatness);
            }
            Paint draw_paint = rc.getEdgeDrawPaintTransformer().apply(e);
            if (draw_paint != null)
            {
                g.setPaint(draw_paint);
                g.draw(edgeShape, flatness);
            }
            
            float scalex = (float)g.getTransform().getScaleX();
            float scaley = (float)g.getTransform().getScaleY();
            // see if arrows are too small to bother drawing
            if(scalex < .3 || scaley < .3) return;
            
            if (rc.getEdgeArrowPredicate().apply(Context.<Graph<V,E>,E>getInstance(graph, e))) {
                
                Shape destVertexShape = 
                    rc.getVertexShapeTransformer().apply(graph.getEndpoints(e).getSecond());

                AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
                destVertexShape = xf.createTransformedShape(destVertexShape);
                
                arrowHit = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(destVertexShape).intersects(deviceRectangle);
                if(arrowHit) {
                    
                    AffineTransform at = 
                        edgeArrowRenderingSupport.getArrowTransform(rc, new GeneralPath(edgeShape), destVertexShape);
                    if(at == null) return;
                    Shape arrow = rc.getEdgeArrowTransformer().apply(Context.<Graph<V,E>,E>getInstance(graph, e));
                    arrow = at.createTransformedShape(arrow);
                    g.setPaint(rc.getArrowFillPaintTransformer().apply(e));
                    g.fill(arrow);
                    g.setPaint(rc.getArrowDrawPaintTransformer().apply(e));
                    g.draw(arrow);
                }
                if (graph.getEdgeType(e) == EdgeType.UNDIRECTED) {
                    Shape vertexShape = 
                        rc.getVertexShapeTransformer().apply(graph.getEndpoints(e).getFirst());
                    xf = AffineTransform.getTranslateInstance(x1, y1);
                    vertexShape = xf.createTransformedShape(vertexShape);
                    
                    arrowHit = rc.getMultiLayerTransformer().getTransformer(Layer.VIEW).transform(vertexShape).intersects(deviceRectangle);
                    
                    if(arrowHit) {
                        AffineTransform at = edgeArrowRenderingSupport.getReverseArrowTransform(rc, new GeneralPath(edgeShape), vertexShape, !isLoop);
                        if(at == null) return;
                        Shape arrow = rc.getEdgeArrowTransformer().apply(Context.<Graph<V,E>,E>getInstance(graph, e));
                        arrow = at.createTransformedShape(arrow);
                        g.setPaint(rc.getArrowFillPaintTransformer().apply(e));
                        g.fill(arrow);
                        g.setPaint(rc.getArrowDrawPaintTransformer().apply(e));
                        g.draw(arrow);
                    }
                }
            }
            // use existing paint for text if no draw paint specified
            if (draw_paint == null)
                g.setPaint(oldPaint);
            
            // restore old paint
            g.setPaint(oldPaint);
        }
    }
    
    /**
     * Returns a transform to position the arrowhead on this edge shape at the
     * point where it intersects the passed vertex shape.
     */
//    public AffineTransform getArrowTransform(RenderContext<V,E> rc, GeneralPath edgeShape, Shape vertexShape) {
//        float[] seg = new float[6];
//        Point2D p1=null;
//        Point2D p2=null;
//        AffineTransform at = new AffineTransform();
//        // when the PathIterator is done, switch to the line-subdivide
//        // method to get the arrowhead closer.
//        for(PathIterator i=edgeShape.getPathIterator(null,1); !i.isDone(); i.next()) {
//            int ret = i.currentSegment(seg);
//            if(ret == PathIterator.SEG_MOVETO) {
//                p2 = new Point2D.Float(seg[0],seg[1]);
//            } else if(ret == PathIterator.SEG_LINETO) {
//                p1 = p2;
//                p2 = new Point2D.Float(seg[0],seg[1]);
//                if(vertexShape.contains(p2)) {
//                    at = getArrowTransform(rc, new Line2D.Float(p1,p2),vertexShape);
//                    break;
//                }
//            } 
//        }
//        return at;
//    }

    /**
     * Returns a transform to position the arrowhead on this edge shape at the
     * point where it intersects the passed vertex shape.
     */
//    public AffineTransform getReverseArrowTransform(RenderContext<V,E> rc, GeneralPath edgeShape, Shape vertexShape) {
//        return getReverseArrowTransform(rc, edgeShape, vertexShape, true);
//    }
            
    /**
     * <p>Returns a transform to position the arrowhead on this edge shape at the
     * point where it intersects the passed vertex shape.
     * 
     * <p>The Loop edge is a special case because its staring point is not inside
     * the vertex. The passedGo flag handles this case.
     * 
     * @param edgeShape
     * @param vertexShape
     * @param passedGo - used only for Loop edges
     */
//    public AffineTransform getReverseArrowTransform(RenderContext<V,E> rc, GeneralPath edgeShape, Shape vertexShape,
//            boolean passedGo) {
//        float[] seg = new float[6];
//        Point2D p1=null;
//        Point2D p2=null;
//
//        AffineTransform at = new AffineTransform();
//        for(PathIterator i=edgeShape.getPathIterator(null,1); !i.isDone(); i.next()) {
//            int ret = i.currentSegment(seg);
//            if(ret == PathIterator.SEG_MOVETO) {
//                p2 = new Point2D.Float(seg[0],seg[1]);
//            } else if(ret == PathIterator.SEG_LINETO) {
//                p1 = p2;
//                p2 = new Point2D.Float(seg[0],seg[1]);
//                if(passedGo == false && vertexShape.contains(p2)) {
//                    passedGo = true;
//                 } else if(passedGo==true &&
//                        vertexShape.contains(p2)==false) {
//                     at = getReverseArrowTransform(rc, new Line2D.Float(p1,p2),vertexShape);
//                    break;
//                }
//            } 
//        }
//        return at;
//    }

    /**
     * This is used for the arrow of a directed and for one of the
     * arrows for non-directed edges
     * Get a transform to place the arrow shape on the passed edge at the
     * point where it intersects the passed shape
     * @param edgeShape
     * @param vertexShape
     * @return
     */
//    public AffineTransform getArrowTransform(RenderContext<V,E> rc, Line2D edgeShape, Shape vertexShape) {
//        float dx = (float) (edgeShape.getX1()-edgeShape.getX2());
//        float dy = (float) (edgeShape.getY1()-edgeShape.getY2());
//        // iterate over the line until the edge shape will place the
//        // arrowhead closer than 'arrowGap' to the vertex shape boundary
//        while((dx*dx+dy*dy) > rc.getArrowPlacementTolerance()) {
//            try {
//                edgeShape = getLastOutsideSegment(edgeShape, vertexShape);
//            } catch(IllegalArgumentException e) {
//                System.err.println(e.toString());
//                return null;
//            }
//            dx = (float) (edgeShape.getX1()-edgeShape.getX2());
//            dy = (float) (edgeShape.getY1()-edgeShape.getY2());
//        }
//        double atheta = Math.atan2(dx,dy)+Math.PI/2;
//        AffineTransform at = 
//            AffineTransform.getTranslateInstance(edgeShape.getX1(), edgeShape.getY1());
//        at.rotate(-atheta);
//        return at;
//    }

    /**
     * This is used for the reverse-arrow of a non-directed edge
     * get a transform to place the arrow shape on the passed edge at the
     * point where it intersects the passed shape
     * @param edgeShape
     * @param vertexShape
     * @return
     */
//    protected AffineTransform getReverseArrowTransform(RenderContext<V,E> rc, Line2D edgeShape, Shape vertexShape) {
//        float dx = (float) (edgeShape.getX1()-edgeShape.getX2());
//        float dy = (float) (edgeShape.getY1()-edgeShape.getY2());
//        // iterate over the line until the edge shape will place the
//        // arrowhead closer than 'arrowGap' to the vertex shape boundary
//        while((dx*dx+dy*dy) > rc.getArrowPlacementTolerance()) {
//            try {
//                edgeShape = getFirstOutsideSegment(edgeShape, vertexShape);
//            } catch(IllegalArgumentException e) {
//                System.err.println(e.toString());
//                return null;
//            }
//            dx = (float) (edgeShape.getX1()-edgeShape.getX2());
//            dy = (float) (edgeShape.getY1()-edgeShape.getY2());
//        }
//        // calculate the angle for the arrowhead
//        double atheta = Math.atan2(dx,dy)-Math.PI/2;
//        AffineTransform at = AffineTransform.getTranslateInstance(edgeShape.getX1(),edgeShape.getY1());
//        at.rotate(-atheta);
//        return at;
//    }
    
    /**
     * Passed Line's point2 must be inside the passed shape or
     * an IllegalArgumentException is thrown
     * @param line line to subdivide
     * @param shape shape to compare with line
     * @return a line that intersects the shape boundary
     * @throws IllegalArgumentException if the passed line's point1 is not inside the shape
     */
//    protected Line2D getLastOutsideSegment(Line2D line, Shape shape) {
//        if(shape.contains(line.getP2())==false) {
//            String errorString =
//                "line end point: "+line.getP2()+" is not contained in shape: "+shape.getBounds2D();
//            throw new IllegalArgumentException(errorString);
//            //return null;
//        }
//        Line2D left = new Line2D.Double();
//        Line2D right = new Line2D.Double();
//        // subdivide the line until its left segment intersects
//        // the shape boundary
//        do {
//            subdivide(line, left, right);
//            line = right;
//        } while(shape.contains(line.getP1())==false);
//        // now that right is completely inside shape,
//        // return left, which must be partially outside
//        return left;
//    }
   
    /**
     * Passed Line's point1 must be inside the passed shape or
     * an IllegalArgumentException is thrown
     * @param line line to subdivide
     * @param shape shape to compare with line
     * @return a line that intersects the shape boundary
     * @throws IllegalArgumentException if the passed line's point1 is not inside the shape
     */
//    protected Line2D getFirstOutsideSegment(Line2D line, Shape shape) {
//        
//        if(shape.contains(line.getP1())==false) {
//            String errorString = 
//                "line start point: "+line.getP1()+" is not contained in shape: "+shape.getBounds2D();
//            throw new IllegalArgumentException(errorString);
//        }
//        Line2D left = new Line2D.Float();
//        Line2D right = new Line2D.Float();
//        // subdivide the line until its right side intersects the
//        // shape boundary
//        do {
//            subdivide(line, left, right);
//            line = left;
//        } while(shape.contains(line.getP2())==false);
//        // now that left is completely inside shape,
//        // return right, which must be partially outside
//        return right;
//    }

    /**
     * divide a Line2D into 2 new Line2Ds that are returned
     * in the passed left and right instances, if non-null
     * @param src the line to divide
     * @param left the left side, or null
     * @param right the right side, or null
     */
//    protected void subdivide(Line2D src,
//            Line2D left,
//            Line2D right) {
//        double x1 = src.getX1();
//        double y1 = src.getY1();
//        double x2 = src.getX2();
//        double y2 = src.getY2();
//        
//        double mx = x1 + (x2-x1)/2.0;
//        double my = y1 + (y2-y1)/2.0;
//        if (left != null) {
//            left.setLine(x1, y1, mx, my);
//        }
//        if (right != null) {
//            right.setLine(mx, my, x2, y2);
//        }
//    }

}
