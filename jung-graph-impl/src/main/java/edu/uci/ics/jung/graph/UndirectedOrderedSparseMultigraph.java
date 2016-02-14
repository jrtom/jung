/*
 * Created on Oct 18, 2005
 *
 * Copyright (c) 2005, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.util.Pair;

/**
 * An implementation of <code>UndirectedGraph</code> that is suitable for sparse graphs,
 * orders its vertex and edge collections according to insertion time, and permits
 * parallel edges.
 */
@SuppressWarnings("serial")
public class UndirectedOrderedSparseMultigraph<V,E> 
    extends UndirectedSparseMultigraph<V,E>
    implements UndirectedGraph<V,E> {
	
    /**
     * @param <V> the vertex type for the graph Supplier
     * @param <E> the edge type for the graph Supplier
     * @return a {@code Supplier} that creates an instance of this graph type.
     */
	public static <V,E> Supplier<UndirectedGraph<V,E>> getFactory() {
		return new Supplier<UndirectedGraph<V,E>> () {
			public UndirectedGraph<V,E> get() {
				return new UndirectedOrderedSparseMultigraph<V,E>();
			}
		};
	}

	/**
	 * Creates a new instance.
	 */
    public UndirectedOrderedSparseMultigraph() {
        vertices = new LinkedHashMap<V, Set<E>>();
        edges = new LinkedHashMap<E, Pair<V>>();
    }

    @Override
    public boolean addVertex(V vertex) {
    	if(vertex == null) {
    		throw new IllegalArgumentException("vertex may not be null");
    	}
        if (!containsVertex(vertex))
        {
            vertices.put(vertex, new LinkedHashSet<E>());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Collection<V> getNeighbors(V vertex) {
        if (!containsVertex(vertex))
            return null;
        
        Set<V> neighbors = new LinkedHashSet<V>();
        for (E edge : getIncident_internal(vertex))
        {
            Pair<V> endpoints = this.getEndpoints(edge);
            V e_a = endpoints.getFirst();
            V e_b = endpoints.getSecond();
            if (vertex.equals(e_a))
                neighbors.add(e_b);
            else
                neighbors.add(e_a);
        }
        
        return Collections.unmodifiableCollection(neighbors);
    }
}
