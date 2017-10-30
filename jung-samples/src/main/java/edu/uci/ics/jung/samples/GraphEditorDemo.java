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
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.util.ParallelEdgeIndexFunction;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.AnnotationControls;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Shows how to create a graph editor with JUNG. Mouse modes and actions are explained in the help
 * text. The application version of GraphEditorDemo provides a File menu with an option to save the
 * visible graph as a jpeg file.
 *
 * @author Tom Nelson
 */
public class GraphEditorDemo extends JApplet implements Printable {

  /** */
  private static final long serialVersionUID = -2023243689258876709L;

  /** the graph */
  MutableNetwork<Number, Number> graph;

  AbstractLayout<Number> layout;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  String instructions =
      "<html>"
          + "<h3>All Modes:</h3>"
          + "<ul>"
          + "<li>Right-click an empty area for <b>Create Vertex</b> popup"
          + "<li>Right-click on a Vertex for <b>Delete Vertex</b> popup"
          + "<li>Right-click on a Vertex for <b>Add Edge</b> menus <br>(if there are selected Vertices)"
          + "<li>Right-click on an Edge for <b>Delete Edge</b> popup"
          + "<li>Mousewheel scales with a crossover value of 1.0.<p>"
          + "     - scales the graph layout when the combined scale is greater than 1<p>"
          + "     - scales the graph view when the combined scale is less than 1"
          + "</ul>"
          + "<h3>Editing Mode:</h3>"
          + "<ul>"
          + "<li>Left-click an empty area to create a new Vertex"
          + "<li>Left-click on a Vertex and drag to another Vertex to create an Undirected Edge"
          + "<li>Shift+Left-click on a Vertex and drag to another Vertex to create a Directed Edge"
          + "</ul>"
          + "<h3>Picking Mode:</h3>"
          + "<ul>"
          + "<li>Mouse1 on a Vertex selects the vertex"
          + "<li>Mouse1 elsewhere unselects all Vertices"
          + "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"
          + "<li>Mouse1+drag on a Vertex moves all selected Vertices"
          + "<li>Mouse1+drag elsewhere selects Vertices in a region"
          + "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
          + "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"
          + "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
          + "</ul>"
          + "<h3>Transforming Mode:</h3>"
          + "<ul>"
          + "<li>Mouse1+drag pans the graph"
          + "<li>Mouse1+Shift+drag rotates the graph"
          + "<li>Mouse1+CTRL(or Command)+drag shears the graph"
          + "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
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

    // create a simple graph for the demo
    graph = NetworkBuilder.directed().allowsParallelEdges(true).allowsSelfLoops(true).build();

    this.layout = new StaticLayout<Number>(graph.asGraph(), new Dimension(600, 600));

    vv = new VisualizationViewer<Number, Number>(graph, layout);
    vv.setBackground(Color.white);

    Function<Object, String> labeller = new ToStringLabeller();
    vv.getRenderContext().setVertexLabelTransformer(labeller);
    vv.getRenderContext().setEdgeLabelTransformer(labeller);
    vv.getRenderContext()
        .setParallelEdgeIndexFunction(new ParallelEdgeIndexFunction<Number, Number>(graph));

    vv.setVertexToolTipTransformer(vv.getRenderContext().getVertexLabelTransformer());

    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);
    Supplier<Number> vertexFactory = new VertexFactory();
    Supplier<Number> edgeFactory = new EdgeFactory();

    final EditingModalGraphMouse<Number, Number> graphMouse =
        new EditingModalGraphMouse<Number, Number>(
            vv.getRenderContext(), vertexFactory, edgeFactory);

    // the EditingGraphMouse will pass mouse event coordinates to the
    // vertexLocations function to set the locations of the vertices as
    // they are created
    vv.setGraphMouse(graphMouse);
    vv.addKeyListener(graphMouse.getModeKeyListener());

    graphMouse.setMode(ModalGraphMouse.Mode.EDITING);

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

    JButton help = new JButton("Help");
    help.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(vv, instructions);
          }
        });

    AnnotationControls<Number, Number> annotationControls =
        new AnnotationControls<Number, Number>(graphMouse.getAnnotatingPlugin());
    JPanel controls = new JPanel();
    controls.add(plus);
    controls.add(minus);
    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    controls.add(modeBox);
    controls.add(annotationControls.getAnnotationsToolBar());
    controls.add(help);
    content.add(controls, BorderLayout.SOUTH);
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

  class VertexFactory implements Supplier<Number> {

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
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
