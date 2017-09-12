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
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Demonstrates the use of images on graph edge labels.
 *
 * @author Tom Nelson
 */
public class ImageEdgeLabelDemo extends JApplet {

  /** */
  private static final long serialVersionUID = -4332663871914930864L;

  private static final int VERTEX_COUNT = 11;

  /** the graph */
  Network<Number, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  public ImageEdgeLabelDemo() {

    // create a simple graph for the demo
    graph = createGraph(VERTEX_COUNT);

    FRLayout<Number> layout = new FRLayout<Number>(graph.asGraph());
    layout.setMaxIterations(100);
    vv = new VisualizationViewer<Number, Number>(graph, layout, new Dimension(400, 400));

    vv.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<Number>(
                vv.getPickedEdgeState(), Color.black, Color.cyan));

    vv.setBackground(Color.white);

    vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));
    vv.getRenderContext()
        .setEdgeLabelTransformer(
            new Function<Number, String>() {
              URL url = getClass().getResource("/images/lightning-s.gif");

              public String apply(Number input) {
                return "<html><img src=" + url + " height=10 width=21>";
              }
            });

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());
    vv.setEdgeToolTipTransformer(new ToStringLabeller());
    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse<Number, Number> graphMouse =
        new DefaultModalGraphMouse<Number, Number>();
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            scaler.scale(vv, 1.1f, vv.getCenter());
          }
        });
    JButton minus = new JButton("-");
    minus.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            scaler.scale(vv, 1 / 1.1f, vv.getCenter());
          }
        });

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
    content.add(controls, BorderLayout.SOUTH);
  }

  /**
   * create some vertices
   *
   * @param count how many to create
   * @return the Vertices in an array
   */
  private Network<Number, Number> createGraph(int vertexCount) {
    MutableNetwork<Number, Number> graph = NetworkBuilder.directed().build();
    for (int i = 0; i < vertexCount; i++) {
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
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    content.add(new ImageEdgeLabelDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
