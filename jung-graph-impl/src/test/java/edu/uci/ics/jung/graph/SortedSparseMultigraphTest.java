package edu.uci.ics.jung.graph;

import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.jung.graph.util.EdgeType;

public class SortedSparseMultigraphTest 
	extends AbstractSortedSparseMultigraphTest {


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Set<Number> seeds = new HashSet<Number>();
        seeds.add(1);
        seeds.add(5);
        graph = new SortedSparseMultigraph<Integer,Double>();
        graph.addEdge(4., 2, 1);
        graph.addEdge(5., 3, 1);
        graph.addEdge(6., 0, 4, EdgeType.DIRECTED);
        graph.addEdge(7., 0, 5, EdgeType.DIRECTED);
        graph.addEdge(1., 0, 1);
        graph.addEdge(2., 1, 2);
        graph.addEdge(3., 0, 2);
        graph.addEdge(8., 5, 1, EdgeType.DIRECTED);
        graph.addEdge(9., 6, 1, EdgeType.DIRECTED);
        graph.addEdge(10., 4, 3, EdgeType.DIRECTED);
        graph.addEdge(16., 8, 3);
        graph.addEdge(17., 5, 7);
        graph.addEdge(11., 2, 7);
        graph.addEdge(12., 1, 5);
        graph.addEdge(13., 2, 6);
        graph.addEdge(14., 6, 4);
        graph.addEdge(15., 7, 8);

        smallGraph = new SparseMultigraph<Integer,Double>();
        smallGraph.addVertex(v0);
        smallGraph.addVertex(v1);
        smallGraph.addVertex(v2);
        smallGraph.addEdge(e01, v0, v1);
        smallGraph.addEdge(e10, v1, v0);
        smallGraph.addEdge(e12, v1, v2);
        smallGraph.addEdge(e21, v2, v1, EdgeType.DIRECTED);
        
        Graph<Foo,Bar> fooBar = new SortedSparseMultigraph<Foo,Bar>();
        try {
        	fooBar.addVertex(new Foo());
        	fooBar.addVertex(new Foo());
        	fooBar.addEdge(new Bar(), new Foo(), new Foo());
        	fail("should have thrown an exception as Foo Bar are not Comparable");
        } catch(Exception ex) {
        	// all is well
        }

    }
}
