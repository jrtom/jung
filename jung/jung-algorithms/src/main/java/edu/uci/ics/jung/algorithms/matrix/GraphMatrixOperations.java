/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved. This software is open-source under the BSD
 * license; see either "license.txt" or http://jung.sourceforge.net/license.txt
 * for a description.
 */
package edu.uci.ics.jung.algorithms.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Factory;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.uci.ics.jung.algorithms.util.ConstantMap;
import edu.uci.ics.jung.algorithms.util.Indexer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;


/**
 * Contains methods for performing the analogues of certain matrix operations on
 * graphs.
 * <p>
 * These implementations are efficient on sparse graphs, but may not be the best
 * implementations for very dense graphs.
 * 
 * @author Joshua O'Madadhain
 * @see MatrixElementOperations
 */
public class GraphMatrixOperations
{
    /**
     * Returns the graph that corresponds to the square of the (weighted)
     * adjacency matrix that the specified graph <code>g</code> encodes. The
     * implementation of MatrixElementOperations that is furnished to the
     * constructor specifies the implementation of the dot product, which is an
     * integral part of matrix multiplication.
     * 
     * @param g
     *            the graph to be squared
     * @return the result of squaring g
     */
    @SuppressWarnings("unchecked")
	public static <V,E> Graph<V,E> square(Graph<V,E> g, 
    		Factory<E> edgeFactory, MatrixElementOperations<E> meo)
    {
        // create new graph of same type
        Graph<V, E> squaredGraph = null;
        try {
			squaredGraph = g.getClass().newInstance();
		} catch (InstantiationException e3) {
			e3.printStackTrace();
		} catch (IllegalAccessException e3) {
			e3.printStackTrace();
		}

        Collection<V> vertices = g.getVertices();
        for (V v : vertices)
        {
        	squaredGraph.addVertex(v);
        }
        for (V v : vertices) 
        {
            for (V src : g.getPredecessors(v))
            {
                // get the edge connecting src to v in G
                E e1 = g.findEdge(src,v);
                for (V dest : g.getSuccessors(v))
                {
                    // get edge connecting v to dest in G
                    E e2 = g.findEdge(v,dest);
                    // collect data on path composed of e1 and e2
                    Number pathData = meo.computePathData(e1, e2);
                    E e = squaredGraph.findEdge(src,dest);
                    // if no edge from src to dest exists in G2, create one
                    if (e == null) {
                    	e = edgeFactory.create();
                    	squaredGraph.addEdge(e, src, dest);
                    }
                    meo.mergePaths(e, pathData);
                }
            }
        }
        return squaredGraph;
    }

