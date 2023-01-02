/*
 * Copyright (c) 2004, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 *
 * Created on Nov 7, 2004
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.algorithms.scoring.VoltageScorer;
import edu.uci.ics.jung.algorithms.scoring.util.NodeScoreTransformer;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.renderers.CenterEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.NodeLabel.Position;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.*;

/**
 * Shows off some of the capabilities of <code>PluggableRenderer</code>. This code provides examples
 * of different ways to provide and change the various functions that provide property information
 * to the renderer.
 *
 * <p>This demo creates a random graph with random edge weights. It then runs <code>VoltageRanker
 * </code> on this graph, using half of the "seed" nodes from the random graph generation as voltage
 * sources, and half of them as voltage sinks.
 *
 * <p>What the controls do:
 *
 * <ul>
 *   <li>Mouse controls:
 *       <ul>
 *         <li>If your mouse has a scroll wheel, scrolling forward zooms out and scrolling backward
 *             zooms in.
 *         <li>Left-clicking on a node or edge selects it, and unselects all others.
 *         <li>Middle-clicking on a node or edge toggles its selection state.
 *         <li>Right-clicking on a node brings up a pop-up menu that allows you to increase or
 *             decrease that node's transparency.
 *         <li>Left-clicking on the background allows you to drag the image around.
 *         <li>Hovering over a node tells you what its voltage is; hovering over an edge shows its
 *             identity; hovering over the background shows an informational message.
 *       </ul>
 *   <li>Node stuff:
 *       <ul>
 *         <li>"node seed coloring": if checked, the seed nodes are colored blue, and all other
 *             nodes are colored red. Otherwise, all nodes are colored a slightly transparent red
 *             (except the currently "picked" node, which is colored transparent purple).
 *         <li>"node selection stroke highlighting": if checked, the picked node and its neighbors
 *             are all drawn with heavy borders. Otherwise, all nodes are drawn with light borders.
 *         <li>"show node ranks (voltages)": if checked, each node is labeled with its calculated
 *             'voltage'. Otherwise, nodes are unlabeled.
 *         <li>"node degree shapes": if checked, nodes are drawn with a polygon with number of sides
 *             proportional to its degree. Otherwise, nodes are drawn as ellipses.
 *         <li>"node voltage layoutSize": if checked, nodes are drawn with a layoutSize proportional
 *             to their voltage ranking. Otherwise, all nodes are drawn at the same layoutSize.
 *         <li>"node degree ratio stretch": if checked, nodes are drawn with an aspect ratio
 *             (height/width ratio) proportional to the ratio of their indegree to their outdegree.
 *             Otherwise, nodes are drawn with an aspect ratio of 1.
 *         <li>"filter nodes of degree &lt; 4": if checked, does not display any nodes (or their
 *             incident edges) whose degree in the original graph is less than 4; otherwise, all
 *             nodes are drawn.
 *       </ul>
 *   <li>Edge stuff:
 *       <ul>
 *         <li>"edge shape": selects between lines, wedges, quadratic curves, and cubic curves for
 *             drawing edges.
 *         <li>"fill edge shapes": if checked, fills the edge shapes. This will have no effect if
 *             "line" is selected.
 *         <li>"edge paint": selects between solid colored edges, and gradient-painted edges.
 *             Gradient painted edges are darkest in the middle for undirected edges, and darkest at
 *             the destination for directed edges.
 *         <li>"show edges": only edges of the checked types are drawn.
 *         <li>"show arrows": only arrows whose edges are of the checked types are drawn.
 *         <li>"edge weight highlighting": if checked, edges with weight greater than a threshold
 *             value are drawn using thick solid lines, and other edges are drawn using thin gray
 *             dotted lines. (This combines edge stroke and paint.) Otherwise, all edges are drawn
 *             with thin solid lines.
 *         <li>"show edge weights": if checked, edges are labeled with their weights. Otherwise,
 *             edges are not labeled.
 *       </ul>
 *   <li>Miscellaneous (center panel)
 *       <ul>
 *         <li>"bold text": if checked, all node and edge labels are drawn using a boldface font.
 *             Otherwise, a normal-weight font is used. (Has no effect if no labels are currently
 *             visible.)
 *         <li>zoom controls:
 *             <ul>
 *               <li>"+" zooms in, "-" zooms out
 *               <li>"zoom at mouse (wheel only)": if checked, zooming (using the mouse scroll
 *                   wheel) is centered on the location of the mouse pointer; otherwise, it is
 *                   centered on the center of the visualization pane.
 *             </ul>
 *       </ul>
 * </ul>
 *
 * @author Danyel Fisher, Joshua O'Madadhain, Tom Nelson
 */
