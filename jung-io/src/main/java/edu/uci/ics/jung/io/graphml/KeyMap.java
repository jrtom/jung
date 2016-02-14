/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io.graphml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A KeyMap is a storage mechanism for the keys read from the GraphML file. It
 * stores the keys indexed by the type of GraphML metadata (node, edge, etc)
 * that the key applies to. The <code>applyKeys</code> method will obtain the
 * list of keys that apply to the given metadata type and apply the keys
 * one-by-one to the metadata.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class KeyMap {

    final private Map<Metadata.MetadataType, List<Key>> map = new HashMap<Metadata.MetadataType, List<Key>>();

    /**
     * Adds the given key to the map.
     *
     * @param key the key to be added.
     */
    public void addKey(Key key) {

        switch (key.getForType()) {
            case EDGE: {
                getKeyList(Metadata.MetadataType.EDGE).add(key);
                break;
            }
            case ENDPOINT: {
                getKeyList(Metadata.MetadataType.ENDPOINT).add(key);
                break;
            }
            case GRAPH: {
                getKeyList(Metadata.MetadataType.GRAPH).add(key);
                break;
            }
            case HYPEREDGE: {
                getKeyList(Metadata.MetadataType.HYPEREDGE).add(key);
                break;
            }
            case NODE: {
                getKeyList(Metadata.MetadataType.NODE).add(key);
                break;
            }
            case PORT: {
                getKeyList(Metadata.MetadataType.PORT).add(key);
                break;
            }
            default: {

                // Default = ALL
                getKeyList(Metadata.MetadataType.EDGE).add(key);
                getKeyList(Metadata.MetadataType.ENDPOINT).add(key);
                getKeyList(Metadata.MetadataType.GRAPH).add(key);
                getKeyList(Metadata.MetadataType.HYPEREDGE).add(key);
                getKeyList(Metadata.MetadataType.NODE).add(key);
                getKeyList(Metadata.MetadataType.PORT).add(key);
            }
        }
    }

    /**
     * Applies all keys that are applicable to the given metadata.
     *
     * @param metadata the target metadata.
     */
    public void applyKeys(Metadata metadata) {

        List<Key> keys = getKeyList(metadata.getMetadataType());
        for (Key key : keys) {
            key.applyKey(metadata);
        }
    }

    /**
     * Clears this map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Retrieves the set of entries contained in this map.
     *
     * @return all of the entries in this map.
     */
    public Set<Map.Entry<Metadata.MetadataType, List<Key>>> entrySet() {
        return map.entrySet();
    }

    /**
     * Gets the list for the given metadata type. If doesn't exist, the list is
     * created.
     *
     * @param type the metadata type.
     * @return the list for the metadata type.
     */
    private List<Key> getKeyList(Metadata.MetadataType type) {

        List<Key> keys = map.get(type);
        if (keys == null) {
            keys = new ArrayList<Key>();
            map.put(type, keys);
        }

        return keys;
    }
}
