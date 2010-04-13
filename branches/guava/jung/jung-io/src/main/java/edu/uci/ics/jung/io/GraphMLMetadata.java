/**
 * Copyright (c) 2008, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 * Created on Jun 30, 2008
 * 
 */
package edu.uci.ics.jung.io;

import org.apache.commons.collections15.Transformer;

/**
 * Maintains information relating to data for the specified type.
 * This includes a transformer from objects to their values,
 * a default value, and a description.
 */
public class GraphMLMetadata<T> 
{
	/**
	 * The description of this data type.
	 */
	public String description;
	
	/**
	 * The default value for objects of this type.
	 */
	public String default_value;
	
	/**
	 * A transformer from objects to string representations 
	 * of their values.
	 */
	public Transformer<T, String> transformer;
	
	/**
	 * Creates a new instance with the specified description,
	 * default value, and transformer.
	 */
	public GraphMLMetadata(String description, String default_value,
			Transformer<T, String> transformer)
	{
		this.description = description;
		this.transformer = transformer;
		this.default_value = default_value;
	}
}
