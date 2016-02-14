/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 26, 2005
 */

package edu.uci.ics.jung.visualization.control;

import java.awt.event.ItemListener;

import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;

/**
 * Interface for a GraphMouse that supports modality.
 * 
 * @author Tom Nelson 
 *
 */
public interface ModalGraphMouse extends GraphMouse {
    
    void setMode(Mode mode);
    
    /**
     * @return Returns the modeListener.
     */
    ItemListener getModeListener();
    
    /**
     */
    enum Mode { TRANSFORMING, PICKING, ANNOTATING, EDITING }
    
}