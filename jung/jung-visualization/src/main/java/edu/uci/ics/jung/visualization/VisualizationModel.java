/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 * Created on May 4, 2005
 */

package edu.uci.ics.jung.visualization;

import java.awt.Dimension;

import javax.swing.event.ChangeListener;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.visualization.util.ChangeEventSupport;

/**
 * Interface for the state holding model of the VisualizationViewer.
 * Refactored and extracted from the 1.6.0 version of VisualizationViewer
 * 
 * @author Tom Nelson 
 */
public interface VisualizationModel<V, E> extends ChangeEventSupport {


	Relaxer getRelaxer();
    /**
     * set the graph Layout
     * @param layout
     */
    void setGraphLayout(Layout<V,E> layout);
    
    /**
     * Sets the graph Layout and initialize the Layout size to
     * the passed dimensions. The passed Dimension will often be
     * the size of the View that will display the graph.
     * @param layout
     * @param d
     */
    void setGraphLayout(Layout<V,E> layout, Dimension d);

    /**
     * Returns the current graph layout.
     */
    Layout<V,E> getGraphLayout();

    /**
     * Register <code>l</code> as a listeners to changes in the model. The View registers
     * in order to repaint itself when the model changes.
     */
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

}