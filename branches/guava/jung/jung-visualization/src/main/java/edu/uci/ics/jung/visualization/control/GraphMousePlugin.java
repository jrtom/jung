/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Jul 6, 2005
 */

package edu.uci.ics.jung.visualization.control;

import java.awt.event.MouseEvent;

/**
 * the interface for all plugins to the PluggableGraphMouse
 * @author Tom Nelson 
 *
 */
public interface GraphMousePlugin {

	/**
	 * return the mouse event modifiers that will activate this plugin
	 * @return modifiers
	 */
    int getModifiers();

    /**
     * set the mouse event modifiers that will activate this plugin
     * @param modifiers
     */
    void setModifiers(int modifiers);
    
    /**
     * compare the set modifiers against those of the supplied event
     * @param e an event to compare to
     * @return whether the member modifers match the event modifiers
     */
    boolean checkModifiers(MouseEvent e);

}
