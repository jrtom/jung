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
import java.util.List;

/** Maintains all the metadata read in from a single GraphML XML document. */
public class GraphMLDocument {

  private final KeyMap keyMap = new KeyMap();
  private final List<GraphMetadata> graphMetadata = new ArrayList<GraphMetadata>();

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
