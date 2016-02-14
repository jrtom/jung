/*
 * Copyright (c) 2003, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Jan 6, 2002
 *
 */
package edu.uci.ics.jung.io;

import edu.uci.ics.jung.graph.Graph;


/**
 * General interface for loading and saving a graph from/to disk.
 * @author Scott
 * @author Tom Nelson - converted to jung2
 *
 */
public interface GraphFile<V,E> {
	
	/**
	 * Loads a graph from a file per the appropriate format
	 * @param filename the location and name of the file
	 * @return the graph
	 */
	Graph<V,E> load(String filename);
	
	/**
	 * Save a graph to disk per the appropriate format
	 * @param graph the location and name of the file
	 * @param filename the graph
	 */
	void save(Graph<V,E> graph, String filename);
}
