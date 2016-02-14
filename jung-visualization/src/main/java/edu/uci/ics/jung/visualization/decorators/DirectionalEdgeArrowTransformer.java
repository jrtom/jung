/*
 * Created on Jul 18, 2004
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

import java.awt.Shape;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.util.ArrowFactory;

/**
 * Returns wedge arrows for undirected edges and notched arrows
 * for directed edges, of the specified dimensions.
 * 
 * @author Joshua O'Madadhain
 */
public class DirectionalEdgeArrowTransformer<V,E> implements Function<Context<Graph<V,E>,E>,Shape> {
    protected Shape undirected_arrow;
    protected Shape directed_arrow;
    
    public DirectionalEdgeArrowTransformer(int length, int width, int notch_depth)
    {
        directed_arrow = ArrowFactory.getNotchedArrow(width, length, notch_depth);
        undirected_arrow = ArrowFactory.getWedgeArrow(width, length);
    }
    
    /**
     * 
     */
    public Shape apply(Context<Graph<V,E>,E> context)
    {
        if (context.graph.getEdgeType(context.element) == EdgeType.DIRECTED)
            return directed_arrow;
        else 
            return undirected_arrow;
    }

}
