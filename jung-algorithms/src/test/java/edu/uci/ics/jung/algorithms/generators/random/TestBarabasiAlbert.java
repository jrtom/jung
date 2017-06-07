package edu.uci.ics.jung.algorithms.generators.random;

import com.google.common.base.Supplier;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class TestBarabasiAlbert extends TestCase {
	public static Test suite() {
		return new TestSuite(TestBarabasiAlbert.class);
	}

	@Override
  protected void setUp() {
	}

	// TODO(jrtom): add tests for
	// * parallel edges
	// * undirected edges
	// * ...
	public void test() 
    {
        int init_vertices = 1;
        int edges_to_add_per_timestep = 1;
        int random_seed = 0;
        int num_tests = 10;
        int num_timesteps = 10;
        
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
            new BarabasiAlbertGenerator<Integer,Number>(NetworkBuilder.directed(), vertexFactory, edgeFactory,
            		init_vertices,edges_to_add_per_timestep,random_seed);
	    for (int i = 1; i <= num_tests; i++) {
	        
	        generator.evolveGraph(num_timesteps);
	        Network<Integer, Number> graph = generator.get();
	        assertEquals(graph.nodes().size(), (i*num_timesteps) + init_vertices);
	        assertEquals(graph.edges().size(), edges_to_add_per_timestep * (i*num_timesteps));
	    }
	}
}
