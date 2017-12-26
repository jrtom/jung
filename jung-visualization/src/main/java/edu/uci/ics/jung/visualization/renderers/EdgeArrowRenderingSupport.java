package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.visualization.RenderContext;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public interface EdgeArrowRenderingSupport<N, E> {

  /**
   * Returns a transform to position the arrowhead on this edge shape at the point where it
   * intersects the passed vertex shape.
   *
   * @param rc the rendering context used for rendering the arrow
   * @param edgeShape the shape used to draw the edge
   * @param vertexShape the shape used to draw the vertex
   * @return a transform used for positioning the arrowhead for this vertex and edge
   */
  AffineTransform getArrowTransform(RenderContext<N, E> rc, Shape edgeShape, Shape vertexShape);

  /**
   * Returns a transform to position the arrowhead on this edge shape at the point where it
   * intersects the passed vertex shape.
   *
   * @param rc the rendering context used for rendering the arrow
   * @param edgeShape the shape used to draw the edge
   * @param vertexShape the shape used to draw the vertex
   * @return a transform used for positioning the arrowhead for this vertex and edge
   */
  AffineTransform getReverseArrowTransform(
      RenderContext<N, E> rc, Shape edgeShape, Shape vertexShape);

  /**
   * Returns a transform to position the arrowhead on this edge shape at the point where it
   * intersects the passed vertex shape.
   *
   * <p>The Loop edge is a special case because its starting point is not inside the vertex. The
   * passedGo flag handles this case.
   *
   * @param rc the rendering context used for rendering the arrow
   * @param edgeShape the shape used to draw the edge
   * @param vertexShape the shape used to draw the vertex
   * @param passedGo used for rendering loop edges
   * @return a transform used for positioning the arrowhead for this vertex and edge
   */
  AffineTransform getReverseArrowTransform(
      RenderContext<N, E> rc, Shape edgeShape, Shape vertexShape, boolean passedGo);

  /**
   * Returns a transform to position the arrowhead on this edge shape at the point where it
   * intersects the passed vertex shape.
   *
   * @param rc the rendering context used for rendering the arrow
   * @param edgeShape the shape used to draw the edge
   * @param vertexShape the shape used to draw the vertex
   * @return a transform used for positioning the arrowhead for this vertex and edge
   */
  AffineTransform getArrowTransform(RenderContext<N, E> rc, Line2D edgeShape, Shape vertexShape);
}
