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
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.AnnotationControls;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.util.ParallelEdgeIndexFunction;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Shows how to create a graph editor with JUNG. Mouse modes and actions are explained in the help
 * text. The application version of GraphEditorDemo provides a File menu with an option to save the
 * visible graph as a jpeg file.
 *
 * @author Tom Nelson
 */
public class GraphEditorDemo extends JPanel implements Printable {

  /** */
  private static final long serialVersionUID = -2023243689258876709L;

  /** the graph */
  MutableNetwork<Number, Number> graph;

  LayoutAlgorithm<Number> layoutAlgorithm;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  String instructions =
      "<html>"
          + "<h3>All Modes:</h3>"
          + "<ul>"
          + "<li>Right-click an empty area for <b>Create Node</b> popup"
          + "<li>Right-click on a Node for <b>Delete Node</b> popup"
          + "<li>Right-click on a Node for <b>Add Edge</b> menus <br>(if there are selected Nodes)"
          + "<li>Right-click on an Edge for <b>Delete Edge</b> popup"
          + "<li>Mousewheel scales with a crossover value of 1.0.<p>"
          + "     - scales the graph layout when the combined scale is greater than 1<p>"
          + "     - scales the graph view when the combined scale is less than 1"
          + "</ul>"
          + "<h3>Editing Mode:</h3>"
          + "<ul>"
          + "<li>Left-click an empty area to create a new Node"
          + "<li>Left-click on a Node and drag to another Node to create an Undirected Edge"
          + "<li>Shift+Left-click on a Node and drag to another Node to create a Directed Edge"
          + "</ul>"
          + "<h3>Picking Mode:</h3>"
          + "<ul>"
          + "<li>Mouse1 on a Node selects the node"
          + "<li>Mouse1 elsewhere unselects all Nodes"
          + "<li>Mouse1+Shift on a Node adds/removes Node selection"
          + "<li>Mouse1+drag on a Node moves all selected Nodes"
          + "<li>Mouse1+drag elsewhere selects Nodes in a region"
          + "<li>Mouse1+Shift+drag adds selection of Nodes in a new region"
          + "<li>Mouse1+CTRL on a Node selects the node and centers the display on it"
          + "<li>Mouse1 double-click on a node or edge allows you to edit the label"
          + "</ul>"
          + "<h3>Transforming Mode:</h3>"
          + "<ul>"
          + "<li>Mouse1+drag pans the graph"
          + "<li>Mouse1+Shift+drag rotates the graph"
          + "<li>Mouse1+CTRL(or Command)+drag shears the graph"
          + "<li>Mouse1 double-click on a node or edge allows you to edit the label"
          + "</ul>"
          + "<h3>Annotation Mode:</h3>"
          + "<ul>"
          + "<li>Mouse1 begins drawing of a Rectangle"
          + "<li>Mouse1+drag defines the Rectangle shape"
          + "<li>Mouse1 release adds the Rectangle as an annotation"
          + "<li>Mouse1+Shift begins drawing of an Ellipse"
          + "<li>Mouse1+Shift+drag defines the Ellipse shape"
          + "<li>Mouse1+Shift release adds the Ellipse as an annotation"
          + "<li>Mouse3 shows a popup to input text, which will become"
          + "<li>a text annotation on the graph at the mouse location"
          + "</ul>"
          + "</html>";

  /** create an instance of a simple graph with popup controls to create a graph. */
  public GraphEditorDemo() {

    setLayout(new BorderLayout());
    // create a simple graph for the demo
    graph = NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();

    this.layoutAlgorithm = new StaticLayoutAlgorithm<>(); // , new Dimension(600, 600));

    vv = new VisualizationViewer<>(graph, layoutAlgorithm, new Dimension(600, 600));
    vv.setBackground(Color.white);

    Function<Object, String> labeller = Object::toString;
    vv.getRenderContext().setNodeLabelFunction(labeller);
    vv.getRenderContext().setEdgeLabelFunction(labeller);
    vv.getRenderContext().setParallelEdgeIndexFunction(new ParallelEdgeIndexFunction<>());

    vv.setNodeToolTipFunction(vv.getRenderContext().getNodeLabelFunction());

    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    add(panel);
    Supplier<Number> nodeFactory = new NodeFactory();
    Supplier<Number> edgeFactory = new EdgeFactory();

    final EditingModalGraphMouse<Number, Number> graphMouse =
        new EditingModalGraphMouse<>(vv.getRenderContext(), nodeFactory, edgeFactory);

    // the EditingGraphMouse will pass mouse event coordinates to the
    // nodeLocations function to set the locations of the nodes as
    // they are created
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    graphMouse.setMode(ModalGraphMouse.Mode.EDITING);

    final ScalingControl scaler = new CrossoverScalingControl();
    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JButton help = new JButton("Help");
    help.addActionListener(e -> JOptionPane.showMessageDialog(vv, instructions));

    AnnotationControls<Number, Number> annotationControls =
        new AnnotationControls<>(graphMouse.getAnnotatingPlugin());
    JPanel controls = new JPanel();
    controls.add(plus);
    controls.add(minus);
    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    controls.add(modeBox);
    controls.add(annotationControls.getAnnotationsToolBar());
    controls.add(help);
    add(controls, BorderLayout.SOUTH);
  }

  /**
   * copy the visible part of the graph to a file as a jpeg image
   *
   * @param file the file in which to save the graph image
   */
  public void writeJPEGImage(File file) {
    int width = vv.getWidth();
    int height = vv.getHeight();

    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = bi.createGraphics();
    vv.paint(graphics);
    graphics.dispose();

    try {
      ImageIO.write(bi, "jpeg", file);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int print(java.awt.Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex)
      throws java.awt.print.PrinterException {
    if (pageIndex > 0) {
      return (Printable.NO_SUCH_PAGE);
    } else {
      java.awt.Graphics2D g2d = (java.awt.Graphics2D) graphics;
      vv.setDoubleBuffered(false);
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

      vv.paint(g2d);
      vv.setDoubleBuffered(true);

      return (Printable.PAGE_EXISTS);
    }
  }

  class NodeFactory implements Supplier<Number> {

    int i = 0;

    public Number get() {
      return i++;
    }
  }

  class EdgeFactory implements Supplier<Number> {

    int i = 0;

    public Number get() {
      return i++;
    }
  }

  @SuppressWarnings("serial")
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    final GraphEditorDemo demo = new GraphEditorDemo();

    JMenu menu = new JMenu("File");
    menu.add(
        new AbstractAction("Make Image") {
          public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            int option = chooser.showSaveDialog(demo);
            if (option == JFileChooser.APPROVE_OPTION) {
              File file = chooser.getSelectedFile();
              demo.writeJPEGImage(file);
            }
          }
        });
    menu.add(
        new AbstractAction("Print") {
          public void actionPerformed(ActionEvent e) {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setPrintable(demo);
            if (printJob.printDialog()) {
              try {
                printJob.print();
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
          }
        });
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    frame.setJMenuBar(menuBar);
    frame.getContentPane().add(demo);
    frame.pack();
    frame.setVisible(true);
  }
}
