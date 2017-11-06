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
import edu.uci.ics.jung.algorithms.layout.DomainModel;
import edu.uci.ics.jung.algorithms.layout.FRLayoutAlgorithm;
import edu.uci.ics.jung.samples.util.ControlHelpers;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.EllipseVertexShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.VertexIconShapeTransformer;
import edu.uci.ics.jung.visualization.layout.AWTDomainModel;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.shape.MagnifyImageLensSupport;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;

/**
 * Demonstrates the use of images to represent graph vertices. The images are added to the
 * DefaultGraphLabelRenderer and can either be offset from the vertex, or centered on the vertex.
 * Additionally, the relative positioning of the label and image is controlled by subclassing the
 * DefaultGraphLabelRenderer and setting the appropriate properties on its JLabel superclass
 * FancyGraphLabelRenderer
 *
 * <p>The images used in this demo (courtesy of slashdot.org) are rectangular but with a transparent
 * background. When vertices are represented by these images, it looks better if the actual shape of
 * the opaque part of the image is computed so that the edge arrowheads follow the visual shape of
 * the image. This demo uses the FourPassImageShaper class to compute the Shape from an image with
 * transparent background.
 *
 * @author Tom Nelson
 */
public class DemoLensVertexImageShaperDemo extends JApplet {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  /** */
  private static final long serialVersionUID = 5432239991020505763L;

  /** the graph */
  Network<Integer, Double> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Integer, Double> vv;

  /** some icon names to use */
  String[] iconNames = {
    "apple",
    "gamespcgames",
    "graphics3",
    "humor",
    "inputdevices",
    "linux",
    "music",
    "os",
    "privacy",
    "wireless",
    "x"
  };

  LensSupport viewSupport;
  LensSupport modelSupport;
  /** create an instance of a simple graph with controls to demo the zoom features. */
  public DemoLensVertexImageShaperDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    // a Map for the labels
    Map<Integer, String> map = new HashMap<>();
    for (int i = 0; i < graph.nodes().size(); i++) {
      map.put(i, iconNames[i % iconNames.length]);
    }

    // a Map for the Icons
    Map<Integer, Icon> iconMap = new HashMap<>();
    for (int i = 0; i < graph.nodes().size(); i++) {
      String name = "/images/topic" + iconNames[i % iconNames.length] + ".gif";
      try {
        Icon icon =
            new LayeredIcon(
                new ImageIcon(DemoLensVertexImageShaperDemo.class.getResource(name)).getImage());
        iconMap.put(i, icon);
      } catch (Exception ex) {
        System.err.println("You need slashdoticons.jar in your classpath to see the image " + name);
      }
    }

    FRLayoutAlgorithm<Integer, Point2D> layout = new FRLayoutAlgorithm<>(domainModel);
    layout.setMaxIterations(100);
    vv = new VisualizationViewer<>(graph, layout, new Dimension(600, 600));

    Function<Integer, Paint> vpf =
        new PickableVertexPaintTransformer<>(vv.getPickedVertexState(), Color.white, Color.yellow);
    vv.getRenderContext().setVertexFillPaintTransformer(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(vv.getPickedEdgeState(), Color.black, Color.cyan));

    vv.setBackground(Color.white);

    final Function<Integer, String> vertexStringerImpl = new VertexStringerImpl<>(map);
    vv.getRenderContext().setVertexLabelTransformer(vertexStringerImpl);
    vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

    // features on and off. For a real application, use VertexIconAndShapeFunction instead.
    final VertexIconShapeTransformer<Integer> vertexImageShapeFunction =
        new VertexIconShapeTransformer<>(new EllipseVertexShapeTransformer<Integer>());
    vertexImageShapeFunction.setIconMap(iconMap);

    final Function<Integer, Icon> vertexIconFunction = iconMap::get;

    vv.getRenderContext().setVertexShapeTransformer(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconTransformer(vertexIconFunction);

    // Get the pickedState and add a listener that will decorate the
    // Vertex images with a checkmark icon when they are picked
    PickedState<Integer> ps = vv.getPickedVertexState();
    ps.addItemListener(new PickWithIconListener(vertexIconFunction));

    vv.addPostRenderPaintable(
        new VisualizationViewer.Paintable() {
          int x;
          int y;
          Font font;
          FontMetrics metrics;
          int swidth;
          int sheight;
          String str = "Thank You, slashdot.org, for the images!";

          public void paint(Graphics g) {
            Dimension d = vv.getSize();
            if (font == null) {
              font = new Font(g.getFont().getName(), Font.BOLD, 20);
              metrics = g.getFontMetrics(font);
              swidth = metrics.stringWidth(str);
              sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
              System.out.println("dimension width: " + d.width);
              System.out.println("string width: " + swidth);
              x = 100; // (d.width-swidth) / 2;
              y = (int) (d.height - sheight * 1.5);
            }
            g.setFont(font);
            Color oldColor = g.getColor();
            g.setColor(Color.lightGray);
            g.drawString(str, x, y);
            g.setColor(oldColor);
          }

          public boolean useTransform() {
            return false;
          }
        });

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(Object::toString);

    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse<Integer, Double> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);

