/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import static edu.uci.ics.jung.visualization.layout.AWT.POINT_MODEL;

import com.google.common.graph.Network;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.*;

/**
 * This demo shows how to use the vertex labels themselves as the vertex shapes. Additionally, it
 * shows html labels so they are multi-line, and gradient painting of the vertex labels.
 *
 * @author Tom Nelson
 */
public class VertexLabelAsShapeDemo extends JApplet {

  /** */
  private static final long serialVersionUID = 1017336668368978842L;

  Network<String, Number> graph;

  VisualizationViewer<String, Number> vv;

  LayoutAlgorithm<String, Point2D> layoutAlgorithm;

  /** create an instance of a simple graph with basic controls */
  public VertexLabelAsShapeDemo() {

    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    layoutAlgorithm = new FRLayoutAlgorithm<>(POINT_MODEL);

    Dimension preferredSize = new Dimension(400, 400);
    final VisualizationModel<String, Number, Point2D> visualizationModel =
        new BaseVisualizationModel<>(graph, layoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<>(visualizationModel, preferredSize);

    // this class will provide both label drawing and vertex shapes
    VertexLabelAsShapeRenderer<String, Number> vlasr =
        new VertexLabelAsShapeRenderer<>(visualizationModel, vv.getRenderContext());

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
        .setVertexRenderer(new GradientVertexRenderer<>(vv, Color.gray, Color.white, true));
    vv.getRenderer().setVertexLabelRenderer(vlasr);

    vv.setBackground(Color.black);

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(n -> n);

    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();

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
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

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
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new VertexLabelAsShapeDemo());
    f.pack();
    f.setVisible(true);
  }
}
