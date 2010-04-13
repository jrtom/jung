/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on Aug 18, 2005
 */

package edu.uci.ics.jung.visualization.util;

import javax.swing.event.ChangeListener;

/**
 * the implementing class provides support for ChangeEvents.
 * 
 * @author Tom Nelson - tomnelson@dev.java.net
 *
 */
public interface ChangeEventSupport {

    void addChangeListener(ChangeListener l);

    /**
     * Removes a ChangeListener.
     * @param l the listener to be removed
     */
    void removeChangeListener(ChangeListener l);

    /**
     * Returns an array of all the <code>ChangeListener</code>s added
     * with addChangeListener().
     *
     * @return all of the <code>ChangeListener</code>s added or an empty
     *         array if no listeners have been added
     */
    ChangeListener[] getChangeListeners();
    
    void fireStateChanged();

}