@SuppressWarnings("serial")
public class PluggableRendererDemo extends JPanel implements ActionListener {

  protected JCheckBox v_color;
  protected JCheckBox e_color;
  protected JCheckBox v_stroke;
  protected JCheckBox e_arrow_centered;
  protected JCheckBox v_shape;
  protected JCheckBox v_size;
  protected JCheckBox v_aspect;
  protected JCheckBox v_labels;
  protected JRadioButton e_line;
  protected JRadioButton e_bent;
  protected JRadioButton e_wedge;
  protected JRadioButton e_quad;
  protected JRadioButton e_ortho;
  protected JRadioButton e_cubic;
  protected JCheckBox e_labels;
  protected JCheckBox font;
  protected JCheckBox e_filter_small;
  protected JCheckBox e_show_u;
  protected JCheckBox v_small;
  protected JCheckBox zoom_at_mouse;
  protected JCheckBox fill_edges;

  protected JRadioButton no_gradient;
  protected JRadioButton gradient_relative;

  protected static final int GRADIENT_NONE = 0;
  protected static final int GRADIENT_RELATIVE = 1;
  protected static int gradient_level = GRADIENT_NONE;

  protected SeedFillColor<Integer> seedFillColor;
  protected SeedDrawColor<Integer> seedDrawColor;
  protected EdgeWeightStrokeFunction<Number> ewcs;
  protected NodeStrokeHighlight<Integer, Number> vsh;
  protected Function<? super Integer, String> vs;
  protected Function<? super Integer, String> vs_none;
  protected Function<? super Number, String> es;
  protected Function<? super Number, String> es_none;
  protected NodeFontTransformer<Integer> vff;
  protected EdgeFontTransformer<Number> eff;
  protected NodeShapeSizeAspect<Integer, Number> vssa;
  protected NodeDisplayPredicate<Integer> showNode;
  protected EdgeDisplayPredicate<Number> showEdge;
  protected Predicate<Number> self_loop;
  protected GradientPickedEdgePaintFunction<Integer, Number> edgeDrawPaint;
  protected GradientPickedEdgePaintFunction<Integer, Number> edgeFillPaint;
  protected static final Object VOLTAGE_KEY = "voltages";
  protected static final Object TRANSPARENCY = "transparency";

  protected Map<Number, Number> edge_weight = new HashMap<>();
  protected Function<Integer, Double> voltages;
  protected Map<Integer, Number> transparency = new HashMap<>();

  protected VisualizationViewer<Integer, Number> vv;
  protected DefaultModalGraphMouse<Integer, Number> gm;
  protected Set<Integer> seedNodes = new HashSet<>();

  private Network<Integer, Number> graph;

  //  public void start() {
  //    setLayout(new BorderLayout());
  //    add(startFunction());
  //  }

  public static void main(String[] s) {
    JFrame jf = new JFrame();
    jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JPanel jp = new PluggableRendererDemo().startFunction();
    jf.getContentPane().add(jp);
    jf.pack();
    jf.setVisible(true);
  }

