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
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EllipseNodeShapeFunction;
import edu.uci.ics.jung.visualization.decorators.NodeIconShapeFunction;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintFunction;
import edu.uci.ics.jung.visualization.decorators.PickableNodePaintFunction;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicNodeRenderer;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultNodeLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.ImageShapeUtils;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;

/**
 * Demonstrates the use of images to represent graph nodes. The images are supplied via the
 * NodeShapeFunction so that both the image and its shape can be utilized.
 *
 * <p>The images used in this demo (courtesy of slashdot.org) are rectangular but with a transparent
 * background. When nodes are represented by these images, it looks better if the actual shape of
 * the opaque part of the image is computed so that the edge arrowheads follow the visual shape of
 * the image. This demo uses the FourPassImageShaper class to compute the Shape from an image with
 * transparent background.
 *
 * @author Tom Nelson
 */
public class NodeImageShaperDemo extends JPanel {

  /** */
  private static final long serialVersionUID = -4332663871914930864L;

  /** the graph */
  Network<Number, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  /** some icon names to use */
  String[] iconNames = {
    "apple",
    "os",
    "x",
    "linux",
    "inputdevices",
    "wireless",
    "graphics3",
    "gamespcgames",
    "humor",
    "music",
    "privacy"
  };

  public NodeImageShaperDemo() {
    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = createGraph();

    // Maps for the labels and icons
    Map<Number, String> map = new HashMap<>();
    Map<Number, Icon> iconMap = new HashMap<>();
    for (Number node : graph.nodes()) {
      int i = node.intValue();
      map.put(node, iconNames[i % iconNames.length]);

      String name = "/images/topic" + iconNames[i] + ".gif";
      try {
        Icon icon =
            new LayeredIcon(new ImageIcon(NodeImageShaperDemo.class.getResource(name)).getImage());
        iconMap.put(node, icon);
      } catch (Exception ex) {
        System.err.println("You need slashdoticons.jar in your classpath to see the image " + name);
      }
    }

    FRLayoutAlgorithm<Number> layoutAlgorithm = new FRLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(100);
    //    treeLayoutAlgorithm.setInitializer(new RandomLocationTransformer<>(new Dimension(400,
    // 400), 0));

    vv =
        new VisualizationViewer<>(
            new BaseVisualizationModel(
                graph,
                layoutAlgorithm,
                new RandomLocationTransformer<>(400, 400, 0),
                new Dimension(400, 400)),
            new Dimension(400, 400));

    // This demo uses a special renderer to turn outlines on and off.
    // you do not need to do this in a real application.
    // Instead, just let vv use the Renderer it already has
    vv.getRenderer().setNodeRenderer(new DemoRenderer<>());

    Function<Number, Paint> vpf =
        new PickableNodePaintFunction<>(vv.getPickedNodeState(), Color.white, Color.yellow);
    vv.getRenderContext().setNodeFillPaintFunction(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintFunction(
            new PickableEdgePaintFunction<>(vv.getPickedEdgeState(), Color.black, Color.cyan));

    vv.setBackground(Color.white);

    final Function<Number, String> nodeStringerImpl = new NodeStringerImpl<>(map);
    vv.getRenderContext().setNodeLabelFunction(nodeStringerImpl);
    vv.getRenderContext().setNodeLabelRenderer(new DefaultNodeLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

    // For this demo only, I use a special class that lets me turn various
    // features on and off. For a real application, use NodeIconShapeTransformer instead.
    final DemoNodeIconShapeFunction<Number> nodeIconShapeTransformer =
        new DemoNodeIconShapeFunction<>(new EllipseNodeShapeFunction<>());
    nodeIconShapeTransformer.setIconMap(iconMap);

    final DemoNodeIconTransformer<Number> nodeIconTransformer =
        new DemoNodeIconTransformer<>(iconMap);

    vv.getRenderContext().setNodeShapeFunction(nodeIconShapeTransformer);
    vv.getRenderContext().setNodeIconFunction(nodeIconTransformer);

    // Get the pickedState and add a listener that will decorate the
    // Node images with a checkmark icon when they are picked
    PickedState<Number> ps = vv.getPickedNodeState();
    ps.addItemListener(new PickWithIconListener<>(nodeIconTransformer));

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
              x = (d.width - swidth) / 2;
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
    vv.setNodeToolTipFunction(Object::toString);

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);

    final DefaultModalGraphMouse<Number, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());
    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JCheckBox shape = new JCheckBox("Shape");
    shape.addItemListener(
        e -> {
          nodeIconShapeTransformer.setShapeImages(e.getStateChange() == ItemEvent.SELECTED);
          vv.repaint();
        });

    shape.setSelected(true);

    JCheckBox fill = new JCheckBox("Fill");
    fill.addItemListener(
        e -> {
          nodeIconTransformer.setFillImages(e.getStateChange() == ItemEvent.SELECTED);
          vv.repaint();
        });

    fill.setSelected(true);

    JCheckBox drawOutlines = new JCheckBox("Outline");
    drawOutlines.addItemListener(
        e -> {
          nodeIconTransformer.setOutlineImages(e.getStateChange() == ItemEvent.SELECTED);
          vv.repaint();
        });

    JComboBox<?> modeBox = graphMouse.getModeComboBox();
    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
    JPanel labelFeatures = new JPanel(new GridLayout(1, 0));
    labelFeatures.setBorder(BorderFactory.createTitledBorder("Image Effects"));
    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    controls.add(scaleGrid);
    labelFeatures.add(shape);
    labelFeatures.add(fill);
    labelFeatures.add(drawOutlines);

    controls.add(labelFeatures);
    controls.add(modePanel);
    add(controls, BorderLayout.SOUTH);
  }

