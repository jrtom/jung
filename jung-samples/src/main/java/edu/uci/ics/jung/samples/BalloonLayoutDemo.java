/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import edu.uci.ics.jung.algorithms.layout.BalloonLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.CTreeNetwork;
import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport;
import edu.uci.ics.jung.visualization.util.Animator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

/**
 * Demonstrates the visualization of a Tree using TreeLayout and BalloonLayout. An examiner lens
 * performing a hyperbolic transformation of the view is also included.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class BalloonLayoutDemo extends JApplet {

  /** the graph */
  CTreeNetwork<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  String root;

  TreeLayout<String> layout;

  BalloonLayout<String> radialLayout;
  /** provides a Hyperbolic lens for the view */
  LensSupport hyperbolicViewSupport;

  public BalloonLayoutDemo() {

    // create a simple graph for the demo
    graph = createTree();

    layout = new TreeLayout<String>(graph.asGraph());
    radialLayout = new BalloonLayout<String>(graph.asGraph());
    radialLayout.setSize(new Dimension(900, 900));
    vv = new VisualizationViewer<String, Integer>(graph, layout, new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.quadCurve(graph));
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());
    vv.getRenderContext().setArrowFillPaintTransformer(a -> Color.lightGray);
    rings = new Rings(radialLayout);

    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse =
        new DefaultModalGraphMouse<String, Integer>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    hyperbolicViewSupport =
        new ViewLensSupport<String, Integer>(
            vv,
            new HyperbolicShapeTransformer(
                vv, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse());

    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

    final ScalingControl scaler = new CrossoverScalingControl();

    vv.scaleToLayout(scaler);

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

    JToggleButton radial = new JToggleButton("Balloon");
    radial.addItemListener(
        new ItemListener() {

          public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {

              LayoutTransition<String, Integer> lt =
                  new LayoutTransition<String, Integer>(vv, layout, radialLayout);
              Animator animator = new Animator(lt);
              animator.start();
              vv.getRenderContext()
                  .getMultiLayerTransformer()
                  .getTransformer(Layer.LAYOUT)
                  .setToIdentity();
              vv.addPreRenderPaintable(rings);
            } else {

              LayoutTransition<String, Integer> lt =
                  new LayoutTransition<String, Integer>(vv, radialLayout, layout);
              Animator animator = new Animator(lt);
              animator.start();
              vv.getRenderContext()
                  .getMultiLayerTransformer()
                  .getTransformer(Layer.LAYOUT)
                  .setToIdentity();
              vv.removePreRenderPaintable(rings);
            }
            vv.repaint();
          }
        });
    final JRadioButton hyperView = new JRadioButton("Hyperbolic View");
    hyperView.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            hyperbolicViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED);
          }
        });

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    controls.add(radial);
    controls.add(scaleGrid);
    controls.add(modeBox);
    controls.add(hyperView);
    content.add(controls, BorderLayout.SOUTH);
  }

  class Rings implements VisualizationServer.Paintable {

    BalloonLayout<String> layout;

    public Rings(BalloonLayout<String> layout) {
      this.layout = layout;
    }

    public void paint(Graphics g) {
      g.setColor(Color.gray);

      Graphics2D g2d = (Graphics2D) g;

      Ellipse2D ellipse = new Ellipse2D.Double();
      for (String v : layout.nodes()) {
        Double radius = layout.getRadii().get(v);
        if (radius == null) {
          continue;
        }
        Point2D p = layout.apply(v);
        ellipse.setFrame(-radius, -radius, 2 * radius, 2 * radius);
        AffineTransform at = AffineTransform.getTranslateInstance(p.getX(), p.getY());
        Shape shape = at.createTransformedShape(ellipse);

        MutableTransformer viewTransformer =
            vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW);

        if (viewTransformer instanceof MutableTransformerDecorator) {
          shape = vv.getRenderContext().getMultiLayerTransformer().transform(shape);
        } else {
          shape = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, shape);
        }

        g2d.draw(shape);
      }
    }

    public boolean useTransform() {
      return true;
    }
  }

  /** */
  private CTreeNetwork<String, Integer> createTree() {
    MutableCTreeNetwork<String, Integer> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(27).build();

    int edgeId = 0;
    tree.addNode("A0");
    tree.addEdge("A0", "B0", edgeId++);
    tree.addEdge("A0", "B1", edgeId++);
    tree.addEdge("A0", "B2", edgeId++);

    tree.addEdge("B0", "C0", edgeId++);
    tree.addEdge("B0", "C1", edgeId++);
    tree.addEdge("B0", "C2", edgeId++);
    tree.addEdge("B0", "C3", edgeId++);

    tree.addEdge("C2", "H0", edgeId++);
    tree.addEdge("C2", "H1", edgeId++);

    tree.addEdge("B1", "D0", edgeId++);
    tree.addEdge("B1", "D1", edgeId++);
    tree.addEdge("B1", "D2", edgeId++);

    tree.addEdge("B2", "E0", edgeId++);
    tree.addEdge("B2", "E1", edgeId++);
    tree.addEdge("B2", "E2", edgeId++);

    tree.addEdge("D0", "F0", edgeId++);
    tree.addEdge("D0", "F1", edgeId++);
    tree.addEdge("D0", "F2", edgeId++);

    tree.addEdge("D1", "G0", edgeId++);
    tree.addEdge("D1", "G1", edgeId++);
    tree.addEdge("D1", "G2", edgeId++);
    tree.addEdge("D1", "G3", edgeId++);
    tree.addEdge("D1", "G4", edgeId++);
    tree.addEdge("D1", "G5", edgeId++);
    tree.addEdge("D1", "G6", edgeId++);
    tree.addEdge("D1", "G7", edgeId++);

    return tree;
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    content.add(new BalloonLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
