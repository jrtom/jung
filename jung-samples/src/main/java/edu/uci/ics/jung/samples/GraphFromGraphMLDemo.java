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
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.io.GraphMLReader;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.layout.AWTDomainModel;
import edu.uci.ics.jung.visualization.layout.DomainModel;
import edu.uci.ics.jung.visualization.layout.FRLayoutAlgorithm;
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithm;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Supplier;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Demonstrates loading (and visualizing) a graph from a GraphML file.
 *
 * @author Tom Nelson
 */
public class GraphFromGraphMLDemo {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  /**
   * Creates an instance showing a simple graph with controls to demonstrate the zoom features.
   *
   * @param filename the file containing the graph data we're reading
   * @throws ParserConfigurationException if a SAX parser cannot be constructed
   * @throws SAXException if the SAX parser factory cannot be constructed
   * @throws IOException if the file cannot be read
   */
  public GraphFromGraphMLDemo(String filename)
      throws ParserConfigurationException, SAXException, IOException {

    Supplier<Number> vertexFactory =
        new Supplier<Number>() {
          int n = 0;

          public Number get() {
            return n++;
          }
        };
    Supplier<Number> edgeFactory =
        new Supplier<Number>() {
          int n = 0;

          public Number get() {
            return n++;
          }
        };

    GraphMLReader<MutableNetwork<Number, Number>, Number, Number> gmlr =
        new GraphMLReader<>(vertexFactory, edgeFactory);
    final MutableNetwork<Number, Number> graph =
        NetworkBuilder.directed().allowsSelfLoops(true).build();
    gmlr.load(new InputStreamReader(this.getClass().getResourceAsStream(filename)), graph);

    // create a simple graph for the demo
    LayoutAlgorithm<Number, Point2D> layoutAlgorithm = new FRLayoutAlgorithm<>(domainModel);
    vv = new VisualizationViewer<>(graph, layoutAlgorithm);

    vv.addGraphMouseListener(new TestGraphMouseListener<>());
    vv.getRenderer()
        .setVertexRenderer(
            new GradientVertexRenderer<>(
                vv, Color.white, Color.red, Color.white, Color.blue, false));

    // add my listeners for ToolTips
    vv.setVertexToolTipTransformer(Object::toString);
    vv.setEdgeToolTipTransformer(edge -> "E" + graph.incidentNodes(edge).toString());

    vv.getRenderContext().setVertexLabelTransformer(Object::toString);
    vv.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);

    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Number, Number>();
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    JMenuBar menubar = new JMenuBar();
    menubar.add(graphMouse.getModeMenu());
    panel.setCorner(menubar);

    vv.addKeyListener(graphMouse.getModeKeyListener());
    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel controls = new JPanel();
    controls.add(plus);
    controls.add(minus);
    content.add(controls, BorderLayout.SOUTH);

    frame.pack();
    frame.setVisible(true);
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

  /**
   * @param args if this contains at least one element, the first will be used as the file to read
   * @throws ParserConfigurationException if a SAX parser cannot be constructed
   * @throws SAXException if the SAX parser factory cannot be constructed
   * @throws IOException if the file cannot be read
   */
  public static void main(String[] args)
      throws ParserConfigurationException, SAXException, IOException {
    String filePath = "/datasets/simple.graphml";
    if (args.length > 0) {
      filePath = args[0];
    }
    new GraphFromGraphMLDemo(filePath);
  }
}
