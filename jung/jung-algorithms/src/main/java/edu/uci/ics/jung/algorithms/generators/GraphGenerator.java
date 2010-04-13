/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package edu.uci.ics.jung.algorithms.generators;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.graph.Graph;

/**
 * An interface for algorithms that generate graphs.
 * @author Scott White
 */
public interface GraphGenerator<V, E> extends Supplier<Graph<V,E>>{ }