  /**
   * When Nodes are picked, add a checkmark icon to the imager. Remove the icon when a Node is
   * unpicked
   *
   * @author Tom Nelson
   */
  public static class PickWithIconListener<N> implements ItemListener {
    Function<N, Icon> imager;
    Icon checked;

    public PickWithIconListener(Function<N, Icon> imager) {
      this.imager = imager;
      checked = new Checkmark();
    }

    public void itemStateChanged(ItemEvent e) {
      @SuppressWarnings("unchecked")
      Icon icon = imager.apply((N) e.getItem());
      if (icon != null && icon instanceof LayeredIcon) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          ((LayeredIcon) icon).add(checked);
        } else {
          ((LayeredIcon) icon).remove(checked);
        }
      }
    }
  }
  /**
   * A simple implementation of Function that gets Node labels from a Map
   *
   * @author Tom Nelson
   */
  public static class NodeStringerImpl<N> implements Function<N, String> {

    Map<N, String> map = new HashMap<>();

    boolean enabled = true;

    public NodeStringerImpl(Map<N, String> map) {
      this.map = map;
    }

    public String apply(N v) {
      if (isEnabled()) {
        return map.get(v);
      } else {
        return "";
      }
    }

    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * @param enabled The enabled to set.
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  Network<Number, Number> createGraph() {
    MutableNetwork<Number, Number> graph = NetworkBuilder.directed().build();
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

  /**
   * This class exists only to provide settings to turn on/off shapes and image fill in this demo.
   *
   * <p>For a real application, just use {@code Functions.forMap(iconMap)} to provide a {@code
   * Function<N, Icon>}.
   */
  public static class DemoNodeIconTransformer<N> implements Function<N, Icon> {
    boolean fillImages = true;
    boolean outlineImages = false;
    Map<N, Icon> iconMap = new HashMap<>();

    public DemoNodeIconTransformer(Map<N, Icon> iconMap) {
      this.iconMap = iconMap;
    }

    /**
     * @return Returns the fillImages.
     */
    public boolean isFillImages() {
      return fillImages;
    }
    /**
     * @param fillImages The fillImages to set.
     */
    public void setFillImages(boolean fillImages) {
      this.fillImages = fillImages;
    }

    public boolean isOutlineImages() {
      return outlineImages;
    }

    public void setOutlineImages(boolean outlineImages) {
      this.outlineImages = outlineImages;
    }

    public Icon apply(N v) {
      if (fillImages) {
        return iconMap.get(v);
      } else {
        return null;
      }
    }
  }

  /**
   * this class exists only to provide settings to turn on/off shapes and image fill in this demo.
   * In a real application, use NodeIconShapeTransformer instead.
   */
  public static class DemoNodeIconShapeFunction<N> extends NodeIconShapeFunction<N> {

    boolean shapeImages = true;

    public DemoNodeIconShapeFunction(Function<N, Shape> delegate) {
      super(delegate);
    }

    /**
     * @return Returns the shapeImages.
     */
    public boolean isShapeImages() {
      return shapeImages;
    }
    /**
     * @param shapeImages The shapeImages to set.
     */
    public void setShapeImages(boolean shapeImages) {
      shapeMap.clear();
      this.shapeImages = shapeImages;
    }

    @Override
    public Shape apply(N v) {
      Icon icon = iconMap.get(v);

      if (icon != null && icon instanceof ImageIcon) {

        Image image = ((ImageIcon) icon).getImage();

        Shape shape = shapeMap.get(image);
        if (shape == null) {
          if (shapeImages) {
            shape = ImageShapeUtils.getShape(image, 30);
          } else {
            shape = new Rectangle2D.Float(0, 0, image.getWidth(null), image.getHeight(null));
          }
          if (shape.getBounds().getWidth() > 0 && shape.getBounds().getHeight() > 0) {
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            AffineTransform transform =
                AffineTransform.getTranslateInstance(-width / 2, -height / 2);
            shape = transform.createTransformedShape(shape);
            shapeMap.put(image, shape);
          }
        }
        return shape;
      } else {
        return delegate.apply(v);
      }
    }
  }

  /**
   * a special renderer that can turn outlines on and off in this demo. You won't need this for a
   * real application. Use BasicNodeRenderer instead
   *
   * @author Tom Nelson
   */
  class DemoRenderer<N, E> extends BasicNodeRenderer<N, E> {

    @Override
    public void paintIconForNode(
        RenderContext<N, E> renderContext, VisualizationModel<N, E> model, N v) {

      Point p = model.getLayoutModel().apply(v);
      Point2D p2d =
          renderContext
              .getMultiLayerTransformer()
              .transform(Layer.LAYOUT, new Point2D.Double(p.x, p.y));
      float x = (float) p2d.getX();
      float y = (float) p2d.getY();

      GraphicsDecorator g = renderContext.getGraphicsContext();
      boolean outlineImages = false;
      Function<N, Icon> nodeIconFunction = renderContext.getNodeIconFunction();

      if (nodeIconFunction instanceof DemoNodeIconTransformer) {
        outlineImages = ((DemoNodeIconTransformer<N>) nodeIconFunction).isOutlineImages();
      }
      Icon icon = nodeIconFunction.apply(v);
      if (icon == null || outlineImages) {

        Shape s =
            AffineTransform.getTranslateInstance(x, y)
                .createTransformedShape(renderContext.getNodeShapeFunction().apply(v));
        paintShapeForNode(renderContext, model, v, s);
      }
      if (icon != null) {
        int xLoc = (int) (x - icon.getIconWidth() / 2);
        int yLoc = (int) (y - icon.getIconHeight() / 2);
        icon.paintIcon(renderContext.getScreenDevice(), g.getDelegate(), xLoc, yLoc);
      }
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new NodeImageShaperDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
