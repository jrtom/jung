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

/**
 * Metadata structure for the 'edge' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 *
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
public class EdgeMetadata extends AbstractMetadata {

    private String id;
    private Boolean directed;
    private String source;
    private String target;
    private String sourcePort;
    private String targetPort;
    private String description;
    private Object edge;
    
    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public Boolean isDirected() {
        return directed;
    }


    public void setDirected(Boolean directed) {
        this.directed = directed;
    }


    public String getSource() {
        return source;
    }


    public void setSource(String source) {
        this.source = source;
    }


    public String getTarget() {
        return target;
    }


    public void setTarget(String target) {
        this.target = target;
    }


    public String getSourcePort() {
        return sourcePort;
    }


    public void setSourcePort(String sourcePort) {
        this.sourcePort = sourcePort;
    }


    public String getTargetPort() {
        return targetPort;
    }


    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public Object getEdge() {
        return edge;
    }


    public void setEdge(Object edge) {
        this.edge = edge;
    }


    public MetadataType getMetadataType() {
        return MetadataType.EDGE;
    }

}
