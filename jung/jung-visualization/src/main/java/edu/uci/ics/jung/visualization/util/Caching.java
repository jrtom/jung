/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * 
 */
package edu.uci.ics.jung.visualization.util;

/**
 * Interface to provide external controls to an
 * implementing class that manages a cache.
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 */
public interface Caching {
	
	/**
	 * ititialize resources for a cache
	 *
	 */
	void init();
	
	/**
	 * clear cache
	 *
	 */
	void clear();

}
