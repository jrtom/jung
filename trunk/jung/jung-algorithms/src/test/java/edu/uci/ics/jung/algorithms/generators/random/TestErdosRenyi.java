package edu.uci.ics.jung.algorithms.generators.random;

/**
 * @author W. Giordano, Scott White
 */

import org.apache.commons.collections15.Factory;

import edu.uci.ics.jung.algorithms.generators.random.ErdosRenyiGenerator;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

import junit.framework.*;


public class TestErdosRenyi extends TestCase {
	
	Factory<UndirectedGraph<String,Number>> graphFactory;
	Factory<String> vertexFactory;
	Factory<Number> edgeFactory;

	public static Test suite() {
		return new TestSuite(TestErdosRenyi.class);
	}

	@Override
  protected void setUp() {
		graphFactory = new Factory<UndirectedGraph<String,Number>>() {
			public UndirectedGraph<String,Number> create() {
				return new UndirectedSparseMultigraph<String,Number>();
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

	public void test() {

        int numVertices = 100;
        int total = 0;
		for (int i = 1; i <= 10; i++) {
			ErdosRenyiGenerator<String,Number> generator = 
				new ErdosRenyiGenerator<String,Number>(graphFactory, vertexFactory, edgeFactory,
					numVertices,0.1);
            generator.setSeed(0);

			Graph<String,Number> graph = generator.create();
			Assert.assertTrue(graph.getVertexCount() == numVertices);
            total += graph.getEdgeCount();
		}
        total /= 10.0;
        Assert.assertTrue(total > 495-50 && total < 495+50);

	}
	  
  
}
