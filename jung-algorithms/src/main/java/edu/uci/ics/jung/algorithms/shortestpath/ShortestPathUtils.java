/*
 * Created on Jul 10, 2005
 *
 * Copyright (c) 2005, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Utilities relating to the shortest paths in a graph.
 */
public class ShortestPathUtils
{
	/**
     * Returns a <code>List</code> of the edges on the shortest path from 
     * <code>source</code> to <code>target</code>, in order of their
     * occurrence on this path.  
	 * 
	 * @param graph the graph for which the shortest path is defined
	 * @param sp holder of the shortest path information
	 * @param source the vertex from which the shortest path is measured
	 * @param target the vertex to which the shortest path is measured
	 * @param <V> the vertex type
	 * @param <E> the edge type
	 * @return the edges on the shortest path from {@code source} to {@code target},
	 *     in the order traversed
	 */
    public static <V, E> List<E> getPath(Graph<V,E> graph, ShortestPath<V,E> sp, V source, V target)
    {
        LinkedList<E> path = new LinkedList<E>();
        
        Map<V,E> incomingEdges = sp.getIncomingEdgeMap(source);
        
        if (incomingEdges.isEmpty() || incomingEdges.get(target) == null)
            return path;
        V current = target;
        while (!current.equals(source))
        {
            E incoming = incomingEdges.get(current);
            path.addFirst(incoming);
            Pair<V> endpoints = graph.getEndpoints(incoming);
            if(endpoints.getFirst().equals(current)) {	
            	current = endpoints.getSecond();
            } else {
            	current = endpoints.getFirst();
            }
        }
        return path;
    }
}
