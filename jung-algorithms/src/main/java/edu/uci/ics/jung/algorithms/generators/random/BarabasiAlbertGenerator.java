/*
 * Copyright (c) 2003, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.generators.EvolvingGraphGenerator;
import edu.uci.ics.jung.algorithms.util.WeightedChoice;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.MultiGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * <p>
 * Simple evolving scale-free random graph generator. At each time step, a new
 * vertex is created and is connected to existing vertices according to the
 * principle of "preferential attachment", whereby vertices with higher degree
 * have a higher probability of being selected for attachment.
 * 
 * <p>
 * At a given timestep, the probability <code>p</code> of creating an edge
 * between an existing vertex <code>v</code> and the newly added vertex is
 * 
 * <pre>
 * p = (degree(v) + 1) / (|E| + |V|);
 * </pre>
 * 
 * <p>
 * where <code>|E|</code> and <code>|V|</code> are, respectively, the number of
 * edges and vertices currently in the network (counting neither the new vertex
 * nor the other edges that are being attached to it).
 * 
 * <p>
 * Note that the formula specified in the original paper (cited below) was
 * 
 * <pre>
 * p = degree(v) / |E|
 * </pre>
 * 
 * 
 * <p>
 * However, this would have meant that the probability of attachment for any
 * existing isolated vertex would be 0. This version uses Lagrangian smoothing
 * to give each existing vertex a positive attachment probability.
 * 
 * <p>
 * The graph created may be either directed or undirected (controlled by a
 * constructor parameter); the default is undirected. If the graph is specified
 * to be directed, then the edges added will be directed from the newly added
 * vertex u to the existing vertex v, with probability proportional to the
 * indegree of v (number of edges directed towards v). If the graph is specified
 * to be undirected, then the (undirected) edges added will connect u to v, with
 * probability proportional to the degree of v.
 * 
 * <p>
 * The <code>parallel</code> constructor parameter specifies whether parallel
 * edges may be created.
 * 
 * @see "A.-L. Barabasi and R. Albert, Emergence of scaling in random networks, Science 286, 1999."
 * @author Scott White
 * @author Joshua O'Madadhain
 * @author Tom Nelson - adapted to jung2
 * @author James Marchant
 */
public class BarabasiAlbertGenerator<V, E> implements EvolvingGraphGenerator<V, E> {
	private Graph<V, E> mGraph = null;
	private int mNumEdgesToAttachPerStep;
	private int mElapsedTimeSteps;
	private Random mRandom;
	protected List<V> vertex_index;
	protected int init_vertices;
	protected Map<V, Integer> index_vertex;
	protected Supplier<Graph<V, E>> graphFactory;
	protected Supplier<V> vertexFactory;
	protected Supplier<E> edgeFactory;

	/**
	 * Constructs a new instance of the generator.
	 * 
	 * @param graphFactory
	 *            factory for graphs of the appropriate type
	 * @param vertexFactory
	 *            factory for vertices of the appropriate type
	 * @param edgeFactory
	 *            factory for edges of the appropriate type
	 * @param init_vertices
	 *            number of unconnected 'seed' vertices that the graph should
	 *            start with
	 * @param numEdgesToAttach
	 *            the number of edges that should be attached from the new
	 *            vertex to pre-existing vertices at each time step
	 * @param seed
	 *            random number seed
	 * @param seedVertices
	 *            storage for the seed vertices that this graph creates
	 */
	// TODO: seedVertices is a bizarre way of exposing that information,
	// refactor
	public BarabasiAlbertGenerator(Supplier<Graph<V, E>> graphFactory, Supplier<V> vertexFactory,
			Supplier<E> edgeFactory, int init_vertices, int numEdgesToAttach, int seed, Set<V> seedVertices) {
		Preconditions.checkArgument(init_vertices > 0,
				"Number of initial unconnected 'seed' vertices must be positive");
		Preconditions.checkArgument(numEdgesToAttach > 0,
				"Number of edges to attach at each time step must be positive");
		Preconditions.checkArgument(numEdgesToAttach <= init_vertices,
				"Number of edges to attach at each time step must less than or equal to the number of initial vertices");

		mNumEdgesToAttachPerStep = numEdgesToAttach;
		mRandom = new Random(seed);
		this.graphFactory = graphFactory;
		this.vertexFactory = vertexFactory;
		this.edgeFactory = edgeFactory;
		this.init_vertices = init_vertices;
		initialize(seedVertices);
	}

	/**
	 * Constructs a new instance of the generator, whose output will be an
	 * undirected graph, and which will use the current time as a seed for the
	 * random number generation.
	 * 
	 * @param graphFactory
	 *            factory for graphs of the appropriate type
	 * @param vertexFactory
	 *            factory for vertices of the appropriate type
	 * @param edgeFactory
	 *            factory for edges of the appropriate type
	 * @param init_vertices
	 *            number of vertices that the graph should start with
	 * @param numEdgesToAttach
	 *            the number of edges that should be attached from the new
	 *            vertex to pre-existing vertices at each time step
	 * @param seedVertices
	 *            storage for the seed vertices that this graph creates
	 */
	public BarabasiAlbertGenerator(Supplier<Graph<V, E>> graphFactory, Supplier<V> vertexFactory,
			Supplier<E> edgeFactory, int init_vertices, int numEdgesToAttach, Set<V> seedVertices) {
		this(graphFactory, vertexFactory, edgeFactory, init_vertices, numEdgesToAttach,
				(int) System.currentTimeMillis(), seedVertices);
	}

