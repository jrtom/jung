package edu.uci.ics.jung.visualization.renderers;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import edu.uci.ics.jung.visualization.RenderContext;

public interface EdgeArrowRenderingSupport<V, E> {

	/**
	 * Returns a transform to position the arrowhead on this edge shape at the
	 * point where it intersects the passed vertex shape.
	 */
	AffineTransform getArrowTransform(RenderContext<V, E> rc,
			Shape edgeShape, Shape vertexShape);

	/**
	 * Returns a transform to position the arrowhead on this edge shape at the
	 * point where it intersects the passed vertex shape.
	 */
	AffineTransform getReverseArrowTransform(
			RenderContext<V, E> rc, Shape edgeShape, Shape vertexShape);

	/**
	 * <p>Returns a transform to position the arrowhead on this edge shape at the
	 * point where it intersects the passed vertex shape.</p>
	 * 
	 * <p>The Loop edge is a special case because its staring point is not inside
	 * the vertex. The passedGo flag handles this case.</p>
	 * 
	 * @param edgeShape
	 * @param vertexShape
	 * @param passedGo - used only for Loop edges
	 */
	AffineTransform getReverseArrowTransform(
			RenderContext<V, E> rc, Shape edgeShape, Shape vertexShape,
			boolean passedGo);

	/**
	 * This is used for the arrow of a directed and for one of the
	 * arrows for non-directed edges
	 * Get a transform to place the arrow shape on the passed edge at the
	 * point where it intersects the passed shape
	 * @param edgeShape
	 * @param vertexShape
	 * @return
	 */
	AffineTransform getArrowTransform(RenderContext<V, E> rc,
			Line2D edgeShape, Shape vertexShape);

}