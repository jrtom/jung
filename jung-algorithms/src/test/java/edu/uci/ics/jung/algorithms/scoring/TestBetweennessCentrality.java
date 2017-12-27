/**
 * Copyright (c) 2008, The JUNG Authors
 *
 * <p>All rights reserved.
 *
 * <p>This software is open-source under the BSD license; see either "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description. Created on Sep 17, 2008
 */
package edu.uci.ics.jung.algorithms.scoring;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import java.util.function.Function;
import junit.framework.TestCase;

/** */
public class TestBetweennessCentrality extends TestCase {
  //    public void testUndirected() {
  //        UndirectedGraph<Integer,Integer> graph =
  //        	new UndirectedSparseGraph<Integer,Integer>();
  //        for(int i=0; i<9; i++) {
  //        	graph.addNode(i);
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
  ////        for (int i = 0; i < graph.getNodeCount(); i++)
  ////        	  System.out.println(String.format("%d: %f", i, bc.getNodeScore(i)));
  //
  //        Assert.assertEquals(bc.getNodeScore(0),6.000,.001);
  //        Assert.assertEquals(bc.getNodeScore(1),7.833,.001);
  //        Assert.assertEquals(bc.getNodeScore(2),2.500,.001);
  //        Assert.assertEquals(bc.getNodeScore(3),2.500,.001);
  //        Assert.assertEquals(bc.getNodeScore(4),7.833,.001);
  //        Assert.assertEquals(bc.getNodeScore(5),6.000,.001);
  //        Assert.assertEquals(bc.getNodeScore(6),4.666,.001);
  //        Assert.assertEquals(bc.getNodeScore(7),4.000,.001);
  //        Assert.assertEquals(bc.getNodeScore(8),4.666,.001);
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
  //    		graph.addNode(i);
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
  //    	Assert.assertEquals(bc.getNodeScore(0),0,.001);
  //    	Assert.assertEquals(bc.getNodeScore(1),2,.001);
  //    	Assert.assertEquals(bc.getNodeScore(2),0,.001);
  //    	Assert.assertEquals(bc.getNodeScore(3),0,.001);
  //    	Assert.assertEquals(bc.getNodeScore(4),0,.001);
  //
  //    	Assert.assertEquals(bc.getEdgeScore(graph.findEdge(0,1)),2,.001);
  //    	Assert.assertEquals(bc.getEdgeScore(graph.findEdge(1,2)),3,.001);
  //    	Assert.assertEquals(bc.getEdgeScore(graph.findEdge(3,1)),2,.001);
  //    	Assert.assertEquals(bc.getEdgeScore(graph.findEdge(4,2)),1,.001);
  //    }

  public void testWeighted() {
    MutableNetwork<Integer, Character> graph = NetworkBuilder.directed().build();

    char edge = 'a';
    graph.addEdge(0, 1, edge++);
    graph.addEdge(0, 2, edge++);
    graph.addEdge(2, 3, edge++);
    graph.addEdge(3, 1, edge++);
    graph.addEdge(1, 4, edge++);

    final int weights[] = {1, 1, 1, 1, 1};

    Function<Character, Integer> edge_weights =
        new Function<Character, Integer>() {
          public Integer apply(Character arg0) {
            return weights[arg0 - 'a'];
          }
        };

    BetweennessCentrality<Integer, Character> bc =
        new BetweennessCentrality<Integer, Character>(graph, edge_weights);

    //    	System.out.println("scoring");
    //    	System.out.println("(weighted)");
    //    	System.out.println("nodes:");
    //    	for (int i = 0; i < graph.getNodeCount(); i++)
    //    		System.out.println(String.format("%d: %f", i, bc.getNodeScore(i)));
    //    	System.out.println("edges:");
    //    	for (int i = 0; i < graph.getEdgeCount(); i++)
    //    	{
    //    		char e = (char)(i + 'a');
    //    		System.out.println(String.format("%c: (weight: %d), %f", e,
    //    				edge_weights.apply(e), bc.getEdgeScore(e)));
    //    	}
  }
}
