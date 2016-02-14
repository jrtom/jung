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

import java.util.Map;

/**
 * GraphML key object that was parsed from the input stream.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class Key {

    /**
     * Enumeration for the 'for' type of this key.  The for property indicates 
     * which elements (e.g. graph, node, edge) this key applies to.
     */
    public enum ForType {
        ALL, GRAPH, NODE, EDGE, HYPEREDGE, PORT, ENDPOINT
    }
    
    private String id;
    private String description;
    private String attributeName;
    private String attributeType;
    private String defaultValue;
    private ForType forType = ForType.ALL;
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setForType(ForType forType) {
        this.forType = forType;
    }

    public String getId() {
        return this.id;
    }
    
    public String defaultValue() {
        return this.defaultValue;
    }
    
    public ForType getForType() {
        return this.forType;
    }

    public void applyKey( Metadata metadata ) {
        Map<String,String> props = metadata.getProperties();                        
        if( defaultValue != null && !props.containsKey(id) ) {
            props.put(id, defaultValue);
        }
    }
}
