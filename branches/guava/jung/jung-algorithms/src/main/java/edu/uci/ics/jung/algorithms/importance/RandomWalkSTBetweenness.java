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

import org.apache.commons.collections15.BidiMap;

import cern.colt.matrix.DoubleMatrix2D;
import edu.uci.ics.jung.algorithms.matrix.GraphMatrixOperations;
import edu.uci.ics.jung.algorithms.util.Indexer;
import edu.uci.ics.jung.graph.UndirectedGraph;


/**
 * /**
 * Computes s-t betweenness centrality for each vertex in the graph. The betweenness values in this case
 * are based on random walks, measuring the expected number of times a node is traversed by a random walk
 * from s to t. The result is that each vertex has a UserData element of type
 * MutableDouble whose key is 'centrality.RandomWalkBetweennessCentrality'
 *
 * A simple example of usage is:  <br>
 * RandomWalkSTBetweenness ranker = new RandomWalkBetweenness(someGraph,someSource,someTarget);   <br>
 * ranker.evaluate(); <br>
 * ranker.printRankings(); <p>
 *
 * Running time is: O(n^3).
 * @see "Mark Newman: A measure of betweenness centrality based on random walks, 2002."

 * @author Scott White
 */
public class RandomWalkSTBetweenness<V,E> extends AbstractRanker<V,E> {

    public static final String CENTRALITY = "centrality.RandomWalkSTBetweennessCentrality";
    private DoubleMatrix2D mVoltageMatrix;
    private BidiMap<V,Integer> mIndexer;
    V mSource;
    V mTarget;

   /**
    * Constructor which initializes the algorithm
    * @param g the graph whose nodes are to be analyzed
    * @param s the source vertex
    * @param t the target vertex
    */
    public RandomWalkSTBetweenness(UndirectedGraph<V,E> g, V s, V t) {
        initialize(g, true, false);
        mSource = s;
        mTarget = t;
    }

    protected BidiMap<V,Integer> getIndexer() {
        return mIndexer;
    }

    protected DoubleMatrix2D getVoltageMatrix() {
        return mVoltageMatrix;
    }

    protected void setUp() {
        mVoltageMatrix = GraphMatrixOperations.<V,E>computeVoltagePotentialMatrix((UndirectedGraph<V,E>) getGraph());
        mIndexer = Indexer.<V>create(getGraph().getVertices());
    }

    protected void computeBetweenness() {
        setUp();

        for (V v : getGraph().getVertices()) {
            setVertexRankScore(v,computeSTBetweenness(v,mSource, mTarget));
        }
    }

    public double computeSTBetweenness(V ithVertex, V source, V target) {
        if (ithVertex == source || ithVertex == target) return 1;
        if (mVoltageMatrix == null) {
            setUp();
        }
        int i = mIndexer.get(ithVertex);
        int s = mIndexer.get(source);
        int t = mIndexer.get(target);
        
        double betweenness = 0;
        for (V jthVertex : getGraph().getSuccessors(ithVertex)) {
            int j = mIndexer.get(jthVertex);
            double currentFlow = 0;
            currentFlow += mVoltageMatrix.get(i,s);
            currentFlow -= mVoltageMatrix.get(i,t);
            currentFlow -= mVoltageMatrix.get(j,s);
            currentFlow += mVoltageMatrix.get(j,t);
            betweenness += Math.abs(currentFlow);
        }
        return betweenness/2.0;
    }

    /**
     * the user datum key used to store the rank scores
     * @return the key
     */
    @Override
    public String getRankScoreKey() {
        return CENTRALITY;
    }

    @Override
    public void step() {
        computeBetweenness();
    }
}
