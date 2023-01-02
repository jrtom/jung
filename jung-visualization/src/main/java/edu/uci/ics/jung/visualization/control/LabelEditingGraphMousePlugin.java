/*
 * Copyright (c) 2005, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Mar 8, 2005
 *
 */
package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.algorithms.util.MapSettableTransformer;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.function.Function;
import javax.swing.JOptionPane;

/**
 * @author Tom Nelson
 */
public class LabelEditingGraphMousePlugin<N, E> extends AbstractGraphMousePlugin
    implements MouseListener {

  /** the picked Node, if any */
  protected N node;

  /** the picked Edge, if any */
  protected E edge;

  /** create an instance with default settings */
  public LabelEditingGraphMousePlugin() {
    this(InputEvent.BUTTON1_MASK);
  }

  /**
   * create an instance with overrides
   *
   * @param selectionModifiers for primary selection
   */
  public LabelEditingGraphMousePlugin(int selectionModifiers) {
    super(selectionModifiers);
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /**
   * For primary modifiers (default, MouseButton1): pick a single Node or Edge that is under the
   * mouse pointer. If no Node or edge is under the pointer, unselect all picked Nodes and edges,
   * and set up to draw a rectangle for multiple selection of contained Nodes. For additional
   * selection (default Shift+MouseButton1): Add to the selection, a single Node or Edge that is
   * under the mouse pointer. If a previously picked Node or Edge is under the pointer, it is
   * un-picked. If no node or Edge is under the pointer, set up to draw a multiple selection
   * rectangle (as above) but do not unpick previously picked elements.
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mouseClicked(MouseEvent e) {
    if (e.getModifiers() == modifiers && e.getClickCount() == 2) {
      VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
      LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
      NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
      if (pickSupport != null) {
        Function<? super N, String> vs = vv.getRenderContext().getNodeLabelFunction();
        if (vs instanceof MapSettableTransformer) {
          MapSettableTransformer<? super N, String> mst =
              (MapSettableTransformer<? super N, String>) vs;
          //    				Layout<N, Point2D> layout = vv.getGraphLayout();
          // p is the screen point for the mouse event
          Point2D p = e.getPoint();

          N node = pickSupport.getNode(layoutModel, p.getX(), p.getY());
          if (node != null) {
            String newLabel = vs.apply(node);
            newLabel = JOptionPane.showInputDialog("New Node Label for " + node);
            if (newLabel != null) {
              mst.set(node, newLabel);
              vv.repaint();
            }
            return;
          }
        }
        Function<? super E, String> es = vv.getRenderContext().getEdgeLabelFunction();
        if (es instanceof MapSettableTransformer) {
          MapSettableTransformer<? super E, String> mst =
              (MapSettableTransformer<? super E, String>) es;
          //    				Layout<N> layout = vv.getGraphLayout();
          // p is the screen point for the mouse event
          Point2D p = e.getPoint();
          // take away the view transform
          Point2D ip =
              vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, p);
          E edge = pickSupport.getEdge(layoutModel, ip.getX(), ip.getY());
          if (edge != null) {
            String newLabel = JOptionPane.showInputDialog("New Edge Label for " + edge);
            if (newLabel != null) {
              mst.set(edge, newLabel);
              vv.repaint();
            }
            return;
          }
        }
      }
      e.consume();
    }
  }

  /**
   * If the mouse is dragging a rectangle, pick the Nodes contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  public void mouseReleased(MouseEvent e) {}

  /**
   * If the mouse is over a picked node, drag all picked nodes with the mouse. If the mouse is not
   * over a Node, draw the rectangle to select multiple Nodes
   */
  public void mousePressed(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}
