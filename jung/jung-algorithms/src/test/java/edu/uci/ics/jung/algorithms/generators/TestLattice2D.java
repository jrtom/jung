package edu.uci.ics.jung.algorithms.generators;


import junit.framework.Assert;
import junit.framework.TestCase;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;


public class TestLattice2D extends TestCase {
	
	protected Supplier<UndirectedGraph<String,Number>> undirectedGraphFactory;
    protected Supplier<DirectedGraph<String,Number>> directedGraphFactory;
	protected Supplier<String> vertexFactory;
	protected Supplier<Number> edgeFactory;

	@Override
	protected void setUp() {
		undirectedGraphFactory = new Supplier<UndirectedGraph<String,Number>>() {
			public UndirectedGraph<String,Number> get() {
				return new UndirectedSparseMultigraph<String,Number>();
			}
		};
		directedGraphFactory = new Supplier<DirectedGraph<String,Number>>() {
            public DirectedGraph<String,Number> get() {
                return new DirectedSparseMultigraph<String,Number>();
            }
        };

		vertexFactory = new Supplier<String>() {
			int count;
			public String get() {
				return Character.toString((char)('A'+count++));
			}
		};
		edgeFactory = 
			new Supplier<Number>() {
			int count;
			public Number get() {
				return count++;
			}
		};
	}

	public void testCreateSingular() 
	{
	    try
	    {
	        generate(1, 0, 0);
	        fail("Did not reject lattice of size < 2");
	    }
	    catch (IllegalArgumentException iae) {}
	}
	
	public void testget() {
		for (int i = 3; i <= 10; i++) {
		    for (int j = 0; j < 2; j++) {
		        for (int k = 0; k < 2; k++) {
        			Lattice2DGenerator<String,Number> generator = generate(i, j, k);
    			    Graph<String,Number> graph = generator.get();
                    Assert.assertEquals(i*i, graph.getVertexCount());
                    checkEdgeCount(generator, graph);
		        }
		    }
		}
	}
	
	protected Lattice2DGenerator<String, Number> generate(int i, int j, int k)
	{
	    return new Lattice2DGenerator<String,Number>(
                k == 0 ? undirectedGraphFactory : directedGraphFactory, 
                vertexFactory, edgeFactory,
                i, j == 0 ? true : false); // toroidal?
	}
	
	protected void checkEdgeCount(Lattice2DGenerator<String, Number> generator,
		Graph<String, Number> graph) 
	{
        Assert.assertEquals(generator.getGridEdgeCount(), graph.getEdgeCount());
	}
}
