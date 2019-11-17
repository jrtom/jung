/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.layout.AggregateLayoutModel;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.util.Collection;
import javax.swing.*;

/**
 * Demonstrates the AggregateLayout class. In this demo, nodes are visually clustered as they are
 * selected. The cluster is formed in a new Layout centered at the middle locations of the selected
 * nodes. The layoutSize and layout algorithm for each new cluster is selectable.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class SubLayoutDemo extends JPanel {

  String instructions =
      "<html>"
          + "Use the Layout combobox to select the "
          + "<p>underlying layout."
          + "<p>Use the SubLayout combobox to select "
          + "<p>the type of layout for any clusters you create."
          + "<p>To create clusters, use the mouse to select "
          + "<p>multiple nodes, either by dragging a region, "
          + "<p>or by shift-clicking on multiple nodes."
          + "<p>After you select nodes, use the "
          + "<p>Cluster Picked button to cluster them using the "
          + "<p>layout and layoutSize specified in the Sublayout comboboxen."
          + "<p>Use the Uncluster All button to remove all"
          + "<p>clusters."
          + "<p>You can drag the cluster with the mouse."
          + "<p>Use the 'Picking'/'Transforming' combo-box to switch"
          + "<p>between picking and transforming mode.</html>";
  /** the graph */
  Network<String, Number> graph;

  @SuppressWarnings({"unchecked", "rawtypes"})
  Class<LayoutAlgorithm>[] layoutClasses =
      new Class[] {
        CircleLayoutAlgorithm.class,
        SpringLayoutAlgorithm.class,
        FRLayoutAlgorithm.class,
        KKLayoutAlgorithm.class
      };
  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  AggregateLayoutModel<String> clusteringLayoutModel;

  Dimension subLayoutSize;

  PickedState<String> ps;

  @SuppressWarnings("rawtypes")
  Class<CircleLayoutAlgorithm> subLayoutType = CircleLayoutAlgorithm.class;

  public SubLayoutDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    // ClusteringLayout is a decorator class that delegates
    // to another layout, but can also separately manage the
    // layout of sub-sets of nodes in circular clusters.
    Dimension preferredSize = new Dimension(600, 600);

    LayoutAlgorithm<String> layoutAlgorithm = new FRLayoutAlgorithm();
    clusteringLayoutModel =
        new AggregateLayoutModel<>(
            LoadingCacheLayoutModel.<String>builder()
                .setGraph(graph.asGraph())
                .setSize(preferredSize.width, preferredSize.height)
                .build());

    clusteringLayoutModel.accept(layoutAlgorithm);

    final VisualizationModel<String, Number> visualizationModel =
        new BaseVisualizationModel<>(graph, clusteringLayoutModel, layoutAlgorithm);

    vv = new VisualizationViewer<>(visualizationModel, preferredSize);

    ps = vv.getPickedNodeState();
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(vv.getPickedEdgeState(), Color.black, Color.red));
    vv.getRenderContext()
        .setNodeFillPaintFunction(
            new PickableNodePaintFunction<>(vv.getPickedNodeState(), Color.red, Color.yellow));
    vv.setBackground(Color.white);

    // add a listener for ToolTips
    vv.setNodeToolTipFunction(Object::toString);

    /** the regular graph mouse for the normal view */
    final DefaultModalGraphMouse<?, ?> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    add(gzsp);

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

    JButton cluster = new JButton("Cluster Picked");
    cluster.addActionListener(e -> clusterPicked());

    JButton uncluster = new JButton("UnCluster All");
    uncluster.addActionListener(e -> uncluster());

    JComboBox<?> layoutTypeComboBox = new JComboBox<Object>(layoutClasses);
    layoutTypeComboBox.setRenderer(
        new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String valueString = value.toString();
            valueString = valueString.substring(valueString.lastIndexOf('.') + 1);
            return super.getListCellRendererComponent(
                list, valueString, index, isSelected, cellHasFocus);
          }
        });
    layoutTypeComboBox.setSelectedItem(FRLayoutAlgorithm.class);
    layoutTypeComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class<CircleLayoutAlgorithm> clazz = (Class<CircleLayoutAlgorithm>) e.getItem();
            try {
              vv.getModel().getLayoutModel().accept(getLayoutAlgorithmFor(clazz));
              vv.repaint();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });

    JComboBox<?> subLayoutTypeComboBox = new JComboBox<Object>(layoutClasses);

    subLayoutTypeComboBox.setRenderer(
        new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String valueString = value.toString();
            valueString = valueString.substring(valueString.lastIndexOf('.') + 1);
            return super.getListCellRendererComponent(
                list, valueString, index, isSelected, cellHasFocus);
          }
        });
    subLayoutTypeComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            subLayoutType = (Class<CircleLayoutAlgorithm>) e.getItem();
            uncluster();
            clusterPicked();
          }
        });

    JComboBox<?> subLayoutDimensionComboBox =
        new JComboBox<Object>(
            new Dimension[] {
              new Dimension(75, 75),
              new Dimension(100, 100),
              new Dimension(150, 150),
              new Dimension(200, 200),
              new Dimension(250, 250),
              new Dimension(300, 300)
            });
    subLayoutDimensionComboBox.setRenderer(
        new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(
              JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String valueString = value.toString();
            valueString = valueString.substring(valueString.lastIndexOf('['));
            valueString = valueString.replaceAll("idth", "");
            valueString = valueString.replaceAll("eight", "");
            return super.getListCellRendererComponent(
                list, valueString, index, isSelected, cellHasFocus);
          }
        });
    subLayoutDimensionComboBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            subLayoutSize = (Dimension) e.getItem();
            uncluster();
            clusterPicked();
          }
        });

    subLayoutDimensionComboBox.setSelectedIndex(1);

    JButton help = new JButton("Help");
    help.addActionListener(
        e ->
            JOptionPane.showMessageDialog(
                (JComponent) e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE));

    Dimension space = new Dimension(20, 20);
    Box controls = Box.createVerticalBox();
    controls.add(Box.createRigidArea(space));

    JComponent zoomControls = ControlHelpers.getZoomControls(vv, "Zoom");
    heightConstrain(zoomControls);
    controls.add(zoomControls);
    controls.add(Box.createRigidArea(space));

    JPanel clusterControls = new JPanel(new GridLayout(0, 1));
    clusterControls.setBorder(BorderFactory.createTitledBorder("Clustering"));
    clusterControls.add(cluster);
    clusterControls.add(uncluster);
    heightConstrain(clusterControls);
    controls.add(clusterControls);
    controls.add(Box.createRigidArea(space));

    JPanel layoutControls = new JPanel(new GridLayout(0, 1));
    layoutControls.setBorder(BorderFactory.createTitledBorder("Layout"));
    layoutControls.add(layoutTypeComboBox);
    heightConstrain(layoutControls);
    controls.add(layoutControls);

    JPanel subLayoutControls = new JPanel(new GridLayout(0, 1));
    subLayoutControls.setBorder(BorderFactory.createTitledBorder("SubLayout"));
    subLayoutControls.add(subLayoutTypeComboBox);
    subLayoutControls.add(subLayoutDimensionComboBox);
    heightConstrain(subLayoutControls);
    controls.add(subLayoutControls);
    controls.add(Box.createRigidArea(space));

    JPanel modePanel = new JPanel(new GridLayout(1, 1));
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);
    heightConstrain(modePanel);
    controls.add(modePanel);
    controls.add(Box.createRigidArea(space));

    controls.add(help);
    controls.add(Box.createVerticalGlue());
    add(controls, BorderLayout.EAST);
  }

  private void heightConstrain(Component component) {
    Dimension d =
        new Dimension(component.getMaximumSize().width, component.getMinimumSize().height);
    component.setMaximumSize(d);
  }

  private LayoutAlgorithm<String> getLayoutAlgorithmFor(Class<CircleLayoutAlgorithm> layoutClass)
      throws Exception {
    Constructor<CircleLayoutAlgorithm> constructor = layoutClass.getConstructor();
    return constructor.newInstance();
  }

  private void clusterPicked() {
    cluster(true);
  }

  private void uncluster() {
    cluster(false);
  }

  private void cluster(boolean state) {
    if (state) {
      // put the picked nodes into a new sublayout
      Collection<String> picked = ps.getPicked();
      if (picked.size() > 1) {
        Point2D center = new Point2D.Double();
        double x = 0;
        double y = 0;
        for (String node : picked) {
          Point p = clusteringLayoutModel.apply(node);
          x += p.x;
          y += p.y;
        }
        x /= picked.size();
        y /= picked.size();
        center.setLocation(x, y);

        MutableNetwork<String, Number> subGraph;
        try {
          subGraph = NetworkBuilder.from(graph).build();
          for (String node : picked) {
            subGraph.addNode(node);
            for (Number edge : graph.incidentEdges(node)) {
              EndpointPair<String> endpoints = graph.incidentNodes(edge);
              String nodeU = endpoints.nodeU();
              String nodeV = endpoints.nodeV();
              if (picked.contains(nodeU) && picked.contains(nodeV)) {
                // put this edge into the subgraph
                subGraph.addEdge(nodeU, nodeV, edge);
              }
            }
          }

          LayoutAlgorithm<String> subLayoutAlgorithm = getLayoutAlgorithmFor(subLayoutType);

          LayoutModel<String> newLayoutModel =
              LoadingCacheLayoutModel.<String>builder()
                  .setGraph(subGraph.asGraph())
                  .setSize(subLayoutSize.width, subLayoutSize.height)
                  .setInitializer(
                      new RandomLocationTransformer<>(subLayoutSize.width, subLayoutSize.height, 0))
                  .build();

          clusteringLayoutModel.put(newLayoutModel, Point.of(center.getX(), center.getY()));
          newLayoutModel.accept(subLayoutAlgorithm);
          vv.repaint();

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } else {
      // remove all sublayouts
      this.clusteringLayoutModel.removeAll();
      vv.repaint();
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new SubLayoutDemo());
    f.pack();
    f.setVisible(true);
  }
}
