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
import edu.uci.ics.jung.visualization.BaseVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.AWTDomainModel;
import edu.uci.ics.jung.visualization.layout.DomainModel;
import edu.uci.ics.jung.visualization.layout.LayoutAlgorithm;
import edu.uci.ics.jung.visualization.layout.StaticLayoutAlgorithm;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer.InsidePositioner;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Shows a graph overlaid on a world map image. Scaling of the graph also scales the image
 * background.
 *
 * @author Tom Nelson
 */
@SuppressWarnings("serial")
public class WorldMapGraphDemo extends JApplet {

  private static final DomainModel<Point2D> domainModel = new AWTDomainModel();

  /** the graph */
  Network<String, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<String, Number> vv;

  List<String> cityList;

  /** create an instance of a simple graph with controls to demo the zoom features. */
  public WorldMapGraphDemo() {
    setLayout(new BorderLayout());

    Map<String, String[]> map = buildMap();

    cityList = new ArrayList<String>(map.keySet());

    // create a simple graph for the demo
    graph = buildGraph(map);

    ImageIcon mapIcon = null;
    String imageLocation = "/images/political_world_map.jpg";
    try {
      mapIcon = new ImageIcon(getClass().getResource(imageLocation));
    } catch (Exception ex) {
      System.err.println("Can't load \"" + imageLocation + "\"");
    }
    final ImageIcon icon = mapIcon;

    Dimension layoutSize = new Dimension(2000, 1000);

    LayoutAlgorithm<String, Point2D> layoutAlgorithm = new StaticLayoutAlgorithm<>(domainModel);

    Function<String, Point2D> initializer =
        new CityTransformer(map).andThen(new LatLonPixelTransformer(new Dimension(2000, 1000)));
    VisualizationModel<String, Number, Point2D> model =
        new BaseVisualizationModel<>(
            graph, layoutAlgorithm, initializer, new Dimension(2000, 1000));

    vv = new VisualizationViewer<>(model, new Dimension(800, 400));

    if (icon != null) {
      vv.addPreRenderPaintable(
          new VisualizationViewer.Paintable() {
            public void paint(Graphics g) {
              Graphics2D g2d = (Graphics2D) g;
              AffineTransform oldXform = g2d.getTransform();
              AffineTransform lat =
                  vv.getRenderContext()
                      .getMultiLayerTransformer()
                      .getTransformer(Layer.LAYOUT)
                      .getTransform();
              AffineTransform vat =
                  vv.getRenderContext()
                      .getMultiLayerTransformer()
                      .getTransformer(Layer.VIEW)
                      .getTransform();
              AffineTransform at = new AffineTransform();
              at.concatenate(g2d.getTransform());
              at.concatenate(vat);
              at.concatenate(lat);
              g2d.setTransform(at);
              g.drawImage(icon.getImage(), 0, 0, icon.getIconWidth(), icon.getIconHeight(), vv);
              g2d.setTransform(oldXform);
            }

            public boolean useTransform() {
              return false;
            }
          });
    }

    vv.getRenderer()
        .setVertexRenderer(
            new GradientVertexRenderer<String, Number>(
                vv, Color.white, Color.red, Color.white, Color.blue, false));

    // add my listeners for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());
    vv.setEdgeToolTipTransformer(
        new Function<Number, String>() {
          public String apply(Number edge) {
            return "E" + graph.incidentNodes(edge).toString();
          }
        });

    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.getRenderer().getVertexLabelRenderer().setPositioner(new InsidePositioner());
    vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);
    final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<String, Number>();
    vv.setGraphMouse(graphMouse);

    vv.addKeyListener(graphMouse.getModeKeyListener());
    vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

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

