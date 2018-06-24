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
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.renderers.BasicNodeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientNodeRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.*;

/**
 * Demonstrates VisualizationImageServer.
 *
 * @author Tom Nelson
 */
public class VisualizationImageServerDemo {

  /** the graph */
  Network<Integer, Double> graph;

  /** the visual component and renderer for the graph */
  VisualizationImageServer<Integer, Double> vv;

  /** */
  public VisualizationImageServerDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    vv = new VisualizationImageServer<>(graph, new KKLayoutAlgorithm<>(), new Dimension(600, 600));

    vv.getRenderer()
        .setNodeRenderer(
            new GradientNodeRenderer<>(vv, Color.white, Color.red, Color.white, Color.blue, false));
    vv.getRenderContext().setEdgeDrawPaintFunction(e -> Color.lightGray);
    vv.getRenderContext().setArrowFillPaintFunction(e -> Color.lightGray);
    vv.getRenderContext().setArrowDrawPaintFunction(e -> Color.lightGray);

    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderer()
        .getNodeLabelRenderer()
        .setPositioner(new BasicNodeLabelRenderer.InsidePositioner());
    vv.getRenderer().getNodeLabelRenderer().setPosition(Renderer.NodeLabel.Position.AUTO);

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();

    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    Image im = vv.getImage(new Point2D.Double(300, 300), new Dimension(600, 600));
    Icon icon = new ImageIcon(im);
    JLabel label = new JLabel(icon);
    content.add(label);
    frame.pack();
    frame.setVisible(true);
  }

  Network<Integer, Double> createGraph() {
    MutableNetwork<Integer, Double> graph = NetworkBuilder.directed().build();
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(3, 0, Math.random());
    graph.addEdge(0, 4, Math.random());
    graph.addEdge(4, 5, Math.random());
    graph.addEdge(5, 3, Math.random());
    graph.addEdge(2, 1, Math.random());
    graph.addEdge(4, 1, Math.random());
    graph.addEdge(8, 2, Math.random());
    graph.addEdge(3, 8, Math.random());
    graph.addEdge(6, 7, Math.random());
    graph.addEdge(7, 5, Math.random());
    graph.addEdge(0, 9, Math.random());
    graph.addEdge(9, 8, Math.random());
    graph.addEdge(7, 6, Math.random());
    graph.addEdge(6, 5, Math.random());
    graph.addEdge(4, 2, Math.random());
    graph.addEdge(5, 4, Math.random());
    graph.addEdge(4, 10, Math.random());
    graph.addEdge(10, 4, Math.random());

    return graph;
  }

  public static void main(String[] args) {
    new VisualizationImageServerDemo();
  }
}
