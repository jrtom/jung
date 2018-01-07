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
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer;
import java.awt.*;
import java.net.URL;
import java.util.function.Function;
import javax.swing.*;

/**
 * Demonstrates the use of images on graph edge labels.
 *
 * @author Tom Nelson
 */
public class ImageEdgeLabelDemo extends JPanel {

  /** */
  private static final long serialVersionUID = -4332663871914930864L;

  private static final int NODE_COUNT = 11;

  /** the graph */
  Network<Number, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  public ImageEdgeLabelDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createGraph(NODE_COUNT);

    FRLayoutAlgorithm<Number> layoutAlgorithm = new FRLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(100);
    vv = new VisualizationViewer<>(graph, layoutAlgorithm, new Dimension(400, 400));

    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(vv.getPickedEdgeState(), Color.black, Color.cyan));

    vv.setBackground(Color.white);

    vv.getRenderContext().setNodeLabelRenderer(new DefaultNodeLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
    vv.getRenderContext()
        .setEdgeLabelFunction(
            new Function<Number, String>() {
              URL url = getClass().getResource("/images/lightning-s.gif");

              public String apply(Number input) {
                return "<html><img src=" + url + " height=10 width=21>";
              }
            });

    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);
    vv.setEdgeToolTipFunction(Object::toString);
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<Number, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));
    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    controls.add(scaleGrid);
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);
  }

  /**
   * create some nodes
   *
   * @param nodeCount how many to create
   * @return the Nodes in an array
   */
  private Network<Number, Number> createGraph(int nodeCount) {
    MutableNetwork<Number, Number> graph = NetworkBuilder.directed().build();
    for (int i = 0; i < nodeCount; i++) {
      graph.addNode(i);
    }
    int j = 0;
    graph.addEdge(0, 1, j++);
    graph.addEdge(3, 0, j++);
    graph.addEdge(0, 4, j++);
    graph.addEdge(4, 5, j++);
    graph.addEdge(5, 3, j++);
    graph.addEdge(2, 1, j++);
    graph.addEdge(4, 1, j++);
    graph.addEdge(8, 2, j++);
    graph.addEdge(3, 8, j++);
    graph.addEdge(6, 7, j++);
    graph.addEdge(7, 5, j++);
    graph.addEdge(0, 9, j++);
    graph.addEdge(9, 8, j++);
    graph.addEdge(7, 6, j++);
    graph.addEdge(6, 5, j++);
    graph.addEdge(4, 2, j++);
    graph.addEdge(5, 4, j++);
    graph.addEdge(4, 10, j++);
    graph.addEdge(10, 4, j++);

    return graph;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new ImageEdgeLabelDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
