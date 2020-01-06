/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.FRBHVisitorLayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.LensMagnificationGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer;
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport;
import edu.uci.ics.jung.visualization.transform.Lens;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.MagnifyTransformer;
import edu.uci.ics.jung.visualization.transform.shape.HyperbolicShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.MagnifyShapeTransformer;
import edu.uci.ics.jung.visualization.transform.shape.ViewLensSupport;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the use of <code>HyperbolicTransform</code> and <code>MagnifyTransform</code>
 * applied to either the model (graph layout) or the view (VisualizationViewer) The hyperbolic
 * transform is applied in an elliptical lens that affects that part of the visualization.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class SpatialLensLargeGraphDemo extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(SpatialLensLargeGraphDemo.class);
  /** the graph */
  Network<String, Number> graph;

  LayoutAlgorithm<String> graphLayoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  /** provides a Hyperbolic lens for the view */
  LensSupport hyperbolicViewSupport;
  /** provides a magnification lens for the view */
  LensSupport magnifyViewSupport;

  /** provides a Hyperbolic lens for the model */
  LensSupport hyperbolicLayoutSupport;
  /** provides a magnification lens for the model */
  LensSupport magnifyLayoutSupport;

  ScalingControl scaler;

  public SpatialLensLargeGraphDemo() {
    setLayout(new BorderLayout());
    graph = SpatialLensLargeGraphDemo.getGraph();

    graphLayoutAlgorithm = new FRBHVisitorLayoutAlgorithm<>();

    Dimension preferredSize = new Dimension(800, 800);
    Dimension viewPreferredSize = new Dimension(800, 800);

    final VisualizationModel<String, Number> visualizationModel =
        new BaseVisualizationModel<>(graph, graphLayoutAlgorithm, preferredSize);
    vv = new VisualizationViewer<>(visualizationModel, viewPreferredSize);
    vv.getRenderContext().setNodeLabelFunction(Object::toString);
    vv.getRenderContext().setNodeShapeFunction(n -> new Rectangle2D.Float(-8, -8, 16, 16));
    vv.setBackground(Color.white);

    vv.getRenderContext().setNodeLabelFunction(Object::toString);

    GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
    add(gzsp);

    // the regular graph mouse for the normal view
    final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<>();

    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    // create a lens to share between the two hyperbolic transformers
    LayoutModel<String> layoutModel = vv.getModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    Lens lens = new Lens(d);
    hyperbolicViewSupport =
        new ViewLensSupport<>(
            vv,
            new HyperbolicShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse());
    hyperbolicLayoutSupport =
        new LayoutLensSupport<>(
            vv,
            new HyperbolicTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new ModalLensGraphMouse());

    // the magnification lens uses a different magnification than the hyperbolic lens
    // create a new one to share between the two magnigy transformers
    lens = new Lens(d);
    lens.setMagnification(3.f);
    magnifyViewSupport =
        new ViewLensSupport<>(
            vv,
            new MagnifyShapeTransformer(
                lens, vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW)),
            new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
    magnifyLayoutSupport =
        new LayoutLensSupport<>(
            vv,
            new MagnifyTransformer(
                lens,
                vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT)),
            new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
    hyperbolicLayoutSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(hyperbolicViewSupport.getLensTransformer().getLens().getLensShape());
    magnifyViewSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(hyperbolicLayoutSupport.getLensTransformer().getLens().getLensShape());
    magnifyLayoutSupport
        .getLensTransformer()
        .getLens()
        .setLensShape(magnifyViewSupport.getLensTransformer().getLens().getLensShape());

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    ButtonGroup radio = new ButtonGroup();
    JRadioButton normal = new JRadioButton("None");
    normal.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (hyperbolicViewSupport != null) {
              hyperbolicViewSupport.deactivate();
            }
            if (hyperbolicLayoutSupport != null) {
              hyperbolicLayoutSupport.deactivate();
            }
            if (magnifyViewSupport != null) {
              magnifyViewSupport.deactivate();
            }
            if (magnifyLayoutSupport != null) {
              magnifyLayoutSupport.deactivate();
            }
          }
        });

    final JRadioButton hyperView = new JRadioButton("Hyperbolic View");
    hyperView.addItemListener(
        e -> hyperbolicViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    final JRadioButton hyperModel = new JRadioButton("Hyperbolic Layout");
    hyperModel.addItemListener(
        e -> hyperbolicLayoutSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    final JRadioButton magnifyView = new JRadioButton("Magnified View");
    magnifyView.addItemListener(
        e -> magnifyViewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    final JRadioButton magnifyModel = new JRadioButton("Magnified Layout");
    magnifyModel.addItemListener(
        e -> magnifyLayoutSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    JLabel modeLabel = new JLabel("     Mode Menu >>");
    modeLabel.setUI(new VerticalLabelUI(false));
    radio.add(normal);
    radio.add(hyperModel);
    radio.add(hyperView);
    radio.add(magnifyModel);
    radio.add(magnifyView);
    normal.setSelected(true);

    graphMouse.addItemListener(hyperbolicLayoutSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(hyperbolicViewSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(magnifyLayoutSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(magnifyViewSupport.getGraphMouse().getModeListener());

    JMenuBar menubar = new JMenuBar();
    menubar.add(graphMouse.getModeMenu());
    gzsp.setCorner(menubar);

    JComboBox modeBox = graphMouse.getModeComboBox();
    modeBox.addItemListener(
        ((DefaultModalGraphMouse<Integer, Number>) vv.getGraphMouse()).getModeListener());

    JRadioButton showSpatialEffects = new JRadioButton("Spatial Structure");
    showSpatialEffects.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            System.err.println("TURNED ON LOGGING");
            // turn on the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo
            // and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("edu.uci.ics.jung.visualization.spatial").setLevel(Level.DEBUG);
            ctx.getLogger("edu.uci.ics.jung.visualization.spatial.rtree").setLevel(Level.DEBUG);
            ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer")
                .setLevel(Level.TRACE);
            //
            // ctx.getLogger("edu.uci.ics.jung.visualization.picking").setLevel(Level.TRACE);
            repaint();

          } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            System.err.println("TURNED OFF LOGGING");
            // turn off the logging
            // programmatically set the log level so that the spatial grid is drawn for this demo
            // and the SpatialGrid logging is output
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) log;
            LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
            ctx.getLogger("edu.uci.ics.jung.visualization.spatial").setLevel(Level.INFO);
            ctx.getLogger("edu.uci.ics.jung.visualization.spatial.rtree").setLevel(Level.INFO);
            ctx.getLogger("edu.uci.ics.jung.visualization.BasicVisualizationServer")
                .setLevel(Level.INFO);
            ctx.getLogger("edu.uci.ics.jung.visualization.picking").setLevel(Level.INFO);
            repaint();
          }
        });

    Box controls = Box.createHorizontalBox();
    JPanel zoomControls = new JPanel(new GridLayout(2, 1));
    JPanel modeControls = new JPanel(new GridLayout(2, 1));
    JPanel leftControls = new JPanel();
    JPanel hyperControls = new JPanel(new GridLayout(3, 2));
    hyperControls.setBorder(BorderFactory.createTitledBorder("Examiner Lens"));
    zoomControls.add(plus);
    zoomControls.add(minus);
    modeControls.add(showSpatialEffects);
    modeControls.add(modeBox);
    leftControls.add(zoomControls);
    leftControls.add(modeControls);

    hyperControls.add(normal);
    hyperControls.add(new JLabel());

    hyperControls.add(hyperModel);
    hyperControls.add(magnifyModel);

    hyperControls.add(hyperView);
    hyperControls.add(magnifyView);

    controls.add(leftControls);
    controls.add(hyperControls);
    controls.add(modeLabel);
    add(controls, BorderLayout.SOUTH);
    vv.setNodeToolTipFunction(n -> n.toString());
  }

  static class VerticalLabelUI extends BasicLabelUI {
    static {
      labelUI = new VerticalLabelUI(false);
    }

    protected boolean clockwise;

    VerticalLabelUI(boolean clockwise) {
      super();
      this.clockwise = clockwise;
    }

    public Dimension getPreferredSize(JComponent c) {
      Dimension dim = super.getPreferredSize(c);
      return new Dimension(dim.height, dim.width);
    }

    private static Rectangle paintIconR = new Rectangle();
    private static Rectangle paintTextR = new Rectangle();
    private static Rectangle paintViewR = new Rectangle();
    private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

    public void paint(Graphics g, JComponent c) {

      JLabel label = (JLabel) c;
      String text = label.getText();
      Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

      if ((icon == null) && (text == null)) {
        return;
      }

      FontMetrics fm = g.getFontMetrics();
      paintViewInsets = c.getInsets(paintViewInsets);

      paintViewR.x = paintViewInsets.left;
      paintViewR.y = paintViewInsets.top;

      // Use inverted height & width
      paintViewR.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
      paintViewR.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

      paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
      paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

      String clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

      Graphics2D g2 = (Graphics2D) g;
      AffineTransform tr = g2.getTransform();
      if (clockwise) {
        g2.rotate(Math.PI / 2);
        g2.translate(0, -c.getWidth());
      } else {
        g2.rotate(-Math.PI / 2);
        g2.translate(-c.getHeight(), 0);
      }

      if (icon != null) {
        icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
      }

      if (text != null) {
        int textX = paintTextR.x;
        int textY = paintTextR.y + fm.getAscent();

        if (label.isEnabled()) {
          paintEnabledText(label, g, clippedText, textX, textY);
        } else {
          paintDisabledText(label, g, clippedText, textX, textY);
        }
      }

      g2.setTransform(tr);
    }
  }

  Network<String, Number> buildOneNode() {
    MutableNetwork<String, Number> graph =
        NetworkBuilder.directed().allowsParallelEdges(true).build();
    graph.addNode("A");
    return graph;
  }

  private static void createEdge(
      MutableNetwork<String, Number> g, String v1Label, String v2Label, int weight) {
    g.addEdge(v1Label, v2Label, new Double(Math.random()));
  }

  public static String[][] pairs = {
    {"a", "b", "3"},
    {"a", "c", "4"},
    {"a", "d", "5"},
    {"d", "c", "6"},
    {"d", "e", "7"},
    {"e", "f", "8"},
    {"f", "g", "9"},
    {"h", "i", "1"},
    {"h", "g", "2"}
  };

  public static Network<String, Number> getGraph() {
    MutableNetwork<String, Number> g =
        NetworkBuilder.undirected().allowsParallelEdges(true).build();

    for (int i = 0; i < pairs.length; i++) {
      String[] pair = pairs[i];
      createEdge(g, pair[0], pair[1], Integer.parseInt(pair[2]));
    }
    int edge = 10;
    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "c" + i;
        String i2 = "c" + j;
        g.addEdge(i1, i2, edge++);
      }
    }

    for (int i = 1; i <= 10; i++) {
      for (int j = i + 1; j <= 10; j++) {
        String i1 = "d" + i;
        String i2 = "d" + j;
        g.addEdge(i1, i2, edge++);
      }
    }

    // and, last, a partial clique
    for (int i = 1; i <= 20; i++) {
      for (int j = i + 1; j <= 20; j++) {
        if (Math.random() > 0.6) {
          continue;
        }
        String i1 = "q" + i;
        String i2 = "q" + j;
        g.addEdge(i1, i2, edge++);
      }
    }

    // and, last, a partial clique
    for (int i = 1; i <= 20; i++) {
      for (int j = i + 1; j <= 20; j++) {
        if (Math.random() > 0.6) {
          continue;
        }
        String i1 = "p" + i;
        String i2 = "p" + j;
        g.addEdge(i1, i2, edge++);
      }
    }
    Iterator<String> nodeIt = g.nodes().iterator();
    String current = nodeIt.next();
    while (nodeIt.hasNext()) {
      String next = nodeIt.next();
      g.addEdge(current, next, edge++);
    }
    return g;
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    f.getContentPane().add(new SpatialLensLargeGraphDemo());
    f.pack();
    f.setVisible(true);
  }
}
