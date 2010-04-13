/*
 * Copyright (c) 2008, the JUNG Project and the Regents of the University
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */

package edu.uci.ics.jung.io.graphml;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains all the metadata read in from a single GraphML XML document.
 */
public class GraphMLDocument {

    final private KeyMap keyMap = new KeyMap();
    final private List<GraphMetadata> graphMetadata = new ArrayList<GraphMetadata>();

    public KeyMap getKeyMap() {
        return keyMap;
    }

    public List<GraphMetadata> getGraphMetadata() {
        return graphMetadata;
    }

    public void clear() {
        graphMetadata.clear();
        keyMap.clear();
    }
}
