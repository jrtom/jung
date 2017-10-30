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
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.util.LayoutMediator;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * A demo that shows how collections of vertices can be collapsed into a single vertex. In this
 * demo, the vertices that are collapsed are those mouse-picked by the user. Any criteria could be
 * used to form the vertex collections to be collapsed, perhaps some common characteristic of those
 * vertex objects.
 *
 * <p>Note that the collection types don't use generics in this demo, because the vertices are of
 * two types: String for plain vertices, and {@code Network<String,Number>} for the collapsed
 * vertices.
 *
 * @author Tom Nelson
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class VertexCollapseDemo extends JApplet {

  String instructions =
      "<html>Use the mouse to select multiple vertices"
          + "<p>either by dragging a region, or by shift-clicking"
          + "<p>on multiple vertices."
          + "<p>After you select vertices, use the Collapse button"
          + "<p>to combine them into a single vertex."
          + "<p>Select a 'collapsed' vertex and use the Expand button"
          + "<p>to restore the collapsed vertices."
          + "<p>The Restore button will restore the original graph."
          + "<p>If you select 2 (and only 2) vertices, then press"
          + "<p>the Compress Edges button, parallel edges between"
          + "<p>those two vertices will no longer be expanded."
          + "<p>If you select 2 (and only 2) vertices, then press"
          + "<p>the Expand Edges button, parallel edges between"
          + "<p>those two vertices will be expanded."
          + "<p>You can drag the vertices with the mouse."
          + "<p>Use the 'Picking'/'Transforming' combo-box to switch"
          + "<p>between picking and transforming mode.</html>";
  /** the graph */
  Network graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer vv;

  Layout layout;

  GraphCollapser collapser;

  public VertexCollapseDemo() {

    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();
    collapser = new GraphCollapser(graph);

    layout = new FRLayout(graph.asGraph());

    Dimension preferredSize = new Dimension(400, 400);
    final VisualizationModel visualizationModel =
        new DefaultVisualizationModel(graph, layout, preferredSize);
    vv = new VisualizationViewer(visualizationModel, preferredSize);

    vv.getRenderContext().setVertexShapeTransformer(new ClusterVertexShapeFunction());

    final Set exclusions = new HashSet();
    final PredicatedParallelEdgeIndexFunction eif =
        new PredicatedParallelEdgeIndexFunction(graph, exclusions::contains);

    vv.getRenderContext().setParallelEdgeIndexFunction(eif);

    vv.setBackground(Color.white);

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(
        new ToStringLabeller() {

          @Override
          public String apply(Object v) {
            if (v instanceof Network) {
              return ((Network) v).nodes().toString();
            }
            return super.apply(v);
          }
        });

    /** the regular graph mouse for the normal view */
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

    vv.setGraphMouse(graphMouse);

    Container content = getContentPane();
    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    content.add(gzsp);

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

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

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
            if (picked.size() > 1) {
              Network inGraph = vv.getModel().getLayoutMediator().getNetwork();
              Network clusterGraph = collapser.getClusterGraph(inGraph, picked);
              Network g = collapser.collapse(inGraph, clusterGraph);
              double sumx = 0;
              double sumy = 0;
              for (Object v : picked) {
                Point2D p = (Point2D) layout.apply(v);
                sumx += p.getX();
                sumy += p.getY();
              }
              Point2D cp = new Point2D.Double(sumx / picked.size(), sumy / picked.size());
              layout.setLocation(clusterGraph, cp);
              vv.getRenderContext().getParallelEdgeIndexFunction().reset();
              LayoutMediator newLayoutMediator = new LayoutMediator(g, layout);
              vv.setLayoutMediator(newLayoutMediator);
              vv.getPickedVertexState().clear();
              vv.repaint();
            }
          }
        });

    JButton compressEdges = new JButton("Compress Edges");
    compressEdges.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            Set picked = vv.getPickedVertexState().getPicked();
            if (picked.size() == 2) {
              Iterator pickedIter = picked.iterator();
              Object nodeU = pickedIter.next();
              Object nodeV = pickedIter.next();
              Network graph = vv.getModel().getLayoutMediator().getNetwork();
              Collection edges = new HashSet(graph.incidentEdges(nodeU));
              edges.retainAll(graph.incidentEdges(nodeV));
              exclusions.addAll(edges);
              vv.repaint();
            }
          }
        });

    JButton expandEdges = new JButton("Expand Edges");
    expandEdges.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            Set picked = vv.getPickedVertexState().getPicked();
            if (picked.size() == 2) {
              Iterator pickedIter = picked.iterator();
              Object nodeU = pickedIter.next();
              Object nodeV = pickedIter.next();
              Network graph = vv.getModel().getLayoutMediator().getNetwork();
              Collection edges = new HashSet(graph.incidentEdges(nodeU));
              edges.retainAll(graph.incidentEdges(nodeV));
              exclusions.removeAll(edges);
              vv.repaint();
            }
          }
        });

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            Collection picked = new HashSet(vv.getPickedVertexState().getPicked());
            for (Object v : picked) {
              if (v instanceof Network) {
                Network inGraph = vv.getModel().getLayoutMediator().getNetwork();
                Network g = collapser.expand(graph, inGraph, (Network) v);
                vv.getRenderContext().getParallelEdgeIndexFunction().reset();
                LayoutMediator newLayoutMediator = new LayoutMediator(g, layout);
                vv.setLayoutMediator(newLayoutMediator);
              }
              vv.getPickedVertexState().clear();
              vv.repaint();
            }
          }
        });

    JButton reset = new JButton("Reset");
    reset.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            LayoutMediator newLayoutMediator = new LayoutMediator(graph, layout);
            vv.setLayoutMediator(newLayoutMediator);
            exclusions.clear();
            vv.repaint();
          }
        });

    JButton help = new JButton("Help");
    help.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(
                (JComponent) e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE);
          }
        });

    JPanel controls = new JPanel();
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
    zoomControls.add(plus);
    zoomControls.add(minus);
    controls.add(zoomControls);
    JPanel collapseControls = new JPanel(new GridLayout(3, 1));
    collapseControls.setBorder(BorderFactory.createTitledBorder("Picked"));
    collapseControls.add(collapse);
    collapseControls.add(expand);
    collapseControls.add(compressEdges);
    collapseControls.add(expandEdges);
    collapseControls.add(reset);
    controls.add(collapseControls);
    controls.add(modeBox);
    controls.add(help);
    content.add(controls, BorderLayout.SOUTH);
  }

  /**
   * a demo class that will create a vertex shape that is either a polygon or star. The number of
   * sides corresponds to the number of vertices that were collapsed into the vertex represented by
   * this shape.
   *
   * @author Tom Nelson
   * @param <V> the vertex type
   */
  class ClusterVertexShapeFunction<V> extends EllipseVertexShapeTransformer<V> {

    ClusterVertexShapeFunction() {
      setSizeTransformer(new ClusterVertexSizeFunction<V>(20));
    }

    @Override
    public Shape apply(V v) {
      if (v instanceof Network) {
        int size = ((Network) v).nodes().size();
        if (size < 8) {
          int sides = Math.max(size, 3);
          return factory.getRegularPolygon(v, sides);
        } else {
          return factory.getRegularStar(v, size);
        }
      }
      return super.apply(v);
    }
  }

  /**
   * A demo class that will make vertices larger if they represent a collapsed collection of
   * original vertices
   *
   * @author Tom Nelson
   * @param <V> the vertex type
   */
  class ClusterVertexSizeFunction<V> implements Function<V, Integer> {
    int size;

    public ClusterVertexSizeFunction(Integer size) {
      this.size = size;
    }

    public Integer apply(V v) {
      if (v instanceof Network) {
        return 30;
      }
      return size;
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().add(new VertexCollapseDemo());
    f.pack();
    f.setVisible(true);
  }
}
