/**
 * Copyright (c) 2008, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Jul 14, 2008
 * 
 */
package edu.uci.ics.jung.algorithms.scoring.util;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * An edge weight function that assigns weights as uniform
 * transition probabilities.
 * For undirected edges, returns 1/degree(v) (where 'v' is the
 * vertex in the VEPair.
 * For directed edges, returns 1/outdegree(source(e)) (where 'e'
 * is the edge in the VEPair).
 * Throws an <code>IllegalArgumentException</code> if the input 
 * edge is neither EdgeType.UNDIRECTED nor EdgeType.DIRECTED.
 *
 */
public class UniformDegreeWeight<V, E> implements
		Function<VEPair<V, E>, Double> 
{
    private Hypergraph<V, E> graph;
    
    /**
     * @param graph the graph for which an instance is being created
     */
    public UniformDegreeWeight(Hypergraph<V, E> graph)
    {
        this.graph = graph;
    }

	public Double apply(VEPair<V, E> ve_pair) 
	{
		E e = ve_pair.getE();
		V v = ve_pair.getV();
		EdgeType edge_type = graph.getEdgeType(e);
		if (edge_type == EdgeType.UNDIRECTED)
			return 1.0 / graph.degree(v);
		if (edge_type == EdgeType.DIRECTED)
			return 1.0 / graph.outDegree(graph.getSource(e));
		throw new IllegalArgumentException("can't handle edge type: " + edge_type);
	}

}
