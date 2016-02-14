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

/**
 * Metadata structure for the 'hyperedge' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 *
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
public class HyperEdgeMetadata extends AbstractMetadata {

    private String id;
    private String description;
    private Object edge;
    final private List<EndpointMetadata> endpoints = new ArrayList<EndpointMetadata>();
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addEndpoint( EndpointMetadata endpoint ) {
        endpoints.add(endpoint);
    }
    
    public List<EndpointMetadata> getEndpoints() {
        return endpoints;
    }

    public Object getEdge() {
        return edge;
    }

    public void setEdge(Object edge) {
        this.edge = edge;
    }

    public MetadataType getMetadataType() {
        return MetadataType.HYPEREDGE;
    }
    
}
