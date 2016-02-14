/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.scoring;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;


/**
 * @author Scott White
 */
public class TestPageRankWithPriors extends TestCase {

//	private Map<Integer,Number> edgeWeights;
	private DirectedGraph<Integer,Integer> graph;
	private Supplier<Integer> edgeFactory;

    public static Test suite() {
        return new TestSuite(TestPageRankWithPriors.class);
    }

    @Override
    protected void setUp() {
//    	edgeWeights = new HashMap<Integer,Number>();
    	edgeFactory = new Supplier<Integer>() {
    		int i=0;
			public Integer get() {
				return i++;
			}};
    }

    private void addEdge(Graph<Integer, Integer> G, Integer v1, Integer v2)
    {
    	Integer edge = edgeFactory.get();
    	graph.addEdge(edge, v1, v2);
//    	edgeWeights.put(edge, weight);
    }

    public void testGraphScoring() {
    	graph = new DirectedSparseMultigraph<Integer,Integer>();

    	double[] expected_score = new double[]{0.1157, 0.2463, 0.4724, 0.1653};
    	
    	for(int i=0; i<4; i++) {
    		graph.addVertex(i);
    	}
        addEdge(graph,0,1);
        addEdge(graph,1,2);
        addEdge(graph,2,3);
        addEdge(graph,3,0);
        addEdge(graph,2,1);

        Set<Integer> priors = new HashSet<Integer>();
        priors.add(2);

        PageRankWithPriors<Integer, Integer> pr = 
            new PageRankWithPriors<Integer, Integer>(graph, ScoringUtils.getUniformRootPrior(priors), 0.3);
        pr.evaluate();

        double score_sum = 0;
        for (int i = 0; i < graph.getVertexCount(); i++)
        {
        	double score = pr.getVertexScore(i);
        	Assert.assertEquals(expected_score[i], score, pr.getTolerance());
        	score_sum += score;
        }
        Assert.assertEquals(1.0, score_sum, pr.getTolerance() * graph.getVertexCount());
    }

    public void testHypergraphScoring() {
    }
}
