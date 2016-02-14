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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * @author Scott White
 */
public class TestBetweennessCentrality extends TestCase {
    public static Test suite() {
        return new TestSuite(TestBetweennessCentrality.class);
    }

    @Override
    protected void setUp() {}

//    private static <V,E> E getEdge(Graph<V,E> g, int v1Index, int v2Index, BidiMap<V,Integer> id) {
//        V v1 = id.getKey(v1Index);
//        V v2 = id.getKey(v2Index);
//        return g.findEdge(v1, v2);
//    }

    public void testRanker() {
        UndirectedGraph<Integer,Integer> graph = 
        	new UndirectedSparseGraph<Integer,Integer>();
        for(int i=0; i<9; i++) {
        	graph.addVertex(i);
        }

		int edge = 0;
		graph.addEdge(edge++, 0,1);
		graph.addEdge(edge++, 0,6);
		graph.addEdge(edge++, 1,2);
		graph.addEdge(edge++, 1,3);
		graph.addEdge(edge++, 2,4);
		graph.addEdge(edge++, 3,4);
		graph.addEdge(edge++, 4,5);
		graph.addEdge(edge++, 5,8);
		graph.addEdge(edge++, 7,8);
		graph.addEdge(edge++, 6,7);

        BetweennessCentrality<Integer,Integer> bc = 
        	new BetweennessCentrality<Integer,Integer>(graph);
        bc.setRemoveRankScoresOnFinalize(false);
        bc.evaluate();

//        System.out.println("ranking");
//        for (int i = 0; i < 9; i++) 
//        	System.out.println(String.format("%d: %f", i, bc.getVertexRankScore(i)));
        
        Assert.assertEquals(bc.getVertexRankScore(0)/28.0,0.2142,.001);
        Assert.assertEquals(bc.getVertexRankScore(1)/28.0,0.2797,.001);
        Assert.assertEquals(bc.getVertexRankScore(2)/28.0,0.0892,.001);
        Assert.assertEquals(bc.getVertexRankScore(3)/28.0,0.0892,.001);
        Assert.assertEquals(bc.getVertexRankScore(4)/28.0,0.2797,.001);
        Assert.assertEquals(bc.getVertexRankScore(5)/28.0,0.2142,.001);
        Assert.assertEquals(bc.getVertexRankScore(6)/28.0,0.1666,.001);
        Assert.assertEquals(bc.getVertexRankScore(7)/28.0,0.1428,.001);
        Assert.assertEquals(bc.getVertexRankScore(8)/28.0,0.1666,.001);

        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(0,1)),
        		10.66666,.001);

        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(0,1)),10.66666,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(0,6)),9.33333,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(1,2)),6.5,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(1,3)),6.5,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(2,4)),6.5,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(3,4)),6.5,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(4,5)),10.66666,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(5,8)),9.33333,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(6,7)),8.0,.001);
        Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(7,8)),8.0,.001);
    }
    
    public void testRankerDirected() {
    	DirectedGraph<Integer,Integer> graph = new DirectedSparseGraph<Integer,Integer>();
    	for(int i=0; i<5; i++) {
    		graph.addVertex(i);
    	}

    	int edge=0;
    	graph.addEdge(edge++, 0,1);
    	graph.addEdge(edge++, 1,2);
    	graph.addEdge(edge++, 3,1);
    	graph.addEdge(edge++, 4,2);

    	BetweennessCentrality<Integer,Integer> bc = 
    		new BetweennessCentrality<Integer,Integer>(graph);
    	bc.setRemoveRankScoresOnFinalize(false);
    	bc.evaluate();

    	Assert.assertEquals(bc.getVertexRankScore(0),0,.001);
    	Assert.assertEquals(bc.getVertexRankScore(1),2,.001);
    	Assert.assertEquals(bc.getVertexRankScore(2),0,.001);
    	Assert.assertEquals(bc.getVertexRankScore(3),0,.001);
    	Assert.assertEquals(bc.getVertexRankScore(4),0,.001);

    	Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(0,1)),2,.001);
    	Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(1,2)),3,.001);
    	Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(3,1)),2,.001);
    	Assert.assertEquals(bc.getEdgeRankScore(graph.findEdge(4,2)),1,.001);
    }
}
