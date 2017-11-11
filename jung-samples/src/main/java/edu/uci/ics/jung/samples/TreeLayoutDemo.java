/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import edu.uci.ics.jung.algorithms.layout.*;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.layout.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

/**
 * Demonsrates TreeLayout and RadialTreeLayout.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class TreeLayoutDemo extends JApplet {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  CTreeNetwork<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  String root;

  TreeLayoutAlgorithm<String, Point2D> treeLayoutAlgorithm;

  RadialTreeLayoutAlgorithm<String, Point2D> radialLayoutAlgorithm;

  public TreeLayoutDemo() {

    // create a simple graph for the demo
    graph = createTree();

    treeLayoutAlgorithm = new TreeLayoutAlgorithm<>(domainModel);
    radialLayoutAlgorithm = new RadialTreeLayoutAlgorithm<>(domainModel);
    //    radialLayout.setSize(new Dimension(600, 600));
    vv = new VisualizationViewer<>(graph, treeLayoutAlgorithm, new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line());
    vv.getRenderContext().setVertexLabelTransformer(Object::toString);
    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(Object::toString);
    vv.getRenderContext().setArrowFillPaintTransformer(n -> Color.lightGray);

    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    JRadioButton animate = new JRadioButton("Animate Transition");
    JToggleButton radial = new JToggleButton("Radial");
    radial.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {

            if (animate.isSelected()) {
              LayoutAlgorithmTransition.animate(vv.getModel(), radialLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv.getModel(), radialLayoutAlgorithm);
            }
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            if (rings == null) {
              rings = new Rings(vv.getModel().getLayoutModel());
            }
            vv.addPreRenderPaintable(rings);
          } else {
            if (animate.isSelected()) {
              LayoutAlgorithmTransition.animate(vv.getModel(), treeLayoutAlgorithm);
            } else {
              LayoutAlgorithmTransition.apply(vv.getModel(), treeLayoutAlgorithm);
            }
            vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
            vv.removePreRenderPaintable(rings);
          }
          vv.repaint();
        });

    JPanel layoutPanel = new JPanel(new GridLayout(2, 1));
    layoutPanel.add(radial);
    layoutPanel.add(animate);
    JPanel controls = new JPanel();
    controls.add(layoutPanel);
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    controls.add(modeBox);

    content.add(controls, BorderLayout.SOUTH);
  }

  class Rings implements VisualizationServer.Paintable {

    Collection<Double> depths;
    LayoutModel<String, Point2D> layoutModel;

    public Rings(LayoutModel<String, Point2D> layoutModel) {
      this.layoutModel = layoutModel;
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<>();
      Map<String, PolarPoint> polarLocations = radialLayoutAlgorithm.getPolarLocations();
      for (String v : graph.nodes()) {
        PolarPoint pp = polarLocations.get(v);
        depths.add(pp.getRadius());
      }
      return depths;
    }

    public void paint(Graphics g) {
      g.setColor(Color.lightGray);

      Graphics2D g2d = (Graphics2D) g;
      Point2D center = radialLayoutAlgorithm.getCenter(layoutModel);

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (double d : depths) {
        ellipse.setFrameFromDiagonal(
            center.getX() - d, center.getY() - d, center.getX() + d, center.getY() + d);
        Shape shape =
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .transform(ellipse);
        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }

  private CTreeNetwork<String, Integer> createTree() {
    MutableCTreeNetwork<String, Integer> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(27).build();

    tree.addNode("root");

    int edgeId = 0;
    tree.addEdge("root", "V0", edgeId++);
    tree.addEdge("V0", "V1", edgeId++);
    tree.addEdge("V0", "V2", edgeId++);
    tree.addEdge("V1", "V4", edgeId++);
    tree.addEdge("V2", "V3", edgeId++);
    tree.addEdge("V2", "V5", edgeId++);
    tree.addEdge("V4", "V6", edgeId++);
    tree.addEdge("V4", "V7", edgeId++);
    tree.addEdge("V3", "V8", edgeId++);
    tree.addEdge("V6", "V9", edgeId++);
    tree.addEdge("V4", "V10", edgeId++);

    tree.addEdge("root", "A0", edgeId++);
    tree.addEdge("A0", "A1", edgeId++);
    tree.addEdge("A0", "A2", edgeId++);
    tree.addEdge("A0", "A3", edgeId++);

    tree.addEdge("root", "B0", edgeId++);
    tree.addEdge("B0", "B1", edgeId++);
    tree.addEdge("B0", "B2", edgeId++);
    tree.addEdge("B1", "B4", edgeId++);
    tree.addEdge("B2", "B3", edgeId++);
    tree.addEdge("B2", "B5", edgeId++);
    tree.addEdge("B4", "B6", edgeId++);
    tree.addEdge("B4", "B7", edgeId++);
    tree.addEdge("B3", "B8", edgeId++);
    tree.addEdge("B6", "B9", edgeId++);

    return tree;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new TreeLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
