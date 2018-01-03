package edu.uci.ics.jung.samples;

import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree;
import edu.uci.ics.jung.layout.spatial.ForceObject;
import edu.uci.ics.jung.layout.spatial.Node;
import edu.uci.ics.jung.layout.spatial.Rectangle;
import edu.uci.ics.jung.layout.util.RandomLocationTransformer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarnesHutVisualizer extends JPanel {

  private static final Logger log = LoggerFactory.getLogger(BarnesHutVisualizer.class);

  LayoutModel<String> layoutModel;
  MutableNetwork<String, Number> network;
  BarnesHutQuadTree<String> tree;

  public BarnesHutVisualizer() {
    setLayout(new BorderLayout());

    network = NetworkBuilder.undirected().allowsParallelEdges(true).build();
    network.addNode("A");
    network.addNode("B");
    network.addNode("C");
    network.addNode("D");

    Graph<String> graph = network.asGraph();

    layoutModel =
        LoadingCacheLayoutModel.<String>builder()
            .setGraph(graph)
            .setSize(600, 600)
            .setInitializer(new RandomLocationTransformer<>(600, 600, System.currentTimeMillis()))
            .build();
    layoutModel.set("A", Point.of(200, 100));
    layoutModel.set("B", Point.of(100, 200));
    layoutModel.set("C", Point.of(100, 100));
    layoutModel.set("D", Point.of(500, 100));

    tree = new BarnesHutQuadTree<>(layoutModel);
    tree.rebuild();

    JPanel drawingPanel =
        new JPanel() {
          @Override
          public Dimension getPreferredSize() {
            return new Dimension(600, 600);
          }

          @Override
          public void paint(Graphics g) {
            log.info("paint");
            super.paint(g);
            Graphics2D g2d = (Graphics2D) g;
            draw(g2d, tree.getRoot());
          }
        };
    add(drawingPanel);
    drawingPanel.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            addShapeAt(e.getPoint());
            repaint();
          }
        });

    JButton clear = new JButton("clear");
    clear.addActionListener(e -> clearNetwork());
    JButton go = new JButton("go");
    go.addActionListener(
        e -> {
          for (String node : graph.nodes()) {
            Point p = layoutModel.apply(node);
            ForceObject<String> fo = new ForceObject(node, p);
            Iterator<ForceObject<String>> foiter =
                new BarnesHutQuadTree.ForceObjectIterator<>(tree, fo);
            while (foiter.hasNext()) {
              ForceObject<String> next = foiter.next();
              log.info("for node {}, next force object is {}", node, next);
            }
          }
        });
    JPanel controls = new JPanel();
    controls.add(go);
    controls.add(clear);
    add(controls, BorderLayout.SOUTH);
  }

  private void clearNetwork() {
    Set<String> nodes = Sets.newHashSet(network.nodes());
    for (String node : nodes) {
      network.removeNode(node);
    }
    tree.clear();
    tree.rebuild();
    repaint();
  }

  private void addShapeAt(Point2D p) {
    String n = "N" + network.nodes().size();
    layoutModel.set(n, p.getX(), p.getY());
    network.addNode(n);
    tree.rebuild();
    repaint();
  }

  private void draw(Graphics2D g, Node node) {
    Rectangle bounds = node.getBounds();
    Rectangle2D r = new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
    g.draw(r);
    ForceObject forceObject = node.getForceObject();
    if (forceObject != null) {
      Point center = node.getForceObject().p;
      Ellipse2D forceCenter = new Ellipse2D.Double(center.x - 5, center.y - 5, 10, 10);
      Color oldColor = g.getColor();
      g.setColor(Color.red);

      Point2D centerOfNode = new Point2D.Double((r.getCenterX()), r.getCenterY());
      Point2D centerOfForce = new Point2D.Double(center.x, center.y);
      g.draw(new Line2D.Double(centerOfNode, centerOfForce));
      g.draw(forceCenter);
      g.setColor(oldColor);
    }
    if (node.getNW() != null) {
      log.info("draw NW:{}", node.getNW());
      draw(g, node.getNW());
    }
    if (node.getNE() != null) {
      log.info("draw NE:{}", node.getNE());
      draw(g, node.getNE());
    }
    if (node.getSW() != null) {
      log.info("draw SW:{}", node.getSW());
      draw(g, node.getSW());
    }
    if (node.getSE() != null) {
      log.info("draw SE:{}", node.getSE());
      draw(g, node.getSE());
    }
    if (forceObject != null) {
      log.info("draw forceObject:{}", forceObject);
      Point p = forceObject.p;
      Ellipse2D circle = new Ellipse2D.Double(p.x - 2, p.y - 2, 4, 4);
      g.fill(circle);
      g.drawString(forceObject.getElement().toString(), (int) p.x + 4, (int) p.y - 4);
    }
  }

  public static void main(String[] args) {

    JFrame frame = new JFrame();
    frame.getContentPane().add(new BarnesHutVisualizer());
    frame.pack();
    frame.setVisible(true);
  }
}
