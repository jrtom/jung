/**
 * Copyright (c) 2008, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Sep 17, 2008
 * 
 */
package edu.uci.ics.jung.algorithms.scoring;

import junit.framework.TestCase;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;

/**
 *
 */
public class TestBetweennessCentrality extends TestCase 
{
//    public void testUndirected() {
//        UndirectedGraph<Integer,Integer> graph = 
//        	new UndirectedSparseGraph<Integer,Integer>();
//        for(int i=0; i<9; i++) {
//        	graph.addVertex(i);
//        }
//
//		int edge = 0;
//		graph.addEdge(edge++, 0,1);
//		graph.addEdge(edge++, 0,6);
//		graph.addEdge(edge++, 1,2);
//		graph.addEdge(edge++, 1,3);
//		graph.addEdge(edge++, 2,4);
//		graph.addEdge(edge++, 3,4);
//		graph.addEdge(edge++, 4,5);
//		graph.addEdge(edge++, 5,8);
//		graph.addEdge(edge++, 7,8);
//		graph.addEdge(edge++, 6,7);
//
//        BetweennessCentrality<Integer,Integer> bc = 
//        	new BetweennessCentrality<Integer,Integer>(graph);
//        
////        System.out.println("scoring");
////        for (int i = 0; i < graph.getVertexCount(); i++) 
////        	  System.out.println(String.format("%d: %f", i, bc.getVertexScore(i)));
//
//        Assert.assertEquals(bc.getVertexScore(0),6.000,.001);
//        Assert.assertEquals(bc.getVertexScore(1),7.833,.001);
//        Assert.assertEquals(bc.getVertexScore(2),2.500,.001);
//        Assert.assertEquals(bc.getVertexScore(3),2.500,.001);
//        Assert.assertEquals(bc.getVertexScore(4),7.833,.001);
//        Assert.assertEquals(bc.getVertexScore(5),6.000,.001);
//        Assert.assertEquals(bc.getVertexScore(6),4.666,.001);
//        Assert.assertEquals(bc.getVertexScore(7),4.000,.001);
//        Assert.assertEquals(bc.getVertexScore(8),4.666,.001);
//
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(0,1)),10.666,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(0,6)),9.333,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(1,2)),6.500,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(1,3)),6.500,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(2,4)),6.500,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(3,4)),6.500,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(4,5)),10.666,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(5,8)),9.333,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(6,7)),8.000,.001);
//        Assert.assertEquals(bc.getEdgeScore(graph.findEdge(7,8)),8.000,.001);
//    }
//    
//    public void testDirected() 
//    {
//    	DirectedGraph<Integer,Integer> graph = new DirectedSparseGraph<Integer,Integer>();
//    	for(int i=0; i<5; i++) 
//    		graph.addVertex(i);
//
//    	int edge=0;
//    	graph.addEdge(edge++, 0,1);
//    	graph.addEdge(edge++, 1,2);
//    	graph.addEdge(edge++, 3,1);
//    	graph.addEdge(edge++, 4,2);
//
//    	BetweennessCentrality<Integer,Integer> bc = 
//    		new BetweennessCentrality<Integer,Integer>(graph);
//
//    	Assert.assertEquals(bc.getVertexScore(0),0,.001);
//    	Assert.assertEquals(bc.getVertexScore(1),2,.001);
//    	Assert.assertEquals(bc.getVertexScore(2),0,.001);
//    	Assert.assertEquals(bc.getVertexScore(3),0,.001);
//    	Assert.assertEquals(bc.getVertexScore(4),0,.001);
//
//    	Assert.assertEquals(bc.getEdgeScore(graph.findEdge(0,1)),2,.001);
//    	Assert.assertEquals(bc.getEdgeScore(graph.findEdge(1,2)),3,.001);
//    	Assert.assertEquals(bc.getEdgeScore(graph.findEdge(3,1)),2,.001);
//    	Assert.assertEquals(bc.getEdgeScore(graph.findEdge(4,2)),1,.001);
//    }
    
    public void testWeighted()
    {
    	Graph<Integer, Character> graph = new DirectedSparseGraph<Integer, Character>();
    	
    	for(int i=0; i<5; i++) 
    		graph.addVertex(i);

    	char edge='a';
    	graph.addEdge(edge++, 0,1);
    	graph.addEdge(edge++, 0,2);
    	graph.addEdge(edge++, 2,3);
    	graph.addEdge(edge++, 3,1);
    	graph.addEdge(edge++, 1,4);

    	final int weights[] = {1, 1, 1, 1, 1};
    	
    	Function<Character, Integer> edge_weights = new Function<Character, Integer>()
    	{
			public Integer apply(Character arg0) { return weights[arg0 - 'a']; }
    	};
    	
    	BetweennessCentrality<Integer,Character> bc = 
    		new BetweennessCentrality<Integer,Character>(graph, edge_weights);

//    	System.out.println("scoring");
//    	System.out.println("(weighted)");
//    	System.out.println("vertices:");
//    	for (int i = 0; i < graph.getVertexCount(); i++) 
//    		System.out.println(String.format("%d: %f", i, bc.getVertexScore(i)));
//    	System.out.println("edges:");
//    	for (int i = 0; i < graph.getEdgeCount(); i++) 
//    	{
//    		char e = (char)(i + 'a');
//    		System.out.println(String.format("%c: (weight: %d), %f", e, 
//    				edge_weights.apply(e), bc.getEdgeScore(e)));
//    	}
    }
}
