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
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A demo that shows drawn Icons as vertices
 *
 * @author Tom Nelson
 */
public class DrawnIconVertexDemo {

  /** the graph */
  Network<Integer, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Number> vv;

  public DrawnIconVertexDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    vv = new VisualizationViewer<Integer, Number>(graph, new FRLayout<Integer>(graph.asGraph()));
    vv.getRenderContext()
        .setVertexLabelTransformer(
            new Function<Integer, String>() {

              public String apply(Integer v) {
                return "Vertex " + v;
              }
            });
    vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

    vv.getRenderContext()
        .setVertexIconTransformer(
            new Function<Integer, Icon>() {

              /*
               * Implements the Icon interface to draw an Icon with background color and
               * a text label
               */
              public Icon apply(final Integer v) {
                return new Icon() {

                  public int getIconHeight() {
                    return 20;
                  }

                  public int getIconWidth() {
                    return 20;
                  }

                  public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (vv.getPickedVertexState().isPicked(v)) {
                      g.setColor(Color.yellow);
                    } else {
                      g.setColor(Color.red);
                    }
                    g.fillOval(x, y, 20, 20);
                    if (vv.getPickedVertexState().isPicked(v)) {
                      g.setColor(Color.black);
                    } else {
                      g.setColor(Color.white);
                    }
                    g.drawString("" + v, x + 6, y + 15);
                  }
                };
              }
            });

    vv.getRenderContext()
        .setVertexFillPaintTransformer(
            new PickableVertexPaintTransformer<Integer>(
                vv.getPickedVertexState(), Color.white, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<Number>(
                vv.getPickedEdgeState(), Color.black, Color.lightGray));

    vv.setBackground(Color.white);

    // add my listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    final DefaultModalGraphMouse<Integer, Number> gm =
        new DefaultModalGraphMouse<Integer, Number>();
    vv.setGraphMouse(gm);

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

    JPanel controls = new JPanel();
    controls.add(plus);
    controls.add(minus);
    controls.add(gm.getModeComboBox());
    content.add(controls, BorderLayout.SOUTH);

    frame.pack();
    frame.setVisible(true);
  }

  Network<Integer, Number> createGraph() {
    MutableNetwork<Integer, Number> graph = NetworkBuilder.directed().build();
    graph.addEdge(0, 1, new Double(Math.random()));
    graph.addEdge(3, 0, new Double(Math.random()));
    graph.addEdge(0, 4, new Double(Math.random()));
    graph.addEdge(4, 5, new Double(Math.random()));
    graph.addEdge(5, 3, new Double(Math.random()));
    graph.addEdge(2, 1, new Double(Math.random()));
    graph.addEdge(4, 1, new Double(Math.random()));
    graph.addEdge(8, 2, new Double(Math.random()));
    graph.addEdge(3, 8, new Double(Math.random()));
    graph.addEdge(6, 7, new Double(Math.random()));
    graph.addEdge(7, 5, new Double(Math.random()));
    graph.addEdge(0, 9, new Double(Math.random()));
    graph.addEdge(9, 8, new Double(Math.random()));
    graph.addEdge(7, 6, new Double(Math.random()));
    graph.addEdge(6, 5, new Double(Math.random()));
    graph.addEdge(4, 2, new Double(Math.random()));
    graph.addEdge(5, 4, new Double(Math.random()));
    graph.addEdge(4, 10, new Double(Math.random()));
    graph.addEdge(10, 4, new Double(Math.random()));

    return graph;
  }

  public static void main(String[] args) {
    new DrawnIconVertexDemo();
  }
}
