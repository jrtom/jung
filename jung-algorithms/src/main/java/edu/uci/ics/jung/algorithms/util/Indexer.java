/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.util;

import java.util.Collection;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * A class providing static methods useful for improving the
 * performance of graph algorithms.
 * 
 * @author Tom Nelson
 *
 */
public class Indexer {
	
	/**
	 * Returns a <code>BiMap</code> mapping each element of the collection to its
	 * index as encountered while iterating over the collection. The purpose
	 * of the index operation is to supply an O(1) replacement operation for the
	 * O(n) <code>indexOf(element)</code> method of a <code>List</code>
	 * @param <T> the type of the collection elements
	 * @param collection the collection whose indices are to be generated
	 * @return a bidirectional map from collection elements to 0-based indices
	 */
	public static <T> BiMap<T,Integer> create(Collection<T> collection) {
	    return create(collection, 0);
	}
	/**
	 * Returns a <code>BiMap</code> mapping each element of the collection to its
	 * index as encountered while iterating over the collection. The purpose
	 * of the index operation is to supply an O(1) replacement operation for the
	 * O(n) <code>indexOf(element)</code> method of a <code>List</code>
	 * @param <T> the type of the collection elements
	 * @param collection the collection whose indices are to be generated
	 * @param start start index
	 * @return a bidirectional map from collection elements to start-based indices
	 */
	public static <T> BiMap<T,Integer> create(Collection<T> collection, int start) {
		BiMap<T,Integer> map = HashBiMap.<T,Integer>create();
		int i=start;
		for(T t : collection) {
			map.put(t,i++);
		}
		return map;
	}
}
