package edu.uci.ics.jung.graph;

import junit.framework.TestCase;


public class SparseGraphAddRemoveAddEdgeTest extends TestCase {
	
    SparseGraph<String,Integer> g;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        g = new SparseGraph<String,Integer>();
    }
    
    public void testAddRemoveAddEdge() {
        g.addVertex("A"); g.addVertex("B");
        g.addEdge(1, "A", "B");
        g.removeEdge(1);  // Remove the edge between A and B
        g.addEdge(2, "A", "B"); // Then add a different edge -> Exception thrown

    }

}
