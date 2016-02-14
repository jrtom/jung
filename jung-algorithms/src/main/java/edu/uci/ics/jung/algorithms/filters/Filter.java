/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.filters;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.Graph;



/**
 * An interface for classes that return a subset of the input <code>Graph</code>
 * as a <code>Graph</code>.  The <code>Graph</code> returned may be either a
 * new graph or a view into an existing graph; the documentation for the filter
 * must specify which.
 * 
 * @author danyelf
 */
public interface Filter<V,E> extends Function<Graph<V,E>, Graph<V,E>>{ }
