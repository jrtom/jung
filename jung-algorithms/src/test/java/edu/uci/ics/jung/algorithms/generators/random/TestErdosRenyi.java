package edu.uci.ics.jung.algorithms.generators.random;

/**
 * @author W. Giordano, Scott White
 */

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;


public class TestErdosRenyi extends TestCase {
	
	Supplier<UndirectedGraph<String,Number>> graphFactory;
	Supplier<String> vertexFactory;
	Supplier<Number> edgeFactory;

	public static Test suite() {
		return new TestSuite(TestErdosRenyi.class);
	}

	@Override
  protected void setUp() {
		graphFactory = new Supplier<UndirectedGraph<String,Number>>() {
			public UndirectedGraph<String,Number> get() {
				return new UndirectedSparseMultigraph<String,Number>();
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

	public void test() {

        int numVertices = 100;
        int total = 0;
		for (int i = 1; i <= 10; i++) {
			ErdosRenyiGenerator<String,Number> generator = 
				new ErdosRenyiGenerator<String,Number>(graphFactory, vertexFactory, edgeFactory,
					numVertices,0.1);
            generator.setSeed(0);

			Graph<String,Number> graph = generator.get();
			Assert.assertTrue(graph.getVertexCount() == numVertices);
            total += graph.getEdgeCount();
		}
        total /= 10.0;
        Assert.assertTrue(total > 495-50 && total < 495+50);

	}
	  
  
}
