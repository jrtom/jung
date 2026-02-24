/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.util

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

/**
 * A class providing static methods useful for improving the performance of graph algorithms.
 *
 * @author Tom Nelson
 */
object Indexer {

  /**
   * Returns a `BiMap` mapping each element of the collection to its index as encountered
   * while iterating over the collection. The purpose of the index operation is to supply an O(1)
   * replacement operation for the O(n) `indexOf(element)` method of a `List`
   *
   * @param T the type of the collection elements
   * @param collection the collection whose indices are to be generated
   * @return a bidirectional map from collection elements to 0-based indices
   */
  @JvmStatic
  fun <T : Any> create(collection: Collection<T>): BiMap<T, Int> = create(collection, 0)

  /**
   * Returns a `BiMap` mapping each element of the collection to its index as encountered
   * while iterating over the collection. The purpose of the index operation is to supply an O(1)
   * replacement operation for the O(n) `indexOf(element)` method of a `List`
   *
   * @param T the type of the collection elements
   * @param collection the collection whose indices are to be generated
   * @param start start index
   * @return a bidirectional map from collection elements to start-based indices
   */
  @JvmStatic
  fun <T : Any> create(collection: Collection<T>, start: Int): BiMap<T, Int> {
    val map: BiMap<T, Int> = HashBiMap.create()
    var i = start
    for (t in collection) {
      map[t] = i++
    }
    return map
  }
}
