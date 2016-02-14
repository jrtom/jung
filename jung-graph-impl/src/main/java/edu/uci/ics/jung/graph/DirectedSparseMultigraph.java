/*
 * Created on Oct 17, 2005
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;


/**
 * An implementation of <code>DirectedGraph</code>, suitable for sparse graphs,
 * that permits parallel edges.
 */
@SuppressWarnings("serial")
public class DirectedSparseMultigraph<V,E> 
    extends AbstractTypedGraph<V,E>
    implements DirectedGraph<V,E>, MultiGraph<V,E> {

    /**
     * @param <V> the vertex type for the graph Supplier
     * @param <E> the edge type for the graph Supplier
     * @return a {@code Supplier} that creates an instance of this graph type.
     */
	public static <V,E> Supplier<DirectedGraph<V,E>> getFactory() {
		return new Supplier<DirectedGraph<V,E>> () {
			public DirectedGraph<V,E> get() {
				return new DirectedSparseMultigraph<V,E>();
			}
		};
	}

	protected Map<V, Pair<Set<E>>> vertices; // Map of vertices to Pair of adjacency sets {incoming, outgoing}
    protected Map<E, Pair<V>> edges;            // Map of edges to incident vertex pairs

    /**
     * Creates a new instance.
     */
    public DirectedSparseMultigraph() {
    	super(EdgeType.DIRECTED);
        vertices = new HashMap<V, Pair<Set<E>>>();
        edges = new HashMap<E, Pair<V>>();
    }
    
    public Collection<E> getEdges() {
        return Collections.unmodifiableCollection(edges.keySet());
    }

    public Collection<V> getVertices() {
        return Collections.unmodifiableCollection(vertices.keySet());
    }

    public boolean containsVertex(V vertex) {
    	return vertices.keySet().contains(vertex);
    }
    
    public boolean containsEdge(E edge) {
    	return edges.keySet().contains(edge);
    }

    protected Collection<E> getIncoming_internal(V vertex)
    {
        return vertices.get(vertex).getFirst();
    }
    
    protected Collection<E> getOutgoing_internal(V vertex)
    {
        return vertices.get(vertex).getSecond();
    }
    
    public boolean addVertex(V vertex) {
    	if(vertex == null) {
    		throw new IllegalArgumentException("vertex may not be null");
    	}
        if (!containsVertex(vertex)) {
            vertices.put(vertex, new Pair<Set<E>>(new HashSet<E>(), new HashSet<E>()));
            return true;
        } else {
            return false;
        }
    }

    public boolean removeVertex(V vertex) {
        if (!containsVertex(vertex))
            return false;
        
        // copy to avoid concurrent modification in removeEdge
        Set<E> incident = new HashSet<E>(getIncoming_internal(vertex));
        incident.addAll(getOutgoing_internal(vertex));
        
        for (E edge : incident)
            removeEdge(edge);
        
        vertices.remove(vertex);
        
        return true;
    }
    
    public boolean removeEdge(E edge) {
        if (!containsEdge(edge))
            return false;
        
        Pair<V> endpoints = this.getEndpoints(edge);
        V source = endpoints.getFirst();
        V dest = endpoints.getSecond();
        
        // remove edge from incident vertices' adjacency sets
        getOutgoing_internal(source).remove(edge);
        getIncoming_internal(dest).remove(edge);
        
        edges.remove(edge);
        return true;
    }

    
    public Collection<E> getInEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;

        return Collections.unmodifiableCollection(getIncoming_internal(vertex));
    }

    public Collection<E> getOutEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        
        return Collections.unmodifiableCollection(getOutgoing_internal(vertex));
    }

    public Collection<V> getPredecessors(V vertex) {
        if (!containsVertex(vertex))
            return null;

        Set<V> preds = new HashSet<V>();
        for (E edge : getIncoming_internal(vertex))
            preds.add(this.getSource(edge));
        
        return Collections.unmodifiableCollection(preds);
    }

    public Collection<V> getSuccessors(V vertex) {
        if (!containsVertex(vertex))
            return null;
        
        Set<V> succs = new HashSet<V>();
        for (E edge : getOutgoing_internal(vertex))
            succs.add(this.getDest(edge));
        
        return Collections.unmodifiableCollection(succs);
    }

    public Collection<V> getNeighbors(V vertex) {
        if (!containsVertex(vertex))
            return null;
        
        Collection<V> neighbors = new HashSet<V>();
        for (E edge : getIncoming_internal(vertex))
            neighbors.add(this.getSource(edge));
        for (E edge : getOutgoing_internal(vertex))
            neighbors.add(this.getDest(edge));
        return Collections.unmodifiableCollection(neighbors);
    }

    public Collection<E> getIncidentEdges(V vertex) {
        if (!containsVertex(vertex))
            return null;
        
        Collection<E> incident = new HashSet<E>();
        incident.addAll(getIncoming_internal(vertex));
        incident.addAll(getOutgoing_internal(vertex));
        return incident;
    }

    @Override
    public E findEdge(V v1, V v2) {
        if (!containsVertex(v1) || !containsVertex(v2))
            return null;
        for (E edge : getOutgoing_internal(v1))
            if (this.getDest(edge).equals(v2))
                return edge;
        
        return null;
    }
    
	@Override
  public boolean addEdge(E edge, Pair<? extends V> endpoints, EdgeType edgeType) 
	{
		this.validateEdgeType(edgeType);
        Pair<V> new_endpoints = getValidatedEndpoints(edge, endpoints);
        if (new_endpoints == null)
            return false;
        
        edges.put(edge, new_endpoints);
        
        V source = new_endpoints.getFirst();
        V dest = new_endpoints.getSecond();

        if (!containsVertex(source))
            this.addVertex(source);
        
        if (!containsVertex(dest))
            this.addVertex(dest);
        
        getIncoming_internal(dest).add(edge);
        getOutgoing_internal(source).add(edge);

        return true;
	}

    
    public V getSource(E edge) {
        if (!containsEdge(edge))
            return null;
        return this.getEndpoints(edge).getFirst();
    }

    public V getDest(E edge) {
        if (!containsEdge(edge))
            return null;
        return this.getEndpoints(edge).getSecond();
    }

    public boolean isSource(V vertex, E edge) {
        if (!containsEdge(edge) || !containsVertex(vertex))
            return false;
        return vertex.equals(this.getEndpoints(edge).getFirst());
    }

    public boolean isDest(V vertex, E edge) {
        if (!containsEdge(edge) || !containsVertex(vertex))
            return false;
        return vertex.equals(this.getEndpoints(edge).getSecond());
    }

    public Pair<V> getEndpoints(E edge) {
        return edges.get(edge);
    }

	public int getEdgeCount() {
		return edges.size();
	}

	public int getVertexCount() {
		return vertices.size();
	}
}
