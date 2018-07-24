/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ParallelEdgeShapeFunction;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.NodeLabelRenderer;
import edu.uci.ics.jung.visualization.util.Context;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.function.Function;
import javax.swing.*;

/**
 * Demonstrates jung support for drawing edge labels that can be positioned at any point along the
 * edge, and can be rotated to be parallel with the edge.
 *
 * @author Tom Nelson
 */
public class EdgeLabelDemo extends JPanel {
  private static final long serialVersionUID = -6077157664507049647L;

  /** the graph */
  Network<Integer, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Number> vv;

  /** */
  NodeLabelRenderer nodeLabelRenderer;

  EdgeLabelRenderer edgeLabelRenderer;

  ScalingControl scaler = new CrossoverScalingControl();

  /** create an instance of a simple graph with controls to demo the label positioning features */
  @SuppressWarnings("serial")
  public EdgeLabelDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = buildGraph();

    LayoutAlgorithm<Integer> layoutAlgorithm = new CircleLayoutAlgorithm<>();
    vv = new VisualizationViewer<>(graph, layoutAlgorithm, new Dimension(600, 400));
    vv.setBackground(Color.white);

    nodeLabelRenderer = vv.getRenderContext().getNodeLabelRenderer();
    edgeLabelRenderer = vv.getRenderContext().getEdgeLabelRenderer();

    Function<Number, String> stringer = e -> "Edge:" + graph.incidentNodes(e).toString();

    vv.getRenderContext().setEdgeLabelFunction(stringer);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            v -> vv.getPickedEdgeState().isPicked(v) ? Color.cyan : Color.black);
    vv.getRenderContext()
        .setNodeFillPaintFunction(
            v -> vv.getPickedNodeState().isPicked(v) ? Color.yellow : Color.red);

    // add my listener for ToolTips
    vv.setNodeToolTipFunction(o -> o + " " + vv.getModel().getLayoutModel().apply(o));

    // create a frome to hold the graph
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<Integer, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    ButtonGroup radio = new ButtonGroup();
    JRadioButton lineButton = new JRadioButton("Line");
    lineButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
            vv.repaint();
          }
        });

    JRadioButton quadButton = new JRadioButton("QuadCurve");
    quadButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.quadCurve());
            vv.repaint();
          }
        });

    JRadioButton cubicButton = new JRadioButton("CubicCurve");
    cubicButton.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            vv.getRenderContext().setEdgeShapeFunction(EdgeShape.cubicCurve());
            vv.repaint();
          }
        });

    radio.add(lineButton);
    radio.add(quadButton);
    radio.add(cubicButton);

    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    JCheckBox rotate = new JCheckBox("<html><center>EdgeType<p>Parallel</center></html>");
    rotate.addItemListener(
        e -> {
          AbstractButton b = (AbstractButton) e.getSource();
          edgeLabelRenderer.setRotateEdgeLabels(b.isSelected());
          vv.repaint();
        });

    rotate.setSelected(true);
    EdgeClosenessUpdater edgeClosenessUpdater = new EdgeClosenessUpdater();
    JSlider closenessSlider =
        new JSlider(edgeClosenessUpdater.rangeModel) {
          public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width /= 2;
            return d;
          }
        };

    JSlider edgeOffsetSlider =
        new JSlider(0, 50) {
          public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width /= 2;
            return d;
          }
        };
    edgeOffsetSlider.addChangeListener(
        e -> {
          JSlider s = (JSlider) e.getSource();
          Function<Context<Network<Integer, Number>, Number>, Shape> edgeShapeFunction =
              vv.getRenderContext().getEdgeShapeFunction();
          if (edgeShapeFunction instanceof ParallelEdgeShapeFunction) {
            ((ParallelEdgeShapeFunction) edgeShapeFunction).setControlOffsetIncrement(s.getValue());
            vv.repaint();
          }
        });

    Box controls = Box.createHorizontalBox();

    JPanel zoomPanel = new JPanel(new GridLayout(0, 1));
    zoomPanel.setBorder(BorderFactory.createTitledBorder("Scale"));
    zoomPanel.add(plus);
    zoomPanel.add(minus);

    JPanel edgePanel = new JPanel(new GridLayout(0, 1));
    edgePanel.setBorder(BorderFactory.createTitledBorder("Edge Shape"));
    edgePanel.add(lineButton);
    edgePanel.add(quadButton);
    edgePanel.add(cubicButton);

    JPanel rotatePanel = new JPanel();
    rotatePanel.setBorder(BorderFactory.createTitledBorder("Alignment"));
    rotatePanel.add(rotate);

    JPanel labelPanel = new JPanel(new BorderLayout());
    JPanel sliderPanel = new JPanel(new GridLayout(3, 1));
    JPanel sliderLabelPanel = new JPanel(new GridLayout(3, 1));
    JPanel offsetPanel = new JPanel(new BorderLayout());
    offsetPanel.setBorder(BorderFactory.createTitledBorder("Offset"));
    sliderPanel.add(closenessSlider);
    sliderPanel.add(edgeOffsetSlider);
    sliderLabelPanel.add(new JLabel("Closeness", JLabel.RIGHT));
    sliderLabelPanel.add(new JLabel("Edges", JLabel.RIGHT));
    offsetPanel.add(sliderLabelPanel, BorderLayout.WEST);
    offsetPanel.add(sliderPanel);
    labelPanel.add(offsetPanel);
    labelPanel.add(rotatePanel, BorderLayout.WEST);

    JPanel modePanel = new JPanel(new GridLayout(2, 1));
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(graphMouse.getModeComboBox());

    controls.add(zoomPanel);
    controls.add(edgePanel);
    controls.add(labelPanel);
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);
    quadButton.setSelected(true);
  }

  /**
   * subclassed to hold two BoundedRangeModel instances that are used by JSliders to move the edge
   * label positions
   *
   * @author Tom Nelson
   */
  class EdgeClosenessUpdater {
    BoundedRangeModel rangeModel;

    public EdgeClosenessUpdater() { // double undirected, double directed) {
      int initialValue = ((int) vv.getRenderContext().getEdgeLabelCloseness() * 10) / 10;
      this.rangeModel = new DefaultBoundedRangeModel(initialValue, 0, 0, 10);

      rangeModel.addChangeListener(
          e -> {
            vv.getRenderContext().setEdgeLabelCloseness(rangeModel.getValue() / 10f);
            vv.repaint();
          });
    }
  }

  Network<Integer, Number> buildGraph() {
    MutableNetwork<Integer, Number> graph =
        NetworkBuilder.directed().allowsParallelEdges(true).build();

    graph.addEdge(0, 1, Math.random());
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(1, 0, Math.random());
    graph.addEdge(1, 0, Math.random());
    graph.addEdge(1, 2, Math.random());
    graph.addEdge(1, 2, Math.random());

    return graph;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    Container content = frame.getContentPane();
    content.add(new EdgeLabelDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
