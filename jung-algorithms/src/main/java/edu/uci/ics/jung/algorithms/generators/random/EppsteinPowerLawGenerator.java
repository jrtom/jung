/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.generators.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
import edu.uci.ics.jung.graph.Graph;

/**
 * Graph generator that generates undirected graphs with power-law degree distributions.
 * @author Scott White
 * @see "A Steady State Model for Graph Power Law by David Eppstein and Joseph Wang"
 */
public class EppsteinPowerLawGenerator<V,E> implements GraphGenerator<V,E> {
    private int mNumVertices;
    private int mNumEdges;
    private int mNumIterations;
    private double mMaxDegree;
    private Random mRandom;
    private Supplier<Graph<V,E>> graphFactory;
    private Supplier<V> vertexFactory;
    private Supplier<E> edgeFactory;

    /**
     * Creates an instance with the specified factories and specifications.
     * @param graphFactory the Supplier to use to generate the graph
     * @param vertexFactory the Supplier to use to create vertices
     * @param edgeFactory the Supplier to use to create edges
     * @param numVertices the number of vertices for the generated graph
     * @param numEdges the number of edges the generated graph will have, should be Theta(numVertices)
     * @param r the number of iterations to use; the larger the value the better the graph's degree
     * distribution will approximate a power-law
     */
    public EppsteinPowerLawGenerator(Supplier<Graph<V,E>> graphFactory,
    		Supplier<V> vertexFactory, Supplier<E> edgeFactory, 
    		int numVertices, int numEdges, int r) {
    	this.graphFactory = graphFactory;
    	this.vertexFactory = vertexFactory;
    	this.edgeFactory = edgeFactory;
        mNumVertices = numVertices;
        mNumEdges = numEdges;
        mNumIterations = r;
        mRandom = new Random();
    }

    protected Graph<V,E> initializeGraph() {
        Graph<V,E> graph = null;
        graph = graphFactory.get();
        for(int i=0; i<mNumVertices; i++) {
        	graph.addVertex(vertexFactory.get());
        }
        List<V> vertices = new ArrayList<V>(graph.getVertices());
        while (graph.getEdgeCount() < mNumEdges) {
            V u = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
            V v = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
            if (!graph.isSuccessor(v,u)) {
            	graph.addEdge(edgeFactory.get(), u, v);
            }
        }

        double maxDegree = 0;
        for (V v : graph.getVertices()) {
            maxDegree = Math.max(graph.degree(v),maxDegree);
        }
        mMaxDegree = maxDegree; //(maxDegree+1)*(maxDegree)/2;

        return graph;
    }

    /**
     * Generates a graph whose degree distribution approximates a power-law.
     * @return the generated graph
     */
    public Graph<V,E> get() {
        Graph<V,E> graph = initializeGraph();

        List<V> vertices = new ArrayList<V>(graph.getVertices());
        for (int rIdx = 0; rIdx < mNumIterations; rIdx++) {

            V v = null;
            int degree = 0;
            do {
                v = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
                degree = graph.degree(v);

            } while (degree == 0);

            List<E> edges = new ArrayList<E>(graph.getIncidentEdges(v));
            E randomExistingEdge = edges.get((int) (mRandom.nextDouble()*degree));

            // FIXME: look at email thread on a more efficient RNG for arbitrary distributions
            
            V x = vertices.get((int) (mRandom.nextDouble() * mNumVertices));
            V y = null;
            do {
                y = vertices.get((int) (mRandom.nextDouble() * mNumVertices));

            } while (mRandom.nextDouble() > ((graph.degree(y)+1)/mMaxDegree));

            if (!graph.isSuccessor(y,x) && x != y) {
                graph.removeEdge(randomExistingEdge);
                graph.addEdge(edgeFactory.get(), x, y);
            }
        }

        return graph;
    }

    /**
     * Sets the seed for the random number generator.
     * @param seed input to the random number generator.
     */
    public void setSeed(long seed) {
        mRandom.setSeed(seed);
    }
}
