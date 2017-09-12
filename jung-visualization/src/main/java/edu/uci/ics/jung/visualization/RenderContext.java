package edu.uci.ics.jung.visualization;

import com.google.common.base.Predicate;
import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.function.Function;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;

public interface RenderContext<V, E> {

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

  Network<V, E> getNetwork();

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

  Function<? super E, Shape> getEdgeShapeTransformer();

  void setEdgeShapeTransformer(Function<? super E, Shape> edgeShapeTransformer);

  Function<? super E, String> getEdgeLabelTransformer();

  void setEdgeLabelTransformer(Function<? super E, String> edgeStringer);

  Function<? super E, Stroke> edgestrokeTransformer();

  void setEdgeStrokeTransformer(Function<? super E, Stroke> edgeStrokeTransformer);

  Function<? super E, Stroke> getEdgeArrowStrokeTransformer();

  void setEdgeArrowStrokeTransformer(Function<? super E, Stroke> edgeArrowStrokeTransformer);

  GraphicsDecorator getGraphicsContext();

  void setGraphicsContext(GraphicsDecorator graphicsContext);

  EdgeIndexFunction<E> getParallelEdgeIndexFunction();

  void setParallelEdgeIndexFunction(EdgeIndexFunction<E> parallelEdgeIndexFunction);

  PickedState<E> getPickedEdgeState();

  void setPickedEdgeState(PickedState<E> pickedEdgeState);

  PickedState<V> getPickedVertexState();

  void setPickedVertexState(PickedState<V> pickedVertexState);

  CellRendererPane getRendererPane();

  void setRendererPane(CellRendererPane rendererPane);

  JComponent getScreenDevice();

  void setScreenDevice(JComponent screenDevice);

  Function<? super V, Font> getVertexFontTransformer();

  void setVertexFontTransformer(Function<? super V, Font> vertexFontTransformer);

  Function<V, Icon> getVertexIconTransformer();

  void setVertexIconTransformer(Function<V, Icon> vertexIconTransformer);

  Predicate<V> getVertexIncludePredicate();

  void setVertexIncludePredicate(Predicate<V> vertexIncludePredicate);

  VertexLabelRenderer getVertexLabelRenderer();

  void setVertexLabelRenderer(VertexLabelRenderer vertexLabelRenderer);

  Function<? super V, Paint> getVertexFillPaintTransformer();

  void setVertexFillPaintTransformer(Function<? super V, Paint> vertexFillPaintTransformer);

  Function<? super V, Paint> getVertexDrawPaintTransformer();

  void setVertexDrawPaintTransformer(Function<? super V, Paint> vertexDrawPaintTransformer);

  Function<? super V, Shape> getVertexShapeTransformer();

  void setVertexShapeTransformer(Function<? super V, Shape> vertexShapeTransformer);

  Function<? super V, String> getVertexLabelTransformer();

  void setVertexLabelTransformer(Function<? super V, String> vertexStringer);

  Function<? super V, Stroke> getVertexStrokeTransformer();

  void setVertexStrokeTransformer(Function<? super V, Stroke> vertexStrokeTransformer);

  class DirectedEdgeArrowPredicate implements Predicate<Network<?, ?>> {

    public boolean apply(Network<?, ?> graph) {
      return graph.isDirected();
    }
  }

  class UndirectedEdgeArrowPredicate implements Predicate<Network<?, ?>> {

    public boolean apply(Network<?, ?> graph) {
      return !graph.isDirected();
    }
  }

  MultiLayerTransformer getMultiLayerTransformer();

  void setMultiLayerTransformer(MultiLayerTransformer basicTransformer);

  /** @return the pickSupport */
  NetworkElementAccessor<V, E> getPickSupport();

  /** @param pickSupport the pickSupport to set */
  void setPickSupport(NetworkElementAccessor<V, E> pickSupport);
}
