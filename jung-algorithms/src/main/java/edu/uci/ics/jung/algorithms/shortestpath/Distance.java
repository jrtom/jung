/*
 * Created on Apr 2, 2004
 *
 * Copyright (c) 2004, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.shortestpath;

import java.util.Map;


/**
 * An interface for classes which calculate the distance between
 * one vertex and another.
 * 
 * @author Joshua O'Madadhain
 */
public interface Distance<V>
{
	/**
	 * Returns the distance from the <code>source</code> vertex to the
	 * <code>target</code> vertex. If <code>target</code> is not reachable from
	 * <code>source</code>, returns null.
	 * 
	 * @param source the vertex from which distance is to be measured
	 * @param target the vertex to which distance is to be measured
	 * @return the distance from {@code source} to {@code target}
	 */
	Number getDistance(V source, V target);

	/**
	 * Returns a <code>Map</code> which maps each vertex in the graph (including
	 * the <code>source</code> vertex) to its distance (represented as a Number)
	 * from <code>source</code>. If any vertex is not reachable from
	 * <code>source</code>, no distance is stored for that vertex.
	 * 
	 * @param source the vertex from which distances are to be measured
	 * @return a {@code Map} of the distances from {@code source} to other vertices in the graph
	 */
	Map<V, Number> getDistanceMap(V source);
}
