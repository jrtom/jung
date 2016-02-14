/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.flows;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.base.Functions;
import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * @author Scott White, Joshua O'Madadhain, Tom Nelson
 */
public class TestEdmondsKarpMaxFlow extends TestCase {

	public static Test suite() {
		return new TestSuite(TestEdmondsKarpMaxFlow.class);
	}

	@Override
  protected void setUp() {

	}

    public void testSanityChecks() 
    {
        DirectedGraph<Number,Number> g = new DirectedSparseMultigraph<Number,Number>();
        Number source = new Integer(1);
        Number sink = new Integer(2);
        g.addVertex(source);
        g.addVertex(sink);
        
        Number v = new Integer(3);
        
        DirectedGraph<Number,Number> h = new DirectedSparseMultigraph<Number,Number>();
        Number w = new Integer(4);
        g.addVertex(w);
        
        try
        {
            new EdmondsKarpMaxFlow<Number,Number>(g, source, source, null, null, null);
            fail("source and sink vertices not distinct");
        }
        catch (IllegalArgumentException iae) {}

        try
        {
            new EdmondsKarpMaxFlow<Number,Number>(h, source, w, null, null, null);
            fail("source and sink vertices not both part of specified graph");
        }
        catch (IllegalArgumentException iae) {}

        try
        {
            new EdmondsKarpMaxFlow<Number,Number>(g, source, v, null, null, null);
            fail("source and sink vertices not both part of specified graph");
        }
        catch (IllegalArgumentException iae) {}
    }
    
	public void testSimpleFlow() {
		DirectedGraph<Number,Number> graph = new DirectedSparseMultigraph<Number,Number>();
		Supplier<Number> edgeFactory = new Supplier<Number>() {
			int count = 0;
			public Number get() {
				return count++;
			}
		};

		Map<Number,Number> edgeCapacityMap = new HashMap<Number,Number>();
		for(int i=0; i<6; i++) {
			graph.addVertex(i);
		}
		
		Map<Number,Number> edgeFlowMap = new HashMap<Number,Number>();

		graph.addEdge(edgeFactory.get(),0,1,EdgeType.DIRECTED);
		edgeCapacityMap.put(0, 16);

		graph.addEdge(edgeFactory.get(),0,2,EdgeType.DIRECTED);
		edgeCapacityMap.put(1,13);

		graph.addEdge(edgeFactory.get(),1,2,EdgeType.DIRECTED);
		edgeCapacityMap.put(2, 6);

		graph.addEdge(edgeFactory.get(),1,3,EdgeType.DIRECTED);
		edgeCapacityMap.put(3, 12);

		graph.addEdge(edgeFactory.get(),2,4,EdgeType.DIRECTED);
		edgeCapacityMap.put(4, 14);

		graph.addEdge(edgeFactory.get(),3,2,EdgeType.DIRECTED);
		edgeCapacityMap.put(5, 9);

		graph.addEdge(edgeFactory.get(),3,5,EdgeType.DIRECTED);
		edgeCapacityMap.put(6, 20);

		graph.addEdge(edgeFactory.get(),4,3,EdgeType.DIRECTED);
		edgeCapacityMap.put(7, 7);

		graph.addEdge(edgeFactory.get(),4,5,EdgeType.DIRECTED);
		edgeCapacityMap.put(8, 4);

		EdmondsKarpMaxFlow<Number,Number> ek =
			new EdmondsKarpMaxFlow<Number,Number>(
				graph,
				0,
				5,
				Functions.<Number,Number>forMap(edgeCapacityMap, null),
				edgeFlowMap,
				edgeFactory);
		ek.evaluate();

		assertTrue(ek.getMaxFlow() == 23);
        Set<Number> nodesInS = ek.getNodesInSourcePartition();
        assertEquals(4,nodesInS.size());

        for (Number v : nodesInS) {
            Assert.assertTrue(v.intValue() != 3 && v.intValue() != 5);
        }

        Set<Number> nodesInT = ek.getNodesInSinkPartition();
        assertEquals(2,nodesInT.size());

        for (Number v : nodesInT) {
            Assert.assertTrue(v.intValue() == 3 || v.intValue() == 5);
        }

        Set<Number> minCutEdges = ek.getMinCutEdges();
        int maxFlow = 0;
        for (Number e : minCutEdges) {
            Number flow = edgeFlowMap.get(e);
            maxFlow += flow.intValue();
        }
        Assert.assertEquals(23,maxFlow);
        Assert.assertEquals(3,minCutEdges.size());
	}

	public void testAnotherSimpleFlow() {
		DirectedGraph<Number,Number> graph = new DirectedSparseMultigraph<Number,Number>();
		Supplier<Number> edgeFactory = new Supplier<Number>() {
			int count=0;
			public Number get() {
				return count++;
			}
		};

		Map<Number,Number> edgeCapacityMap = new HashMap<Number,Number>();
		for(int i=0; i<6; i++) {
			graph.addVertex(i);
		}
		
		Map<Number,Number> edgeFlowMap = new HashMap<Number,Number>();

		graph.addEdge(edgeFactory.get(),0,1,EdgeType.DIRECTED);
		edgeCapacityMap.put(0,5);
		
		graph.addEdge(edgeFactory.get(),0,2,EdgeType.DIRECTED);
		edgeCapacityMap.put(1,3);
		
		graph.addEdge(edgeFactory.get(),1,5,EdgeType.DIRECTED);
		edgeCapacityMap.put(2,2);
		
		graph.addEdge(edgeFactory.get(),1,2,EdgeType.DIRECTED);
		edgeCapacityMap.put(3,8);
		
		graph.addEdge(edgeFactory.get(),2,3,EdgeType.DIRECTED);
		edgeCapacityMap.put(4,4);
		
		graph.addEdge(edgeFactory.get(),2,4,EdgeType.DIRECTED);
		edgeCapacityMap.put(5,2);
		
		graph.addEdge(edgeFactory.get(),3,4,EdgeType.DIRECTED);
		edgeCapacityMap.put(6,3);
		
		graph.addEdge(edgeFactory.get(),3,5,EdgeType.DIRECTED);
		edgeCapacityMap.put(7,6);
		
		graph.addEdge(edgeFactory.get(),4,5,EdgeType.DIRECTED);
		edgeCapacityMap.put(8,1);

		EdmondsKarpMaxFlow<Number,Number> ek =
			new EdmondsKarpMaxFlow<Number,Number>(
				graph,
				0,
				5,
				Functions.<Number,Number>forMap(edgeCapacityMap, null),
				edgeFlowMap,
				edgeFactory);
		ek.evaluate();

		assertTrue(ek.getMaxFlow() == 7);
	}
}
