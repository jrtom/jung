/*
* Copyright (c) 2003, the JUNG Project and the Regents of the University 
* of California
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* http://jung.sourceforge.net/license.txt for a description.
*/
package edu.uci.ics.jung.visualization.picking;



/**
 * An interface for classes that return information regarding whether a 
 * given graph element (vertex or edge) has been selected.
 * 
 * @author danyelf
 */
public interface PickedInfo<T> {

	public boolean isPicked(T t);
}
