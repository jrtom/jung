/*
 * Created on Jan 2, 2004
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Demonstrates use of the shortest path algorithm and visualization of the results.
 *
 * @author danyelf
 */
public class ShortestPathDemo extends JPanel {

  /** */
  private static final long serialVersionUID = 7526217664458188502L;

  /** Starting vertex */
  private String mFrom;

  /** Ending vertex */
  private String mTo;

  private Network<String, Number> mGraph;
  private Set<String> mPred;

  public ShortestPathDemo() {

    this.mGraph = getGraph();
    setBackground(Color.WHITE);
    // show graph
    final Layout<String> layout = new FRLayout<String>(mGraph.asGraph());
    final VisualizationViewer vv = new VisualizationViewer(mGraph, layout);
    vv.setBackground(Color.WHITE);

    vv.getRenderContext().setVertexDrawPaintTransformer(new MyVertexDrawPaintFunction());
    vv.getRenderContext().setVertexFillPaintTransformer(new MyVertexFillPaintFunction());
    vv.getRenderContext().setEdgeDrawPaintTransformer(new MyEdgePaintFunction());
    vv.getRenderContext().setEdgeStrokeTransformer(new MyEdgeStrokeFunction());
    vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
    vv.setGraphMouse(new DefaultModalGraphMouse());
    vv.addPostRenderPaintable(
        new VisualizationViewer.Paintable() {

          public boolean useTransform() {
            return true;
          }

          public void paint(Graphics g) {
            if (mPred == null) {
              return;
            }

            // for all edges, paint edges that are in shortest path
            for (Number e : mGraph.edges()) {
              if (isBlessed(e)) {
                EndpointPair<String> endpoints = mGraph.incidentNodes(e);
                String v1 = endpoints.nodeU();
                String v2 = endpoints.nodeV();
                Point2D p1 = layout.apply(v1);
                Point2D p2 = layout.apply(v2);
                p1 = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
                p2 = vv.getRenderContext().getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
                Renderer renderer = vv.getRenderer();
                renderer.renderEdge(vv.getRenderContext(), vv.getModel().getLayoutMediator(), e);
              }
            }
          }
        });

    setLayout(new BorderLayout());
    add(vv, BorderLayout.CENTER);
    // set up controls
    add(setUpControls(), BorderLayout.SOUTH);
  }

  boolean isBlessed(Object e) {
    EndpointPair<String> endpoints = mGraph.incidentNodes((Number) e);
    String v1 = endpoints.nodeU();
    String v2 = endpoints.nodeV();
    return v1.equals(v2) == false && mPred.contains(v1) && mPred.contains(v2);
  }

  /** @author danyelf */
  public class MyEdgePaintFunction implements Function<Object, Paint> {

    public Paint apply(Object e) {
      if (mPred == null || mPred.size() == 0) {
        return Color.BLACK;
      }
      if (isBlessed(e)) {
        return new Color(0.0f, 0.0f, 1.0f, 0.5f); //Color.BLUE;
      } else {
        return Color.LIGHT_GRAY;
      }
    }
  }

  public class MyEdgeStrokeFunction implements Function<Object, Stroke> {
    protected final Stroke THIN = new BasicStroke(1);
    protected final Stroke THICK = new BasicStroke(1);

    public Stroke apply(Object e) {
      if (mPred == null || mPred.size() == 0) {
        return THIN;
      }
      if (isBlessed(e)) {
        return THICK;
      } else {
        return THIN;
      }
    }
  }

  /** @author danyelf */
  public class MyVertexDrawPaintFunction<V> implements Function<V, Paint> {

    public Paint apply(V v) {
      return Color.black;
    }
  }

  public class MyVertexFillPaintFunction<V> implements Function<V, Paint> {

    public Paint apply(V v) {
      if (v == mFrom) {
        return Color.BLUE;
      }
      if (v == mTo) {
        return Color.BLUE;
      }
      if (mPred == null) {
        return Color.LIGHT_GRAY;
      } else {
        if (mPred.contains(v)) {
          return Color.RED;
        } else {
          return Color.LIGHT_GRAY;
        }
      }
    }
  }

  /** */
  private JPanel setUpControls() {
    JPanel jp = new JPanel();
    jp.setBackground(Color.WHITE);
    jp.setLayout(new BoxLayout(jp, BoxLayout.PAGE_AXIS));
    jp.setBorder(BorderFactory.createLineBorder(Color.black, 3));
    jp.add(new JLabel("Select a pair of vertices for which a shortest path will be displayed"));
    JPanel jp2 = new JPanel();
    jp2.add(new JLabel("vertex from", SwingConstants.LEFT));
    jp2.add(getSelectionBox(true));
    jp2.setBackground(Color.white);
    JPanel jp3 = new JPanel();
    jp3.add(new JLabel("vertex to", SwingConstants.LEFT));
    jp3.add(getSelectionBox(false));
    jp3.setBackground(Color.white);
    jp.add(jp2);
    jp.add(jp3);
    return jp;
  }

  private Component getSelectionBox(final boolean from) {
    String[] nodes = new String[mGraph.nodes().size()];
    int i = 0;
    for (String node : mGraph.nodes()) {
      nodes[i++] = node;
    }
    final JComboBox<String> choices = new JComboBox<String>(nodes);
    choices.setSelectedIndex(-1);
    choices.setBackground(Color.WHITE);
    choices.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            String v = (String) choices.getSelectedItem();

            if (from) {
              mFrom = v;
            } else {
              mTo = v;
            }
            drawShortest();
            repaint();
          }
        });
    return choices;
  }

  /** */
  protected void drawShortest() {
    if (mFrom == null || mTo == null) {
      return;
    }
    BFSDistanceLabeler<String> bdl = new BFSDistanceLabeler<String>();
    bdl.labelDistances(mGraph.asGraph(), mFrom);
    mPred = new HashSet<String>();

    // grab a predecessor
    String v = mTo;
    Set<String> prd = bdl.getPredecessors(v);
    mPred.add(mTo);
    while (prd != null && prd.size() > 0) {
      v = prd.iterator().next();
      mPred.add(v);
      if (v == mFrom) {
        return;
      }
      prd = bdl.getPredecessors(v);
    }
  }

  public static void main(String[] s) {
    JFrame jf = new JFrame();
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.getContentPane().add(new ShortestPathDemo());
    jf.pack();
    jf.setVisible(true);
  }

  /** @return the graph for this demo */
  Network<String, Number> getGraph() {
    Graph<String> g = new EppsteinPowerLawGenerator<String>(new VertexFactory(), 26, 50, 50).get();
    // convert this graph into a Network because the visualization system can't handle Graphs (yet)
    MutableNetwork<String, Number> graph =
        NetworkBuilder.undirected().nodeOrder(ElementOrder.<String>natural()).build();
    EdgeFactory edgeFactory = new EdgeFactory();
    // this implicitly removes any isolated nodes, as intended
    for (EndpointPair<String> endpoints : g.edges()) {
      graph.addEdge(endpoints.nodeU(), endpoints.nodeV(), edgeFactory.get());
    }
    return graph;
  }

  static class VertexFactory implements Supplier<String> {
    char a = 'a';

    public String get() {
      return Character.toString(a++);
    }
  }

  static class EdgeFactory implements Supplier<Number> {
    int count;

    public Number get() {
      return count++;
    }
  }
}
