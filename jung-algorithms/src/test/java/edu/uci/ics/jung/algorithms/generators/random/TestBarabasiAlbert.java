/*
 * Copyright (c) 2016, the JUNG Project and the Regents of the University 
 * of California.  All rights reserved.
 *
 * This software is open-source under the BSD license; see
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random;

import java.util.HashSet;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author W. Giordano
 * @author Scott White
 * @author James Marchant
 */
public class TestBarabasiAlbert extends TestCase {
	protected Supplier<Graph<Integer, Number>> graphFactory;
	protected Supplier<Integer> vertexFactory;
	protected Supplier<Number> edgeFactory;

	protected int init_vertices = 1;
	protected int edges_to_add_per_timestep = 1;
	protected int random_seed = 0;
	protected int num_timesteps = 10;
	protected int num_tests = 10;

	public static Test suite() {
		return new TestSuite(TestBarabasiAlbert.class);
	}

	@Override
	protected void setUp() {
		graphFactory = new Supplier<Graph<Integer, Number>>() {
			public Graph<Integer, Number> get() {
				return new SparseMultigraph<Integer, Number>();
			}
		};
		vertexFactory = new Supplier<Integer>() {
			int count;

			public Integer get() {
				return count++;
			}
		};
		edgeFactory = new Supplier<Number>() {
			int count;

			public Number get() {
				return count++;
			}
		};
	}

	private Graph<Integer, Number> generateAndTestSizeOfBarabasiAlbertGraph(
			Supplier<Graph<Integer, Number>> graphFactory, Supplier<Integer> vertexFactory,
			Supplier<Number> edgeFactory, int init_vertices, int edges_to_add_per_timestep, int random_seed,
			int num_tests) {
		BarabasiAlbertGenerator<Integer, Number> generator = new BarabasiAlbertGenerator<Integer, Number>(graphFactory,
				vertexFactory, edgeFactory, init_vertices, edges_to_add_per_timestep, random_seed,
				new HashSet<Integer>());

		Graph<Integer, Number> graph = null;
		// test the graph size over {@code num_tests} intervals of {@code
		// num_timesteps} timesteps
		for (int i = 1; i <= num_tests; i++) {
			generator.evolveGraph(num_timesteps);
			graph = generator.get();
			assertEquals(graph.getVertexCount(), (i * num_timesteps) + init_vertices);
			assertEquals(graph.getEdgeCount(), edges_to_add_per_timestep * (i * num_timesteps));
		}

		return graph;
	}

	public void testMultigraphCreation() {
		generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices,
				edges_to_add_per_timestep, random_seed, num_tests);
	}

	public void testDirectedMultigraphCreation() {
		graphFactory = new Supplier<Graph<Integer, Number>>() {
			public Graph<Integer, Number> get() {
				return new DirectedSparseMultigraph<Integer, Number>();
			}
		};

		generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices,
				edges_to_add_per_timestep, random_seed, num_tests);
	}

	public void testUndirectedMultigraphCreation() {
		graphFactory = new Supplier<Graph<Integer, Number>>() {
			public Graph<Integer, Number> get() {
				return new UndirectedSparseMultigraph<Integer, Number>();
			}
		};

		generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices,
				edges_to_add_per_timestep, random_seed, num_tests);
	}

	public void testGraphCreation() {
		graphFactory = new Supplier<Graph<Integer, Number>>() {
			public Graph<Integer, Number> get() {
				return new SparseGraph<Integer, Number>();
			}
		};

		generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices,
				edges_to_add_per_timestep, random_seed, num_tests);
	}

	public void testDirectedGraphCreation() {
		graphFactory = new Supplier<Graph<Integer, Number>>() {
			public Graph<Integer, Number> get() {
				return new DirectedSparseGraph<Integer, Number>();
			}
		};

		generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices,
				edges_to_add_per_timestep, random_seed, num_tests);
	}

	public void testUndirectedGraphCreation() {
		graphFactory = new Supplier<Graph<Integer, Number>>() {
			public Graph<Integer, Number> get() {
				return new UndirectedSparseGraph<Integer, Number>();
			}
		};

		generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices,
				edges_to_add_per_timestep, random_seed, num_tests);
	}

	/**
	 * Due to the way the Barabasi-Albert algorithm works there should be no
	 * opportunities for the generation of self-loops within the graph.
	 */
	public void testNoSelfLoops() {
		graphFactory = new Supplier<Graph<Integer, Number>>() {
			public Graph<Integer, Number> get() {
				return new UndirectedSparseGraph<Integer, Number>() {
					private static final long serialVersionUID = 1L;

					/**
					 * This anonymous class works as an UndirectedSparseGraph
					 * but will not accept edges that connect a vertex to
					 * itself.
					 */
					@Override
					public boolean addEdge(Number edge, Pair<? extends Integer> endpoints, EdgeType edgeType) {
						if (endpoints == null)
							throw new IllegalArgumentException("endpoints may not be null");

						Integer v1 = endpoints.getFirst();
						Integer v2 = endpoints.getSecond();

						if (v1.equals(v2))
							throw new IllegalArgumentException("No self-loops");
						else
							return super.addEdge(edge, endpoints, edgeType);
					}
				};
			}
		};

		generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices,
				edges_to_add_per_timestep, random_seed, num_tests);
	}

	public void testPreconditions() {
		// test init_vertices = 0
		try {
			generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, 0,
					edges_to_add_per_timestep, random_seed, num_tests);
			fail();
		} catch (IllegalArgumentException e) {
		}

		// test negative init_vertices
		try {
			generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, -1,
					edges_to_add_per_timestep, random_seed, num_tests);
			fail();
		} catch (IllegalArgumentException e) {
		}

		// test edges_to_add_per_timestep = 0
		try {
			generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices, 0,
					random_seed, num_tests);
			fail();
		} catch (IllegalArgumentException e) {
		}

		// test negative edges_to_add_per_timestep
		try {
			generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, init_vertices, -1,
					random_seed, num_tests);
			fail();
		} catch (IllegalArgumentException e) {
		}

		// test edges_to_add_per_timestep > init_vertices
		try {
			generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory, edgeFactory, 2, 3, random_seed,
					num_tests);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Every node should have an out-degree AT LEAST equal to the number of
	 * edges added per timestep (dependent on if it is directed or undirected).
	 */
	public void testEveryNodeHasCorrectMinimumNumberOfEdges() {
		Graph<Integer, Number> graph = generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory,
				edgeFactory, init_vertices, edges_to_add_per_timestep, random_seed, num_tests);

		for (Integer v : graph.getVertices()) {
			assertTrue(graph.outDegree(v) >= edges_to_add_per_timestep);
		}
	}

	/**
	 * Check that not every edge goes to one node; the in-degree of any node
	 * should be strictly less than the number of edges.
	 */
	public void testNotEveryEdgeToOneNode() {
		Graph<Integer, Number> graph = generateAndTestSizeOfBarabasiAlbertGraph(graphFactory, vertexFactory,
				edgeFactory, init_vertices, edges_to_add_per_timestep, random_seed, num_tests);

		for (Integer v : graph.getVertices()) {
			assertTrue(graph.inDegree(v) < graph.getEdgeCount());
		}
	}
}
