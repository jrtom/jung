/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Jul 2, 2003
 *  
 */
package edu.uci.ics.jung.algorithms.generators.random;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Generates a mixed-mode random graph (with random edge weights) based on the output of 
 * <code>BarabasiAlbertGenerator</code>.
 * Primarily intended for providing a heterogeneous sample graph for visualization testing, etc.
 */
public class MixedRandomGraphGenerator {

    /**
     * Returns a random mixed-mode graph.  Starts with a randomly generated 
     * Barabasi-Albert (preferential attachment) generator 
     * (4 initial vertices, 3 edges added at each step, and num_vertices - 4 evolution steps).
     * Then takes the resultant graph, replaces random undirected edges with directed
     * edges, and assigns random weights to each edge.
	 * @param <V> the vertex type
	 * @param <E> the edge type
     * @param graphFactory factory for graphs of the appropriate type
     * @param vertexFactory factory for vertices of the appropriate type
     * @param edgeFactory factory for edges of the appropriate type
     * @param edge_weights storage for the edge weights that this generator creates
     * @param num_vertices number of vertices to generate
     * @param seedVertices storage for the seed vertices that this generator creates
     * @return the generated graph
     */
    public static <V,E> Graph<V,E> generateMixedRandomGraph(
    		Supplier<Graph<V,E>> graphFactory,
    		Supplier<V> vertexFactory,
    		Supplier<E> edgeFactory,
    		Map<E,Number> edge_weights, 
            int num_vertices, Set<V> seedVertices)
    {
        int seed = (int)(Math.random() * 10000);
        BarabasiAlbertGenerator<V,E> bag = 
            new BarabasiAlbertGenerator<V,E>(graphFactory, vertexFactory, edgeFactory,
            		4, 3, //false, parallel, 
            		seed, seedVertices);
        bag.evolveGraph(num_vertices - 4);
        Graph<V, E> ug = bag.get();

        Graph<V, E> g = graphFactory.get();
        for(V v : ug.getVertices()) {
        	g.addVertex(v);
        }
        
        // randomly replace some of the edges by directed edges to 
        // get a mixed-mode graph, add random weights
        
        for(E e : ug.getEdges()) {
            V v1 = ug.getEndpoints(e).getFirst();
            V v2 = ug.getEndpoints(e).getSecond();

            E me = edgeFactory.get();
            g.addEdge(me, v1, v2, Math.random() < .5 ? EdgeType.DIRECTED : EdgeType.UNDIRECTED);
            edge_weights.put(me, Math.random());
        }
        
        return g;
    }
    
}
