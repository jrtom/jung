/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
* 
* Created on Feb 12, 2004
*/
package edu.uci.ics.jung.algorithms.shortestpath;

import java.util.Map;


/**
 * An interface for algorithms that calculate shortest paths.
 */
public interface ShortestPath<V, E>
{
    /**
     * Returns a map from vertices to the last edge on the shortest path to that vertex
     * starting from {@code source}.
     * 
     * @param source the starting point for the shortest paths
     * @return a map from vertices to the last edge on the shortest path to that vertex
     *     starting from {@code source}
     */ 
     Map<V,E> getIncomingEdgeMap(V source);
}
