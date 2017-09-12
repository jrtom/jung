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
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.function.Function;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Demonstrates the use of <code>GraphZoomScrollPane</code>. This class shows the <code>
 * VisualizationViewer</code> zooming and panning capabilities, using horizontal and vertical
 * scrollbars.
 *
 * <p>This demo also shows ToolTips on graph vertices and edges, and a key listener to change graph
 * mouse modes.
 *
 * @author Tom Nelson
 */
public class GraphZoomScrollPaneDemo {

  /** the graph */
  Network<Integer, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Number> vv;

  /** create an instance of a simple graph with controls to demo the zoom features. */
  public GraphZoomScrollPaneDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    ImageIcon sandstoneIcon = null;
    String imageLocation = "/images/Sandstone.jpg";
    try {
      sandstoneIcon = new ImageIcon(getClass().getResource(imageLocation));
    } catch (Exception ex) {
      System.err.println("Can't load \"" + imageLocation + "\"");
    }
    final ImageIcon icon = sandstoneIcon;
    vv = new VisualizationViewer<Integer, Number>(graph, new KKLayout<Integer>(graph.asGraph()));

    if (icon != null) {
      vv.addPreRenderPaintable(
          new VisualizationViewer.Paintable() {
            public void paint(Graphics g) {
              Dimension d = vv.getSize();
              g.drawImage(icon.getImage(), 0, 0, d.width, d.height, vv);
            }

            public boolean useTransform() {
              return false;
            }
          });
    }
    vv.addPostRenderPaintable(
        new VisualizationViewer.Paintable() {
          int x;
          int y;
          Font font;
          FontMetrics metrics;
          int swidth;
          int sheight;
          String str = "GraphZoomScrollPane Demo";

          public void paint(Graphics g) {
            Dimension d = vv.getSize();
            if (font == null) {
              font = new Font(g.getFont().getName(), Font.BOLD, 30);
              metrics = g.getFontMetrics(font);
              swidth = metrics.stringWidth(str);
              sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
              x = (d.width - swidth) / 2;
              y = (int) (d.height - sheight * 1.5);
            }
            g.setFont(font);
            Color oldColor = g.getColor();
            g.setColor(Color.lightGray);
            g.drawString(str, x, y);
            g.setColor(oldColor);
          }

          public boolean useTransform() {
            return false;
          }
        });

    vv.addGraphMouseListener(new TestGraphMouseListener<Integer>());
    vv.getRenderer()
        .setVertexRenderer(
            new GradientVertexRenderer<Integer>(
                vv, Color.white, Color.red, Color.white, Color.blue, false));
    vv.getRenderContext().setEdgeDrawPaintTransformer(e -> Color.lightGray);
    vv.getRenderContext().setArrowFillPaintTransformer(a -> Color.lightGray);
    vv.getRenderContext().setArrowDrawPaintTransformer(a -> Color.lightGray);

    // add my listeners for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());
    vv.setEdgeToolTipTransformer(
        new Function<Number, String>() {
          public String apply(Number edge) {
            return "E" + graph.incidentNodes(edge).toString();
          }
        });

    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);
    vv.setForeground(Color.lightGray);

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Integer, Number>();
    vv.setGraphMouse(graphMouse);

    vv.addKeyListener(graphMouse.getModeKeyListener());
    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

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

    JButton reset = new JButton("reset");
    reset.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .setToIdentity();
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.VIEW)
                .setToIdentity();
          }
        });

    JPanel controls = new JPanel();
    controls.add(plus);
    controls.add(minus);
    controls.add(reset);
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

  /** A nested class to demo the GraphMouseListener finding the right vertices after zoom/pan */
  static class TestGraphMouseListener<V> implements GraphMouseListener<V> {

    public void graphClicked(V v, MouseEvent me) {
      System.err.println("Vertex " + v + " was clicked at (" + me.getX() + "," + me.getY() + ")");
    }

    public void graphPressed(V v, MouseEvent me) {
      System.err.println("Vertex " + v + " was pressed at (" + me.getX() + "," + me.getY() + ")");
    }

    public void graphReleased(V v, MouseEvent me) {
      System.err.println("Vertex " + v + " was released at (" + me.getX() + "," + me.getY() + ")");
    }
  }

  public static void main(String[] args) {
    new GraphZoomScrollPaneDemo();
  }
}
