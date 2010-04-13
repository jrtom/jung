/*
 * Created on Oct 21, 2004
 *
 * Copyright (c) 2004, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.visualization.decorators;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;


/**
 * Returns the constructor-specified value for each edge type.
 * 
 * @author Joshua O'Madadhain
 */
public class ConstantDirectionalEdgeValueTransformer<V,E> implements Transformer<Context<Graph<V,E>,E>,Number>
{
    protected Double undirected_value;
    protected Double directed_value;

    /**
     * 
     * @param undirected
     * @param directed
     */
    public ConstantDirectionalEdgeValueTransformer(double undirected, double directed)
    {
        this.undirected_value = new Double(undirected);
        this.directed_value = new Double(directed);
    }
    
    /**
     * @see Transformer#transform(Object)
     */
    public Number transform(Context<Graph<V,E>,E> context) {
    	Graph<V,E> graph = context.graph;
    	E e = context.element;
        if (graph.getEdgeType(e) == EdgeType.DIRECTED)
            return directed_value;
        else 
            return undirected_value;
    }
    
    /**
     * Sets the value returned for undirected edges to <code>value</code>.
     * @param value the new value to return for undirected edges
     */
    public void setUndirectedValue(double value)
    {
    	this.undirected_value = value;
    }
    
    /**
     * Sets the value returned for directed edges to <code>value</code>.
     * @param value the new value to return for directed edges
     */
    public void setDirectedValue(double value)
    {
    	this.directed_value = value;
    }
}
