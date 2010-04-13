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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * @author Scott White, adapted to jung2 by Tom Nelson
 */
public class TestWeightedNIPaths extends TestCase {
	
	Factory<String> vertexFactory;
	Factory<Number> edgeFactory;

    public static Test suite() {
        return new TestSuite(TestWeightedNIPaths.class);
    }

    @Override
    protected void setUp() {
    	vertexFactory = new Factory<String>() {
    		char a = 'A';
			public String create() {
				return Character.toString(a++);
			}};
    	edgeFactory = new Factory<Number>() {
    		int count;
			public Number create() {
				return count++;
			}};
    }

    public void testRanker() {

        DirectedGraph<String,Number> graph = new DirectedSparseMultigraph<String,Number>();
        for(int i=0; i<5; i++) {
        	graph.addVertex(vertexFactory.create());
        }

        graph.addEdge(edgeFactory.create(), "A", "B");
        graph.addEdge(edgeFactory.create(), "A", "C");
        graph.addEdge(edgeFactory.create(), "A", "D");
        graph.addEdge(edgeFactory.create(), "B", "A");
        graph.addEdge(edgeFactory.create(), "B", "E");
        graph.addEdge(edgeFactory.create(), "B", "D");
        graph.addEdge(edgeFactory.create(), "C", "A");
        graph.addEdge(edgeFactory.create(), "C", "E");
        graph.addEdge(edgeFactory.create(), "C", "D");
        graph.addEdge(edgeFactory.create(), "D", "A");
        graph.addEdge(edgeFactory.create(), "D", "B");
        graph.addEdge(edgeFactory.create(), "D", "C");
        graph.addEdge(edgeFactory.create(), "D", "E");
        
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