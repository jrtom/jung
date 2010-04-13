/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package edu.uci.ics.jung.algorithms.importance;

import edu.uci.ics.jung.graph.UndirectedGraph;


/**
 * Computes betweenness centrality for each vertex in the graph. The betweenness values in this case
 * are based on random walks, measuring the expected number of times a node is traversed by a random walk
 * averaged over all pairs of nodes. The result is that each vertex has a UserData element of type
 * MutableDouble whose key is 'centrality.RandomWalkBetweennessCentrality'
 *
 * A simple example of usage is:  <br>
 * RandomWalkBetweenness ranker = new RandomWalkBetweenness(someGraph);   <br>
 * ranker.evaluate(); <br>
 * ranker.printRankings(); <p>
 *
 * Running time is: O((m+n)*n^2).
 * @see "Mark Newman: A measure of betweenness centrality based on random walks, 2002."

 * @author Scott White
 */
public class RandomWalkBetweenness<V,E> extends RandomWalkSTBetweenness<V,E> {

    public static final String CENTRALITY = "centrality.RandomWalkBetweennessCentrality";

    /**
     * Constructor which initializes the algorithm
     * @param g the graph whose nodes are to be analyzed
     */
    public RandomWalkBetweenness(UndirectedGraph<V,E> g) {
       super(g,null,null);
    }

    @Override
    protected void computeBetweenness() {
        setUp();

        int numVertices = getGraph().getVertexCount();
        double normalizingConstant = numVertices*(numVertices-1)/2.0;

        for (V ithVertex : getGraph().getVertices()) {

            double ithBetweenness = 0;
            for (int t=0;t<numVertices;t++) {
                for (int s=0;s<t;s++) {
                    V sthVertex = getIndexer().getKey(s);
                    V tthVertex = getIndexer().getKey(t);
                    ithBetweenness += computeSTBetweenness(ithVertex,sthVertex, tthVertex);
                }
            }
            setVertexRankScore(ithVertex,ithBetweenness/normalizingConstant);
        }
    }



    /**
     * the user datum key used to store the rank scores
     * @return the key
     */
    @Override
    public String getRankScoreKey() {
        return CENTRALITY;
    }

    protected double evaluateIteration() {
        computeBetweenness();
        return 0;
    }
}
