/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeFunction;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.NodeLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.ArrowFactory;
import edu.uci.ics.jung.visualization.util.Context;
import edu.uci.ics.jung.visualization.util.EdgeIndexFunction;
import edu.uci.ics.jung.visualization.util.ParallelEdgeIndexFunction;
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

public class PluggableRenderContext<N, E> implements RenderContext<N, E> {

  protected float arrowPlacementTolerance = 1;
  protected Predicate<N> nodeIncludePredicate = n -> true;
  protected Function<? super N, Stroke> nodeStrokeFunction = n -> new BasicStroke(1.0f);

  protected Function<? super N, Shape> nodeShapeFunction =
      n -> new Ellipse2D.Float(-10, -10, 20, 20);

  protected Function<? super N, String> nodeLabelFunction = n -> null;
  protected Function<N, Icon> nodeIconFunction;
  protected Function<? super N, Font> nodeFontFunction = n -> new Font("Helvetica", Font.PLAIN, 12);

  protected Function<? super N, Paint> nodeDrawPaintFunction = n -> Color.BLACK;
  protected Function<? super N, Paint> nodeFillPaintFunction = n -> Color.RED;
  protected Function<? super N, Paint> nodeLabelDrawPaintFunction = n -> Color.BLACK;

  protected Function<? super E, String> edgeLabelFunction = e -> null;
  protected Function<? super E, Stroke> edgeStrokeFunction = e -> new BasicStroke(1.0f);
  protected Function<? super E, Stroke> edgeArrowStrokeFunction = e -> new BasicStroke(1.0f);

  private static final int EDGE_ARROW_LENGTH = 10;
  private static final int EDGE_ARROW_WIDTH = 8;
  private static final int EDGE_ARROW_NOTCH_DEPTH = 4;
  protected Shape edgeArrow;
  protected boolean renderEdgeArrow;

  protected Predicate<E> edgeIncludePredicate = n -> true;
  protected Function<? super E, Font> edgeFontFunction = n -> new Font("Helvetica", Font.PLAIN, 12);

  private static final float DIRECTED_EDGE_LABEL_CLOSENESS = 0.65f;
  private static final float UNDIRECTED_EDGE_LABEL_CLOSENESS = 0.65f;
  protected float edgeLabelCloseness;

  protected Function<Context<Network<N, E>, E>, Shape> edgeShapeFunction;
  protected Function<? super E, Paint> edgeFillPaintFunction = n -> null;
  protected Function<? super E, Paint> edgeDrawPaintFunction = n -> Color.black;
  protected Function<? super E, Paint> arrowFillPaintFunction = n -> Color.black;
  protected Function<? super E, Paint> arrowDrawPaintFunction = n -> Color.black;

  protected EdgeIndexFunction<N, E> parallelEdgeIndexFunction;

  protected MultiLayerTransformer multiLayerTransformer = new BasicTransformer();

  /** pluggable support for picking graph elements by finding them based on their coordinates. */
  protected NetworkElementAccessor<N, E> pickSupport;

  protected int labelOffset = LABEL_OFFSET;

  /** the JComponent that this Renderer will display the graph on */
  protected JComponent screenDevice;

  protected PickedState<N> pickedNodeState;
  protected PickedState<E> pickedEdgeState;

  /**
   * The CellRendererPane is used here just as it is in JTree and JTable, to allow a pluggable
   * JLabel-based renderer for Node and Edge label strings and icons.
   */
  protected CellRendererPane rendererPane = new CellRendererPane();

  /** A default GraphLabelRenderer - picked Node labels are blue, picked edge labels are cyan */
  protected NodeLabelRenderer nodeLabelRenderer = new DefaultNodeLabelRenderer(Color.blue);

  protected EdgeLabelRenderer edgeLabelRenderer = new DefaultEdgeLabelRenderer(Color.cyan);

  protected GraphicsDecorator graphicsContext;

  private EdgeShape<E> edgeShape;

