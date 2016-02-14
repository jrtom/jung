/*
 * Copyright (c) 2003, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators;



/**
 * An interface for algorithms that generate graphs that evolve iteratively.
 * @author Scott White
 */
public interface EvolvingGraphGenerator<V, E> extends GraphGenerator<V,E> {

    /**
     * Instructs the algorithm to evolve the graph N steps.
     * @param numSteps number of steps to iterate from the current state
     */
    void evolveGraph(int numSteps);

    /**
     * Retrieves the total number of steps elapsed.
     * @return number of elapsed steps
     */
    int numIterations();
}
