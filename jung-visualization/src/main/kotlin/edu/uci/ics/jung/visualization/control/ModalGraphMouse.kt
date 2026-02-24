/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 26, 2005
 */

package edu.uci.ics.jung.visualization.control

import edu.uci.ics.jung.visualization.VisualizationViewer
import java.awt.event.ItemListener

/**
 * Interface for a GraphMouse that supports modality.
 *
 * @author Tom Nelson
 */
interface ModalGraphMouse : VisualizationViewer.GraphMouse {

    fun setMode(mode: Mode)

    /**
     * @return Returns the modeListener.
     */
    fun getModeListener(): ItemListener

    enum class Mode {
        TRANSFORMING,
        PICKING,
        ANNOTATING,
        EDITING
    }
}
