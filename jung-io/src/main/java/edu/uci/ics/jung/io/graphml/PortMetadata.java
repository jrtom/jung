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
 * Metadata structure for the 'port' GraphML element.
 *
 * @author Nathan Mittler - nathan.mittler@gmail.com
 * 
 * @see "http://graphml.graphdrawing.org/specification.html"
 */
public class PortMetadata extends AbstractMetadata {

    private String name;
    private String description;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String desc) {
        this.description = desc;
    }
    
    public MetadataType getMetadataType() {
        return MetadataType.PORT;
    }

}
