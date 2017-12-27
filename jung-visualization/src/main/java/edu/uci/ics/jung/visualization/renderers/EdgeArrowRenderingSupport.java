package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.visualization.RenderContext;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public interface EdgeArrowRenderingSupport<N, E> {

  /**
   * Returns a transform to position the arrowhead on this edge shape at the point where it
   * intersects the passed node shape.
   *
   * @param rc the rendering context used for rendering the arrow
   * @param edgeShape the shape used to draw the edge
   * @param nodeShape the shape used to draw the node
   * @return a transform used for positioning the arrowhead for this node and edge
   */
  AffineTransform getArrowTransform(RenderContext<N, E> rc, Shape edgeShape, Shape nodeShape);

  /**
   * Returns a transform to position the arrowhead on this edge shape at the point where it
   * intersects the passed node shape.
   *
   * @param rc the rendering context used for rendering the arrow
   * @param edgeShape the shape used to draw the edge
   * @param nodeShape the shape used to draw the node
   * @return a transform used for positioning the arrowhead for this node and edge
   */
  AffineTransform getReverseArrowTransform(
      RenderContext<N, E> rc, Shape edgeShape, Shape nodeShape);

  /**
   * Returns a transform to position the arrowhead on this edge shape at the point where it
   * intersects the passed node shape.
   *
   * <p>The Loop edge is a special case because its starting point is not inside the node. The
   * passedGo flag handles this case.
   *
   * @param rc the rendering context used for rendering the arrow
   * @param edgeShape the shape used to draw the edge
   * @param nodeShape the shape used to draw the node
   * @param passedGo used for rendering loop edges
   * @return a transform used for positioning the arrowhead for this node and edge
   */
  AffineTransform getReverseArrowTransform(
      RenderContext<N, E> rc, Shape edgeShape, Shape nodeShape, boolean passedGo);

  /**
   * Returns a transform to position the arrowhead on this edge shape at the point where it
   * intersects the passed node shape.
   *
   * @param rc the rendering context used for rendering the arrow
   * @param edgeShape the shape used to draw the edge
   * @param nodeShape the shape used to draw the node
   * @return a transform used for positioning the arrowhead for this node and edge
   */
  AffineTransform getArrowTransform(RenderContext<N, E> rc, Line2D edgeShape, Shape nodeShape);
}
