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
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer;
import java.awt.*;
import javax.swing.*;

/**
 * A demo that shows drawn Icons as nodes
 *
 * @author Tom Nelson
 */
public class DrawnIconNodeDemo {

  /** the graph */
  Network<Integer, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Number> vv;

  public DrawnIconNodeDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    vv = new VisualizationViewer<>(graph, new FRLayoutAlgorithm<>(), new Dimension(700, 700));
    vv.getRenderContext().setNodeLabelFunction(v -> "Node " + v);

    vv.getRenderContext().setNodeLabelRenderer(new DefaultNodeLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

    vv.getRenderContext()
        .setNodeIconFunction(
            v ->
                new Icon() {

                  public int getIconHeight() {
                    return 20;
                  }

                  public int getIconWidth() {
                    return 20;
                  }

                  public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (vv.getPickedNodeState().isPicked(v)) {
                      g.setColor(Color.yellow);
                    } else {
                      g.setColor(Color.red);
                    }
                    g.fillOval(x, y, 20, 20);
                    if (vv.getPickedNodeState().isPicked(v)) {
                      g.setColor(Color.black);
                    } else {
                      g.setColor(Color.white);
                    }
                    g.drawString("" + v, x + 6, y + 15);
                  }
                });

    vv.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableNodePaintFunction<>(vv.getPickedNodeState(), Color.white, Color.yellow));
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(vv.getPickedEdgeState(), Color.black, Color.lightGray));

    vv.setBackground(Color.white);

    // add my listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    final DefaultModalGraphMouse<Integer, Number> gm = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(gm);

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv, ""));
    controls.add(gm.getModeComboBox());
    content.add(controls, BorderLayout.SOUTH);

    frame.pack();
    frame.setVisible(true);
  }

  Network<Integer, Number> createGraph() {
    MutableNetwork<Integer, Number> graph = NetworkBuilder.directed().build();
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
    new DrawnIconNodeDemo();
  }
}
