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
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EllipseNodeShapeFunction;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
import java.awt.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A demo that shows how collections of nodes can be collapsed into a single node. In this demo, the
 * nodes that are collapsed are those mouse-picked by the user. Any criteria could be used to form
 * the node collections to be collapsed, perhaps some common characteristic of those node objects.
 *
 * <p>Note that the collection types don't use generics in this demo, because the nodes are of two
 * types: String for plain nodes, and {@code Network<String,Number>} for the collapsed nodes.
 *
 * @author Tom Nelson
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class NodeCollapseDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(NodeCollapseDemo.class);

  String instructions =
      "<html>Use the mouse to select multiple nodes"
          + "<p>either by dragging a region, or by shift-clicking"
          + "<p>on multiple nodes."
          + "<p>After you select nodes, use the Collapse button"
          + "<p>to combine them into a single node."
          + "<p>Select a 'collapsed' node and use the Expand button"
          + "<p>to restore the collapsed nodes."
          + "<p>The Restore button will restore the original graph."
          + "<p>If you select 2 (and only 2) nodes, then press"
          + "<p>the Compress Edges button, parallel edges between"
          + "<p>those two nodes will no longer be expanded."
          + "<p>If you select 2 (and only 2) nodes, then press"
          + "<p>the Expand Edges button, parallel edges between"
          + "<p>those two nodes will be expanded."
          + "<p>You can drag the nodes with the mouse."
          + "<p>Use the 'Picking'/'Transforming' combo-box to switch"
          + "<p>between picking and transforming mode.</html>";
  /** the graph */
  Network graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer vv;

  LayoutAlgorithm layoutAlgorithm;

  GraphCollapser collapser;

  public NodeCollapseDemo() {

    setLayout(new BorderLayout());

    // create a simple graph for the demo
    graph =
        //            getSmallGraph();
        TestGraphs.getOneComponentGraph();

    collapser = new GraphCollapser(graph);

    layoutAlgorithm = new FRLayoutAlgorithm();

    Dimension preferredSize = new Dimension(400, 400);

    final VisualizationModel visualizationModel =
        new BaseVisualizationModel(graph, layoutAlgorithm, preferredSize);
    vv = new VisualizationViewer(visualizationModel, preferredSize);

    vv.getRenderContext().setNodeShapeFunction(new ClusterNodeShapeFunction());

    final Set exclusions = new HashSet();
    final PredicatedParallelEdgeIndexFunction eif =
        new PredicatedParallelEdgeIndexFunction(exclusions::contains);

    vv.getRenderContext().setParallelEdgeIndexFunction(eif);

    vv.setBackground(Color.white);

    // add a listener for ToolTips
    vv.setNodeToolTipFunction(
        v -> {
          if (v instanceof Network) {
            return ((Network) v).nodes().toString();
          }
          return v;
        });

    /** the regular graph mouse for the normal view */
    final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

    vv.setGraphMouse(graphMouse);

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    add(gzsp);

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(graphMouse.getModeListener());
    graphMouse.setMode(ModalGraphMouse.Mode.PICKING);

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        e -> {
          Collection picked = new HashSet(vv.getPickedNodeState().getPicked());
          if (picked.size() > 1) {
            Network inGraph = vv.getModel().getNetwork();
            LayoutModel layoutModel = vv.getModel().getLayoutModel();
            Network clusterGraph = collapser.getClusterGraph(inGraph, picked);
            log.info("clusterGraph:" + clusterGraph);
            Network g = collapser.collapse(inGraph, clusterGraph);
            log.info("g:" + g);

            double sumx = 0;
            double sumy = 0;
            for (Object v : picked) {
              Point p = (Point) layoutModel.apply(v);
              sumx += p.x;
              sumy += p.y;
            }
            Point cp = Point.of(sumx / picked.size(), sumy / picked.size());
            layoutModel.lock(false);
            layoutModel.set(clusterGraph, cp);
            log.info("put the cluster at " + cp);
            layoutModel.lock(clusterGraph, true);
            layoutModel.lock(true);
            vv.getModel().setNetwork(g);

            vv.getRenderContext().getParallelEdgeIndexFunction().reset();
            layoutModel.accept(vv.getModel().getLayoutAlgorithm());
            vv.getPickedNodeState().clear();
            vv.repaint();
          }
        });

    JButton expand = new JButton("Expand");
    expand.addActionListener(
        e -> {
          Collection picked = new HashSet(vv.getPickedNodeState().getPicked());
          for (Object v : picked) {
            if (v instanceof Network) {
              Network inGraph = vv.getModel().getNetwork();
              LayoutModel layoutModel = vv.getModel().getLayoutModel();
              Network g = collapser.expand(graph, inGraph, (Network) v);

              layoutModel.lock(false);
              vv.getModel().setNetwork(g);

              vv.getRenderContext().getParallelEdgeIndexFunction().reset();
              //                vv.getModel().setLayout(layout);
            }
            vv.getPickedNodeState().clear();
            vv.repaint();
          }
        });

    JButton compressEdges = new JButton("Compress Edges");
    compressEdges.addActionListener(
        e -> {
          Set picked = vv.getPickedNodeState().getPicked();
          if (picked.size() == 2) {
            Iterator pickedIter = picked.iterator();
            Object nodeU = pickedIter.next();
            Object nodeV = pickedIter.next();
            Network graph = vv.getModel().getNetwork();
            Collection edges = new HashSet(graph.incidentEdges(nodeU));
            edges.retainAll(graph.incidentEdges(nodeV));
            exclusions.addAll(edges);
            vv.repaint();
          }
        });

    JButton expandEdges = new JButton("Expand Edges");
    expandEdges.addActionListener(
        e -> {
          Set picked = vv.getPickedNodeState().getPicked();
          if (picked.size() == 2) {
            Iterator pickedIter = picked.iterator();
            Object nodeU = pickedIter.next();
            Object nodeV = pickedIter.next();
            Network graph = vv.getModel().getNetwork();
            Collection edges = new HashSet(graph.incidentEdges(nodeU));
            edges.retainAll(graph.incidentEdges(nodeV));
            exclusions.removeAll(edges);
            vv.repaint();
          }
        });

    JButton reset = new JButton("Reset");
    reset.addActionListener(
        e -> {
          vv.getModel().setNetwork(graph);
          exclusions.clear();
          vv.repaint();
        });

    JButton help = new JButton("Help");
    help.addActionListener(
        e ->
            JOptionPane.showMessageDialog(
                (JComponent) e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE));

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
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
    add(controls, BorderLayout.SOUTH);
  }

  /**
   * a demo class that will create a node shape that is either a polygon or star. The number of
   * sides corresponds to the number of nodes that were collapsed into the node represented by this
   * shape.
   *
   * @author Tom Nelson
   * @param <N> the node type
   */
  class ClusterNodeShapeFunction<N> extends EllipseNodeShapeFunction<N> {

    ClusterNodeShapeFunction() {
      setSizeTransformer(new ClusterNodeSizeFunction<N>(20));
    }

    @Override
    public Shape apply(N v) {
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
   * A demo class that will make nodes larger if they represent a collapsed collection of original
   * nodes
   *
   * @author Tom Nelson
   * @param <N> the node type
   */
  class ClusterNodeSizeFunction<N> implements Function<N, Integer> {
    int size;

    public ClusterNodeSizeFunction(Integer size) {
      this.size = size;
    }

    public Integer apply(N v) {
      if (v instanceof Network) {
        return 30;
      }
      return size;
    }
  }

  public static Network<String, Number> getSmallGraph() {
    MutableNetwork g = NetworkBuilder.undirected().allowsParallelEdges(true).build();

    int nodeIt;
    int current;
    String i;
    String next;
    for (nodeIt = 1; nodeIt <= 3; ++nodeIt) {
      for (current = nodeIt + 1; current <= 3; ++current) {
        i = "" + nodeIt;
        next = "" + current;
        g.addEdge(i, next, Math.pow((double) (nodeIt + 2), (double) current));
      }
    }

    for (nodeIt = 11; nodeIt <= 4; ++nodeIt) {
      for (current = nodeIt + 1; current <= 4; ++current) {
        if (Math.random() <= 0.6D) {
          i = "" + nodeIt;
          next = "" + current;
          g.addEdge(i, next, Math.pow((double) (nodeIt + 2), (double) current));
        }
      }
    }

    //    Iterator var5 = g.nodes().iterator();
    //    String var6 = (String) var5.next();
    //    int var7 = 0;

    //    while(var5.hasNext()) {
    //      next = (String)var5.next();
    //      g.addEdge(var6, next, new Integer(var7++));
    //    }

    return g;
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new NodeCollapseDemo());
    f.pack();
    f.setVisible(true);
  }
}