    /**
     * Creates a graph from a square (weighted) adjacency matrix.  If 
     * <code>nev</code> is non-null then it will be used to store the edge weights.
     * 
     * <p>Notes on implementation: 
     * <ul>
     * <li>The matrix indices will be mapped onto vertices in the order in which the
     * vertex factory generates the vertices.  This means the user is responsible
     * <li>The type of edges created (directed or undirected) depends
     * entirely on the graph factory supplied, regardless of whether the 
     * matrix is symmetric or not.  The Colt {@code Property.isSymmetric} 
     * method may be used to find out whether the matrix
     * is symmetric prior to making this call.
     * <li>The matrix supplied need not be square.  If it is not square, then 
     * the 
     * 
     * @return a representation of <code>matrix</code> as a JUNG
     *         <code>Graph</code>
     */
    public static <V,E> Graph<V,E> matrixToGraph(DoubleMatrix2D matrix, 
    		Factory<? extends Graph<V,E>> graphFactory,
    		Factory<V> vertexFactory, Factory<E> edgeFactory, 
    		Map<E,Number> nev)
    {
        if (matrix.rows() != matrix.columns())
        {
            throw new IllegalArgumentException("Matrix must be square.");
        }
        int size = matrix.rows();

        Graph<V,E> graph = graphFactory.create();
        
        for(int i = 0; i < size; i++) 
        {
            V vertex = vertexFactory.create();
        	graph.addVertex(vertex);
        }

        List<V> vertices = new ArrayList<V>(graph.getVertices());
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                double value = matrix.getQuick(i, j);
                if (value != 0)
                {
                    E e = edgeFactory.create();
                    if (graph.addEdge(e, vertices.get(i), vertices.get(j)))
                    {
                        if (e != null && nev != null)
                            nev.put(e, value);
                    }
                }
            }
        }
        
        
        return graph;
    }
    
    
    /**
     * Creates a graph from a square (weighted) adjacency matrix.
     *  
     * @return a representation of <code>matrix</code> as a JUNG <code>Graph</code>
     */
    public static <V,E> Graph<V,E> matrixToGraph(DoubleMatrix2D matrix,
    		Factory<? extends Graph<V,E>> graphFactory,
    		Factory<V> vertexFactory, Factory<E> edgeFactory)
    {
        return GraphMatrixOperations.<V,E>matrixToGraph(matrix, 
                graphFactory, vertexFactory, edgeFactory, null);
    }
    
    /**
     * Returns an unweighted (0-1) adjacency matrix based on the specified graph.
     * @param <V> the vertex type
     * @param <E> the edge type
     * @param g the graph to convert to a matrix
     */
    public static <V,E> SparseDoubleMatrix2D graphToSparseMatrix(Graph<V,E> g)
    {
        return graphToSparseMatrix(g, null);
    }
    
    /**
     * Returns a SparseDoubleMatrix2D whose entries represent the edge weights for the
     * edges in <code>g</code>, as specified by <code>nev</code>.  
     * 
     * <p>The <code>(i,j)</code> entry of the matrix returned will be equal to the sum
     * of the weights of the edges connecting the vertex with index <code>i</code> to 
     * <code>j</code>.
     * 
     * <p>If <code>nev</code> is <code>null</code>, then a constant edge weight of 1 is used.
     * 
     * @param g
     * @param nev
     */
    public static <V,E> SparseDoubleMatrix2D graphToSparseMatrix(Graph<V,E> g, Map<E,Number> nev)
    {
        if (nev == null)
            nev = new ConstantMap<E,Number>(1);
        int numVertices = g.getVertexCount();
        SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(numVertices,
                numVertices);

        BidiMap<V,Integer> indexer = Indexer.<V>create(g.getVertices());
        int i=0;
        
        for(V v : g.getVertices())
        {
            for (E e : g.getOutEdges(v))
            {
                V w = g.getOpposite(v,e);
                int j = indexer.get(w);
                matrix.set(i, j, matrix.getQuick(i,j) + nev.get(e).doubleValue());
            }
            i++;
        }
        return matrix;
    }

    /**
     * Returns a diagonal matrix whose diagonal entries contain the degree for
     * the corresponding node.
     * 
     * <p>NOTE: the vertices will be traversed in the order given by the graph's vertex 
     * collection.  If you want to be assured of a particular ordering, use a graph 
     * implementation that guarantees such an ordering (see the implementations with {@code Ordered}
     * or {@code Sorted} in their name).
     * 
     * @return SparseDoubleMatrix2D
     */
    public static <V,E> SparseDoubleMatrix2D createVertexDegreeDiagonalMatrix(Graph<V,E> graph)
    {
        int numVertices = graph.getVertexCount();
        SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(numVertices,
                numVertices);
        int i = 0;
        for (V v : graph.getVertices())
        {
            matrix.set(i, i, graph.degree(v));
            i++;
        }
        return matrix;
    }

    /**
     * The idea here is based on the metaphor of an electric circuit. We assume
     * that an undirected graph represents the structure of an electrical
     * circuit where each edge has unit resistance. One unit of current is
     * injected into any arbitrary vertex s and one unit of current is extracted
     * from any arbitrary vertex t. The voltage at some vertex i for source
     * vertex s and target vertex t can then be measured according to the
     * equation: V_i^(s,t) = T_is - T-it where T is the voltage potential matrix
     * returned by this method. *
     * 
     * @param graph
     *            an undirected graph representing an electrical circuit
     * @return the voltage potential matrix
     * @see "P. Doyle and J. Snell, 'Random walks and electric networks,', 1989"
     * @see "M. Newman, 'A measure of betweenness centrality based on random walks', pp. 5-7, 2003"
     */
    public static <V,E> DoubleMatrix2D computeVoltagePotentialMatrix(
            UndirectedGraph<V,E> graph)
    {
        int numVertices = graph.getVertexCount();
        //create adjacency matrix from graph
        DoubleMatrix2D A = GraphMatrixOperations.graphToSparseMatrix(graph,
                null);
        //create diagonal matrix of vertex degrees
        DoubleMatrix2D D = GraphMatrixOperations
                .createVertexDegreeDiagonalMatrix(graph);
        DoubleMatrix2D temp = new SparseDoubleMatrix2D(numVertices - 1,
                numVertices - 1);
        //compute D - A except for last row and column
        for (int i = 0; i < numVertices - 1; i++)
        {
            for (int j = 0; j < numVertices - 1; j++)
            {
                temp.set(i, j, D.get(i, j) - A.get(i, j));
            }
        }
        Algebra algebra = new Algebra();
        DoubleMatrix2D tempInverse = algebra.inverse(temp);
        DoubleMatrix2D T = new SparseDoubleMatrix2D(numVertices, numVertices);
        //compute "voltage" matrix
        for (int i = 0; i < numVertices - 1; i++)
        {
            for (int j = 0; j < numVertices - 1; j++)
            {
                T.set(i, j, tempInverse.get(i, j));
            }
        }
        return T;
    }

    /**
     * Converts a Map of (Vertex, Double) pairs to a DoubleMatrix1D.
     * 
     * <p>Note: the vertices will appear in the output array in the order given
     * by {@code map}'s iterator.  If you want a particular ordering, use a {@code Map}
     * implementation that provides such an ordering ({@code SortedMap, LinkedHashMap}, etc.).
     */
    public static <V> DoubleMatrix1D mapTo1DMatrix(Map<V,Number> map)
    {
        int numVertices = map.size();
        DoubleMatrix1D vector = new DenseDoubleMatrix1D(numVertices);
        int i = 0;
        for (V v : map.keySet())
        {
            vector.set(i, map.get(v).doubleValue());
            i++;
        }
        return vector;
    }

    /**
     * Computes the all-pairs mean first passage time for the specified graph,
     * given an existing stationary probability distribution.
     * <P>
     * The mean first passage time from vertex v to vertex w is defined, for a
     * Markov network (in which the vertices represent states and the edge
     * weights represent state->state transition probabilities), as the expected
     * number of steps required to travel from v to w if the steps occur
     * according to the transition probabilities.
     * <P>
     * The stationary distribution is the fraction of time, in the limit as the
     * number of state transitions approaches infinity, that a given state will
     * have been visited. Equivalently, it is the probability that a given state
     * will be the current state after an arbitrarily large number of state
     * transitions.
     * 
     * @param G
     *            the graph on which the MFPT will be calculated
     * @param edgeWeights
     *            the edge weights
     * @param stationaryDistribution
     *            the asymptotic state probabilities
     * @return the mean first passage time matrix
     */
    public static <V,E> DoubleMatrix2D computeMeanFirstPassageMatrix(Graph<V,E> G,
            Map<E,Number> edgeWeights, DoubleMatrix1D stationaryDistribution)
    {
        DoubleMatrix2D temp = GraphMatrixOperations.graphToSparseMatrix(G,
                edgeWeights);
        for (int i = 0; i < temp.rows(); i++)
        {
            for (int j = 0; j < temp.columns(); j++)
            {
                double value = -1 * temp.get(i, j)
                        + stationaryDistribution.get(j);
                if (i == j)
                    value += 1;
                if (value != 0)
                    temp.set(i, j, value);
            }
        }
        Algebra algebra = new Algebra();
        DoubleMatrix2D fundamentalMatrix = algebra.inverse(temp);
        temp = new SparseDoubleMatrix2D(temp.rows(), temp.columns());
        for (int i = 0; i < temp.rows(); i++)
        {
            for (int j = 0; j < temp.columns(); j++)
            {
                double value = -1.0 * fundamentalMatrix.get(i, j);
                value += fundamentalMatrix.get(j, j);
                if (i == j)
                    value += 1;
                if (value != 0)
                    temp.set(i, j, value);
            }
        }
        DoubleMatrix2D stationaryMatrixDiagonal = new SparseDoubleMatrix2D(temp
                .rows(), temp.columns());
        int numVertices = stationaryDistribution.size();
        for (int i = 0; i < numVertices; i++)
            stationaryMatrixDiagonal.set(i, i, 1.0 / stationaryDistribution
                    .get(i));
        DoubleMatrix2D meanFirstPassageMatrix = algebra.mult(temp,
                stationaryMatrixDiagonal);
        return meanFirstPassageMatrix;
    }
}