package edu.uci.ics.jung.algorithms.generators.random;

/**
 * @author W. Giordano, Scott White
 */

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;


public class TestBarabasiAlbert extends TestCase {
	public static Test suite() {
		return new TestSuite(TestBarabasiAlbert.class);
	}

	@Override
  protected void setUp() {
	}

	public void test() 
    {
        int init_vertices = 1;
        int edges_to_add_per_timestep = 1;
        int random_seed = 0;
        int num_tests = 10;
        int num_timesteps = 10;
        
        Supplier<Graph<Integer,Number>> graphFactory =
        	new Supplier<Graph<Integer,Number>>() {
        	public Graph<Integer,Number> get() {
        		return new SparseMultigraph<Integer,Number>();
        	}
        };
    	Supplier<Integer> vertexFactory = 
    		new Supplier<Integer>() {
    			int count;
				public Integer get() {
					return count++;
				}};
		Supplier<Number> edgeFactory = 
		    new Supplier<Number>() {
			    int count;
				public Number get() {
					return count++;
				}};

	    BarabasiAlbertGenerator<Integer,Number> generator = 
            new BarabasiAlbertGenerator<Integer,Number>(graphFactory, vertexFactory, edgeFactory,
            		init_vertices,edges_to_add_per_timestep,random_seed, new HashSet<Integer>());
	    for (int i = 1; i <= num_tests; i++) {
	        
	        generator.evolveGraph(num_timesteps);
	        Graph<Integer, Number> graph = generator.get();
	        assertEquals(graph.getVertexCount(), (i*num_timesteps) + init_vertices);
	        assertEquals(graph.getEdgeCount(), edges_to_add_per_timestep * (i*num_timesteps));
	    }
	}
}