    JPanel controls = new JPanel();
    controls.add(ControlHelpers.getZoomControls(vv, "Zoom"));

    controls.add(modePanel);
    content.add(controls, BorderLayout.SOUTH);

    this.viewSupport = new MagnifyImageLensSupport<>(vv);

    this.modelSupport = new LayoutLensSupport<>(vv);

    graphMouse.addItemListener(modelSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(viewSupport.getGraphMouse().getModeListener());

    ButtonGroup radio = new ButtonGroup();
    JRadioButton none = new JRadioButton("None");
    none.addItemListener(
        e -> {
          if (viewSupport != null) {
            viewSupport.deactivate();
          }
          if (modelSupport != null) {
            modelSupport.deactivate();
          }
        });
    none.setSelected(true);

    JRadioButton hyperView = new JRadioButton("View");
    hyperView.addItemListener(e -> viewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    JRadioButton hyperModel = new JRadioButton("Layout");
    hyperModel.addItemListener(
        e -> modelSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    radio.add(none);
    radio.add(hyperView);
    radio.add(hyperModel);

    JMenuBar menubar = new JMenuBar();
    JMenu modeMenu = graphMouse.getModeMenu();
    menubar.add(modeMenu);

    JPanel lensPanel = new JPanel(new GridLayout(2, 0));
    lensPanel.setBorder(BorderFactory.createTitledBorder("Lens"));
    lensPanel.add(none);
    lensPanel.add(hyperView);
    lensPanel.add(hyperModel);
    controls.add(lensPanel);
  }

  /**
   * A simple implementation of VertexStringer that gets Vertex labels from a Map
   *
   * @author Tom Nelson
   */
  class VertexStringerImpl<V> implements Function<V, String> {

    Map<V, String> map = new HashMap<>();

    boolean enabled = true;

    public VertexStringerImpl(Map<V, String> map) {
      this.map = map;
    }

    /** */
    public String apply(V v) {
      if (isEnabled()) {
        return map.get(v);
      } else {
        return "";
      }
    }

    /** @return Returns the enabled. */
    public boolean isEnabled() {
      return enabled;
    }

    /** @param enabled The enabled to set. */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  Network<Integer, Double> createGraph() {
    MutableNetwork<Integer, Double> graph = NetworkBuilder.directed().build();
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(3, 0, Math.random());
    graph.addEdge(0, 4, Math.random());
    graph.addEdge(4, 5, Math.random());
    graph.addEdge(5, 3, Math.random());
    graph.addEdge(2, 1, Math.random());
    graph.addEdge(4, 1, Math.random());
    graph.addEdge(8, 2, Math.random());
    graph.addEdge(3, 8, Math.random());
    graph.addEdge(6, 7, Math.random());
    graph.addEdge(7, 5, Math.random());
    graph.addEdge(0, 9, Math.random());
    graph.addEdge(9, 8, Math.random());
    graph.addEdge(7, 6, Math.random());
    graph.addEdge(6, 5, Math.random());
    graph.addEdge(4, 2, Math.random());
    graph.addEdge(5, 4, Math.random());
    graph.addEdge(4, 10, Math.random());
    graph.addEdge(10, 4, Math.random());

    return graph;
  }

  public static class PickWithIconListener implements ItemListener {
    Function<Integer, Icon> imager;
    Icon checked;

    public PickWithIconListener(Function<Integer, Icon> imager) {
      this.imager = imager;
      checked = new Checkmark(Color.red);
    }

    public void itemStateChanged(ItemEvent e) {
      Icon icon = imager.apply((Integer) e.getItem());
      if (icon != null && icon instanceof LayeredIcon) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          ((LayeredIcon) icon).add(checked);
        } else {
          ((LayeredIcon) icon).remove(checked);
        }
      }
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new DemoLensVertexImageShaperDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
