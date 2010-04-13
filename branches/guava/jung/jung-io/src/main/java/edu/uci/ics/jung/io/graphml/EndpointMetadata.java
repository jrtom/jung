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

/**
 * Metadata structure for the 'endpoint' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 *
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
public class EndpointMetadata extends AbstractMetadata {

    public enum EndpointType {
        IN,
        OUT,
        UNDIR
    }
    
    private String id;
    private String port;
    private String node;
    private String description;
    private EndpointType endpointType = EndpointType.UNDIR;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPort() {
        return port;
    }
    public void setPort(String port) {
        this.port = port;
    }
    public String getNode() {
        return node;
    }
    public void setNode(String node) {
        this.node = node;
    }
    public EndpointType getEndpointType() {
        return endpointType;
    }
    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }        
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public MetadataType getMetadataType() {
        return MetadataType.ENDPOINT;
    }
    
}