  public JPanel startFunction() {
    this.graph = buildGraph();

    LayoutAlgorithm<Integer> layoutAlgorithm = new FRLayoutAlgorithm<>();
    vv = new VisualizationViewer<>(graph, layoutAlgorithm, new Dimension(1000, 800));

    //    vv.getRenderer().setNodeRenderer(new CachingNodeRenderer<Integer, Number>(vv));
    //    vv.getRenderer().setEdgeRenderer(new CachingEdgeRenderer<Integer, Number>(vv));

    PickedState<Integer> picked_state = vv.getPickedNodeState();

    self_loop = (e) -> Graphs.isSelfLoop(graph, e);

    // create decorators
    seedFillColor = new SeedFillColor<>(picked_state);
    seedDrawColor = new SeedDrawColor<>();
    ewcs = new EdgeWeightStrokeFunction<>(edge_weight);
    vsh = new NodeStrokeHighlight<>(graph, picked_state);
    vff = new NodeFontTransformer<>();
    eff = new EdgeFontTransformer<>();
    vs_none = n -> null;
    es_none = e -> null;
    vssa = new NodeShapeSizeAspect<>(graph, voltages);
    showNode = new NodeDisplayPredicate<>(graph, false);
    showEdge = new EdgeDisplayPredicate<>(edge_weight, false);

    // uses a gradient edge if unpicked, otherwise uses picked selection
    edgeDrawPaint =
        new GradientPickedEdgePaintFunction<>(
            new PickableEdgePaintFunction<>(vv.getPickedEdgeState(), Color.black, Color.cyan), vv);
    edgeFillPaint =
        new GradientPickedEdgePaintFunction<>(
            new PickableEdgePaintFunction<>(vv.getPickedEdgeState(), Color.black, Color.cyan), vv);

    vv.getRenderContext().setNodeFillPaintFunction(seedFillColor);
    vv.getRenderContext().setNodeDrawPaintFunction(seedDrawColor);
    vv.getRenderContext().setNodeStrokeFunction(vsh);
    vv.getRenderContext().setNodeLabelFunction(vs_none);
    vv.getRenderContext().setNodeFontFunction(vff);
    vv.getRenderContext().setNodeShapeFunction(vssa);
    vv.getRenderContext().setNodeIncludePredicate(showNode);

    vv.getRenderContext().setEdgeDrawPaintFunction(edgeDrawPaint);
    vv.getRenderContext().setEdgeLabelFunction(es_none);
    vv.getRenderContext().setEdgeFontFunction(eff);
    vv.getRenderContext().setEdgeStrokeFunction(ewcs);
    vv.getRenderContext().setEdgeIncludePredicate(showEdge);
    vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());

    vv.getRenderContext().setArrowFillPaintFunction(n -> Color.lightGray);
    vv.getRenderContext().setArrowDrawPaintFunction(n -> Color.black);
    JPanel jp = new JPanel();
    jp.setPreferredSize(new Dimension(800, 800));
    jp.setLayout(new BorderLayout());

    vv.setBackground(Color.white);
    GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(vv);
    jp.add(scrollPane);
    gm = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(gm);
    gm.add(new PopupGraphMousePlugin());

    addBottomControls(jp);
    vssa.setScaling(true);

    vv.setNodeToolTipFunction(new VoltageTips<Number>());
    vv.setToolTipText(
        "<html><center>Use the mouse wheel to zoom<p>Click and Drag the mouse to pan<p>Shift-click and Drag to Rotate</center></html>");

