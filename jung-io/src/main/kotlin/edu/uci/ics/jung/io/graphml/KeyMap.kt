/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.io.graphml

/**
 * A KeyMap is a storage mechanism for the keys read from the GraphML file. It stores the keys
 * indexed by the type of GraphML metadata (node, edge, etc) that the key applies to. The
 * `applyKeys` method will obtain the list of keys that apply to the given metadata type and
 * apply the keys one-by-one to the metadata.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
class KeyMap {

  private val map: MutableMap<Metadata.MetadataType, MutableList<Key>> = HashMap()

  /**
   * Adds the given key to the map.
   *
   * @param key the key to be added.
   */
  fun addKey(key: Key) {
    when (key.forType) {
      Key.ForType.EDGE -> getKeyList(Metadata.MetadataType.EDGE).add(key)
      Key.ForType.ENDPOINT -> getKeyList(Metadata.MetadataType.ENDPOINT).add(key)
      Key.ForType.GRAPH -> getKeyList(Metadata.MetadataType.GRAPH).add(key)
      Key.ForType.NODE -> getKeyList(Metadata.MetadataType.NODE).add(key)
      Key.ForType.PORT -> getKeyList(Metadata.MetadataType.PORT).add(key)
      Key.ForType.ALL -> {
        // Default = ALL
        getKeyList(Metadata.MetadataType.EDGE).add(key)
        getKeyList(Metadata.MetadataType.ENDPOINT).add(key)
        getKeyList(Metadata.MetadataType.GRAPH).add(key)
        getKeyList(Metadata.MetadataType.NODE).add(key)
        getKeyList(Metadata.MetadataType.PORT).add(key)
      }
    }
  }

  /**
   * Applies all keys that are applicable to the given metadata.
   *
   * @param metadata the target metadata.
   */
  fun applyKeys(metadata: Metadata) {
    val keys = getKeyList(metadata.metadataType)
    for (key in keys) {
      key.applyKey(metadata)
    }
  }

  /** Clears this map. */
  fun clear() {
    map.clear()
  }

  /**
   * Retrieves the set of entries contained in this map.
   *
   * @return all of the entries in this map.
   */
  fun entrySet(): Set<Map.Entry<Metadata.MetadataType, MutableList<Key>>> = map.entries

  /**
   * Gets the list for the given metadata type. If doesn't exist, the list is created.
   *
   * @param type the metadata type.
   * @return the list for the metadata type.
   */
  private fun getKeyList(type: Metadata.MetadataType): MutableList<Key> =
    map.getOrPut(type) { ArrayList() }
}
