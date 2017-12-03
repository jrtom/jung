package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.Context;
import edu.uci.ics.jung.visualization.util.EdgeIndexFunction;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;

public interface RenderContext<N, E> {

  float[] dotting = {1.0f, 3.0f};
  float[] dashing = {5.0f};

  /**
   * A stroke for a dotted line: 1 pixel width, round caps, round joins, and an array of {1.0f,
   * 3.0f}.
   */
  Stroke DOTTED =
      new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dotting, 0f);

  /**
   * A stroke for a dashed line: 1 pixel width, square caps, beveled joins, and an array of {5.0f}.
   */
  Stroke DASHED =
      new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f, dashing, 0f);

  /** Specifies the offset for the edge labels. */
  int LABEL_OFFSET = 10;

  int getLabelOffset();

  void setLabelOffset(int labelOffset);

  float getArrowPlacementTolerance();

  void setArrowPlacementTolerance(float arrow_placement_tolerance);

  Shape getEdgeArrow();

  void setEdgeArrow(Shape shape);

  boolean renderEdgeArrow();

  void setRenderEdgeArrow(boolean render);

  Function<? super E, Font> getEdgeFontTransformer();

  void setEdgeFontTransformer(Function<? super E, Font> edgeFontTransformer);

  Predicate<E> getEdgeIncludePredicate();

  void setEdgeIncludePredicate(Predicate<E> edgeIncludePredicate);

  public float getEdgeLabelCloseness();

  public void setEdgeLabelCloseness(float closeness);

  EdgeLabelRenderer getEdgeLabelRenderer();

  void setEdgeLabelRenderer(EdgeLabelRenderer edgeLabelRenderer);

  Function<? super E, Paint> getEdgeFillPaintTransformer();

  void setEdgeFillPaintTransformer(Function<? super E, Paint> edgePaintTransformer);

  Function<? super E, Paint> getEdgeDrawPaintTransformer();

  void setEdgeDrawPaintTransformer(Function<? super E, Paint> edgeDrawPaintTransformer);

  Function<? super E, Paint> getArrowDrawPaintTransformer();

  void setArrowDrawPaintTransformer(Function<? super E, Paint> arrowDrawPaintTransformer);

  Function<? super E, Paint> getArrowFillPaintTransformer();

  void setArrowFillPaintTransformer(Function<? super E, Paint> arrowFillPaintTransformer);

  Function<Context<Network<N, E>, E>, Shape> getEdgeShapeTransformer();

  void setEdgeShapeTransformer(Function<Context<Network<N, E>, E>, Shape> edgeShapeTransformer);

  Function<? super E, String> getEdgeLabelTransformer();

  void setEdgeLabelTransformer(Function<? super E, String> edgeStringer);

  Function<? super E, Stroke> edgestrokeTransformer();

  void setEdgeStrokeTransformer(Function<? super E, Stroke> edgeStrokeTransformer);

  Function<? super E, Stroke> getEdgeArrowStrokeTransformer();

  void setEdgeArrowStrokeTransformer(Function<? super E, Stroke> edgeArrowStrokeTransformer);

  GraphicsDecorator getGraphicsContext();

  void setGraphicsContext(GraphicsDecorator graphicsContext);

  EdgeIndexFunction<N, E> getParallelEdgeIndexFunction();

  void setParallelEdgeIndexFunction(EdgeIndexFunction<N, E> parallelEdgeIndexFunction);

  PickedState<E> getPickedEdgeState();

  void setPickedEdgeState(PickedState<E> pickedEdgeState);

  PickedState<N> getPickedVertexState();

  void setPickedVertexState(PickedState<N> pickedVertexState);

  CellRendererPane getRendererPane();

  void setRendererPane(CellRendererPane rendererPane);

  JComponent getScreenDevice();

  void setScreenDevice(JComponent screenDevice);

  Function<? super N, Font> getVertexFontTransformer();

  void setVertexFontTransformer(Function<? super N, Font> vertexFontTransformer);

  Function<N, Icon> getVertexIconTransformer();

  void setVertexIconTransformer(Function<N, Icon> vertexIconTransformer);

  Predicate<N> getVertexIncludePredicate();

  void setVertexIncludePredicate(Predicate<N> vertexIncludePredicate);

  VertexLabelRenderer getVertexLabelRenderer();

  void setVertexLabelRenderer(VertexLabelRenderer vertexLabelRenderer);

  Function<? super N, Paint> getVertexFillPaintTransformer();

  void setVertexFillPaintTransformer(Function<? super N, Paint> vertexFillPaintTransformer);

  Function<? super N, Paint> getVertexDrawPaintTransformer();

  void setVertexDrawPaintTransformer(Function<? super N, Paint> vertexDrawPaintTransformer);

  Function<? super N, Shape> getVertexShapeTransformer();

  void setVertexShapeTransformer(Function<? super N, Shape> vertexShapeTransformer);

  Function<? super N, String> getVertexLabelTransformer();

  void setVertexLabelTransformer(Function<? super N, String> vertexStringer);

  Function<? super N, Paint> getVertexLabelDrawPaintTransformer();

  void setVertexLabelDrawPaintTransformer(
      Function<? super N, Paint> vertexLabelDrawPaintTransformer);

  Function<? super N, Stroke> getVertexStrokeTransformer();

  void setVertexStrokeTransformer(Function<? super N, Stroke> vertexStrokeTransformer);

  class DirectedEdgeArrowPredicate implements Predicate<Network<?, ?>> {

    public boolean test(Network<?, ?> graph) {
      return graph.isDirected();
    }
  }

  class UndirectedEdgeArrowPredicate implements Predicate<Network<?, ?>> {

    public boolean test(Network<?, ?> graph) {
      return !graph.isDirected();
    }
  }

  MultiLayerTransformer getMultiLayerTransformer();

  void setMultiLayerTransformer(MultiLayerTransformer basicTransformer);

  /** @return the pickSupport */
  NetworkElementAccessor<N, E> getPickSupport();

  /** @param pickSupport the pickSupport to set */
  void setPickSupport(NetworkElementAccessor<N, E> pickSupport);
}