  PluggableRenderContext(Network<N, E> graph) {
    this.edgeShapeFunction = new EdgeShape.QuadCurve<N, E>();
    this.parallelEdgeIndexFunction = new ParallelEdgeIndexFunction<>();
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

  /**
   * @return the nodeShapeFunction
   */
  public Function<? super N, Shape> getNodeShapeFunction() {
    return nodeShapeFunction;
  }

  /**
   * @param nodeShapeFunction the nodeShapeFunction to set
   */
  public void setNodeShapeFunction(Function<? super N, Shape> nodeShapeFunction) {
    this.nodeShapeFunction = nodeShapeFunction;
  }

  /**
   * @return the nodeStrokeFunction
   */
  public Function<? super N, Stroke> getNodeStrokeFunction() {
    return nodeStrokeFunction;
  }

  /**
   * @param nodeStrokeFunction the nodeStrokeFunction to set
   */
  public void setNodeStrokeFunction(Function<? super N, Stroke> nodeStrokeFunction) {
    this.nodeStrokeFunction = nodeStrokeFunction;
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

  public Function<? super E, Font> getEdgeFontFunction() {
    return edgeFontFunction;
  }

  public void setEdgeFontFunction(Function<? super E, Font> edgeFontFunction) {
    this.edgeFontFunction = edgeFontFunction;
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

  public Function<? super E, Paint> getEdgeFillPaintFunction() {
    return edgeFillPaintFunction;
  }

  public void setEdgeDrawPaintFunction(Function<? super E, Paint> edgeDrawPaintFunction) {
    this.edgeDrawPaintFunction = edgeDrawPaintFunction;
  }

  public Function<? super E, Paint> getEdgeDrawPaintFunction() {
    return edgeDrawPaintFunction;
  }

  public void setEdgeFillPaintFunction(Function<? super E, Paint> edgeFillPaintFunction) {
    this.edgeFillPaintFunction = edgeFillPaintFunction;
  }

  public Function<Context<Network<N, E>, E>, Shape> getEdgeShapeFunction() {
    return edgeShapeFunction;
  }

  public void setEdgeShapeFunction(Function<Context<Network<N, E>, E>, Shape> edgeShapeFunction) {
    this.edgeShapeFunction = edgeShapeFunction;
    if (edgeShapeFunction instanceof ParallelEdgeShapeFunction) {
      @SuppressWarnings("unchecked")
      ParallelEdgeShapeFunction<N, E> function =
          (ParallelEdgeShapeFunction<N, E>) edgeShapeFunction;
      function.setEdgeIndexFunction(this.parallelEdgeIndexFunction);
    }
  }

  public Function<? super E, String> getEdgeLabelFunction() {
    return edgeLabelFunction;
  }

  public void setEdgeLabelFunction(Function<? super E, String> edgeLabelFunction) {
    this.edgeLabelFunction = edgeLabelFunction;
  }

  public Function<? super E, Stroke> edgeStrokeFunction() {
    return edgeStrokeFunction;
  }

  public void setEdgeStrokeFunction(Function<? super E, Stroke> edgeStrokeFunction) {
    this.edgeStrokeFunction = edgeStrokeFunction;
  }

  public Function<? super E, Stroke> getEdgeArrowStrokeFunction() {
    return edgeArrowStrokeFunction;
  }

  public void setEdgeArrowStrokeFunction(Function<? super E, Stroke> edgeArrowStrokeFunction) {
    this.edgeArrowStrokeFunction = edgeArrowStrokeFunction;
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

  public EdgeIndexFunction<N, E> getParallelEdgeIndexFunction() {
    return parallelEdgeIndexFunction;
  }

  public void setParallelEdgeIndexFunction(EdgeIndexFunction<N, E> parallelEdgeIndexFunction) {
    this.parallelEdgeIndexFunction = parallelEdgeIndexFunction;
    // reset the edge shape Function, as the parallel edge index function
    // is used by it
    this.setEdgeShapeFunction(getEdgeShapeFunction());
  }

  public PickedState<E> getPickedEdgeState() {
    return pickedEdgeState;
  }

  public void setPickedEdgeState(PickedState<E> pickedEdgeState) {
    this.pickedEdgeState = pickedEdgeState;
  }

  public PickedState<N> getPickedNodeState() {
    return pickedNodeState;
  }

  public void setPickedNodeState(PickedState<N> pickedNodeState) {
    this.pickedNodeState = pickedNodeState;
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

  public Function<? super N, Font> getNodeFontFunction() {
    return nodeFontFunction;
  }

  public void setNodeFontFunction(Function<? super N, Font> nodeFontFunction) {
    this.nodeFontFunction = nodeFontFunction;
  }

  public Function<N, Icon> getNodeIconFunction() {
    return nodeIconFunction;
  }

  public void setNodeIconFunction(Function<N, Icon> nodeIconFunction) {
    this.nodeIconFunction = nodeIconFunction;
  }

  public Predicate<N> getNodeIncludePredicate() {
    return nodeIncludePredicate;
  }

  public void setNodeIncludePredicate(Predicate<N> nodeIncludePredicate) {
    this.nodeIncludePredicate = nodeIncludePredicate;
  }

  public NodeLabelRenderer getNodeLabelRenderer() {
    return nodeLabelRenderer;
  }

  public void setNodeLabelRenderer(NodeLabelRenderer nodeLabelRenderer) {
    this.nodeLabelRenderer = nodeLabelRenderer;
  }

  public Function<? super N, Paint> getNodeFillPaintFunction() {
    return nodeFillPaintFunction;
  }

  public void setNodeFillPaintFunction(Function<? super N, Paint> nodeFillPaintFunction) {
    this.nodeFillPaintFunction = nodeFillPaintFunction;
  }

  public Function<? super N, Paint> getNodeDrawPaintFunction() {
    return nodeDrawPaintFunction;
  }

  public void setNodeDrawPaintFunction(Function<? super N, Paint> nodeDrawPaintFunction) {
    this.nodeDrawPaintFunction = nodeDrawPaintFunction;
  }

  public Function<? super N, String> getNodeLabelFunction() {
    return nodeLabelFunction;
  }

  public void setNodeLabelFunction(Function<? super N, String> nodeLabelFunction) {
    this.nodeLabelFunction = nodeLabelFunction;
  }

  public void setNodeLabelDrawPaintFunction(Function<? super N, Paint> nodeLabelDrawPaintFunction) {
    this.nodeLabelDrawPaintFunction = nodeLabelDrawPaintFunction;
  }

  public Function<? super N, Paint> getNodeLabelDrawPaintFunction() {
    return nodeLabelDrawPaintFunction;
  }

  public NetworkElementAccessor<N, E> getPickSupport() {
    return pickSupport;
  }

  public void setPickSupport(NetworkElementAccessor<N, E> pickSupport) {
    this.pickSupport = pickSupport;
  }

  public MultiLayerTransformer getMultiLayerTransformer() {
    return multiLayerTransformer;
  }

  public void setMultiLayerTransformer(MultiLayerTransformer basicTransformer) {
    this.multiLayerTransformer = basicTransformer;
  }

  public Function<? super E, Paint> getArrowDrawPaintFunction() {
    return arrowDrawPaintFunction;
  }

  public Function<? super E, Paint> getArrowFillPaintFunction() {
    return arrowFillPaintFunction;
  }

  public void setArrowDrawPaintFunction(Function<? super E, Paint> arrowDrawPaintFunction) {
    this.arrowDrawPaintFunction = arrowDrawPaintFunction;
  }

  public void setArrowFillPaintFunction(Function<? super E, Paint> arrowFillPaintFunction) {
    this.arrowFillPaintFunction = arrowFillPaintFunction;
  }
}
