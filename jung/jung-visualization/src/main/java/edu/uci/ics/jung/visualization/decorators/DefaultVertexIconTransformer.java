/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 1, 2005
 */

package edu.uci.ics.jung.visualization.decorators;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.apache.commons.collections15.Transformer;

/**
 * A simple, stateful VertexIconFunction.
 * Stores icons in a Map keyed on the Vertex
 * 
 * @author Tom Nelson 
 *
 *
 */
public class DefaultVertexIconTransformer<V> implements Transformer<V,Icon> {
     
    /**
     * icon storage
     */
     protected Map<V,Icon> iconMap = new HashMap<V,Icon>();

     /**
      * Returns the icon storage as a <code>Map</code>.
      */
    public Map<V,Icon> getIconMap() {
		return iconMap;
	}

    /**
     * Sets the icon storage to the specified <code>Map</code>.
     */
	public void setIconMap(Map<V,Icon> iconMap) {
		this.iconMap = iconMap;
	}

    /**
     * Returns the <code>Icon</code> associated with <code>v</code>.
     */
	public Icon transform(V v) {
		return iconMap.get(v);
	}
}
