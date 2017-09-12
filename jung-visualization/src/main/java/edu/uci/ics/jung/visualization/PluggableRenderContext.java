/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.graph.util.EdgeIndexFunction;
import edu.uci.ics.jung.graph.util.ParallelEdgeIndexFunction;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.ArrowFactory;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JComponent;

public class PluggableRenderContext<V, E> implements RenderContext<V, E> {

  private final Network<V, E> network;

  protected float arrowPlacementTolerance = 1;
  protected Predicate<V> vertexIncludePredicate = n -> true;
  protected Function<? super V, Stroke> vertexStrokeTransformer = n -> new BasicStroke(1.0f);

  protected Function<? super V, Shape> vertexShapeTransformer =
      n -> new Ellipse2D.Float(-10, -10, 20, 20);

  protected Function<? super V, String> vertexLabelTransformer = n -> null;
  protected Function<V, Icon> vertexIconTransformer;
  protected Function<? super V, Font> vertexFontTransformer =
      n -> new Font("Helvetica", Font.PLAIN, 12);

  protected Function<? super V, Paint> vertexDrawPaintTransformer = n -> Color.BLACK;
  protected Function<? super V, Paint> vertexFillPaintTransformer = n -> Color.RED;

  protected Function<? super E, String> edgeLabelTransformer = e -> null;
  protected Function<? super E, Stroke> edgeStrokeTransformer = e -> new BasicStroke(1.0f);
  protected Function<? super E, Stroke> edgeArrowStrokeTransformer = e -> new BasicStroke(1.0f);

  private static final int EDGE_ARROW_LENGTH = 10;
  private static final int EDGE_ARROW_WIDTH = 8;
  private static final int EDGE_ARROW_NOTCH_DEPTH = 4;
  protected Shape edgeArrow;
  protected boolean renderEdgeArrow;

  protected Predicate<E> edgeIncludePredicate = n -> true;
  protected Function<? super E, Font> edgeFontTransformer =
      n -> new Font("Helvetica", Font.PLAIN, 12);

  private static final float DIRECTED_EDGE_LABEL_CLOSENESS = 0.65f;
  private static final float UNDIRECTED_EDGE_LABEL_CLOSENESS = 0.65f;
  protected float edgeLabelCloseness;

  protected Function<? super E, Shape> edgeShapeTransformer;
  protected Function<? super E, Paint> edgeFillPaintTransformer = n -> null;
  protected Function<? super E, Paint> edgeDrawPaintTransformer = n -> Color.black;
  protected Function<? super E, Paint> arrowFillPaintTransformer = n -> Color.black;
  protected Function<? super E, Paint> arrowDrawPaintTransformer = n -> Color.black;

  protected EdgeIndexFunction<E> parallelEdgeIndexFunction;

  protected MultiLayerTransformer multiLayerTransformer = new BasicTransformer();

  /** pluggable support for picking graph elements by finding them based on their coordinates. */
  protected NetworkElementAccessor<V, E> pickSupport;

  protected int labelOffset = LABEL_OFFSET;

  /** the JComponent that this Renderer will display the graph on */
  protected JComponent screenDevice;

  protected PickedState<V> pickedVertexState;
  protected PickedState<E> pickedEdgeState;

  /**
   * The CellRendererPane is used here just as it is in JTree and JTable, to allow a pluggable
   * JLabel-based renderer for Vertex and Edge label strings and icons.
   */
  protected CellRendererPane rendererPane = new CellRendererPane();

  /** A default GraphLabelRenderer - picked Vertex labels are blue, picked edge labels are cyan */
  protected VertexLabelRenderer vertexLabelRenderer = new DefaultVertexLabelRenderer(Color.blue);

  protected EdgeLabelRenderer edgeLabelRenderer = new DefaultEdgeLabelRenderer(Color.cyan);

  protected GraphicsDecorator graphicsContext;

  private EdgeShape<E> edgeShape;

  PluggableRenderContext(Network<V, E> graph) {
    this.network = graph;
    this.edgeShape = new EdgeShape<E>(graph);
    this.edgeShapeTransformer = edgeShape.new QuadCurve();
    this.parallelEdgeIndexFunction = new ParallelEdgeIndexFunction<V, E>(graph);
    if (graph.isDirected()) {
      this.edgeArrow =
          ArrowFactory.getNotchedArrow(EDGE_ARROW_WIDTH, EDGE_ARROW_LENGTH, EDGE_ARROW_NOTCH_DEPTH);
      this.renderEdgeArrow = true;
      this.edgeLabelCloseness = DIRECTED_EDGE_LABEL_CLOSENESS;
    } else {
      this.edgeArrow = ArrowFactory.getWedgeArrow(EDGE_ARROW_WIDTH, EDGE_ARROW_LENGTH);
      this.renderEdgeArrow = false;
      this.edgeLabelCloseness = UNDIRECTED_EDGE_LABEL_CLOSENESS;
    }
  }