    JButton reset = new JButton("reset");
    reset.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.LAYOUT)
                .setToIdentity();
            vv.getRenderContext()
                .getMultiLayerTransformer()
                .getTransformer(Layer.VIEW)
                .setToIdentity();
          }
        });

    JPanel controls = new JPanel();
    controls.add(plus);
    controls.add(minus);
    controls.add(reset);
    add(controls, BorderLayout.SOUTH);
  }

  private Map<String, String[]> buildMap() {
    Map<String, String[]> map = new HashMap<String, String[]>();

    map.put("TYO", new String[] {"35 40 N", "139 45 E"});
    map.put("PEK", new String[] {"39 55 N", "116 26 E"});
    map.put("MOW", new String[] {"55 45 N", "37 42 E"});
    map.put("JRS", new String[] {"31 47 N", "35 13 E"});
    map.put("CAI", new String[] {"30 03 N", "31 15 E"});
    map.put("CPT", new String[] {"33 55 S", "18 22 E"});
    map.put("PAR", new String[] {"48 52 N", "2 20 E"});
    map.put("LHR", new String[] {"51 30 N", "0 10 W"});
    map.put("HNL", new String[] {"21 18 N", "157 51 W"});
    map.put("NYC", new String[] {"40 77 N", "73 98 W"});
    map.put("SFO", new String[] {"37 62 N", "122 38 W"});
    map.put("AKL", new String[] {"36 55 S", "174 47 E"});
    map.put("BNE", new String[] {"27 28 S", "153 02 E"});
    map.put("HKG", new String[] {"22 15 N", "114 10 E"});
    map.put("KTM", new String[] {"27 42 N", "85 19 E"});
    map.put("IST", new String[] {"41 01 N", "28 58 E"});
    map.put("STO", new String[] {"59 20 N", "18 03 E"});
    map.put("RIO", new String[] {"22 54 S", "43 14 W"});
    map.put("LIM", new String[] {"12 03 S", "77 03 W"});
    map.put("YTO", new String[] {"43 39 N", "79 23 W"});

    return map;
  }

  private Network<String, Number> buildGraph(Map<String, String[]> map) {
    MutableNetwork<String, Number> graph =
        NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();
    for (String city : map.keySet()) {
      graph.addNode(city);
    }
    for (int i = 0; i < map.keySet().size() * 1.3; i++) {
      graph.addEdge(randomCity(), randomCity(), new Double(Math.random()));
    }
    return graph;
  }

  private String randomCity() {
    int m = cityList.size();
    return cityList.get((int) (Math.random() * m));
  }

  static class CityTransformer implements Function<String, String[]> {

    Map<String, String[]> map;

    public CityTransformer(Map<String, String[]> map) {
      this.map = map;
    }

    /** transform airport code to latlon string */
    public String[] apply(String city) {
      return map.get(city);
    }
  }

  static class LatLonPixelTransformer implements Function<String[], Point2D> {
    Dimension d;
    int startOffset;

    public LatLonPixelTransformer(Dimension d) {
      this.d = d;
    }
    /** transform a lat */
    public Point2D apply(String[] latlon) {
      double latitude = 0;
      double longitude = 0;
      String[] lat = latlon[0].split(" ");
      String[] lon = latlon[1].split(" ");
      latitude = Integer.parseInt(lat[0]) + Integer.parseInt(lat[1]) / 60f;
      latitude *= d.height / 180f;
      longitude = Integer.parseInt(lon[0]) + Integer.parseInt(lon[1]) / 60f;
      longitude *= d.width / 360f;
      if (lat[2].equals("N")) {
        latitude = d.height / 2 - latitude;

      } else { // assume S
        latitude = d.height / 2 + latitude;
      }

      if (lon[2].equals("W")) {
        longitude = d.width / 2 - longitude;

      } else { // assume E
        longitude = d.width / 2 + longitude;
      }

      return new Point2D.Double(longitude, latitude);
    }
  }

  public static void main(String[] args) {
    // create a frome to hold the graph
    final JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    content.add(new WorldMapGraphDemo());
    frame.pack();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
