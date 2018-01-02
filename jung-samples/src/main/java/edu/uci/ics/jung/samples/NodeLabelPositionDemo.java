/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.NodeLabel.Position;
import java.awt.*;
import javax.swing.*;

/**
 * Demonstrates node label positioning controlled by the user. In the AUTO setting, labels are
 * placed according to which quadrant the node is in
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class NodeLabelPositionDemo extends JPanel {

  /** the graph */
  Network<String, Number> graph;

  FRLayoutAlgorithm<String> graphLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  ScalingControl scaler;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public NodeLabelPositionDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    graphLayoutAlgorithm = new FRLayoutAlgorithm<>();
    graphLayoutAlgorithm.setMaxIterations(1000);

    Dimension preferredSize = new Dimension(600, 600);

    final VisualizationModel<String, Number> visualizationModel =
        new BaseVisualizationModel<>(graph, graphLayoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<>(visualizationModel, preferredSize);

    PickedState<String> ps = vv.getPickedNodeState();
    PickedState<Number> pes = vv.getPickedEdgeState();
    vv.getRenderContext()
        .setNodeFillPaintFunction(new PickableNodePaintFunction<>(ps, Color.red, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(new PickableEdgePaintFunction<>(pes, Color.black, Color.cyan));
    vv.setBackground(Color.white);
    vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.W);

    vv.getRenderContext().setNodeLabelFunction(n -> n);

    // add a listener for ToolTips
    vv.setNodeToolTipFunction(n -> n);

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    add(gzsp);

    // the regular graph mouse for the normal view
    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel positionPanel = new JPanel();
    positionPanel.setBorder(BorderFactory.createTitledBorder("Label Position"));
    JMenuBar menubar = new JMenuBar();
    menubar.add(graphMouse.getModeMenu());
    gzsp.setCorner(menubar);
    JComboBox<Position> cb = new JComboBox<>();
    cb.addItem(Renderer.NodeLabel.Position.N);
    cb.addItem(Renderer.NodeLabel.Position.NE);
    cb.addItem(Renderer.NodeLabel.Position.E);
    cb.addItem(Renderer.NodeLabel.Position.SE);
    cb.addItem(Renderer.NodeLabel.Position.S);
    cb.addItem(Renderer.NodeLabel.Position.SW);
    cb.addItem(Renderer.NodeLabel.Position.W);
    cb.addItem(Renderer.NodeLabel.Position.NW);
    cb.addItem(Renderer.NodeLabel.Position.N);
    cb.addItem(Renderer.NodeLabel.Position.CNTR);
    cb.addItem(Renderer.NodeLabel.Position.AUTO);
    cb.addItemListener(
        e -> {
          Renderer.NodeLabel.Position position = (Renderer.NodeLabel.Position) e.getItem();
          vv.getRenderer().getNodeLabelRenderer().setPosition(position);
          vv.repaint();
        });

    cb.setSelectedItem(Renderer.NodeLabel.Position.SE);
    positionPanel.add(cb);
    JPanel controls = new JPanel();
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
    zoomControls.add(plus);
    zoomControls.add(minus);

    controls.add(zoomControls);
    controls.add(positionPanel);
    add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new NodeLabelPositionDemo());
    f.pack();
    f.setVisible(true);
  }
}
