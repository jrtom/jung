package edu.uci.ics.jung.graph;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class DirectedSparseMultigraphTest 
	extends AbstractDirectedSparseMultigraphTest {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        graph = new DirectedSparseMultigraph<Integer,Number>();
        graph.addEdge(e01, v0, v1);
        graph.addEdge(e10, v1, v0);
        graph.addEdge(e12, v1, v2);
        graph.addEdge(e21, v2, v1);

    }

}
