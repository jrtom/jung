package edu.uci.ics.jung.visualization.renderers

import edu.uci.ics.jung.visualization.RenderContext
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Line2D

interface EdgeArrowRenderingSupport<N : Any, E : Any> {

    /**
     * Returns a transform to position the arrowhead on this edge shape at the point where it
     * intersects the passed node shape.
     *
     * @param rc the rendering context used for rendering the arrow
     * @param edgeShape the shape used to draw the edge
     * @param nodeShape the shape used to draw the node
     * @return a transform used for positioning the arrowhead for this node and edge
     */
    fun getArrowTransform(rc: RenderContext<N, E>, edgeShape: Shape, nodeShape: Shape): AffineTransform?

    /**
     * Returns a transform to position the arrowhead on this edge shape at the point where it
     * intersects the passed node shape.
     *
     * @param rc the rendering context used for rendering the arrow
     * @param edgeShape the shape used to draw the edge
     * @param nodeShape the shape used to draw the node
     * @return a transform used for positioning the arrowhead for this node and edge
     */
    fun getReverseArrowTransform(rc: RenderContext<N, E>, edgeShape: Shape, nodeShape: Shape): AffineTransform?

    /**
     * Returns a transform to position the arrowhead on this edge shape at the point where it
     * intersects the passed node shape.
     *
     * The Loop edge is a special case because its starting point is not inside the node. The
     * passedGo flag handles this case.
     *
     * @param rc the rendering context used for rendering the arrow
     * @param edgeShape the shape used to draw the edge
     * @param nodeShape the shape used to draw the node
     * @param passedGo used for rendering loop edges
     * @return a transform used for positioning the arrowhead for this node and edge
     */
    fun getReverseArrowTransform(
        rc: RenderContext<N, E>,
        edgeShape: Shape,
        nodeShape: Shape,
        passedGo: Boolean
    ): AffineTransform?

    /**
     * Returns a transform to position the arrowhead on this edge shape at the point where it
     * intersects the passed node shape.
     *
     * @param rc the rendering context used for rendering the arrow
     * @param edgeShape the shape used to draw the edge
     * @param nodeShape the shape used to draw the node
     * @return a transform used for positioning the arrowhead for this node and edge
     */
    fun getArrowTransform(rc: RenderContext<N, E>, edgeShape: Line2D, nodeShape: Shape): AffineTransform?
}
