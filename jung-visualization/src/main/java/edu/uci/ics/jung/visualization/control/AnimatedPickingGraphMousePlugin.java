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

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.MultiLayerTransformer.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import javax.swing.JComponent;

/**
 * AnimatedPickingGraphMousePlugin supports the picking of one Graph Node. When the mouse is
 * released, the graph is translated so that the picked Node is moved to the center of the view.
 * This translation is conducted in an animation Thread so that the graph slides to its new position
 *
 * @author Tom Nelson
 */
public class AnimatedPickingGraphMousePlugin<N, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  /** the picked Vertex */
  protected N vertex;

  /** Creates an instance with default modifiers of BUTTON1_MASK and CTRL_MASK */
  public AnimatedPickingGraphMousePlugin() {
    this(InputEvent.BUTTON1_MASK | InputEvent.CTRL_MASK);
  }

  /**
   * Creates an instance with the specified mouse event modifiers.
   *
   * @param selectionModifiers the mouse event modifiers to use.
   */
  public AnimatedPickingGraphMousePlugin(int selectionModifiers) {
    super(selectionModifiers);
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /**
   * If the event occurs on a Vertex, pick that single Vertex
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mousePressed(MouseEvent e) {
    if (e.getModifiers() == modifiers) {
      VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
      LayoutModel<N, Point2D> layoutModel = vv.getModel().getLayoutModel();
      NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
      PickedState<N> pickedNodeState = vv.getPickedNodeState();
      if (pickSupport != null && pickedNodeState != null) {
        // p is the screen point for the mouse event
        Point2D p = e.getPoint();
        vertex = pickSupport.getNode(layoutModel, p.getX(), p.getY());
        if (vertex != null) {
          if (pickedNodeState.isPicked(vertex) == false) {
            pickedNodeState.clear();
            pickedNodeState.pick(vertex, true);
          }
        }
      }
      e.consume();
    }
  }

  /**
   * If a Node was picked in the mousePressed event, start a Thread to animate the translation of
   * the graph so that the picked Node moves to the center of the view
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    if (e.getModifiers() == modifiers) {
      final VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
      Point2D newCenter = null;
      if (vertex != null) {
        // center the picked node
        LayoutModel<N, Point2D> layoutModel = vv.getModel().getLayoutModel();
        newCenter = layoutModel.apply(vertex);
      } else {
        // they did not pick a node to center, so
        // just center the graph
        newCenter = vv.getCenter();
      }
      Point2D lvc =
          vv.getRenderContext().getMultiLayerTransformer().inverseTransform(vv.getCenter());
      final double dx = (lvc.getX() - newCenter.getX()) / 10;
      final double dy = (lvc.getY() - newCenter.getY()) / 10;

      Runnable animator =
          new Runnable() {

            public void run() {
              for (int i = 0; i < 10; i++) {
                vv.getRenderContext()
                    .getMultiLayerTransformer()
                    .getTransformer(Layer.LAYOUT)
                    .translate(dx, dy);
                try {
                  Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
              }
            }
          };
      Thread thread = new Thread(animator);
      thread.start();
    }
  }

  public void mouseClicked(MouseEvent e) {}

  /** show a special cursor while the mouse is inside the window */
  public void mouseEntered(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(cursor);
  }

  /** revert to the default cursor when the mouse leaves this window */
  public void mouseExited(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public void mouseMoved(MouseEvent e) {}

  public void mouseDragged(MouseEvent arg0) {}
}
