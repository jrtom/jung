/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.importance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;


/**
 * @author Scott White
 * @author Tom Nelson - adapted to jung2
 */
public class TestKStepMarkov extends TestCase {
    public final static String EDGE_WEIGHT = "edu.uci.ics.jung.edge_weight";
	DirectedGraph<Number,Number> mGraph;
    double[][] mTransitionMatrix;
    Map<Number,Number> edgeWeights = new HashMap<Number,Number>();

    public static Test suite() {
        return new TestSuite(TestKStepMarkov.class);
    }

    @Override
    protected void setUp()
    {
        mGraph = new DirectedSparseMultigraph<Number,Number>();
        mTransitionMatrix = new double[][]
           {{0.0, 0.5, 0.5},
            {1.0/3.0, 0.0, 2.0/3.0},
            {1.0/3.0, 2.0/3.0, 0.0}};

        for (int i = 0; i < mTransitionMatrix.length; i++)
        	mGraph.addVertex(i);

        for (int i = 0; i < mTransitionMatrix.length; i++) {
            for (int j = 0; j < mTransitionMatrix[i].length; j++)
            {
                if (mTransitionMatrix[i][j] > 0)
                {
                	int edge = i*mTransitionMatrix.length+j;
                	mGraph.addEdge(edge, i, j);
                	edgeWeights.put(edge, mTransitionMatrix[i][j]);
                }
            }
        }
    }

    public void testRanker() {

        Set<Number> priors = new HashSet<Number>();
        priors.add(1);
        priors.add(2);
        KStepMarkov<Number,Number> ranker = new KStepMarkov<Number,Number>(mGraph,priors,2,edgeWeights);
//        ranker.evaluate();
//        System.out.println(ranker.getIterations());

        for (int i = 0; i < 10; i++) 
        {
//            System.out.println(ranker.getIterations());
//	        for (Number n : mGraph.getVertices())
//	        	System.out.println(n + ": " + ranker.getVertexRankScore(n));
            
	        ranker.step();
        }
        
        
        List<Ranking<?>> rankings = ranker.getRankings();
//        System.out.println(rankings);
    }
}