	private void initialize(Set<V> seedVertices) {
		mGraph = graphFactory.get();

		vertex_index = new ArrayList<V>(2 * init_vertices);
		index_vertex = new HashMap<V, Integer>(2 * init_vertices);
		for (int i = 0; i < init_vertices; i++) {
			V v = vertexFactory.get();
			mGraph.addVertex(v);
			vertex_index.add(v);
			index_vertex.put(v, i);
			seedVertices.add(v);
		}

		mElapsedTimeSteps = 0;
	}

	public void evolveGraph(int numTimeSteps) {

		for (int i = 0; i < numTimeSteps; i++) {
			evolveGraph();
			mElapsedTimeSteps++;
		}
	}

	private void evolveGraph() {
		Collection<V> preexistingNodes = mGraph.getVertices();
		V newVertex = vertexFactory.get();

		mGraph.addVertex(newVertex);

		// generate and store the new edges; don't add them to the graph
		// yet because we don't want to bias the degree calculations
		// (all new edges in a timestep should be added in parallel)
		Set<Pair<V>> added_pairs = createRandomEdges(preexistingNodes, newVertex, mNumEdgesToAttachPerStep);

		for (Pair<V> pair : added_pairs) {
			V v1 = pair.getFirst();
			V v2 = pair.getSecond();
			if (mGraph.getDefaultEdgeType() != EdgeType.UNDIRECTED || !mGraph.isNeighbor(v1, v2))
				mGraph.addEdge(edgeFactory.get(), pair);
		}
		// now that we're done attaching edges to this new vertex,
		// add it to the index
		vertex_index.add(newVertex);
		index_vertex.put(newVertex, new Integer(vertex_index.size() - 1));
	}

	private Set<Pair<V>> createRandomEdges(Collection<V> preexistingNodes, V newVertex, int numEdges) {
		Set<Pair<V>> added_pairs = new HashSet<Pair<V>>(numEdges * 3);

		/* Generate the probability distribution */
		Map<V, Double> item_weights = new HashMap<V, Double>();
		for (V v : preexistingNodes) {
			/*
			 * as preexistingNodes is a view onto the vertex set, it will
			 * contain the new node. In the construction of Barabasi-Albert,
			 * there should be no self-loops.
			 */
			if (v == newVertex)
				continue;

			double degree;
			double denominator;

			/*
			 * Attachment probability is dependent on whether the graph is
			 * directed or undirected.
			 * 
			 * Subtract 1 from numVertices because we don't want to count
			 * newVertex (which has already been added to the graph, but not to
			 * vertex_index).
			 */
			if (mGraph.getDefaultEdgeType() == EdgeType.UNDIRECTED) {
				degree = mGraph.degree(v);
				denominator = (2 * mGraph.getEdgeCount()) + mGraph.getVertexCount() - 1;
			} else {
				degree = mGraph.inDegree(v);
				denominator = mGraph.getEdgeCount() + mGraph.getVertexCount() - 1;
			}

			double prob = (degree + 1) / denominator;
			item_weights.put(v, prob);
		}
		WeightedChoice<V> nodeProbabilities = new WeightedChoice<V>(item_weights, mRandom);

		for (int i = 0; i < numEdges; i++) {
			createRandomEdge(preexistingNodes, newVertex, added_pairs, nodeProbabilities);
		}

		return added_pairs;
	}

	private void createRandomEdge(Collection<V> preexistingNodes, V newVertex, Set<Pair<V>> added_pairs,
			WeightedChoice<V> weightedProbabilities) {
		V attach_point;
		boolean created_edge = false;
		Pair<V> endpoints;

		do {
			attach_point = weightedProbabilities.nextItem();

			endpoints = new Pair<V>(newVertex, attach_point);

			/*
			 * If parallel edges are not allowed, skip attach_point if
			 * <newVertex, attach_point> already exists; note that because of
			 * the way the new node's edges are added, we only need to check the
			 * list of candidate edges for duplicates.
			 */
			if (!(mGraph instanceof MultiGraph)) {
				if (added_pairs.contains(endpoints))
					continue;
				if (mGraph.getDefaultEdgeType() == EdgeType.UNDIRECTED
						&& added_pairs.contains(new Pair<V>(attach_point, newVertex)))
					continue;
			}
			created_edge = true;
		} while (!created_edge);

		added_pairs.add(endpoints);

		if (mGraph.getDefaultEdgeType() == EdgeType.UNDIRECTED) {
			added_pairs.add(new Pair<V>(attach_point, newVertex));
		}
	}

	public int numIterations() {
		return mElapsedTimeSteps;
	}

	public Graph<V, E> get() {
		return mGraph;
	}
}