    return jp;
  }

  /**
   * Generates a random graph, runs VoltageRanker on it, and returns the resultant graph.
   *
   * @return the generated graph
   */
  public Network<Integer, Number> buildGraph() {
    Supplier<Integer> nodeFactory =
        new Supplier<Integer>() {
          int count;

          public Integer get() {
            return count++;
          }
        };
    Supplier<Number> edgeFactory =
        new Supplier<Number>() {
          int count;

          public Number get() {
            return count++;
          }
        };
    BarabasiAlbertGenerator<Integer, Number> generator =
        new BarabasiAlbertGenerator<>(
            NetworkBuilder.directed().allowsSelfLoops(true).allowsParallelEdges(true),
            nodeFactory,
            edgeFactory,
            4,
            3);
    generator.evolveGraph(200);
    MutableNetwork<Integer, Number> g = generator.get();
    for (Number e : g.edges()) {
      edge_weight.put(e, Math.random());
    }
    es = new NumberFormattingFunction<>(edge_weight::get);

    // collect the seeds used to define the random graph
    seedNodes = generator.seedNodes();

    if (seedNodes.size() < 2) {
      System.out.println("need at least 2 seeds (one source, one sink)");
    }

    // use these seeds as source and sink nodes, run VoltageRanker
    boolean source = true;
    Set<Integer> sources = new HashSet<>();
    Set<Integer> sinks = new HashSet<>();
    for (Integer v : seedNodes) {
      if (source) {
        sources.add(v);
      } else {
        sinks.add(v);
      }
      source = !source;
    }
    VoltageScorer<Integer, Number> voltage_scores =
        new VoltageScorer<>(g, edge_weight::get, sources, sinks);
    voltage_scores.evaluate();
    voltages = new NodeScoreTransformer<>(voltage_scores);
    vs = new NumberFormattingFunction<>(voltages);

    Collection<Integer> verts = g.nodes();

    // assign a transparency value of 0.9 to all nodes
    for (Integer v : verts) {
      transparency.put(v, 0.9);
    }

    // add a couple of self-loops (sanity check on rendering)
    Integer v = verts.iterator().next();
    Number e = Math.random();
    edge_weight.put(e, e);
    g.addEdge(v, v, e);
    e = Math.random();
    edge_weight.put(e, e);
    g.addEdge(v, v, e);
    return g;
  }

  /**
   * @param jp panel to which controls will be added
   */
  protected void addBottomControls(final JPanel jp) {
    final JPanel control_panel = new JPanel();
    jp.add(control_panel, BorderLayout.EAST);
    control_panel.setLayout(new BorderLayout());
    final Box nodePanel = Box.createVerticalBox();
    nodePanel.setBorder(BorderFactory.createTitledBorder("Nodes"));
    final Box edgePanel = Box.createVerticalBox();
    edgePanel.setBorder(BorderFactory.createTitledBorder("Edges"));
    final Box bothPanel = Box.createVerticalBox();

    control_panel.add(nodePanel, BorderLayout.NORTH);
    control_panel.add(edgePanel, BorderLayout.SOUTH);
    control_panel.add(bothPanel, BorderLayout.CENTER);

    // set up node controls
    v_color = new JCheckBox("seed highlight");
    v_color.addActionListener(this);
    v_stroke = new JCheckBox("stroke highlight on selection");
    v_stroke.addActionListener(this);
    v_labels = new JCheckBox("show voltage values");
    v_labels.addActionListener(this);
    v_shape = new JCheckBox("shape by degree");
    v_shape.addActionListener(this);
    v_size = new JCheckBox("layoutSize by voltage");
    v_size.addActionListener(this);
    v_size.setSelected(true);
    v_aspect = new JCheckBox("stretch by degree ratio");
    v_aspect.addActionListener(this);
    v_small = new JCheckBox("filter when degree < " + NodeDisplayPredicate.MIN_DEGREE);
    v_small.addActionListener(this);

    nodePanel.add(v_color);
    nodePanel.add(v_stroke);
    nodePanel.add(v_labels);
    nodePanel.add(v_shape);
    nodePanel.add(v_size);
    nodePanel.add(v_aspect);
    nodePanel.add(v_small);

    // set up edge controls
    JPanel gradient_panel = new JPanel(new GridLayout(1, 0));
    gradient_panel.setBorder(BorderFactory.createTitledBorder("Edge paint"));
    no_gradient = new JRadioButton("Solid color");
    no_gradient.addActionListener(this);
    no_gradient.setSelected(true);
    //		gradient_absolute = new JRadioButton("Absolute gradient");
    //		gradient_absolute.addActionListener(this);
    gradient_relative = new JRadioButton("Gradient");
    gradient_relative.addActionListener(this);
    ButtonGroup bg_grad = new ButtonGroup();
    bg_grad.add(no_gradient);
    bg_grad.add(gradient_relative);
    // bg_grad.add(gradient_absolute);
    gradient_panel.add(no_gradient);
    // gradientGrid.add(gradient_absolute);
    gradient_panel.add(gradient_relative);

    JPanel shape_panel = new JPanel(new GridLayout(3, 2));
    shape_panel.setBorder(BorderFactory.createTitledBorder("Edge shape"));
    e_line = new JRadioButton("line");
    e_line.addActionListener(this);
    e_line.setSelected(true);
    //        e_bent = new JRadioButton("bent line");
    //        e_bent.addActionListener(this);
    e_wedge = new JRadioButton("wedge");
    e_wedge.addActionListener(this);
    e_quad = new JRadioButton("quad curve");
    e_quad.addActionListener(this);
    e_cubic = new JRadioButton("cubic curve");
    e_cubic.addActionListener(this);
    e_ortho = new JRadioButton("orthogonal");
    e_ortho.addActionListener(this);
    ButtonGroup bg_shape = new ButtonGroup();
    bg_shape.add(e_line);
    //        bg.add(e_bent);
    bg_shape.add(e_wedge);
    bg_shape.add(e_quad);
    bg_shape.add(e_ortho);
    bg_shape.add(e_cubic);
    shape_panel.add(e_line);
    //        shape_panel.add(e_bent);
    shape_panel.add(e_wedge);
    shape_panel.add(e_quad);
    shape_panel.add(e_cubic);
    shape_panel.add(e_ortho);
    fill_edges = new JCheckBox("fill edge shapes");
    fill_edges.setSelected(false);
    fill_edges.addActionListener(this);
    shape_panel.add(fill_edges);
    shape_panel.setOpaque(true);
    e_color = new JCheckBox("highlight edge weights");
    e_color.addActionListener(this);
    e_labels = new JCheckBox("show edge weight values");
    e_labels.addActionListener(this);
    e_arrow_centered = new JCheckBox("centered");
    e_arrow_centered.addActionListener(this);
    JPanel arrow_panel = new JPanel(new GridLayout(1, 0));
    arrow_panel.setBorder(BorderFactory.createTitledBorder("Show arrows"));
    arrow_panel.add(e_arrow_centered);

    e_filter_small = new JCheckBox("filter small-weight edges");
    e_filter_small.addActionListener(this);
    e_filter_small.setSelected(true);
    JPanel show_edge_panel = new JPanel(new GridLayout(1, 0));
    show_edge_panel.setBorder(BorderFactory.createTitledBorder("Show edges"));
    show_edge_panel.add(e_filter_small);

    shape_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    edgePanel.add(shape_panel);
    gradient_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    edgePanel.add(gradient_panel);
    show_edge_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    edgePanel.add(show_edge_panel);
    arrow_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    edgePanel.add(arrow_panel);

    e_color.setAlignmentX(Component.LEFT_ALIGNMENT);
    edgePanel.add(e_color);
    e_labels.setAlignmentX(Component.LEFT_ALIGNMENT);
    edgePanel.add(e_labels);

    // set up zoom controls
    zoom_at_mouse = new JCheckBox("<html><center>zoom at mouse<p>(wheel only)</center></html>");
    zoom_at_mouse.addActionListener(this);
    zoom_at_mouse.setSelected(true);

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JPanel zoomPanel = new JPanel();
    zoomPanel.setBorder(BorderFactory.createTitledBorder("Zoom"));
    plus.setAlignmentX(Component.CENTER_ALIGNMENT);
    zoomPanel.add(plus);
    minus.setAlignmentX(Component.CENTER_ALIGNMENT);
    zoomPanel.add(minus);
    zoom_at_mouse.setAlignmentX(Component.CENTER_ALIGNMENT);
    zoomPanel.add(zoom_at_mouse);

    JPanel fontPanel = new JPanel();
    // add font and zoom controls to center panel
    font = new JCheckBox("bold text");
    font.addActionListener(this);
    font.setAlignmentX(Component.CENTER_ALIGNMENT);
    fontPanel.add(font);

    bothPanel.add(zoomPanel);
    bothPanel.add(fontPanel);

    JComboBox<?> modeBox = gm.getModeComboBox();
    modeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    JPanel modePanel =
        new JPanel(new BorderLayout()) {
          public Dimension getMaximumSize() {
            return getPreferredSize();
          }
        };
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);
    JPanel comboGrid = new JPanel(new GridLayout(0, 1));
    comboGrid.add(modePanel);
    fontPanel.add(comboGrid);

    JComboBox<Position> cb = new JComboBox<>();
    cb.addItem(Renderer.NodeLabel.Position.N);
    cb.addItem(Renderer.NodeLabel.Position.NE);
    cb.addItem(Renderer.NodeLabel.Position.E);
    cb.addItem(Renderer.NodeLabel.Position.SE);
    cb.addItem(Renderer.NodeLabel.Position.S);
    cb.addItem(Renderer.NodeLabel.Position.SW);
    cb.addItem(Renderer.NodeLabel.Position.W);
    cb.addItem(Renderer.NodeLabel.Position.NW);
    cb.addItem(Renderer.NodeLabel.Position.N);
    cb.addItem(Renderer.NodeLabel.Position.CNTR);
    cb.addItem(Renderer.NodeLabel.Position.AUTO);
    cb.addItemListener(
        e -> {
          Renderer.NodeLabel.Position position = (Renderer.NodeLabel.Position) e.getItem();
          vv.getRenderer().getNodeLabelRenderer().setPosition(position);
          vv.repaint();
        });
    cb.setSelectedItem(Renderer.NodeLabel.Position.SE);
    JPanel positionPanel = new JPanel();
    positionPanel.setBorder(BorderFactory.createTitledBorder("Label Position"));
    positionPanel.add(cb);

    comboGrid.add(positionPanel);
  }

  public void actionPerformed(ActionEvent e) {
    AbstractButton source = (AbstractButton) e.getSource();
    if (source == v_color) {
      seedFillColor.setSeedColoring(source.isSelected());
    } else if (source == e_color) {
      ewcs.setWeighted(source.isSelected());
    } else if (source == v_stroke) {
      vsh.setHighlight(source.isSelected());
    } else if (source == v_labels) {
      if (source.isSelected()) {
        vv.getRenderContext().setNodeLabelFunction(vs);
      } else {
        vv.getRenderContext().setNodeLabelFunction(vs_none);
      }
    } else if (source == e_labels) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeLabelFunction(es);
      } else {
        vv.getRenderContext().setEdgeLabelFunction(es_none);
      }
    } else if (source == e_arrow_centered) {
      if (source.isSelected()) {
        vv.getRenderer()
            .getEdgeRenderer()
            .setEdgeArrowRenderingSupport(new CenterEdgeArrowRenderingSupport<>());
      } else {
        vv.getRenderer()
            .getEdgeRenderer()
            .setEdgeArrowRenderingSupport(new BasicEdgeArrowRenderingSupport<>());
      }
    } else if (source == font) {
      vff.setBold(source.isSelected());
      eff.setBold(source.isSelected());
    } else if (source == v_shape) {
      vssa.useFunnyShapes(source.isSelected());
    } else if (source == v_size) {
      vssa.setScaling(source.isSelected());
    } else if (source == v_aspect) {
      vssa.setStretching(source.isSelected());
    } else if (source == e_line) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.line());
      }
    } else if (source == e_ortho) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.orthogonal());
      }
    } else if (source == e_wedge) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.wedge(10));
      }
    }
    //        else if (source == e_bent)
    //        {
    //            if(source.isSelected())
    //            {
    //                vv.getRenderContext().setEdgeShapeFunction(new EdgeShape.BentLine());
    //            }
    //        }
    else if (source == e_quad) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.quadCurve());
      }
    } else if (source == e_cubic) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeShapeFunction(EdgeShape.cubicCurve());
      }
    } else if (source == e_filter_small) {
      showEdge.filterSmall(source.isSelected());
    } else if (source == v_small) {
      showNode.filterSmall(source.isSelected());
    } else if (source == zoom_at_mouse) {
      gm.setZoomAtMouse(source.isSelected());
    } else if (source == no_gradient) {
      if (source.isSelected()) {
        gradient_level = GRADIENT_NONE;
      }
    } else if (source == gradient_relative) {
      if (source.isSelected()) {
        gradient_level = GRADIENT_RELATIVE;
      }
    } else if (source == fill_edges) {
      vv.getRenderContext()
          .setEdgeFillPaintFunction(source.isSelected() ? edgeFillPaint : edge -> null);
    }
    vv.repaint();
  }

  private final class SeedDrawColor<N> implements Function<N, Paint> {
    public Paint apply(N v) {
      return Color.BLACK;
    }
  }

  private final class SeedFillColor<N> implements Function<N, Paint> {
    protected PickedInfo<N> pi;
    protected static final float dark_value = 0.8f;
    protected static final float light_value = 0.2f;
    protected boolean seed_coloring;

    public SeedFillColor(PickedInfo<N> pi) {
      this.pi = pi;
      seed_coloring = false;
    }

    public void setSeedColoring(boolean b) {
      this.seed_coloring = b;
    }

    public Paint apply(N v) {
      float alpha = transparency.get(v).floatValue();
      if (pi.isPicked(v)) {
        return new Color(1f, 1f, 0, alpha);
      } else {
        if (seed_coloring && seedNodes.contains(v)) {
          Color dark = new Color(0, 0, dark_value, alpha);
          Color light = new Color(0, 0, light_value, alpha);
          return new GradientPaint(0, 0, dark, 10, 0, light, true);
        } else {
          return new Color(1f, 0, 0, alpha);
        }
      }
    }
  }

  private static final class EdgeWeightStrokeFunction<E> implements Function<E, Stroke> {
    protected static final Stroke basic = new BasicStroke(1);
    protected static final Stroke heavy = new BasicStroke(2);
    protected static final Stroke dotted = RenderContext.DOTTED;

    protected boolean weighted = false;
    protected Map<E, Number> edge_weight;

    public EdgeWeightStrokeFunction(Map<E, Number> edge_weight) {
      this.edge_weight = edge_weight;
    }

    public void setWeighted(boolean weighted) {
      this.weighted = weighted;
    }

    public Stroke apply(E e) {
      if (weighted) {
        if (drawHeavy(e)) {
          return heavy;
        } else {
          return dotted;
        }
      } else {
        return basic;
      }
    }

    protected boolean drawHeavy(E e) {
      double value = edge_weight.get(e).doubleValue();
      return value > 0.7;
    }
  }

  private static final class NodeStrokeHighlight<N, E> implements Function<N, Stroke> {
    protected boolean highlight = false;
    protected Stroke heavy = new BasicStroke(5);
    protected Stroke medium = new BasicStroke(3);
    protected Stroke light = new BasicStroke(1);
    protected PickedInfo<N> pi;
    protected Network<N, E> graph;

    public NodeStrokeHighlight(Network<N, E> graph, PickedInfo<N> pi) {
      this.graph = graph;
      this.pi = pi;
    }

    public void setHighlight(boolean highlight) {
      this.highlight = highlight;
    }

    public Stroke apply(N v) {
      if (highlight) {
        if (pi.isPicked(v)) {
          return heavy;
        } else {
          for (N w : graph.adjacentNodes(v)) {
            if (pi.isPicked(w)) {
              return medium;
            }
          }
          return light;
        }
      } else {
        return light;
      }
    }
  }

  private static final class NodeFontTransformer<N> implements Function<N, Font> {
    protected boolean bold = false;
    Font f = new Font("Helvetica", Font.PLAIN, 12);
    Font b = new Font("Helvetica", Font.BOLD, 12);

    public void setBold(boolean bold) {
      this.bold = bold;
    }

    public Font apply(N v) {
      if (bold) {
        return b;
      } else {
        return f;
      }
    }
  }

  private static final class EdgeFontTransformer<E> implements Function<E, Font> {
    protected boolean bold = false;
    Font f = new Font("Helvetica", Font.PLAIN, 12);
    Font b = new Font("Helvetica", Font.BOLD, 12);

    public void setBold(boolean bold) {
      this.bold = bold;
    }

    public Font apply(E e) {
      if (bold) {
        return b;
      } else {
        return f;
      }
    }
  }

  private static final class NodeDisplayPredicate<N> implements Predicate<N> {
    protected boolean filter_small;
    protected static final int MIN_DEGREE = 4;
    protected final Network<N, ?> graph;

    public NodeDisplayPredicate(Network<N, ?> graph, boolean filter) {
      this.graph = graph;
      this.filter_small = filter;
    }

    public void filterSmall(boolean b) {
      filter_small = b;
    }

    public boolean test(N node) {
      return filter_small ? graph.degree(node) >= MIN_DEGREE : true;
    }
  }

  private static final class EdgeDisplayPredicate<E> implements Predicate<E> {
    protected boolean filter_small;
    protected final double MIN_WEIGHT = 0.5;
    protected final Map<E, Number> edge_weights;

    public EdgeDisplayPredicate(Map<E, Number> edge_weights, boolean filter) {
      this.edge_weights = edge_weights;
      this.filter_small = filter;
    }

    public void filterSmall(boolean b) {
      filter_small = b;
    }

    public boolean test(E edge) {
      return filter_small ? edge_weights.get(edge).doubleValue() >= MIN_WEIGHT : true;
    }
  }

  /**
   * Controls the shape, layoutSize, and aspect ratio for each node.
   *
   * @author Joshua O'Madadhain
   */
  private static final class NodeShapeSizeAspect<N, E> extends AbstractNodeShapeFunction<N>
      implements Function<N, Shape> {

    protected boolean stretch = false;
    protected boolean scale = false;
    protected boolean funny_shapes = false;
    protected Function<N, Double> voltages;
    protected Network<N, E> graph;
    //        protected AffineTransform scaleTransform = new AffineTransform();

    public NodeShapeSizeAspect(Network<N, E> graphIn, Function<N, Double> voltagesIn) {
      this.graph = graphIn;
      this.voltages = voltagesIn;
      setSizeTransformer(n -> scale ? (int) (voltages.apply(n) * 15) + 10 : 10);
      setAspectRatioTransformer(
          n -> stretch ? (float) (graph.inDegree(n) + 1) / (graph.outDegree(n) + 1) : 1.0f);
    }

    public void setStretching(boolean stretch) {
      this.stretch = stretch;
    }

    public void setScaling(boolean scale) {
      this.scale = scale;
    }

    public void useFunnyShapes(boolean use) {
      this.funny_shapes = use;
    }

    public Shape apply(N v) {
      if (funny_shapes) {
        if (graph.degree(v) < 5) {
          int sides = Math.max(graph.degree(v), 3);
          return factory.getRegularPolygon(v, sides);
        } else {
          return factory.getRegularStar(v, graph.degree(v));
        }
      } else {
        return factory.getEllipse(v);
      }
    }
  }

  /** a GraphMousePlugin that offers popup menu support */
  protected class PopupGraphMousePlugin extends AbstractPopupGraphMousePlugin
      implements MouseListener {

    public PopupGraphMousePlugin() {
      this(MouseEvent.BUTTON3_MASK);
    }

    public PopupGraphMousePlugin(int modifiers) {
      super(modifiers);
    }

    /**
     * If this event is over a Node, pop up a menu to allow the user to increase/decrease the
     * voltage attribute of this Node
     *
     * @param e the event to be handled
     */
    @SuppressWarnings("unchecked")
    protected void handlePopup(MouseEvent e) {
      final VisualizationViewer<Integer, Number> vv =
          (VisualizationViewer<Integer, Number>) e.getSource();
      final LayoutModel<Integer> layoutModel = vv.getModel().getLayoutModel();
      Point2D p =
          e
              .getPoint(); // vv.getRenderContext().getBasicTransformer().inverseViewTransform(e.getPoint());

      NetworkElementAccessor<Integer, Number> pickSupport = vv.getPickSupport();
      if (pickSupport != null) {
        final Integer v = pickSupport.getNode(layoutModel, p.getX(), p.getY());
        if (v != null) {
          JPopupMenu popup = new JPopupMenu();
          popup.add(
              new AbstractAction("Decrease Transparency") {
                public void actionPerformed(ActionEvent e) {
                  Double value = Math.min(1, transparency.get(v).doubleValue() + 0.1);
                  transparency.put(v, value);
                  //                        	transparency.put(v, )transparency.get(v);
                  //                            MutableDouble value =
                  // (MutableDouble)transparency.getNumber(v);
                  //                            value.setDoubleValue(Math.min(1, value.doubleValue()
                  // + 0.1));
                  vv.repaint();
                }
              });
          popup.add(
              new AbstractAction("Increase Transparency") {
                public void actionPerformed(ActionEvent e) {
                  Double value = Math.max(0, transparency.get(v).doubleValue() - 0.1);
                  transparency.put(v, value);
                  //                            MutableDouble value =
                  // (MutableDouble)transparency.getNumber(v);
                  //                            value.setDoubleValue(Math.max(0, value.doubleValue()
                  // - 0.1));
                  vv.repaint();
                }
              });
          popup.show(vv, e.getX(), e.getY());
        } else {
          final Number edge = pickSupport.getEdge(layoutModel, p.getX(), p.getY());
          if (edge != null) {
            JPopupMenu popup = new JPopupMenu();
            popup.add(
                new AbstractAction(edge.toString()) {
                  public void actionPerformed(ActionEvent e) {
                    System.err.println("got " + edge);
                  }
                });
            popup.show(vv, e.getX(), e.getY());
          }
        }
      }
    }
  }

  public class VoltageTips<E> implements Function<Integer, String> {

    public String apply(Integer node) {
      return "Voltage:" + voltages.apply(node);
    }
  }

  public class GradientPickedEdgePaintFunction<N, E> extends GradientEdgePaintFunction<N, E> {
    private Function<E, Paint> defaultFunc;
    protected boolean fill_edge = false;
    private VisualizationViewer<N, E> vv;

    public GradientPickedEdgePaintFunction(
        Function<E, Paint> defaultEdgePaintFunction, VisualizationViewer<N, E> vv) {
      super(Color.WHITE, Color.BLACK, vv);
      this.vv = vv;
      this.defaultFunc = defaultEdgePaintFunction;
    }

    public void useFill(boolean b) {
      fill_edge = b;
    }

    public Paint apply(E e) {
      if (gradient_level == GRADIENT_NONE) {
        return defaultFunc.apply(e);
      } else {
        return super.apply(e);
      }
    }

    protected Color getColor2(E e) {
      return this.vv.getPickedEdgeState().isPicked(e) ? Color.CYAN : c2;
    }

    //        public Paint getFillPaint(E e)
    //        {
    //            if (selfLoop.evaluateEdge(vv.getGraphLayout().getGraph(), e) || !fill_edge)
    //                return null;
    //            else
    //                return getDrawPaint(e);
    //        }

  }
}
