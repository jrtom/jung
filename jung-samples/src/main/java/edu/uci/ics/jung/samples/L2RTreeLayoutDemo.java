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
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.*;

/**
 * A variant of TreeLayoutDemo that rotates the view by 90 degrees from the default orientation.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class L2RTreeLayoutDemo extends JApplet {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  /** the graph */
  CTreeNetwork<String, Integer> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Integer> vv;

  VisualizationServer.Paintable rings;

  String root;

  TreeLayoutAlgorithm<String, Point2D> treeLayoutAlgorithm;

  RadialTreeLayoutAlgorithm<String, Point2D> radialLayoutAlgorithm;

  public L2RTreeLayoutDemo() {

    // create a simple graph for the demo
    graph = createTree();

    treeLayoutAlgorithm = new TreeLayoutAlgorithm<>(domainModel);
    radialLayoutAlgorithm = new RadialTreeLayoutAlgorithm<>(domainModel);
    //    radialLayoutAlgorithm.setSize(new Dimension(600, 600));
    vv =
        new VisualizationViewer<String, Integer>(
            graph, treeLayoutAlgorithm, new Dimension(600, 600));
    vv.setBackground(Color.white);
    vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.quadCurve());
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());
    vv.getRenderContext().setArrowFillPaintTransformer(a -> Color.lightGray);
    rings = new Rings(vv.getModel().getLayoutModel());

    setLtoR(vv);

    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse<String, Integer> graphMouse =
        new DefaultModalGraphMouse<String, Integer>();

    vv.setGraphMouse(graphMouse);

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
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

    JToggleButton radial = new JToggleButton("Radial");
    radial.addItemListener(
        new ItemListener() {

          public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {

              //              LayoutAlgorithmTransition<String, Integer, Point2D> lt =
              //                  new LayoutAlgorithmTransition<>(vv.getModel(), radialLayoutAlgorithm);
              //              Animator animator = new Animator(lt);
              //              animator.start();
              vv.getModel().setLayoutAlgorithm(radialLayoutAlgorithm);
              vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
              vv.addPreRenderPaintable(rings);
            } else {
              //              LayoutAlgorithmTransition<String, Integer, Point2D> lt =
              //                  new LayoutAlgorithmTransition<>(vv.getModel(), treeLayoutAlgorithm);
              //              Animator animator = new Animator(lt);
              //              animator.start();
              vv.getModel().setLayoutAlgorithm(treeLayoutAlgorithm);
              vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
              setLtoR(vv);
              vv.removePreRenderPaintable(rings);
            }

            vv.repaint();
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

    content.add(controls, BorderLayout.SOUTH);
  }

  private void setLtoR(VisualizationViewer<String, Integer> vv) {
    //    LayoutModel<String, Point2D> layoutModel = vv.getModel().getLayoutModel();
    Dimension d = vv.getModel().getLayoutSize();
    Point2D center = new Point2D.Double(d.width / 2, d.height / 2);
    vv.getRenderContext()
        .getMultiLayerTransformer()
        .getTransformer(Layer.LAYOUT)
        .rotate(-Math.PI / 2, center);
  }

  class Rings implements VisualizationServer.Paintable {

    Collection<Double> depths;
    LayoutModel<String, Point2D> layoutModel;

    public Rings(LayoutModel<String, Point2D> layoutModel) {
      this.layoutModel = layoutModel;
      depths = getDepths();
    }

    private Collection<Double> getDepths() {
      Set<Double> depths = new HashSet<Double>();
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

  /** */
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
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    content.add(new L2RTreeLayoutDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
