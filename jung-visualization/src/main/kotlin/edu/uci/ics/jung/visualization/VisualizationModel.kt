/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization

import com.google.common.graph.Network
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.layout.util.LayoutEventSupport
import edu.uci.ics.jung.visualization.util.ChangeEventSupport
import java.awt.Dimension
import javax.swing.event.ChangeListener

interface VisualizationModel<N : Any, E : Any> : LayoutEventSupport<N>, ChangeEventSupport {

    enum class SpatialSupport {
        RTREE,
        QUADTREE,
        GRID,
        NONE
    }

    /**
     * @return the current layoutSize of the visualization's space
     */
    fun getLayoutSize(): Dimension

    fun setLayoutAlgorithm(layoutAlgorithm: LayoutAlgorithm<N>)

    fun getLayoutAlgorithm(): LayoutAlgorithm<N>

    fun getLayoutModel(): LayoutModel<N>

    fun setLayoutModel(layoutModel: LayoutModel<N>)

    fun getNetwork(): Network<N, E>

    fun setNetwork(network: Network<N, E>)

    fun setNetwork(network: Network<N, E>, forceUpdate: Boolean)

    override fun addChangeListener(changeListener: ChangeListener)
}
