package edu.uci.ics.jung.algorithms.generators;


import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;


public class TestLattice2D extends TestCase {
	
	protected Factory<UndirectedGraph<String,Number>> undirectedGraphFactory;
    protected Factory<DirectedGraph<String,Number>> directedGraphFactory;
	protected Factory<String> vertexFactory;
	protected Factory<Number> edgeFactory;

	@Override
	protected void setUp() {
		undirectedGraphFactory = new Factory<UndirectedGraph<String,Number>>() {
			public UndirectedGraph<String,Number> create() {
				return new UndirectedSparseMultigraph<String,Number>();
			}
		};
		directedGraphFactory = new Factory<DirectedGraph<String,Number>>() {
            public DirectedGraph<String,Number> create() {
                return new DirectedSparseMultigraph<String,Number>();
            }
        };

		vertexFactory = new Factory<String>() {
			int count;
			public String create() {
				return Character.toString((char)('A'+count++));
			}
		};
		edgeFactory = 
			new Factory<Number>() {
			int count;
			public Number create() {
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
	
	public void testCreate() {
		for (int i = 3; i <= 10; i++) {
		    for (int j = 0; j < 2; j++) {
		        for (int k = 0; k < 2; k++) {
        			Lattice2DGenerator<String,Number> generator = generate(i, j, k);
    			    Graph<String,Number> graph = generator.create();
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
	
	protected void checkEdgeCount(Lattice2DGenerator generator, Graph<String, Number> graph) 
	{
        Assert.assertEquals(generator.getGridEdgeCount(), graph.getEdgeCount());
	}
}
