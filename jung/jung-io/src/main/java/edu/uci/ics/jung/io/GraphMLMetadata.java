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

import com.google.common.base.Function;

/**
 * Maintains information relating to data for the specified type.
 * This includes a Function from objects to their values,
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
	 * A Function from objects to string representations 
	 * of their values.
	 */
	public Function<T, String> transformer;
	
	/**
	 * Creates a new instance with the specified description,
	 * default value, and Function.
	 */
	public GraphMLMetadata(String description, String default_value,
			Function<T, String> Function)
	{
		this.description = description;
		this.transformer = Function;
		this.default_value = default_value;
	}
}
