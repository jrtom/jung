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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;


/**
 * @author Scott White
 * @author Tom Nelson - adapted to jung2
 */
public class TestHITS extends TestCase {

	DirectedGraph<Number,Number> graph;
	
    public static Test suite() {
        return new TestSuite(TestHITS.class);
    }

    @Override
    protected void setUp() {
        graph = new DirectedSparseMultigraph<Number,Number>();
        for(int i=0; i<5; i++) {
        	graph.addVertex(i);
        }

        int j=0;
        graph.addEdge(j++, 0, 1);
        graph.addEdge(j++, 1, 2);
        graph.addEdge(j++, 2, 3);
        graph.addEdge(j++, 3, 0);
        graph.addEdge(j++, 2, 1);
    }

    public void testRanker() {

        HITS<Number,Number> ranker = new HITS<Number,Number>(graph);
        for (int i = 0; i < 10; i++)
        {
            ranker.step();
//            // check hub scores in terms of previous authority scores
//            Assert.assertEquals(t.transform(0).hub, 
//            		0.5*ranker.getAuthScore(1) + 0.2*ranker.getAuthScore(4));
//            Assert.assertEquals(t.transform(1).hub, 
//            		ranker.getAuthScore(2) + 0.2*ranker.getAuthScore(4));
//            Assert.assertEquals(t.transform(2).hub, 
//            		0.5*ranker.getAuthScore(1) + ranker.getAuthScore(3) + 0.2*ranker.getAuthScore(4));
//            Assert.assertEquals(t.transform(3).hub, 
//            		ranker.getAuthScore(0) + 0.2*ranker.getAuthScore(4));
//            Assert.assertEquals(t.transform(4).hub, 
//            		0.2*ranker.getAuthScore(4));
//            
//            // check authority scores in terms of previous hub scores
//            Assert.assertEquals(t.transform(0).authority, 
//            		ranker.getVertexScore(3) + 0.2*ranker.getVertexScore(4));
//            Assert.assertEquals(t.transform(1).authority, 
//            		ranker.getVertexScore(0) + 0.5 * ranker.getVertexScore(2) + 0.2*ranker.getVertexScore(4));
//            Assert.assertEquals(t.transform(2).authority, 
//            		ranker.getVertexScore(1) + 0.2*ranker.getVertexScore(4));
//            Assert.assertEquals(t.transform(3).authority, 
//            		0.5*ranker.getVertexScore(2) + 0.2*ranker.getVertexScore(4));
//            Assert.assertEquals(t.transform(4).authority, 
//            		0.2*ranker.getVertexScore(4));
//            
            // verify that sums of each scores are 1.0
            double auth_sum = 0;
            double hub_sum = 0;
            for (int j = 0; j < 5; j++)
            {
//                auth_sum += ranker.getAuthScore(j);
//                hub_sum += ranker.getVertexScore(j);
//            	auth_sum += (ranker.getAuthScore(j) * ranker.getAuthScore(j));
//            	hub_sum += (ranker.getVertexScore(j) * ranker.getVertexScore(j));
            	HITS.Scores score = ranker.getVertexScore(j);
            	auth_sum += score.authority * score.authority;
            	hub_sum += score.hub * score.hub;
            }
            Assert.assertEquals(auth_sum, 1.0, .0001);
            Assert.assertEquals(hub_sum, 1.0, 0.0001);
        }
        
        ranker.evaluate();

        Assert.assertEquals(ranker.getVertexScore(0).authority, 0, .0001);  
        Assert.assertEquals(ranker.getVertexScore(1).authority, 0.8507, .001);
        Assert.assertEquals(ranker.getVertexScore(2).authority, 0.0, .0001);
        Assert.assertEquals(ranker.getVertexScore(3).authority, 0.5257, .001);

        Assert.assertEquals(ranker.getVertexScore(0).hub, 0.5257, .001);
        Assert.assertEquals(ranker.getVertexScore(1).hub, 0.0, .0001);
        Assert.assertEquals(ranker.getVertexScore(2).hub, 0.8507, .0001);
        Assert.assertEquals(ranker.getVertexScore(3).hub, 0.0, .0001);

        // the values below assume scores sum to 1 
        // (rather than that sum of squares of scores sum to 1)
//        Assert.assertEquals(ranker.getVertexScore(0).authority, 0, .0001);  
//        Assert.assertEquals(ranker.getVertexScore(1).authority, 0.618, .001);
//        Assert.assertEquals(ranker.getVertexScore(2).authority, 0.0, .0001);
//        Assert.assertEquals(ranker.getVertexScore(3).authority, 0.3819, .001);
//
//        Assert.assertEquals(ranker.getVertexScore(0).hub, 0.38196, .001);
//        Assert.assertEquals(ranker.getVertexScore(1).hub, 0.0, .0001);
//        Assert.assertEquals(ranker.getVertexScore(2).hub, 0.618, .0001);
//        Assert.assertEquals(ranker.getVertexScore(3).hub, 0.0, .0001);
    }

}
