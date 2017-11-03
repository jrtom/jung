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
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.AWTDomainModel;
import edu.uci.ics.jung.visualization.layout.DomainModel;
import edu.uci.ics.jung.visualization.layout.FRLayoutAlgorithm;
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithm;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This demo shows how to use the vertex labels themselves as the vertex shapes. Additionally, it
 * shows html labels so they are multi-line, and gradient painting of the vertex labels.
 *
 * @author Tom Nelson
 */
public class VertexLabelAsShapeDemo extends JApplet {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  /** */
  private static final long serialVersionUID = 1017336668368978842L;

  Network<String, Number> graph;

  VisualizationViewer<String, Number> vv;

  LayoutAlgorithm<String, Point2D> layoutAlgorithm;

  /** create an instance of a simple graph with basic controls */
  public VertexLabelAsShapeDemo() {

    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    layoutAlgorithm = new FRLayoutAlgorithm<>(domainModel);

    Dimension preferredSize = new Dimension(400, 400);
    final VisualizationModel<String, Number, Point2D> visualizationModel =
        new BaseVisualizationModel<>(graph, layoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<String, Number>(visualizationModel, preferredSize);

    // this class will provide both label drawing and vertex shapes
    VertexLabelAsShapeRenderer<String, Number> vlasr =
        new VertexLabelAsShapeRenderer<String, Number>(visualizationModel, vv.getRenderContext());

    // customize the render context
    vv.getRenderContext()
        .setVertexLabelTransformer(
            new ToStringLabeller().andThen(input -> "<html><center>Vertex<p>" + input));
    vv.getRenderContext().setVertexShapeTransformer(vlasr);
    vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.red));
    vv.getRenderContext().setEdgeDrawPaintTransformer(e -> Color.yellow);
    vv.getRenderContext().setEdgeStrokeTransformer(e -> new BasicStroke(2.5f));

    // customize the renderer
    vv.getRenderer()
        .setVertexRenderer(
            new GradientVertexRenderer<String, Number>(vv, Color.gray, Color.white, true));
    vv.getRenderer().setVertexLabelRenderer(vlasr);

    vv.setBackground(Color.black);

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());

    final DefaultModalGraphMouse<String, Number> graphMouse =
        new DefaultModalGraphMouse<String, Number>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    Container content = getContentPane();
    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    content.add(gzsp);

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

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
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
    zoomControls.add(plus);
    zoomControls.add(minus);
    controls.add(zoomControls);
    controls.add(modeBox);
    content.add(controls, BorderLayout.SOUTH);
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().add(new VertexLabelAsShapeDemo());
    f.pack();
    f.setVisible(true);
  }
}