  @Override
  public Network<V, E> getNetwork() {
    return network;
  }

  /** @return the vertexShapeTransformer */
  public Function<? super V, Shape> getVertexShapeTransformer() {
    return vertexShapeTransformer;
  }

  /** @param vertexShapeTransformer the vertexShapeTransformer to set */
  public void setVertexShapeTransformer(Function<? super V, Shape> vertexShapeTransformer) {
    this.vertexShapeTransformer = vertexShapeTransformer;
  }

  /** @return the vertexStrokeTransformer */
  public Function<? super V, Stroke> getVertexStrokeTransformer() {
    return vertexStrokeTransformer;
  }

  /** @param vertexStrokeTransformer the vertexStrokeTransformer to set */
  public void setVertexStrokeTransformer(Function<? super V, Stroke> vertexStrokeTransformer) {
    this.vertexStrokeTransformer = vertexStrokeTransformer;
  }

  public static float[] getDashing() {
    return dashing;
  }

  public static float[] getDotting() {
    return dotting;
  }

  public float getArrowPlacementTolerance() {
    return arrowPlacementTolerance;
  }

  public void setArrowPlacementTolerance(float arrow_placement_tolerance) {
    this.arrowPlacementTolerance = arrow_placement_tolerance;
  }

  public Shape getEdgeArrow() {
    return edgeArrow;
  }

  public void setEdgeArrow(Shape shape) {
    this.edgeArrow = shape;
  }

  public boolean renderEdgeArrow() {
    return this.renderEdgeArrow;
  }

  public void setRenderEdgeArrow(boolean render) {
    this.renderEdgeArrow = render;
  }

  public Function<? super E, Font> getEdgeFontTransformer() {
    return edgeFontTransformer;
  }

  public void setEdgeFontTransformer(Function<? super E, Font> edgeFontTransformer) {
    this.edgeFontTransformer = edgeFontTransformer;
  }

  public Predicate<E> getEdgeIncludePredicate() {
    return edgeIncludePredicate;
  }

  public void setEdgeIncludePredicate(Predicate<E> edgeIncludePredicate) {
    this.edgeIncludePredicate = edgeIncludePredicate;
  }

  public float getEdgeLabelCloseness() {
    return edgeLabelCloseness;
  }

  public void setEdgeLabelCloseness(float closeness) {
    this.edgeLabelCloseness = closeness;
  }

  public EdgeLabelRenderer getEdgeLabelRenderer() {
    return edgeLabelRenderer;
  }

  public void setEdgeLabelRenderer(EdgeLabelRenderer edgeLabelRenderer) {
    this.edgeLabelRenderer = edgeLabelRenderer;
  }

  public Function<? super E, Paint> getEdgeFillPaintTransformer() {
    return edgeFillPaintTransformer;
  }

  public void setEdgeDrawPaintTransformer(Function<? super E, Paint> edgeDrawPaintTransformer) {
    this.edgeDrawPaintTransformer = edgeDrawPaintTransformer;
  }

  public Function<? super E, Paint> getEdgeDrawPaintTransformer() {
    return edgeDrawPaintTransformer;
  }

  public void setEdgeFillPaintTransformer(Function<? super E, Paint> edgeFillPaintTransformer) {
    this.edgeFillPaintTransformer = edgeFillPaintTransformer;
  }

  public Function<? super E, Shape> getEdgeShapeTransformer() {
    return edgeShapeTransformer;
  }

  public void setEdgeShapeTransformer(Function<? super E, Shape> edgeShapeTransformer) {
    this.edgeShapeTransformer = edgeShapeTransformer;
    if (edgeShapeTransformer instanceof ParallelEdgeShapeTransformer) {
      @SuppressWarnings("unchecked")
      ParallelEdgeShapeTransformer<E> transformer =
          (ParallelEdgeShapeTransformer<E>) edgeShapeTransformer;
      transformer.setEdgeIndexFunction(this.parallelEdgeIndexFunction);
    }
  }

  public Function<? super E, String> getEdgeLabelTransformer() {
    return edgeLabelTransformer;
  }

  public void setEdgeLabelTransformer(Function<? super E, String> edgeLabelTransformer) {
    this.edgeLabelTransformer = edgeLabelTransformer;
  }

  public Function<? super E, Stroke> edgestrokeTransformer() {
    return edgeStrokeTransformer;
  }

  public void setEdgeStrokeTransformer(Function<? super E, Stroke> edgeStrokeTransformer) {
    this.edgeStrokeTransformer = edgeStrokeTransformer;
  }

  public Function<? super E, Stroke> getEdgeArrowStrokeTransformer() {
    return edgeArrowStrokeTransformer;
  }

