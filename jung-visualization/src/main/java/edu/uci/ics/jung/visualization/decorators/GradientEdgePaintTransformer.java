/*
 * Created on Apr 8, 2005
 *
 * Copyright (c) 2004, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.util.SelfLoopEdgePredicate;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;

/**
 * Creates <code>GradientPaint</code> instances which can be used
 * to paint an <code>Edge</code>.  For <code>DirectedEdge</code>s, 
 * the color will blend from <code>c1</code> (source) to 
 * <code>c2</code> (destination); for <code>UndirectedEdge</code>s,
 * the color will be <code>c1</code> at each end and <code>c2</code>
 * in the middle.
 * 
 * @author Joshua O'Madadhain
 */
public class GradientEdgePaintTransformer<V, E> 
	implements Function<E,Paint>
{
    protected Color c1;
    protected Color c2;
    protected VisualizationViewer<V,E> vv;
    protected BidirectionalTransformer transformer;
    protected Predicate<Context<Graph<V,E>,E>> selfLoop = new SelfLoopEdgePredicate<V,E>();

    public GradientEdgePaintTransformer(Color c1, Color c2, 
            VisualizationViewer<V,E> vv)
    {
        this.c1 = c1;
        this.c2 = c2;
        this.vv = vv;
        this.transformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT);
    }
    
    public Paint apply(E e)
    {
        Layout<V, E> layout = vv.getGraphLayout();
        Pair<V> p = layout.getGraph().getEndpoints(e);
        V b = p.getFirst();
        V f = p.getSecond();
        Point2D pb = transformer.transform(layout.apply(b));
        Point2D pf = transformer.transform(layout.apply(f));
        float xB = (float) pb.getX();
        float yB = (float) pb.getY();
        float xF = (float) pf.getX();
        float yF = (float) pf.getY();
        if ((layout.getGraph().getEdgeType(e)) == EdgeType.UNDIRECTED)  {
            xF = (xF + xB) / 2;
            yF = (yF + yB) / 2;
        } 
        if(selfLoop.apply(Context.<Graph<V,E>,E>getInstance(layout.getGraph(), e))) {
        	yF += 50;
        	xF += 50;
        }

        return new GradientPaint(xB, yB, getColor1(e), xF, yF, getColor2(e), true);
    }
    
    /**
     * Returns <code>c1</code>.  Subclasses may override
     * this method to enable more complex behavior (e.g., for
     * picked edges).
     * @param e the edge for which a color is to be retrieved
     * @return the constructor-supplied color {@code c1}
     */
    protected Color getColor1(E e)
    {
        return c1;
    }

    /**
     * Returns <code>c2</code>.  Subclasses may override
     * this method to enable more complex behavior (e.g., for
     * picked edges).
     * @param e the edge for which a color is to be retrieved
     * @return the constructor-supplied color {@code c2}
     */
    protected Color getColor2(E e)
    {
        return c2;
    }
}
