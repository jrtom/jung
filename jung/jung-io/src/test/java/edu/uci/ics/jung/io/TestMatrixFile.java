/*
 * Created on Jan 6, 2002
 *
 */
package edu.uci.ics.jung.io;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

/**
 * @author Scott
 * @author Tom Nelson - converted to jung2
 *
 */
public class TestMatrixFile extends TestCase {

	Factory<UndirectedGraph<Number, Number>> undirectedGraphFactory;
	Factory<DirectedGraph<Number, Number>> directedGraphFactory;
	Factory<Number> vertexFactory;
	Factory<Number> edgeFactory;
	Map<Number,Number> weights;

    @Override
    protected void setUp() {
    	undirectedGraphFactory = new Factory<UndirectedGraph<Number,Number>>() {
    		public UndirectedGraph<Number,Number> create() {
    			return new UndirectedSparseMultigraph<Number,Number>();
    		}
    	};
    	directedGraphFactory = new Factory<DirectedGraph<Number,Number>>() {
    		public DirectedGraph<Number,Number> create() {
    			return new DirectedSparseMultigraph<Number,Number>();
    		}
    	};
    	vertexFactory = new Factory<Number>() {
    		int n = 0;
    		public Number create() { return n++; }
    	};
    	edgeFactory = new Factory<Number>() {
    		int n = 0;
    		public Number create() { return n++; }
    	};

    }


	public static Test suite() {
		return new TestSuite(TestMatrixFile.class);
	}
	public DirectedGraph<Number,Number> createSimpleDirectedGraph() {
		DirectedGraph<Number,Number> graph1 = directedGraphFactory.create();
		for(int i=0; i<5; i++) {
			graph1.addVertex(i);
		}
		weights = new HashMap<Number,Number>();

		Number e = edgeFactory.create();
		graph1.addEdge(e, 0, 1);
		weights.put(e, 5.0);
		e = edgeFactory.create();
		graph1.addEdge(e, 0, 2);
		weights.put(e, 10.0);
		e = edgeFactory.create();
		graph1.addEdge(e, 1, 2);
		weights.put(e, 3.0);
		e = edgeFactory.create();
		graph1.addEdge(e, 1, 3);
		weights.put(e, 700.0);
		e = edgeFactory.create();
		graph1.addEdge(e, 1, 4);
		weights.put(e, 0.5);
		e = edgeFactory.create();
		graph1.addEdge(e, 4, 3);
		weights.put(e, 5.0);

		return graph1;
	}

	public void testUnweightedLoadSave() {
		MatrixFile<Number,Number> mf = new MatrixFile<Number,Number>(null,
		    directedGraphFactory, vertexFactory, edgeFactory);
		DirectedGraph<Number,Number> dg = createSimpleDirectedGraph();
		String filename = "testMatrixLoadSaveUW.mat";
		mf.save(dg, filename);
		Graph<Number, Number> g = mf.load(filename);
		Assert.assertEquals(dg.getVertexCount(), g.getVertexCount());
		Assert.assertEquals(dg.getEdgeCount(), g.getEdgeCount());
		File file = new File(filename);
		file.delete();
	}

	public void testWeightedLoadSave() {
		MatrixFile<Number,Number> mf = new MatrixFile<Number,Number>(weights,
		    directedGraphFactory, vertexFactory, edgeFactory);
		DirectedGraph<Number,Number> dg = createSimpleDirectedGraph();
		String filename = "testMatrixLoadSaveW.mat";
		mf.save(dg, filename);
		Graph<Number,Number> g = mf.load(filename);
		Assert.assertEquals(dg.getVertexCount(), g.getVertexCount());
		Assert.assertEquals(dg.getEdgeCount(), g.getEdgeCount());
		File file = new File(filename);
		file.delete();
	}
}
