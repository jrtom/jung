
package edu.uci.ics.jung.algorithms.generators.random;

/*
* Copyright (c) 2009, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.algorithms.util.WeightedChoice;
import edu.uci.ics.jung.graph.Graph;

/**
 * Graph generator that produces a random graph with small world properties. 
 * The underlying model is an mxn (optionally toroidal) lattice. Each node u 
 * has four local connections, one to each of its neighbors, and
 * in addition 1+ long range connections to some node v where v is chosen randomly according to
 * probability proportional to d^-alpha where d is the lattice distance between u and v and alpha
 * is the clustering exponent.
 * 
 * @see "Navigation in a small world J. Kleinberg, Nature 406(2000), 845."
 * @author Joshua O'Madadhain
 */
public class KleinbergSmallWorldGenerator<V, E> extends Lattice2DGenerator<V, E> {
    private double clustering_exponent;
    private Random random;
    private int num_connections = 1;
    
    /**
     * Creates an instance with the specified parameters, whose underlying lattice is (a) of size
     * {@code latticeSize} x {@code latticeSize}, and (b) toroidal.
     * @param graphFactory factory for graphs of the appropriate type
     * @param vertexFactory factory for vertices of the appropriate type
     * @param edgeFactory factory for edges of the appropriate type
     * @param latticeSize the number of rows and columns of the underlying lattice
     * @param clusteringExponent the clustering exponent
     */
    public KleinbergSmallWorldGenerator(Supplier<? extends Graph<V,E>> graphFactory,
    		Supplier<V> vertexFactory, Supplier<E> edgeFactory,
    		int latticeSize, double clusteringExponent) 
    {
        this(graphFactory, vertexFactory, edgeFactory, latticeSize, latticeSize, clusteringExponent);
    }

    /**
     * Creates an instance with the specified parameters, whose underlying lattice is toroidal.
     * @param graphFactory factory for graphs of the appropriate type
     * @param vertexFactory factory for vertices of the appropriate type
     * @param edgeFactory factory for edges of the appropriate type
     * @param row_count number of rows of the underlying lattice
     * @param col_count number of columns of the underlying lattice
     * @param clusteringExponent the clustering exponent
     */
    public KleinbergSmallWorldGenerator(Supplier<? extends Graph<V,E>> graphFactory,
    		Supplier<V> vertexFactory, Supplier<E> edgeFactory,
            int row_count, int col_count, double clusteringExponent) 
    {
        super(graphFactory, vertexFactory, edgeFactory, row_count, col_count, true);
        clustering_exponent = clusteringExponent;
        initialize();
    }

    /**
     * Creates an instance with the specified parameters.
     * @param graphFactory factory for graphs of the appropriate type
     * @param vertexFactory factory for vertices of the appropriate type
     * @param edgeFactory factory for edges of the appropriate type
     * @param row_count number of rows of the underlying lattice
     * @param col_count number of columns of the underlying lattice
     * @param clusteringExponent the clustering exponent
     * @param isToroidal whether the underlying lattice is toroidal
     */
    public KleinbergSmallWorldGenerator(Supplier<? extends Graph<V,E>> graphFactory,
    		Supplier<V> vertexFactory, Supplier<E> edgeFactory, 
    		int row_count, int col_count, double clusteringExponent, boolean isToroidal) 
    {
        super(graphFactory, vertexFactory, edgeFactory, row_count, col_count, isToroidal);
        clustering_exponent = clusteringExponent;
        initialize();
    }

    private void initialize()
    {
        this.random = new Random();
    }
    
    /**
     * Sets the {@code Random} instance used by this instance.  Useful for 
     * unit testing.
     * @param random the {@code Random} instance for this class to use
     */
    public void setRandom(Random random)
    {
        this.random = random;
    }
    
    /**
     * Sets the seed of the internal random number generator.  May be used to provide repeatable
     * experiments.
     * @param seed the random seed that this class's random number generator is to use
     */
    public void setRandomSeed(long seed) 
    {
        random.setSeed(seed);
    }

    /**
     * Sets the number of new 'small-world' connections (outgoing edges) to be added to each vertex.
     * @param num_connections the number of outgoing small-world edges to add to each vertex
     */
    public void setConnectionCount(int num_connections)
    {
        if (num_connections <= 0)
        {
            throw new IllegalArgumentException("Number of new connections per vertex must be >= 1");
        }
        this.num_connections = num_connections;
    }

    /**
     * @return the number of new 'small-world' connections that will originate at each vertex
     */
    public int getConnectionCount()
    {
        return this.num_connections;
    }
    
    /**
     * Generates a random small world network according to the parameters given
     * @return a random small world graph
     */
    @Override
    public Graph<V,E> get() 
    {
        Graph<V, E> graph = super.get();
        
        // TODO: For toroidal graphs, we can make this more clever by pre-creating the WeightedChoice object
        // and using the output as an offset to the current vertex location.
        WeightedChoice<V> weighted_choice;
        
        // Add long range connections
        for (int i = 0; i < graph.getVertexCount(); i++)
        {
            V source = getVertex(i);
            int row = getRow(i);
            int col = getCol(i);
            int row_offset = row < row_count/2 ? -row_count : row_count;
            int col_offset = col < col_count/2 ? -col_count : col_count;

            Map<V, Float> vertex_weights = new HashMap<V, Float>();
            for (int j = 0; j < row_count; j++)
            {
                for (int k = 0; k < col_count; k++)
                {
                    if (j == row && k == col)
                        continue;
                    int v_dist = Math.abs(j - row);
                    int h_dist = Math.abs(k - col);
                    if (is_toroidal)
                    {
                        v_dist = Math.min(v_dist, Math.abs(j - row+row_offset));
                        h_dist = Math.min(h_dist, Math.abs(k - col+col_offset));
                    }
                    int distance = v_dist + h_dist;
                    if (distance < 2)
                        continue;
                    else
                        vertex_weights.put(getVertex(j,k), (float)Math.pow(distance, -clustering_exponent));
                }
            }

            for (int j = 0; j < this.num_connections; j++) {
                weighted_choice = new WeightedChoice<V>(vertex_weights, random);
                V target = weighted_choice.nextItem();
                graph.addEdge(edge_factory.get(), source, target);
            }
        }

        return graph;
    }
}
