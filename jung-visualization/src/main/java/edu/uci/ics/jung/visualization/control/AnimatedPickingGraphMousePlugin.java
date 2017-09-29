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

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import javax.swing.JComponent;

/**
 * AnimatedPickingGraphMousePlugin supports the picking of one Graph Vertex. When the mouse is
 * released, the graph is translated so that the picked Vertex is moved to the center of the view.
 * This translation is conducted in an animation Thread so that the graph slides to its new position
 *
 * @author Tom Nelson
 */
public class AnimatedPickingGraphMousePlugin extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  /** the picked Vertex */
  protected Object vertex;

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
      VisualizationViewer vv = (VisualizationViewer) e.getSource();
      NetworkElementAccessor pickSupport = vv.getPickSupport();
      PickedState pickedVertexState = vv.getPickedVertexState();
      if (pickSupport != null && pickedVertexState != null) {
        // p is the screen point for the mouse event
        Point2D p = e.getPoint();
        vertex = pickSupport.getNode(p.getX(), p.getY());
        if (vertex != null) {
          if (pickedVertexState.isPicked(vertex) == false) {
            pickedVertexState.clear();
            pickedVertexState.pick(vertex, true);
          }
        }
      }
      e.consume();
    }
  }

  /**
   * If a Vertex was picked in the mousePressed event, start a Thread to animate the translation of
   * the graph so that the picked Vertex moves to the center of the view
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    if (e.getModifiers() == modifiers) {
      final VisualizationViewer vv = (VisualizationViewer) e.getSource();
      Point2D newCenter = null;
      if (vertex != null) {
        // center the picked vertex
        Layout<Object> layout = vv.getGraphLayout();
        newCenter = layout.apply(vertex);
      } else {
        // they did not pick a vertex to center, so
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
