/*
 * Created on Apr 8, 2005
 *
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization.decorators

import com.google.common.graph.Network
import edu.uci.ics.jung.graph.util.Graphs.isSelfLoop
import edu.uci.ics.jung.layout.model.LayoutModel
import edu.uci.ics.jung.visualization.MultiLayerTransformer
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Paint
import java.util.function.Function

/**
 * Creates `GradientPaint` instances which can be used to paint an `Edge`. For
 * `DirectedEdge`s, the color will blend from `c1` (source) to `c2`
 * (destination); for `UndirectedEdge`s, the color will be `c1` at each end
 * and `c2` in the middle.
 *
 * @author Joshua O'Madadhain
 */
open class GradientEdgePaintFunction<N : Any, E : Any>(
    protected val c1: Color,
    protected val c2: Color,
    vv: VisualizationViewer<N, E>
) : Function<E, Paint> {

    protected val graph: Network<N, E> = vv.getModel().getNetwork()
    protected val layoutModel: LayoutModel<N> = vv.getModel().getLayoutModel()
    protected val transformer: BidirectionalTransformer =
        vv.getRenderContext().getMultiLayerTransformer().getTransformer(MultiLayerTransformer.Layer.LAYOUT)

    override fun apply(e: E): Paint {
        val endpoints = graph.incidentNodes(e)
        val b = endpoints.nodeU()
        val f = endpoints.nodeV()
        val pb = layoutModel.apply(b)
        val pf = layoutModel.apply(f)
        val p2db = transformer.transform(pb.x, pb.y)!!
        val p2df = transformer.transform(pf.x, pf.y)!!
        var xB = p2db.x.toFloat()
        var yB = p2db.y.toFloat()
        var xF = p2df.x.toFloat()
        var yF = p2df.y.toFloat()
        if (!graph.isDirected) {
            xF = (xF + xB) / 2
            yF = (yF + yB) / 2
        }
        if (isSelfLoop(endpoints)) {
            yF += 50
            xF += 50
        }

        return GradientPaint(xB, yB, getColor1(e), xF, yF, getColor2(e), true)
    }

    /**
     * Returns `c1`. Subclasses may override this method to enable more complex behavior
     * (e.g., for picked edges).
     *
     * @param e the edge for which a color is to be retrieved
     * @return the constructor-supplied color `c1`
     */
    protected open fun getColor1(e: E): Color = c1

    /**
     * Returns `c2`. Subclasses may override this method to enable more complex behavior
     * (e.g., for picked edges).
     *
     * @param e the edge for which a color is to be retrieved
     * @return the constructor-supplied color `c2`
     */
    protected open fun getColor2(e: E): Color = c2
}
