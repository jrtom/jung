/*
 * Created on Sep 24, 2005
 *
 * Copyright (c) 2005, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Predicate;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * A class which creates and maintains indices for parallel edges.
 * Edges are evaluated by a Predicate function and those that
 * evaluate to true are excluded from computing a parallel offset
 * 
 * @author Tom Nelson
 *
 */
public class PredicatedParallelEdgeIndexFunction<V,E> implements EdgeIndexFunction<V,E> {
    protected Map<E, Integer> edge_index = new HashMap<E, Integer>();
    protected Predicate<E> predicate;
    
	/**
     * @param graph the graph with respect to which the index is calculated
	 */
    private PredicatedParallelEdgeIndexFunction() {
    }
    
    public static <V,E> PredicatedParallelEdgeIndexFunction<V,E> getInstance() {
        return new PredicatedParallelEdgeIndexFunction<V,E>();
    }

    /**
     * Returns the index for the specified edge.
     * Calculates the indices for <code>e</code> and for all edges parallel
     * to <code>e</code>.
     * @param graph the graph with respect to which the index is calculated
     * @param e the edge whose index is to be calculated
     * 
     * @return the index of the edge with respect to this index function
     */
    public int getIndex(Graph<V, E> graph, E e) {
    	
    	if(predicate.apply(e)) {
    		return 0;
    	}
        Integer index = edge_index.get(e);
        if(index == null) {
        	Pair<V> endpoints = graph.getEndpoints(e);
        	V u = endpoints.getFirst();
        	V v = endpoints.getSecond();
        	if(u.equals(v)) {
        		index = getIndex(graph, e, v);
        	} else {
        		index = getIndex(graph, e, u, v);
        	}
        }
        return index.intValue();
    }

    protected int getIndex(Graph<V,E> graph, E e, V v, V u) {
    	Collection<E> commonEdgeSet = new HashSet<E>(graph.getIncidentEdges(u));
    	commonEdgeSet.retainAll(graph.getIncidentEdges(v));
    	for(Iterator<E> iterator=commonEdgeSet.iterator(); iterator.hasNext(); ) {
    		E edge = iterator.next();
    		Pair<V> ep = graph.getEndpoints(edge);
    		V first = ep.getFirst();
    		V second = ep.getSecond();
    		// remove loops
    		if(first.equals(second) == true) {
    			iterator.remove();
    		}
    		// remove edges in opposite direction
    		if(first.equals(v) == false) {
    			iterator.remove();
    		}
    	}
    	int count=0;
    	for(E other : commonEdgeSet) {
    		if(e.equals(other) == false) {
    			edge_index.put(other, count);
    			count++;
    		}
    	}
    	edge_index.put(e, count);
    	return count;
     }
    
    protected int getIndex(Graph<V,E> graph, E e, V v) {
    	Collection<E> commonEdgeSet = new HashSet<E>();
    	for(E another : graph.getIncidentEdges(v)) {
    		V u = graph.getOpposite(v, another);
    		if(u.equals(v)) {
    			commonEdgeSet.add(another);
    		}
    	}
    	int count=0;
    	for(E other : commonEdgeSet) {
    		if(e.equals(other) == false) {
    			edge_index.put(other, count);
    			count++;
    		}
    	}
    	edge_index.put(e, count);
    	return count;
    }

	public Predicate<E> getPredicate() {
		return predicate;
	}

	public void setPredicate(Predicate<E> predicate) {
		this.predicate = predicate;
	}

	/**
     * Resets the indices for this edge and its parallel edges.
     * Should be invoked when an edge parallel to <code>e</code>
     * has been added or removed in this graph.
     * @param graph the graph with respect to which the index is calculated
     * @param e the edge whose indices are to be reset for {@code graph}
     */
    public void reset(Graph<V,E> graph, E e) {
    	Pair<V> endpoints = graph.getEndpoints(e);
        getIndex(graph, e, endpoints.getFirst());
        getIndex(graph, e, endpoints.getFirst(), endpoints.getSecond());
    }
    
    /**
     * Clears all edge indices for all edges in all graphs.
     * Does not recalculate the indices.
     */
    public void reset()
    {
        edge_index.clear();
    }
}
