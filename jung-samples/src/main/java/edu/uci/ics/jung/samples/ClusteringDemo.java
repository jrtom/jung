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
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.layout.*;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ItemEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

/**
 * This simple app demonstrates how one can use our algorithms and visualization libraries in
 * unison. In this case, we generate use the Zachary karate club data set, widely known in the
 * social networks literature, then we cluster the vertices using an edge-betweenness clusterer, and
 * finally we visualize the graph using Fruchtermain-Rheingold layout and provide a slider so that
 * the user can adjust the clustering granularity.
 *
 * @author Scott White
 */
@SuppressWarnings("serial")
public class ClusteringDemo extends JApplet {

  VisualizationViewer<Number, Number> vv;
  DomainModel<Point2D> domainModel = new AWTDomainModel();

  LoadingCache<Number, Paint> vertexPaints =
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

    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    PajekNetReader<MutableNetwork<Number, Number>, Number, Number> pnr =
        new PajekNetReader<MutableNetwork<Number, Number>, Number, Number>(
            vertexFactory, edgeFactory);

    final MutableNetwork<Number, Number> graph = NetworkBuilder.undirected().build();

    pnr.load(br, graph);

    //Create a simple layout frame
    //specify the Fruchterman-Rheingold layout algorithm
    LayoutAlgorithm<Number, Point2D> algorithm = new FRLayoutAlgorithm<>(domainModel);
    LayoutModel<Number, Point2D> delegateModel =
        new LoadingCacheLayoutModel<Number, Point2D>(graph.asGraph(), domainModel, 600, 600);

    final AggregateLayoutModel<Number, Point2D> layoutModel =
        new AggregateLayoutModel<Number, Point2D>(delegateModel);
    VisualizationModel visualizationModel =
        new BaseVisualizationModel(graph, layoutModel, algorithm);

    vv = new VisualizationViewer<>(visualizationModel);
    vv.setBackground(Color.white);
    //Tell the renderer to use our own customized color rendering
    vv.getRenderContext().setVertexFillPaintTransformer(vertexPaints);
    vv.getRenderContext()
        .setVertexDrawPaintTransformer(
            v -> vv.getPickedVertexState().isPicked(v) ? Color.CYAN : Color.BLACK);

    vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaints);

    vv.getRenderContext()
        .setEdgeStrokeTransformer(
            e -> edgePaints.getUnchecked(e) == Color.LIGHT_GRAY ? THIN : THICK);

    //add restart button
    JButton scramble = new JButton("Restart");
    scramble.addActionListener(
        e -> {
          LayoutAlgorithm<Number, Point2D> layoutAlgorithm = vv.getModel().getLayoutAlgorithm();
          vv.getModel().getLayoutModel().accept(layoutAlgorithm);
        });

    DefaultModalGraphMouse<Number, Number> gm = new DefaultModalGraphMouse<Number, Number>();
    vv.setGraphMouse(gm);

    final JToggleButton groupVertices = new JToggleButton("Group Clusters");

    //Create slider to adjust the number of edges to remove when clustering
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
    //TO DO: edgeBetweennessSlider.add(new JLabel("Node Size (PageRank With Priors):"));
    //I also want the slider value to appear
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

    groupVertices.addItemListener(
        e -> {
          clusterAndRecolor(
              layoutModel,
              graph,
              edgeBetweennessSlider.getValue(),
              similarColors,
              e.getStateChange() == ItemEvent.SELECTED);
          vv.repaint();
        });

    clusterAndRecolor(layoutModel, graph, 0, similarColors, groupVertices.isSelected());

    edgeBetweennessSlider.addChangeListener(
        e -> {
          JSlider source = (JSlider) e.getSource();
          if (!source.getValueIsAdjusting()) {
            int numEdgesToRemove = source.getValue();
            clusterAndRecolor(
                layoutModel, graph, numEdgesToRemove, similarColors, groupVertices.isSelected());
            sliderBorder.setTitle(COMMANDSTRING + edgeBetweennessSlider.getValue());
            eastControls.repaint();
            vv.validate();
            vv.repaint();
          }
        });

    Container content = getContentPane();
    content.add(new GraphZoomScrollPane(vv));
    JPanel south = new JPanel();
    JPanel grid = new JPanel(new GridLayout(2, 1));
    grid.add(scramble);
    grid.add(groupVertices);
    south.add(grid);
    south.add(eastControls);
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    p.add(gm.getModeComboBox());
    south.add(p);
    content.add(south, BorderLayout.SOUTH);
  }

  public void clusterAndRecolor(
      AggregateLayoutModel<Number, Point2D> layoutModel,
      Network<Number, Number> graph,
      int numEdgesToRemove,
      Color[] colors,
      boolean groupClusters) {

    layoutModel.removeAll();

    EdgeBetweennessClusterer<Number, Number> clusterer =
        new EdgeBetweennessClusterer<Number, Number>(numEdgesToRemove);
    Set<Set<Number>> clusterSet = clusterer.apply(graph);
    Set<Number> edges = clusterer.getEdgesRemoved();

    int i = 0;
    //Set the colors of each node so that each cluster's vertices have the same color
    for (Iterator<Set<Number>> cIt = clusterSet.iterator(); cIt.hasNext(); ) {

      Set<Number> vertices = cIt.next();
      Color c = colors[i % colors.length];

      colorCluster(vertices, c);
      if (groupClusters == true) {
        groupCluster(layoutModel, vertices);
      }
      i++;
    }
    for (Number e : graph.edges()) {
      edgePaints.put(e, edges.contains(e) ? Color.LIGHT_GRAY : Color.BLACK);
    }
  }

  private void colorCluster(Set<Number> vertices, Color c) {
    for (Number v : vertices) {
      vertexPaints.put(v, c);
    }
  }

  private void groupCluster(
      AggregateLayoutModel<Number, Point2D> layoutModel, Set<Number> vertices) {
    if (vertices.size() < vv.getModel().getNetwork().nodes().size()) {
      Point2D center = layoutModel.apply(vertices.iterator().next());
      MutableNetwork<Number, Number> subGraph = NetworkBuilder.undirected().build();
      for (Number v : vertices) {
        subGraph.addNode(v);
      }
      LayoutAlgorithm<Number, Point2D> subLayoutAlgorithm =
          new CircleLayoutAlgorithm<>(domainModel);

      LayoutModel<Number, Point2D> subModel =
          new LoadingCacheLayoutModel(subGraph.asGraph(), domainModel, 40, 40);
      layoutModel.put(subModel, center);
      subModel.accept(subLayoutAlgorithm);
      vv.repaint();
    }
  }
}
