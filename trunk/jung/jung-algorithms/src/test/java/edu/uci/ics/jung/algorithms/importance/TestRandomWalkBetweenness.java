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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.uci.ics.jung.algorithms.importance.RandomWalkBetweenness;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

/**
 * @author Scott White
 */
public class TestRandomWalkBetweenness extends TestCase {
    public static Test suite() {
        return new TestSuite(TestRandomWalkBetweenness.class);
    }

    @Override
    protected void setUp() {

    }

    public void testRanker() {
        UndirectedGraph<Number,Number> graph = new UndirectedSparseMultigraph<Number,Number>();
        for(int i=0; i<11; i++) {
        	graph.addVertex(i);
        }

        int j=0;
        graph.addEdge(j++, 0, 1);
        graph.addEdge(j++, 0, 2);
        graph.addEdge(j++, 0, 3);
        graph.addEdge(j++, 0, 4);
        graph.addEdge(j++, 1, 2);
        graph.addEdge(j++, 1, 3);
        graph.addEdge(j++, 1, 4);
        graph.addEdge(j++, 2, 3);
        graph.addEdge(j++, 2, 4);
        graph.addEdge(j++, 3, 4);
        graph.addEdge(j++, 4, 5);
        graph.addEdge(j++, 4, 6);
        graph.addEdge(j++, 5, 6);
        graph.addEdge(j++, 6, 7);
        graph.addEdge(j++, 6, 8);
        graph.addEdge(j++, 6, 9);
        graph.addEdge(j++, 6, 10);
        graph.addEdge(j++, 7, 8);
        graph.addEdge(j++, 7, 9);
        graph.addEdge(j++, 7, 10);
        graph.addEdge(j++, 8, 9);
        graph.addEdge(j++, 8, 10);
        graph.addEdge(j++, 9, 10);

        RandomWalkBetweenness<Number,Number> bc = new RandomWalkBetweenness<Number,Number>(graph);
        bc.setRemoveRankScoresOnFinalize(false);
        bc.evaluate();

        /*
        System.out.println("C: " + bc.getRankScore(5));
        System.out.println("B: " + bc.getRankScore(6));
        System.out.println("A: " + bc.getRankScore(4));
        System.out.println("X: " + bc.getRankScore(0));
        System.out.println("Y: " + bc.getRankScore(10));
        */

        Assert.assertEquals(bc.getVertexRankScore(5),0.333,.001);
        Assert.assertEquals(bc.getVertexRankScore(6),0.67,.001);
        Assert.assertEquals(bc.getVertexRankScore(4),0.67,.001);
        Assert.assertEquals(bc.getVertexRankScore(0),0.269,.001);
        Assert.assertEquals(bc.getVertexRankScore(10),0.269,.001);



    }
}
