/*
 * Created on Jul 12, 2007
 *
 * Copyright (c) 2007, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Hypergraph;

/**
 * Calculates eigenvector centrality for each vertex in the graph.
 * The 'eigenvector centrality' for a vertex is defined as the fraction of
 * time that a random walk(er) will spend at that vertex over an infinite
 * time horizon.
 * Assumes that the graph is strongly connected.
 */
public class EigenvectorCentrality<V,E> extends PageRank<V,E>
{
    /**
     * Creates an instance with the specified graph and edge weights.
     * The outgoing edge weights for each edge must sum to 1.
     * (See <code>UniformDegreeWeight</code> for one way to handle this for
     * undirected graphs.)
     * @param graph the graph for which the centrality is to be calculated
     * @param edge_weights the edge weights 
     */
    public EigenvectorCentrality(Hypergraph<V,E> graph, 
    		Function<E, ? extends Number> edge_weights)
    {
        super(graph, edge_weights, 0);
        acceptDisconnectedGraph(false);
    }

    /**
     * Creates an instance with the specified graph and default edge weights.
     * (Default edge weights: <code>UniformDegreeWeight</code>.)
     * @param graph the graph for which the centrality is to be calculated.
     */
    public EigenvectorCentrality(Hypergraph<V,E> graph)
    {
        super(graph, 0);
        acceptDisconnectedGraph(false);
    }
}
