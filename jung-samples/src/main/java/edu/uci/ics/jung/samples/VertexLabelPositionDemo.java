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
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.*;

/**
 * Demonstrates vertex label positioning controlled by the user. In the AUTO setting, labels are
 * placed according to which quadrant the vertex is in
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class VertexLabelPositionDemo extends JApplet {

  /** the graph */
  Network<String, Number> graph;

  FRLayoutAlgorithm<String, Point2D> graphLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  ScalingControl scaler;

  /** create an instance of a simple graph with controls to demo the zoomand hyperbolic features. */
  public VertexLabelPositionDemo() {

    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    graphLayoutAlgorithm = new FRLayoutAlgorithm<>();
    graphLayoutAlgorithm.setMaxIterations(1000);

    Dimension preferredSize = new Dimension(600, 600);

    final VisualizationModel<String, Number, Point2D> visualizationModel =
        new BaseVisualizationModel<>(graph, graphLayoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<>(visualizationModel, preferredSize);

    PickedState<String> ps = vv.getPickedVertexState();
    PickedState<Number> pes = vv.getPickedEdgeState();
    vv.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<>(ps, Color.red, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(pes, Color.black, Color.cyan));
    vv.setBackground(Color.white);
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.W);

    vv.getRenderContext().setVertexLabelTransformer(n -> n);

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(n -> n);

    Container content = getContentPane();
    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    content.add(gzsp);

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
    cb.addItem(Renderer.VertexLabel.Position.N);
    cb.addItem(Renderer.VertexLabel.Position.NE);
    cb.addItem(Renderer.VertexLabel.Position.E);
    cb.addItem(Renderer.VertexLabel.Position.SE);
    cb.addItem(Renderer.VertexLabel.Position.S);
    cb.addItem(Renderer.VertexLabel.Position.SW);
    cb.addItem(Renderer.VertexLabel.Position.W);
    cb.addItem(Renderer.VertexLabel.Position.NW);
    cb.addItem(Renderer.VertexLabel.Position.N);
    cb.addItem(Renderer.VertexLabel.Position.CNTR);
    cb.addItem(Renderer.VertexLabel.Position.AUTO);
    cb.addItemListener(
        e -> {
          Renderer.VertexLabel.Position position = (Renderer.VertexLabel.Position) e.getItem();
          vv.getRenderer().getVertexLabelRenderer().setPosition(position);
          vv.repaint();
        });

    cb.setSelectedItem(Renderer.VertexLabel.Position.SE);
    positionPanel.add(cb);
    JPanel controls = new JPanel();
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
    zoomControls.add(plus);
    zoomControls.add(minus);

    controls.add(zoomControls);
    controls.add(positionPanel);
    content.add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new VertexLabelPositionDemo());
    f.pack();
    f.setVisible(true);
  }
}
