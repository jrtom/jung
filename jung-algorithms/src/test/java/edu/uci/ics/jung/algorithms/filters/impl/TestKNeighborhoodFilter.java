package edu.uci.ics.jung.algorithms.filters.impl;

/**
 * @author Tom Nelson
 */

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter.EdgeType;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;


public class TestKNeighborhoodFilter extends TestCase {
	
	DirectedGraph<Number,Number> graph;
	
	public static Test suite() {
		return new TestSuite(TestKNeighborhoodFilter.class);
	}

	@Override
	protected void setUp() {
		graph = new DirectedSparseMultigraph<Number,Number>();
		for(int i=0; i<7; i++) {
			graph.addVertex(i);
		}
		int j=0;
		graph.addEdge(j++, 0, 1);
		graph.addEdge(j++, 0, 2);
		graph.addEdge(j++, 2, 3);
		graph.addEdge(j++, 2, 4);
		graph.addEdge(j++, 3, 5);
		graph.addEdge(j++, 5, 6);
		graph.addEdge(j++, 5, 0);
		graph.addEdge(j++, 3, 0);
		graph.addEdge(j++, 6, 7);
	}

	public void testIn() {
		Filter<Number,Number> filter = new KNeighborhoodFilter<Number,Number>(0, 2, EdgeType.IN);
		Graph<Number,Number> result = filter.apply(graph);
		assertEquals(result.getVertexCount(), 4);
		assertEquals(result.getEdgeCount(), 5);
	}
	public void testOut() {
		Filter<Number,Number> filter = new KNeighborhoodFilter<Number,Number>(0, 2, EdgeType.OUT);
		Graph<Number,Number> result = filter.apply(graph);
		assertEquals(result.getVertexCount(), 5);
		assertEquals(result.getEdgeCount(), 5);
	}
	public void testInOut() {
		Filter<Number,Number> filter = new KNeighborhoodFilter<Number,Number>(0, 2, EdgeType.IN_OUT);
		Graph<Number,Number> result = filter.apply(graph);
		assertEquals(result.getVertexCount(), 7);
		assertEquals(result.getEdgeCount(), 8);
	}
}
