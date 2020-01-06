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
import edu.uci.ics.jung.layout.algorithms.CircleLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.ISOMLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.KKLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.SpringLayoutAlgorithm;
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
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithmTransition;
import edu.uci.ics.jung.visualization.subLayout.GraphCollapser;
import edu.uci.ics.jung.visualization.util.PredicatedParallelEdgeIndexFunction;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * types: String for plain nodes, and {@code Network<String, Number>} for the collapsed nodes.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class NodeCollapseDemoWithLayouts extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(NodeCollapseDemoWithLayouts.class);

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
  @SuppressWarnings("rawtypes")
  Network graph;

  enum Layouts {
    KAMADA_KAWAI,
    FRUCHTERMAN_REINGOLD,
    CIRCLE,
    SPRING,
    SELF_ORGANIZING_MAP
  };

  /** the visual component and renderer for the graph */
  @SuppressWarnings("rawtypes")
  VisualizationViewer vv;

  @SuppressWarnings("rawtypes")
  LayoutAlgorithm layoutAlgorithm;

  GraphCollapser collapser;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public NodeCollapseDemoWithLayouts() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = TestGraphs.getOneComponentGraph();

    collapser = new GraphCollapser(graph);

    layoutAlgorithm = new FRLayoutAlgorithm();

    Dimension preferredSize = new Dimension(600, 600);
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

    Layouts[] combos = getCombos();
    final JComboBox jcb = new JComboBox(combos);
    // use a renderer to shorten the layout name presentation
    //        jcb.setRenderer(new DefaultListCellRenderer() {
    //            public Component getListCellRendererComponent(JList list, Object value, int index,
    // boolean isSelected, boolean cellHasFocus) {
    //                String valueString = value.toString();
    //                valueString = valueString.substring(valueString.lastIndexOf('.')+1);
    //                return super.getListCellRendererComponent(list, valueString, index,
    // isSelected,
    //                        cellHasFocus);
    //            }
    //        });
    jcb.addActionListener(new LayoutChooser(jcb, vv));

    jcb.setSelectedItem(Layouts.FRUCHTERMAN_REINGOLD);

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(
        e -> {
          Collection picked = new HashSet(vv.getPickedNodeState().getPicked());
          if (picked.size() > 1) {
            LayoutModel layoutModel = vv.getModel().getLayoutModel();
            Network inGraph = vv.getModel().getNetwork();
            Network clusterGraph = collapser.getClusterGraph(inGraph, picked);
            Network g = collapser.collapse(inGraph, clusterGraph);
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
            log.trace("put the cluster at " + cp);
            layoutModel.lock(clusterGraph, true);
            vv.getModel().setNetwork(g);
            layoutModel.lock(clusterGraph, false);

            vv.getRenderContext().getParallelEdgeIndexFunction().reset();
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

              vv.getModel().setNetwork(g);

              vv.getRenderContext().getParallelEdgeIndexFunction().reset();
            }
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
          layoutAlgorithm = createLayout((Layouts) jcb.getSelectedItem());
          LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
          exclusions.clear();
          vv.repaint();
        });

    JButton help = new JButton("Help");
    help.addActionListener(
        e ->
            JOptionPane.showMessageDialog(
                (JComponent) e.getSource(), instructions, "Help", JOptionPane.PLAIN_MESSAGE));

    JPanel controls = new JPanel();
    controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));
    JPanel collapseControls = new JPanel(new GridLayout(0, 1));
    collapseControls.setBorder(BorderFactory.createTitledBorder("Picked"));
    collapseControls.add(collapse);
    collapseControls.add(expand);
    collapseControls.add(compressEdges);
    collapseControls.add(expandEdges);
    collapseControls.add(reset);
    controls.add(collapseControls);
    JPanel modePanel = new JPanel();
    modePanel.add(modeBox);
    controls.add(modePanel);
    JPanel jcbPanel = new JPanel();
    jcbPanel.add(jcb);
    controls.add(jcbPanel);
    controls.add(help);
    add(controls, BorderLayout.EAST);
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
      setSizeTransformer(new ClusterNodeSizeFunction<>(20));
    }

    @Override
    public Shape apply(N v) {
      if (v instanceof Network) {
        @SuppressWarnings("rawtypes")
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

  private static <N> LayoutAlgorithm<N> createLayout(Layouts layoutType) {
    switch (layoutType) {
      case CIRCLE:
        return new CircleLayoutAlgorithm<>();
      case FRUCHTERMAN_REINGOLD:
        return new FRLayoutAlgorithm<>();
      case KAMADA_KAWAI:
        return new KKLayoutAlgorithm<>();
      case SELF_ORGANIZING_MAP:
        return new ISOMLayoutAlgorithm<>();
      case SPRING:
        return new SpringLayoutAlgorithm<>();
      default:
        throw new IllegalArgumentException("Unrecognized layout type");
    }
  }

  private class LayoutChooser implements ActionListener {
    private final JComboBox<?> jcb;

    @SuppressWarnings("rawtypes")
    private final VisualizationViewer vv;

    private LayoutChooser(JComboBox<?> jcb, VisualizationViewer<Object, ?> vv) {
      super();
      this.jcb = jcb;
      this.vv = vv;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void actionPerformed(ActionEvent arg0) {
      Layouts layoutType = (Layouts) jcb.getSelectedItem();

      try {
        layoutAlgorithm = createLayout(layoutType);
        LayoutAlgorithmTransition.animate(vv, layoutAlgorithm);
        vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
        vv.repaint();

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private Layouts[] getCombos() {
    return Layouts.values();
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new NodeCollapseDemoWithLayouts());
    f.pack();
    f.setVisible(true);
  }
}
