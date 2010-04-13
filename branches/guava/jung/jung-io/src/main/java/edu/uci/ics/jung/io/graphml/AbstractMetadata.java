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

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for metadata - implements the property functionality
 * 
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public abstract class AbstractMetadata implements Metadata {

    final private Map<String,String> properties = new HashMap<String, String>();
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
    
    public String setProperty(String key, String value) {
        return properties.put(key, value);
    }
    
    public void addData(DataMetadata data) {
        properties.put(data.getKey(), data.getValue());
    }
}
