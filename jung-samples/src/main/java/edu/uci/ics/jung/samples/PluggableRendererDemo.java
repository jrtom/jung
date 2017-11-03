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
import edu.uci.ics.jung.algorithms.scoring.util.VertexScoreTransformer;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.GradientEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.NumberFormattingTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.layout.*;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.renderers.CenterEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;

/**
 * Shows off some of the capabilities of <code>PluggableRenderer</code>. This code provides examples
 * of different ways to provide and change the various functions that provide property information
 * to the renderer.
 *
 * <p>This demo creates a random graph with random edge weights. It then runs <code>VoltageRanker
 * </code> on this graph, using half of the "seed" vertices from the random graph generation as
 * voltage sources, and half of them as voltage sinks.
 *
 * <p>What the controls do:
 *
 * <ul>
 *   <li>Mouse controls:
 *       <ul>
 *         <li>If your mouse has a scroll wheel, scrolling forward zooms out and scrolling backward
 *             zooms in.
 *         <li>Left-clicking on a vertex or edge selects it, and unselects all others.
 *         <li>Middle-clicking on a vertex or edge toggles its selection state.
 *         <li>Right-clicking on a vertex brings up a pop-up menu that allows you to increase or
 *             decrease that vertex's transparency.
 *         <li>Left-clicking on the background allows you to drag the image around.
 *         <li>Hovering over a vertex tells you what its voltage is; hovering over an edge shows its
 *             identity; hovering over the background shows an informational message.
 *       </ul>
 *   <li>Vertex stuff:
 *       <ul>
 *         <li>"vertex seed coloring": if checked, the seed vertices are colored blue, and all other
 *             vertices are colored red. Otherwise, all vertices are colored a slightly transparent
 *             red (except the currently "picked" vertex, which is colored transparent purple).
 *         <li>"vertex selection stroke highlighting": if checked, the picked vertex and its
 *             neighbors are all drawn with heavy borders. Otherwise, all vertices are drawn with
 *             light borders.
 *         <li>"show vertex ranks (voltages)": if checked, each vertex is labeled with its
 *             calculated 'voltage'. Otherwise, vertices are unlabeled.
 *         <li>"vertex degree shapes": if checked, vertices are drawn with a polygon with number of
 *             sides proportional to its degree. Otherwise, vertices are drawn as ellipses.
 *         <li>"vertex voltage layoutSize": if checked, vertices are drawn with a layoutSize
 *             proportional to their voltage ranking. Otherwise, all vertices are drawn at the same
 *             layoutSize.
 *         <li>"vertex degree ratio stretch": if checked, vertices are drawn with an aspect ratio
 *             (height/width ratio) proportional to the ratio of their indegree to their outdegree.
 *             Otherwise, vertices are drawn with an aspect ratio of 1.
 *         <li>"filter vertices of degree &lt; 4": if checked, does not display any vertices (or
 *             their incident edges) whose degree in the original graph is less than 4; otherwise,
 *             all vertices are drawn.
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
 *         <li>"bold text": if checked, all vertex and edge labels are drawn using a boldface font.
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
public class PluggableRendererDemo extends JApplet implements ActionListener {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

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
  protected VertexStrokeHighlight<Integer, Number> vsh;
  protected Function<? super Integer, String> vs;
  protected Function<? super Integer, String> vs_none;
  protected Function<? super Number, String> es;
  protected Function<? super Number, String> es_none;
  protected VertexFontTransformer<Integer> vff;
  protected EdgeFontTransformer<Number> eff;
  protected VertexShapeSizeAspect<Integer, Number> vssa;
  protected VertexDisplayPredicate<Integer> show_vertex;
  protected EdgeDisplayPredicate<Number> show_edge;
  protected Predicate<Number> self_loop;
  protected GradientPickedEdgePaintFunction<Integer, Number> edgeDrawPaint;
  protected GradientPickedEdgePaintFunction<Integer, Number> edgeFillPaint;
  protected static final Object VOLTAGE_KEY = "voltages";
  protected static final Object TRANSPARENCY = "transparency";

  protected Map<Number, Number> edge_weight = new HashMap<Number, Number>();
  protected Function<Integer, Double> voltages;
  protected Map<Integer, Number> transparency = new HashMap<Integer, Number>();

  protected VisualizationViewer<Integer, Number> vv;
  protected DefaultModalGraphMouse<Integer, Number> gm;
  protected Set<Integer> seedVertices = new HashSet<Integer>();

  private Network<Integer, Number> graph;

  public void start() {
    getContentPane().add(startFunction());
  }

  public static void main(String[] s) {
    JFrame jf = new JFrame();
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel jp = new PluggableRendererDemo().startFunction();
    jf.getContentPane().add(jp);
    jf.pack();
    jf.setVisible(true);
  }

  public JPanel startFunction() {
    this.graph = buildGraph();

    LayoutAlgorithm<Integer, Point2D> layoutAlgorithm = new FRLayoutAlgorithm<>(domainModel);
    vv = new VisualizationViewer<>(graph, layoutAlgorithm);

    //    vv.getRenderer().setVertexRenderer(new CachingVertexRenderer<Integer, Number>(vv));
    //    vv.getRenderer().setEdgeRenderer(new CachingEdgeRenderer<Integer, Number>(vv));

    PickedState<Integer> picked_state = vv.getPickedVertexState();

    self_loop =
        (e) -> {
          return Graphs.isSelfLoop(graph, e);
        };
    // create decorators
    seedFillColor = new SeedFillColor<Integer>(picked_state);
    seedDrawColor = new SeedDrawColor<Integer>();
    ewcs = new EdgeWeightStrokeFunction<Number>(edge_weight);
    vsh = new VertexStrokeHighlight<Integer, Number>(graph, picked_state);
    vff = new VertexFontTransformer<Integer>();
    eff = new EdgeFontTransformer<Number>();
    vs_none = n -> null;
    es_none = e -> null;
    vssa = new VertexShapeSizeAspect<Integer, Number>(graph, voltages);
    show_vertex = new VertexDisplayPredicate<Integer>(graph, false);
    show_edge = new EdgeDisplayPredicate<Number>(edge_weight, false);

    // uses a gradient edge if unpicked, otherwise uses picked selection
    edgeDrawPaint =
        new GradientPickedEdgePaintFunction<Integer, Number>(
            new PickableEdgePaintTransformer<Number>(
                vv.getPickedEdgeState(), Color.black, Color.cyan),
            vv);
    edgeFillPaint =
        new GradientPickedEdgePaintFunction<Integer, Number>(
            new PickableEdgePaintTransformer<Number>(
                vv.getPickedEdgeState(), Color.black, Color.cyan),
            vv);

    vv.getRenderContext().setVertexFillPaintTransformer(seedFillColor);
    vv.getRenderContext().setVertexDrawPaintTransformer(seedDrawColor);
    vv.getRenderContext().setVertexStrokeTransformer(vsh);
    vv.getRenderContext().setVertexLabelTransformer(vs_none);
    vv.getRenderContext().setVertexFontTransformer(vff);
    vv.getRenderContext().setVertexShapeTransformer(vssa);
    vv.getRenderContext().setVertexIncludePredicate(show_vertex);

    vv.getRenderContext().setEdgeDrawPaintTransformer(edgeDrawPaint);
    vv.getRenderContext().setEdgeLabelTransformer(es_none);
    vv.getRenderContext().setEdgeFontTransformer(eff);
    vv.getRenderContext().setEdgeStrokeTransformer(ewcs);
    vv.getRenderContext().setEdgeIncludePredicate(show_edge);
    vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line());

    vv.getRenderContext().setArrowFillPaintTransformer(n -> Color.lightGray);
    vv.getRenderContext().setArrowDrawPaintTransformer(n -> Color.black);
    JPanel jp = new JPanel();
    jp.setLayout(new BorderLayout());

    vv.setBackground(Color.white);
    GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(vv);
    jp.add(scrollPane);
    gm = new DefaultModalGraphMouse<Integer, Number>();
    vv.setGraphMouse(gm);
    gm.add(new PopupGraphMousePlugin());

    addBottomControls(jp);
    vssa.setScaling(true);

    vv.setVertexToolTipTransformer(new VoltageTips<Number>());
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
    Supplier<Integer> vertexFactory =
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
        new BarabasiAlbertGenerator<Integer, Number>(
            NetworkBuilder.directed().allowsSelfLoops(true).allowsParallelEdges(true),
            vertexFactory,
            edgeFactory,
            4,
            3);
    generator.evolveGraph(20);
    MutableNetwork<Integer, Number> g = generator.get();
    for (Number e : g.edges()) {
      edge_weight.put(e, Math.random());
    }
    es = new NumberFormattingTransformer<Number>(edge_weight::get);

    // collect the seeds used to define the random graph
    seedVertices = generator.seedNodes();

    if (seedVertices.size() < 2) {
      System.out.println("need at least 2 seeds (one source, one sink)");
    }

    // use these seeds as source and sink vertices, run VoltageRanker
    boolean source = true;
    Set<Integer> sources = new HashSet<Integer>();
    Set<Integer> sinks = new HashSet<Integer>();
    for (Integer v : seedVertices) {
      if (source) {
        sources.add(v);
      } else {
        sinks.add(v);
      }
      source = !source;
    }
    VoltageScorer<Integer, Number> voltage_scores =
        new VoltageScorer<Integer, Number>(g, edge_weight::get, sources, sinks);
    voltage_scores.evaluate();
    voltages = new VertexScoreTransformer<Integer, Double>(voltage_scores);
    vs = new NumberFormattingTransformer<Integer>(voltages);

    Collection<Integer> verts = g.nodes();

    // assign a transparency value of 0.9 to all vertices
    for (Integer v : verts) {
      transparency.put(v, new Double(0.9));
    }

    // add a couple of self-loops (sanity check on rendering)
    Integer v = verts.iterator().next();
    Number e = new Float(Math.random());
    edge_weight.put(e, e);
    g.addEdge(v, v, e);
    e = new Float(Math.random());
    edge_weight.put(e, e);
    g.addEdge(v, v, e);
    return g;
  }

  /** @param jp panel to which controls will be added */
  protected void addBottomControls(final JPanel jp) {
    final JPanel control_panel = new JPanel();
    jp.add(control_panel, BorderLayout.EAST);
    control_panel.setLayout(new BorderLayout());
    final Box vertex_panel = Box.createVerticalBox();
    vertex_panel.setBorder(BorderFactory.createTitledBorder("Vertices"));
    final Box edge_panel = Box.createVerticalBox();
    edge_panel.setBorder(BorderFactory.createTitledBorder("Edges"));
    final Box both_panel = Box.createVerticalBox();

    control_panel.add(vertex_panel, BorderLayout.NORTH);
    control_panel.add(edge_panel, BorderLayout.SOUTH);
    control_panel.add(both_panel, BorderLayout.CENTER);

    // set up vertex controls
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
    v_small = new JCheckBox("filter when degree < " + VertexDisplayPredicate.MIN_DEGREE);
    v_small.addActionListener(this);

    vertex_panel.add(v_color);
    vertex_panel.add(v_stroke);
    vertex_panel.add(v_labels);
    vertex_panel.add(v_shape);
    vertex_panel.add(v_size);
    vertex_panel.add(v_aspect);
    vertex_panel.add(v_small);

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
    //bg_grad.add(gradient_absolute);
    gradient_panel.add(no_gradient);
    //gradientGrid.add(gradient_absolute);
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
    edge_panel.add(shape_panel);
    gradient_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    edge_panel.add(gradient_panel);
    show_edge_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    edge_panel.add(show_edge_panel);
    arrow_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    edge_panel.add(arrow_panel);

    e_color.setAlignmentX(Component.LEFT_ALIGNMENT);
    edge_panel.add(e_color);
    e_labels.setAlignmentX(Component.LEFT_ALIGNMENT);
    edge_panel.add(e_labels);

    // set up zoom controls
    zoom_at_mouse = new JCheckBox("<html><center>zoom at mouse<p>(wheel only)</center></html>");
    zoom_at_mouse.addActionListener(this);
    zoom_at_mouse.setSelected(true);

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

    both_panel.add(zoomPanel);
    both_panel.add(fontPanel);

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

    JComboBox<Position> cb = new JComboBox<Position>();
    cb.addItem(Renderer.VertexLabel.Position.N);
    cb.addItem(Renderer.VertexLabel.Position.NE);
    cb.addItem(Renderer.VertexLabel.Position.E);
    cb.addItem(Renderer.VertexLabel.Position.SE);
    cb.addItem(Renderer.VertexLabel.Position.S);
    cb.addItem(Renderer.VertexLabel.Position.SW);
    cb.addItem(Renderer.VertexLabel.Position.W);
    cb.addItem(Renderer.VertexLabel.Position.NW);
    cb.addItem(Renderer.VertexLabel.Position.N);
    cb.addItem(Renderer.VertexLabel.Position.CNTR);
    cb.addItem(Renderer.VertexLabel.Position.AUTO);
    cb.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            Renderer.VertexLabel.Position position = (Renderer.VertexLabel.Position) e.getItem();
            vv.getRenderer().getVertexLabelRenderer().setPosition(position);
            vv.repaint();
          }
        });
    cb.setSelectedItem(Renderer.VertexLabel.Position.SE);
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
        vv.getRenderContext().setVertexLabelTransformer(vs);
      } else {
        vv.getRenderContext().setVertexLabelTransformer(vs_none);
      }
    } else if (source == e_labels) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeLabelTransformer(es);
      } else {
        vv.getRenderContext().setEdgeLabelTransformer(es_none);
      }
    } else if (source == e_arrow_centered) {
      if (source.isSelected()) {
        vv.getRenderer()
            .getEdgeRenderer()
            .setEdgeArrowRenderingSupport(new CenterEdgeArrowRenderingSupport<Integer, Number>());
      } else {
        vv.getRenderer()
            .getEdgeRenderer()
            .setEdgeArrowRenderingSupport(new BasicEdgeArrowRenderingSupport<Integer, Number>());
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
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.line());
      }
    } else if (source == e_ortho) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.orthogonal());
      }
    } else if (source == e_wedge) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.wedge(10));
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
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.quadCurve());
      }
    } else if (source == e_cubic) {
      if (source.isSelected()) {
        vv.getRenderContext().setEdgeShapeTransformer(EdgeShape.cubicCurve());
      }
    } else if (source == e_filter_small) {
      show_edge.filterSmall(source.isSelected());
    } else if (source == v_small) {
      show_vertex.filterSmall(source.isSelected());
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
          .setEdgeFillPaintTransformer(source.isSelected() ? edgeFillPaint : edge -> null);
    }
    vv.repaint();
  }

  private final class SeedDrawColor<V> implements Function<V, Paint> {
    public Paint apply(V v) {
      return Color.BLACK;
    }
  }

  private final class SeedFillColor<V> implements Function<V, Paint> {
    protected PickedInfo<V> pi;
    protected static final float dark_value = 0.8f;
    protected static final float light_value = 0.2f;
    protected boolean seed_coloring;

    public SeedFillColor(PickedInfo<V> pi) {
      this.pi = pi;
      seed_coloring = false;
    }

    public void setSeedColoring(boolean b) {
      this.seed_coloring = b;
    }

    public Paint apply(V v) {
      float alpha = transparency.get(v).floatValue();
      if (pi.isPicked(v)) {
        return new Color(1f, 1f, 0, alpha);
      } else {
        if (seed_coloring && seedVertices.contains(v)) {
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
      if (value > 0.7) {
        return true;
      } else {
        return false;
      }
    }
  }

  private static final class VertexStrokeHighlight<V, E> implements Function<V, Stroke> {
    protected boolean highlight = false;
    protected Stroke heavy = new BasicStroke(5);
    protected Stroke medium = new BasicStroke(3);
    protected Stroke light = new BasicStroke(1);
    protected PickedInfo<V> pi;
    protected Network<V, E> graph;

    public VertexStrokeHighlight(Network<V, E> graph, PickedInfo<V> pi) {
      this.graph = graph;
      this.pi = pi;
    }

    public void setHighlight(boolean highlight) {
      this.highlight = highlight;
    }

    public Stroke apply(V v) {
      if (highlight) {
        if (pi.isPicked(v)) {
          return heavy;
        } else {
          for (V w : graph.adjacentNodes(v)) {
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

  private static final class VertexFontTransformer<V> implements Function<V, Font> {
    protected boolean bold = false;
    Font f = new Font("Helvetica", Font.PLAIN, 12);
    Font b = new Font("Helvetica", Font.BOLD, 12);

    public void setBold(boolean bold) {
      this.bold = bold;
    }

    public Font apply(V v) {
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

  private static final class VertexDisplayPredicate<V> implements Predicate<V> {
    protected boolean filter_small;
    protected static final int MIN_DEGREE = 4;
    protected final Network<V, ?> graph;

    public VertexDisplayPredicate(Network<V, ?> graph, boolean filter) {
      this.graph = graph;
      this.filter_small = filter;
    }

    public void filterSmall(boolean b) {
      filter_small = b;
    }

    public boolean test(V node) {
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
   * Controls the shape, layoutSize, and aspect ratio for each vertex.
   *
   * @author Joshua O'Madadhain
   */
  private static final class VertexShapeSizeAspect<V, E> extends AbstractVertexShapeTransformer<V>
      implements Function<V, Shape> {

    protected boolean stretch = false;
    protected boolean scale = false;
    protected boolean funny_shapes = false;
    protected Function<V, Double> voltages;
    protected Network<V, E> graph;
    //        protected AffineTransform scaleTransform = new AffineTransform();

    public VertexShapeSizeAspect(Network<V, E> graphIn, Function<V, Double> voltagesIn) {
      this.graph = graphIn;
      this.voltages = voltagesIn;
      setSizeTransformer(n -> scale ? (int) (voltages.apply(n) * 30) + 20 : 20);
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

    public Shape apply(V v) {
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
     * If this event is over a Vertex, pop up a menu to allow the user to increase/decrease the
     * voltage attribute of this Vertex
     *
     * @param e the event to be handled
     */
    @SuppressWarnings("unchecked")
    protected void handlePopup(MouseEvent e) {
      final VisualizationViewer<Integer, Number> vv =
          (VisualizationViewer<Integer, Number>) e.getSource();
      Point2D p =
          e
              .getPoint(); //vv.getRenderContext().getBasicTransformer().inverseViewTransform(e.getPoint());

      NetworkElementAccessor<Integer, Number> pickSupport = vv.getPickSupport();
      if (pickSupport != null) {
        final Integer v = pickSupport.getNode(p.getX(), p.getY());
        if (v != null) {
          JPopupMenu popup = new JPopupMenu();
          popup.add(
              new AbstractAction("Decrease Transparency") {
                public void actionPerformed(ActionEvent e) {
                  Double value = Math.min(1, transparency.get(v).doubleValue() + 0.1);
                  transparency.put(v, value);
                  //                        	transparency.put(v, )transparency.get(v);
                  //                            MutableDouble value = (MutableDouble)transparency.getNumber(v);
                  //                            value.setDoubleValue(Math.min(1, value.doubleValue() + 0.1));
                  vv.repaint();
                }
              });
          popup.add(
              new AbstractAction("Increase Transparency") {
                public void actionPerformed(ActionEvent e) {
                  Double value = Math.max(0, transparency.get(v).doubleValue() - 0.1);
                  transparency.put(v, value);
                  //                            MutableDouble value = (MutableDouble)transparency.getNumber(v);
                  //                            value.setDoubleValue(Math.max(0, value.doubleValue() - 0.1));
                  vv.repaint();
                }
              });
          popup.show(vv, e.getX(), e.getY());
        } else {
          final Number edge = pickSupport.getEdge(p.getX(), p.getY());
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

    public String apply(Integer vertex) {
      return "Voltage:" + voltages.apply(vertex);
    }
  }

  public class GradientPickedEdgePaintFunction<V, E> extends GradientEdgePaintTransformer<V, E> {
    private Function<E, Paint> defaultFunc;
    protected boolean fill_edge = false;
    private VisualizationViewer<V, E> vv;

    public GradientPickedEdgePaintFunction(
        Function<E, Paint> defaultEdgePaintFunction, VisualizationViewer<V, E> vv) {
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