  public void setEdgeArrowStrokeTransformer(
      Function<? super E, Stroke> edgeArrowStrokeTransformer) {
    this.edgeArrowStrokeTransformer = edgeArrowStrokeTransformer;
  }

  public GraphicsDecorator getGraphicsContext() {
    return graphicsContext;
  }

  public void setGraphicsContext(GraphicsDecorator graphicsContext) {
    this.graphicsContext = graphicsContext;
  }

  public int getLabelOffset() {
    return labelOffset;
  }

  public void setLabelOffset(int labelOffset) {
    this.labelOffset = labelOffset;
  }

  public EdgeIndexFunction<E> getParallelEdgeIndexFunction() {
    return parallelEdgeIndexFunction;
  }

  public void setParallelEdgeIndexFunction(EdgeIndexFunction<E> parallelEdgeIndexFunction) {
    this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
    // reset the edge shape Function, as the parallel edge index function
    // is used by it
    this.setEdgeShapeTransformer(getEdgeShapeTransformer());
  }

  public PickedState<E> getPickedEdgeState() {
    return pickedEdgeState;
  }

  public void setPickedEdgeState(PickedState<E> pickedEdgeState) {
    this.pickedEdgeState = pickedEdgeState;
  }

  public PickedState<V> getPickedVertexState() {
    return pickedVertexState;
  }

  public void setPickedVertexState(PickedState<V> pickedVertexState) {
    this.pickedVertexState = pickedVertexState;
  }

  public CellRendererPane getRendererPane() {
    return rendererPane;
  }

  public void setRendererPane(CellRendererPane rendererPane) {
    this.rendererPane = rendererPane;
  }

  public JComponent getScreenDevice() {
    return screenDevice;
  }

  public void setScreenDevice(JComponent screenDevice) {
    this.screenDevice = screenDevice;
    screenDevice.add(rendererPane);
  }

  public Function<? super V, Font> getVertexFontTransformer() {
    return vertexFontTransformer;
  }

  public void setVertexFontTransformer(Function<? super V, Font> vertexFontTransformer) {
    this.vertexFontTransformer = vertexFontTransformer;
  }

  public Function<V, Icon> getVertexIconTransformer() {
    return vertexIconTransformer;
  }

  public void setVertexIconTransformer(Function<V, Icon> vertexIconTransformer) {
    this.vertexIconTransformer = vertexIconTransformer;
  }

  public Predicate<V> getVertexIncludePredicate() {
    return vertexIncludePredicate;
  }

  public void setVertexIncludePredicate(Predicate<V> vertexIncludePredicate) {
    this.vertexIncludePredicate = vertexIncludePredicate;
  }

  public VertexLabelRenderer getVertexLabelRenderer() {
    return vertexLabelRenderer;
  }

  public void setVertexLabelRenderer(VertexLabelRenderer vertexLabelRenderer) {
    this.vertexLabelRenderer = vertexLabelRenderer;
  }

  public Function<? super V, Paint> getVertexFillPaintTransformer() {
    return vertexFillPaintTransformer;
  }

  public void setVertexFillPaintTransformer(Function<? super V, Paint> vertexFillPaintTransformer) {
    this.vertexFillPaintTransformer = vertexFillPaintTransformer;
  }

  public Function<? super V, Paint> getVertexDrawPaintTransformer() {
    return vertexDrawPaintTransformer;
  }

  public void setVertexDrawPaintTransformer(Function<? super V, Paint> vertexDrawPaintTransformer) {
    this.vertexDrawPaintTransformer = vertexDrawPaintTransformer;
  }

  public Function<? super V, String> getVertexLabelTransformer() {
    return vertexLabelTransformer;
  }

  public void setVertexLabelTransformer(Function<? super V, String> vertexLabelTransformer) {
    this.vertexLabelTransformer = vertexLabelTransformer;
  }

  public NetworkElementAccessor<V, E> getPickSupport() {
    return pickSupport;
  }

  public void setPickSupport(NetworkElementAccessor<V, E> pickSupport) {
    this.pickSupport = pickSupport;
  }

  public MultiLayerTransformer getMultiLayerTransformer() {
    return multiLayerTransformer;
  }

  public void setMultiLayerTransformer(MultiLayerTransformer basicTransformer) {
    this.multiLayerTransformer = basicTransformer;
  }

  public Function<? super E, Paint> getArrowDrawPaintTransformer() {
    return arrowDrawPaintTransformer;
  }

  public Function<? super E, Paint> getArrowFillPaintTransformer() {
    return arrowFillPaintTransformer;
  }

  public void setArrowDrawPaintTransformer(Function<? super E, Paint> arrowDrawPaintTransformer) {
    this.arrowDrawPaintTransformer = arrowDrawPaintTransformer;
  }

  public void setArrowFillPaintTransformer(Function<? super E, Paint> arrowFillPaintTransformer) {
    this.arrowFillPaintTransformer = arrowFillPaintTransformer;
  }
}
