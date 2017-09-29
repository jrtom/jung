package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.Context;
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

public interface RenderContext {

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

  //  Network getNetwork();

  int getLabelOffset();

  void setLabelOffset(int labelOffset);

  float getArrowPlacementTolerance();

  void setArrowPlacementTolerance(float arrow_placement_tolerance);

  Shape getEdgeArrow();

  void setEdgeArrow(Shape shape);

  boolean renderEdgeArrow();

  void setRenderEdgeArrow(boolean render);

  Function<Object, Font> getEdgeFontTransformer();

  void setEdgeFontTransformer(Function<Object, Font> edgeFontTransformer);

  Predicate<Object> getEdgeIncludePredicate();

  void setEdgeIncludePredicate(Predicate<Object> edgeIncludePredicate);

  public float getEdgeLabelCloseness();

  public void setEdgeLabelCloseness(float closeness);

  EdgeLabelRenderer getEdgeLabelRenderer();

  void setEdgeLabelRenderer(EdgeLabelRenderer edgeLabelRenderer);

  Function<Object, Paint> getEdgeFillPaintTransformer();

  void setEdgeFillPaintTransformer(Function<Object, Paint> edgePaintTransformer);

  Function<Object, Paint> getEdgeDrawPaintTransformer();

  void setEdgeDrawPaintTransformer(Function<Object, Paint> edgeDrawPaintTransformer);

  Function<Object, Paint> getArrowDrawPaintTransformer();

  void setArrowDrawPaintTransformer(Function<Object, Paint> arrowDrawPaintTransformer);

  Function<Object, Paint> getArrowFillPaintTransformer();

  void setArrowFillPaintTransformer(Function<Object, Paint> arrowFillPaintTransformer);

  Function<Context<Network, Object>, Shape> getEdgeShapeTransformer();

  void setEdgeShapeTransformer(Function<Context<Network, Object>, Shape> edgeShapeTransformer);

  Function<Object, String> getEdgeLabelTransformer();

  void setEdgeLabelTransformer(Function<Object, String> edgeStringer);

  Function<Object, Stroke> edgestrokeTransformer();

  void setEdgeStrokeTransformer(Function<Object, Stroke> edgeStrokeTransformer);

  Function<Object, Stroke> getEdgeArrowStrokeTransformer();

  void setEdgeArrowStrokeTransformer(Function<Object, Stroke> edgeArrowStrokeTransformer);

  GraphicsDecorator getGraphicsContext();

  void setGraphicsContext(GraphicsDecorator graphicsContext);

  EdgeIndexFunction getParallelEdgeIndexFunction();

  void setParallelEdgeIndexFunction(EdgeIndexFunction parallelEdgeIndexFunction);

  PickedState getPickedEdgeState();

  void setPickedEdgeState(PickedState pickedEdgeState);

  PickedState getPickedVertexState();

  void setPickedVertexState(PickedState pickedVertexState);

  CellRendererPane getRendererPane();

  void setRendererPane(CellRendererPane rendererPane);

  JComponent getScreenDevice();

  void setScreenDevice(JComponent screenDevice);

  Function<Object, Font> getVertexFontTransformer();

  void setVertexFontTransformer(Function<Object, Font> vertexFontTransformer);

  Function<Object, Icon> getVertexIconTransformer();

  void setVertexIconTransformer(Function<Object, Icon> vertexIconTransformer);

  Predicate<Object> getVertexIncludePredicate();

  void setVertexIncludePredicate(Predicate<Object> vertexIncludePredicate);

  VertexLabelRenderer getVertexLabelRenderer();

  void setVertexLabelRenderer(VertexLabelRenderer vertexLabelRenderer);

  Function<Object, Paint> getVertexFillPaintTransformer();

  void setVertexFillPaintTransformer(Function<Object, Paint> vertexFillPaintTransformer);

  Function<Object, Paint> getVertexDrawPaintTransformer();

  void setVertexDrawPaintTransformer(Function<Object, Paint> vertexDrawPaintTransformer);

  Function<Object, Shape> getVertexShapeTransformer();

  void setVertexShapeTransformer(Function<Object, Shape> vertexShapeTransformer);

  Function<Object, String> getVertexLabelTransformer();

  void setVertexLabelTransformer(Function<Object, String> vertexStringer);

  Function<Object, Stroke> getVertexStrokeTransformer();

  void setVertexStrokeTransformer(Function<Object, Stroke> vertexStrokeTransformer);

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
  NetworkElementAccessor getPickSupport();

  /** @param pickSupport the pickSupport to set */
  void setPickSupport(NetworkElementAccessor pickSupport);
}
