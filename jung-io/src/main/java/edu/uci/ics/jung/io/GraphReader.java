/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io;

import edu.uci.ics.jung.graph.Hypergraph;

/**
 * Interface for a reader of graph objects
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * 
 * @param <G>
 *            the graph type
 * @param <V> the vertex type
 *            the vertex type
 * @param <V> the edge type
 *            the edge type
 */
public interface GraphReader<G extends Hypergraph<V, E>, V, E> {

    /**
     * Reads a single graph object, if one is available.
     * 
     * @return the next graph object, or null if none exists.
     * @throws GraphIOException
     *             thrown if an error occurred.
     */
    G readGraph() throws GraphIOException;

    /**
     * Closes this resource and frees any resources.
     * 
     * @throws GraphIOException
     *             thrown if an error occurred.
     */
    void close() throws GraphIOException;
}
