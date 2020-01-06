/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.samples;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.io.PajekNetReader;
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.layout.AggregateLayoutModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * This simple app demonstrates how one can use our algorithms and visualization libraries in
 * unison. In this case, we generate use the Zachary karate club data set, widely known in the
 * social networks literature, then we cluster the nodes using an edge-betweenness clusterer, and
 * finally we visualize the graph using Fruchtermain-Rheingold layout and provide a slider so that
 * the user can adjust the clustering granularity.
 *
 * @author Scott White
 */
@SuppressWarnings("serial")
public class ClusteringDemo extends JPanel {

  VisualizationViewer<Number, Number> vv;

  LoadingCache<Number, Paint> nodePaints =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> Color.white));
  LoadingCache<Number, Paint> edgePaints =
      CacheBuilder.newBuilder().build(CacheLoader.from(() -> Color.blue));

  private static final Stroke THIN = new BasicStroke(1);
  private static final Stroke THICK = new BasicStroke(2);

  public final Color[] similarColors = {
    new Color(216, 134, 134),
    new Color(135, 137, 211),
    new Color(134, 206, 189),
    new Color(206, 176, 134),
    new Color(194, 204, 134),
    new Color(145, 214, 134),
    new Color(133, 178, 209),
    new Color(103, 148, 255),
    new Color(60, 220, 220),
    new Color(30, 250, 100)
  };

  public static void main(String[] args) throws IOException {

    ClusteringDemo cd = new ClusteringDemo();
    cd.start();
    // Add a restart button so the graph can be redrawn to fit the layoutSize of the frame
    JFrame jf = new JFrame();
    jf.getContentPane().add(cd);

    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    jf.pack();
    jf.setVisible(true);
  }

  public void start() {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("datasets/zachary.net");
    BufferedReader br = new BufferedReader(new InputStreamReader(is));

    try {
      setUpView(br);
    } catch (IOException e) {
      System.out.println("Error in loading graph");
      e.printStackTrace();
    }
  }

  private void setUpView(BufferedReader br) throws IOException {

    Supplier<Number> nodeFactory =
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

    PajekNetReader<MutableNetwork<Number, Number>, Number, Number> pnr =
        new PajekNetReader<>(nodeFactory, edgeFactory);

    final MutableNetwork<Number, Number> graph = NetworkBuilder.undirected().build();

    pnr.load(br, graph);

    // Create a simple layout frame
    // specify the Fruchterman-Rheingold layout algorithm
    LayoutAlgorithm<Number> algorithm = new FRLayoutAlgorithm<>();
    LayoutModel<Number> delegateModel =
        LoadingCacheLayoutModel.<Number>builder()
            .setGraph(graph.asGraph())
            .setSize(600, 600)
            .build();

    setLayout(new BorderLayout());

    final AggregateLayoutModel<Number> layoutModel = new AggregateLayoutModel<>(delegateModel);
    VisualizationModel visualizationModel =
        new BaseVisualizationModel(graph, layoutModel, algorithm);

    vv = new VisualizationViewer<>(visualizationModel, new Dimension(800, 800));
    vv.setBackground(Color.white);
    // Tell the renderer to use our own customized color rendering
    vv.getRenderContext().setNodeFillPaintFunction(nodePaints);
    vv.getRenderContext()
        .setNodeDrawPaintFunction(
            v -> vv.getPickedNodeState().isPicked(v) ? Color.CYAN : Color.BLACK);

    vv.getRenderContext().setEdgeDrawPaintFunction(edgePaints);

    vv.getRenderContext()
        .setEdgeStrokeFunction(e -> edgePaints.getUnchecked(e) == Color.LIGHT_GRAY ? THIN : THICK);

    // add restart button
    JButton scramble = new JButton("Restart");
    scramble.addActionListener(
        e -> {
          LayoutAlgorithm<Number> layoutAlgorithm = vv.getModel().getLayoutAlgorithm();
          vv.getModel().getLayoutModel().accept(layoutAlgorithm);
        });

    DefaultModalGraphMouse<Number, Number> gm = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(gm);

    final JToggleButton groupNodes = new JToggleButton("Group Clusters");

    // Create slider to adjust the number of edges to remove when clustering
    final JSlider edgeBetweennessSlider = new JSlider(JSlider.HORIZONTAL);
    edgeBetweennessSlider.setBackground(Color.WHITE);
    edgeBetweennessSlider.setPreferredSize(new Dimension(210, 50));
    edgeBetweennessSlider.setPaintTicks(true);
    edgeBetweennessSlider.setMaximum(graph.edges().size());
    edgeBetweennessSlider.setMinimum(0);
    edgeBetweennessSlider.setValue(0);
    edgeBetweennessSlider.setMajorTickSpacing(10);
    edgeBetweennessSlider.setPaintLabels(true);
    edgeBetweennessSlider.setPaintTicks(true);

    //		edgeBetweennessSlider.setBorder(BorderFactory.createLineBorder(Color.black));
    // TO DO: edgeBetweennessSlider.add(new JLabel("Node Size (PageRank With Priors):"));
    // I also want the slider value to appear
    final JPanel eastControls = new JPanel();
    eastControls.setOpaque(true);
    eastControls.setLayout(new BoxLayout(eastControls, BoxLayout.Y_AXIS));
    eastControls.add(Box.createVerticalGlue());
    eastControls.add(edgeBetweennessSlider);

    final String COMMANDSTRING = "Edges removed for clusters: ";
    final String eastSize = COMMANDSTRING + edgeBetweennessSlider.getValue();

    final TitledBorder sliderBorder = BorderFactory.createTitledBorder(eastSize);
    eastControls.setBorder(sliderBorder);
    eastControls.add(Box.createVerticalGlue());

    groupNodes.addItemListener(
        e -> {
          clusterAndRecolor(
              layoutModel,
              graph,
              edgeBetweennessSlider.getValue(),
              similarColors,
              e.getStateChange() == ItemEvent.SELECTED);
          vv.repaint();
        });

    clusterAndRecolor(layoutModel, graph, 0, similarColors, groupNodes.isSelected());

    edgeBetweennessSlider.addChangeListener(
        e -> {
          JSlider source = (JSlider) e.getSource();
          if (!source.getValueIsAdjusting()) {
            int numEdgesToRemove = source.getValue();
            clusterAndRecolor(
                layoutModel, graph, numEdgesToRemove, similarColors, groupNodes.isSelected());
            sliderBorder.setTitle(COMMANDSTRING + edgeBetweennessSlider.getValue());
            eastControls.repaint();
            vv.validate();
            vv.repaint();
          }
        });

    add(new GraphZoomScrollPane(vv));
    JPanel south = new JPanel();
    JPanel grid = new JPanel(new GridLayout(2, 1));
    grid.add(scramble);
    grid.add(groupNodes);
    south.add(grid);
    south.add(eastControls);
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    p.add(gm.getModeComboBox());
    south.add(p);
    add(south, BorderLayout.SOUTH);
  }

  public void clusterAndRecolor(
      AggregateLayoutModel<Number> layoutModel,
      Network<Number, Number> graph,
      int numEdgesToRemove,
      Color[] colors,
      boolean groupClusters) {

    layoutModel.removeAll();

    EdgeBetweennessClusterer<Number, Number> clusterer =
        new EdgeBetweennessClusterer<>(numEdgesToRemove);
    Set<Set<Number>> clusterSet = clusterer.apply(graph);
    Set<Number> edges = clusterer.getEdgesRemoved();

    int i = 0;
    // Set the colors of each node so that each cluster's nodes have the same color
    for (Iterator<Set<Number>> cIt = clusterSet.iterator(); cIt.hasNext(); ) {

      Set<Number> nodes = cIt.next();
      Color c = colors[i % colors.length];

      colorCluster(nodes, c);
      if (groupClusters == true) {
        groupCluster(layoutModel, nodes);
      }
      i++;
    }
    for (Number e : graph.edges()) {
      edgePaints.put(e, edges.contains(e) ? Color.LIGHT_GRAY : Color.BLACK);
    }
  }

  private void colorCluster(Set<Number> nodes, Color c) {
    for (Number v : nodes) {
      nodePaints.put(v, c);
    }
  }

  private void groupCluster(AggregateLayoutModel<Number> layoutModel, Set<Number> nodes) {
    if (nodes.size() < vv.getModel().getNetwork().nodes().size()) {
      Point center = layoutModel.apply(nodes.iterator().next());
      MutableNetwork<Number, Number> subGraph = NetworkBuilder.undirected().build();
      for (Number v : nodes) {
        subGraph.addNode(v);
      }
      LayoutAlgorithm<Number> subLayoutAlgorithm = new CircleLayoutAlgorithm<>();

      LayoutModel<Number> subModel =
          LoadingCacheLayoutModel.<Number>builder()
              .setGraph(subGraph.asGraph())
              .setSize(40, 40)
              .build();

      layoutModel.put(subModel, center);
      subModel.accept(subLayoutAlgorithm);
      vv.repaint();
    }
  }
}
