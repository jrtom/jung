/*
 * Created on Aug 22, 2003
 *
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.base.Supplier;
import com.google.common.collect.BiMap;

import edu.uci.ics.jung.algorithms.util.Indexer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

/**
 * @author Scott White
 */
public class TestUnweightedShortestPath extends TestCase
{
    private Supplier<String> vertexFactory =
    	new Supplier<String>() {
    	int count = 0;
    	public String get() {
    		return "V"+count++;
    	}};
    	
     private Supplier<Integer> edgeFactory =
        	new Supplier<Integer>() {
        	int count = 0;
        	public Integer get() {
        		return count++;
        	}};
    BiMap<String,Integer> id;

    @Override
    protected void setUp() {
    }
	public static Test suite()
	{
		return new TestSuite(TestUnweightedShortestPath.class);
	}
	
	public void testUndirected() {
		UndirectedGraph<String,Integer> ug = 
			new UndirectedSparseMultigraph<String,Integer>();
		for(int i=0; i<5; i++) {
			ug.addVertex(vertexFactory.get());
		}
		id = Indexer.<String>create(ug.getVertices());

//		GraphUtils.addVertices(ug,5);
//		Indexer id = Indexer.getIndexer(ug);
		ug.addEdge(edgeFactory.get(), id.inverse().get(0), id.inverse().get(1));
		ug.addEdge(edgeFactory.get(), id.inverse().get(1), id.inverse().get(2));
		ug.addEdge(edgeFactory.get(), id.inverse().get(2), id.inverse().get(3));
		ug.addEdge(edgeFactory.get(), id.inverse().get(0), id.inverse().get(4));
		ug.addEdge(edgeFactory.get(), id.inverse().get(4), id.inverse().get(3));
		
		UnweightedShortestPath<String,Integer> usp = 
			new UnweightedShortestPath<String,Integer>(ug);
		Assert.assertEquals(usp.getDistance(id.inverse().get(0),id.inverse().get(3)).intValue(),2);
		Assert.assertEquals((usp.getDistanceMap(id.inverse().get(0)).get(id.inverse().get(3))).intValue(),2);
		Assert.assertNull(usp.getIncomingEdgeMap(id.inverse().get(0)).get(id.inverse().get(0)));
		Assert.assertNotNull(usp.getIncomingEdgeMap(id.inverse().get(0)).get(id.inverse().get(3)));
	}
	
	public void testDirected() {
			DirectedGraph<String,Integer> dg = 
				new DirectedSparseMultigraph<String,Integer>();
			for(int i=0; i<5; i++) {
				dg.addVertex(vertexFactory.get());
			}
			id = Indexer.<String>create(dg.getVertices());
			dg.addEdge(edgeFactory.get(), id.inverse().get(0), id.inverse().get(1));
			dg.addEdge(edgeFactory.get(), id.inverse().get(1), id.inverse().get(2));
			dg.addEdge(edgeFactory.get(), id.inverse().get(2), id.inverse().get(3));
			dg.addEdge(edgeFactory.get(), id.inverse().get(0), id.inverse().get(4));
			dg.addEdge(edgeFactory.get(), id.inverse().get(4), id.inverse().get(3));
			dg.addEdge(edgeFactory.get(), id.inverse().get(3), id.inverse().get(0));
		
			UnweightedShortestPath<String,Integer> usp = 
				new UnweightedShortestPath<String,Integer>(dg);
			Assert.assertEquals(usp.getDistance(id.inverse().get(0),id.inverse().get(3)).intValue(),2);
			Assert.assertEquals((usp.getDistanceMap(id.inverse().get(0)).get(id.inverse().get(3))).intValue(),2);
			Assert.assertNull(usp.getIncomingEdgeMap(id.inverse().get(0)).get(id.inverse().get(0)));
			Assert.assertNotNull(usp.getIncomingEdgeMap(id.inverse().get(0)).get(id.inverse().get(3)));

		}
}
