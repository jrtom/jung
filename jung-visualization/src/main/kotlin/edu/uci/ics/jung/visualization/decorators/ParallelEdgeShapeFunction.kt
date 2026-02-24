/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on March 10, 2005
 */
package edu.uci.ics.jung.visualization.decorators

import com.google.common.graph.Network
import edu.uci.ics.jung.visualization.util.Context
import edu.uci.ics.jung.visualization.util.EdgeIndexFunction
import java.awt.Shape
import java.util.function.Function

/**
 * An abstract class for edge-to-Shape functions that work with parallel edges.
 *
 * @author Tom Nelson
 */
abstract class ParallelEdgeShapeFunction<N, E> :
    Function<Context<Network<N, E>, E>, Shape> {

    /** Specifies the distance between control points for edges being drawn in parallel. */
    protected var control_offset_increment: Float = 20.0f

    protected var _edgeIndexFunction: EdgeIndexFunction<N, E>? = null

    open fun setControlOffsetIncrement(y: Float) {
        control_offset_increment = y
    }

    open fun setEdgeIndexFunction(edgeIndexFunction: EdgeIndexFunction<N, E>?) {
        this._edgeIndexFunction = edgeIndexFunction
    }

    open fun getEdgeIndexFunction(): EdgeIndexFunction<N, E>? = _edgeIndexFunction
}
