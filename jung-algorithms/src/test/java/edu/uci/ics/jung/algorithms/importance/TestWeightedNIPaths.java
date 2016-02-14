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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * @author Scott White, adapted to jung2 by Tom Nelson
 */
public class TestWeightedNIPaths extends TestCase {
	
	Supplier<String> vertexFactory;
	Supplier<Number> edgeFactory;

    public static Test suite() {
        return new TestSuite(TestWeightedNIPaths.class);
    }

    @Override
    protected void setUp() {
    	vertexFactory = new Supplier<String>() {
    		char a = 'A';
			public String get() {
				return Character.toString(a++);
			}};
    	edgeFactory = new Supplier<Number>() {
    		int count;
			public Number get() {
				return count++;
			}};
    }

    public void testRanker() {

        DirectedGraph<String,Number> graph = new DirectedSparseMultigraph<String,Number>();
        for(int i=0; i<5; i++) {
        	graph.addVertex(vertexFactory.get());
        }

        graph.addEdge(edgeFactory.get(), "A", "B");
        graph.addEdge(edgeFactory.get(), "A", "C");
        graph.addEdge(edgeFactory.get(), "A", "D");
        graph.addEdge(edgeFactory.get(), "B", "A");
        graph.addEdge(edgeFactory.get(), "B", "E");
        graph.addEdge(edgeFactory.get(), "B", "D");
        graph.addEdge(edgeFactory.get(), "C", "A");
        graph.addEdge(edgeFactory.get(), "C", "E");
        graph.addEdge(edgeFactory.get(), "C", "D");
        graph.addEdge(edgeFactory.get(), "D", "A");
        graph.addEdge(edgeFactory.get(), "D", "B");
        graph.addEdge(edgeFactory.get(), "D", "C");
        graph.addEdge(edgeFactory.get(), "D", "E");
        
        Set<String> priors = new HashSet<String>();
        priors.add("A");

        WeightedNIPaths<String,Number> ranker = 
        	new WeightedNIPaths<String,Number>(graph, vertexFactory, edgeFactory, 2.0,3,priors);
        ranker.evaluate();

        Assert.assertEquals(ranker.getRankings().get(0).rankScore,0.277787,.0001);
        Assert.assertEquals(ranker.getRankings().get(1).rankScore,0.222222,.0001);
        Assert.assertEquals(ranker.getRankings().get(2).rankScore,0.166676,.0001);
        Assert.assertEquals(ranker.getRankings().get(3).rankScore,0.166676,.0001);
        Assert.assertEquals(ranker.getRankings().get(4).rankScore,0.166676,.0001);
    }
}