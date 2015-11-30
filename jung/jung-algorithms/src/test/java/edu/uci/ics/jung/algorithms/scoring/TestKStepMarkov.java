package edu.uci.ics.jung.algorithms.scoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.base.Functions;

import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class TestKStepMarkov extends TestCase 
{
	DirectedGraph<Number,Number> mGraph;
    double[][] mTransitionMatrix;
    Map<Number,Number> edgeWeights = new HashMap<Number,Number>();

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
        KStepMarkov<Number,Number> ranker = 
        	new KStepMarkov<Number,Number>(mGraph, Functions.forMap(edgeWeights), 
        			ScoringUtils.getUniformRootPrior(priors),2);
//        ranker.evaluate();
//        System.out.println(ranker.getIterations());

        for (int i = 0; i < 10; i++) 
        {
//            System.out.println(ranker.getIterations());
//	        for (Number n : mGraph.getVertices())
//	        	System.out.println(n + ": " + ranker.getVertexScore(n));
	        ranker.step();
        }
//        List<Ranking<?>> rankings = ranker.getRankings();
//        System.out.println("New version:");
//        System.out.println(rankings);
    }

}
