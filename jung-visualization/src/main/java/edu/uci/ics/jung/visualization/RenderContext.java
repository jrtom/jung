package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.NodeLabelRenderer;
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

  Function<? super E, Font> getEdgeFontFunction();

  void setEdgeFontFunction(Function<? super E, Font> edgeFontFunction);

  Predicate<E> getEdgeIncludePredicate();

  void setEdgeIncludePredicate(Predicate<E> edgeIncludePredicate);

  public float getEdgeLabelCloseness();

  public void setEdgeLabelCloseness(float closeness);

  EdgeLabelRenderer getEdgeLabelRenderer();

  void setEdgeLabelRenderer(EdgeLabelRenderer edgeLabelRenderer);

  Function<? super E, Paint> getEdgeFillPaintFunction();

  void setEdgeFillPaintFunction(Function<? super E, Paint> edgePaintFunction);

  Function<? super E, Paint> getEdgeDrawPaintFunction();

  void setEdgeDrawPaintFunction(Function<? super E, Paint> edgeDrawPaintFunction);

  Function<? super E, Paint> getArrowDrawPaintFunction();

  void setArrowDrawPaintFunction(Function<? super E, Paint> arrowDrawPaintFunction);

  Function<? super E, Paint> getArrowFillPaintFunction();

  void setArrowFillPaintFunction(Function<? super E, Paint> arrowFillPaintFunction);

  Function<Context<Network<N, E>, E>, Shape> getEdgeShapeFunction();

  void setEdgeShapeFunction(Function<Context<Network<N, E>, E>, Shape> edgeShapeFunction);

  Function<? super E, String> getEdgeLabelFunction();

  void setEdgeLabelFunction(Function<? super E, String> edgeStringer);

  Function<? super E, Stroke> edgeStrokeFunction();

  void setEdgeStrokeFunction(Function<? super E, Stroke> edgeStrokeFunction);

  Function<? super E, Stroke> getEdgeArrowStrokeFunction();

  void setEdgeArrowStrokeFunction(Function<? super E, Stroke> edgeArrowStrokeFunction);

  GraphicsDecorator getGraphicsContext();

  void setGraphicsContext(GraphicsDecorator graphicsContext);

  EdgeIndexFunction<N, E> getParallelEdgeIndexFunction();

  void setParallelEdgeIndexFunction(EdgeIndexFunction<N, E> parallelEdgeIndexFunction);

  PickedState<E> getPickedEdgeState();

  void setPickedEdgeState(PickedState<E> pickedEdgeState);

  PickedState<N> getPickedNodeState();

  void setPickedNodeState(PickedState<N> pickedNodeState);

  CellRendererPane getRendererPane();

  void setRendererPane(CellRendererPane rendererPane);

  JComponent getScreenDevice();

  void setScreenDevice(JComponent screenDevice);

  Function<? super N, Font> getNodeFontFunction();

  void setNodeFontFunction(Function<? super N, Font> nodeFontFunction);

  Function<N, Icon> getNodeIconFunction();

  void setNodeIconFunction(Function<N, Icon> nodeIconFunction);

  Predicate<N> getNodeIncludePredicate();

  void setNodeIncludePredicate(Predicate<N> nodeIncludePredicate);

  NodeLabelRenderer getNodeLabelRenderer();

  void setNodeLabelRenderer(NodeLabelRenderer nodeLabelRenderer);

  Function<? super N, Paint> getNodeFillPaintFunction();

  void setNodeFillPaintFunction(Function<? super N, Paint> nodeFillPaintFunction);

  Function<? super N, Paint> getNodeDrawPaintFunction();

  void setNodeDrawPaintFunction(Function<? super N, Paint> nodeDrawPaintFunction);

  Function<? super N, Shape> getNodeShapeFunction();

  void setNodeShapeFunction(Function<? super N, Shape> nodeShapeFunction);

  Function<? super N, String> getNodeLabelFunction();

  void setNodeLabelFunction(Function<? super N, String> nodeStringer);

  Function<? super N, Paint> getNodeLabelDrawPaintFunction();

  void setNodeLabelDrawPaintFunction(Function<? super N, Paint> nodeLabelDrawPaintFunction);

  Function<? super N, Stroke> getNodeStrokeFunction();

  void setNodeStrokeFunction(Function<? super N, Stroke> nodeStrokeFunction);

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
