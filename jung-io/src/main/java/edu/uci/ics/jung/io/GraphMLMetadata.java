/**
 * Copyright (c) 2008, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
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
	 * A Function mapping objects to string representations of their values.
	 */
	public Function<T, String> transformer;
	
	/**
	 * Creates a new instance with the specified description,
	 * default value, and function.
	 * 
	 * @param description a textual description of the object
	 * @param default_value the default value for the object, as a String
	 * @param function maps objects of this type to string representations
	 */
	public GraphMLMetadata(String description, String default_value,
			Function<T, String> function)
	{
		this.description = description;
		this.transformer = function;
		this.default_value = default_value;
	}
}
