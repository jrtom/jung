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

import java.util.Map;

/**
 * Interface for any GraphML metadata.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public interface Metadata {

    /**
     * Metadata type enumeration
     */
    enum MetadataType {
        GRAPH, NODE, EDGE, HYPEREDGE, PORT, ENDPOINT
    }

    /**
     * Gets the metadata type of this object.
     * 
     * @return the metadata type
     */
    MetadataType getMetadataType();

    /**
     * Gets any properties that were associated with this metadata in the
     * GraphML
     * 
     * @return GraphML properties
     */
    Map<String, String> getProperties();
}