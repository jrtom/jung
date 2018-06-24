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
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PickingGraphMousePlugin supports the picking of graph elements with the mouse. MouseButtonOne
 * picks a single node or edge, and MouseButtonTwo adds to the set of selected Nodes or EdgeType. If
 * a Node is selected and the mouse is dragged while on the selected Node, then that Node will be
 * repositioned to follow the mouse until the button is released.
 *
 * @author Tom Nelson
 */
public class PickingGraphMousePlugin<N, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(PickingGraphMousePlugin.class);
  /** the picked Node, if any */
  protected N node;

  /** the picked Edge, if any */
  protected E edge;

  /** controls whether the Nodes may be moved with the mouse */
  protected boolean locked;

  /** additional modifiers for the action of adding to an existing selection */
  protected int addToSelectionModifiers;

  /** used to draw a rectangle to contain picked nodes */
  protected Rectangle2D viewRectangle = new Rectangle2D.Float();
  // viewRectangle projected onto the layout coordinate system
  protected Shape layoutTargetShape = viewRectangle;

  /** the Paintable for the lens picking rectangle */
  protected VisualizationServer.Paintable lensPaintable;

  /** color for the picking rectangle */
  protected Color lensColor = Color.cyan;

  protected Point2D deltaDown;

  /** create an instance with default settings */
  public PickingGraphMousePlugin() {
    this(InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK);
  }

  /**
   * create an instance with overides
   *
   * @param selectionModifiers for primary selection
   * @param addToSelectionModifiers for additional selection
   */
  public PickingGraphMousePlugin(int selectionModifiers, int addToSelectionModifiers) {
    super(selectionModifiers);
    this.addToSelectionModifiers = addToSelectionModifiers;
    this.lensPaintable = new LensPaintable();
    this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  }

  /** @return Returns the lensColor. */
  public Color getLensColor() {
    return lensColor;
  }

  /** @param lensColor The lensColor to set. */
  public void setLensColor(Color lensColor) {
    this.lensColor = lensColor;
  }

  /**
   * a Paintable to draw the rectangle used to pick multiple Nodes
   *
   * @author Tom Nelson
   */
  class LensPaintable implements VisualizationServer.Paintable {

    public void paint(Graphics g) {
      Color oldColor = g.getColor();
      g.setColor(lensColor);
      ((Graphics2D) g).draw(viewRectangle);
      g.setColor(oldColor);
    }

    public boolean useTransform() {
      return false;
    }
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
  public void mousePressed(MouseEvent e) {
    down = e.getPoint();
    log.trace("mouse pick at screen coords {}", e.getPoint());
    deltaDown = down;
    VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
    TransformSupport<N, E> transformSupport = vv.getTransformSupport();
    LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
    NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
    PickedState<N> pickedNodeState = vv.getPickedNodeState();
    PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
    if (pickSupport != null && pickedNodeState != null) {
      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();

      // subclass can override to account for view distortion effects
      updatePickingTargets(vv, multiLayerTransformer, down, down);

      // layoutPoint is the mouse event point projected on the layout coordinate system

      // subclass can override to account for view distortion effects
      Point2D layoutPoint = transformSupport.inverseTransform(vv, down);
      log.trace("layout coords of mouse click {}", layoutPoint);
      if (e.getModifiers() == modifiers) {

        node = pickSupport.getNode(layoutModel, layoutPoint.getX(), layoutPoint.getY());
        log.trace("mousePressed set the node to {}", node);
        if (node != null) {
          // picked a node
          if (pickedNodeState.isPicked(node) == false) {
            pickedNodeState.clear();
            pickedNodeState.pick(node, true);
          }

        } else if ((edge = pickSupport.getEdge(layoutModel, layoutPoint)) != null) {
          // picked an edge
          pickedEdgeState.clear();
          pickedEdgeState.pick(edge, true);
        } else {
          // prepare to draw a pick area and clear previous picks
          vv.addPostRenderPaintable(lensPaintable);
          pickedEdgeState.clear();
          pickedNodeState.clear();
        }

      } else if (e.getModifiers() == addToSelectionModifiers) {
        vv.addPostRenderPaintable(lensPaintable);

        node = pickSupport.getNode(layoutModel, layoutPoint.getX(), layoutPoint.getY());
        log.trace("mousePressed with add set the node to {}", node);
        if (node != null) {
          boolean wasThere = pickedNodeState.pick(node, !pickedNodeState.isPicked(node));
          if (wasThere) {
            log.trace("already, so now node will be null");
            node = null;
          }
        } else if ((edge = pickSupport.getEdge(layoutModel, layoutPoint)) != null) {
          pickedEdgeState.pick(edge, !pickedEdgeState.isPicked(edge));
        }
      }
    }
    if (node != null) {
      e.consume();
    }
  }

  /**
   * If the mouse is dragging a rectangle, pick the Nodes contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    Point2D out = e.getPoint();

    VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
    vv.getNodeSpatial().setActive(true);
    vv.getEdgeSpatial().setActive(true);
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();

    if (e.getModifiers() == modifiers) {
      if (down != null) {

        if (node == null && heyThatsTooClose(down, out, 5) == false) {
          pickContainedNodes(vv, layoutTargetShape, true);
        }
      }
    } else if (e.getModifiers() == this.addToSelectionModifiers) {
      if (down != null) {

        if (node == null && heyThatsTooClose(down, out, 5) == false) {
          pickContainedNodes(vv, layoutTargetShape, false);
        }
      }
    }
    log.trace("down:{} out:{}", down, out);
    if (node != null && !down.equals(out)) {

      // dragging points and changing their layout locations
      Point2D graphPoint = multiLayerTransformer.inverseTransform(out);
      log.trace("p in graph coords is {}", graphPoint);
      Point2D graphDown = multiLayerTransformer.inverseTransform(deltaDown);
      log.trace("graphDown (down in graph coords) is {}", graphDown);
      VisualizationModel<N, E> visualizationModel = vv.getModel();
      LayoutModel<N> layoutModel = visualizationModel.getLayoutModel();
      double dx = graphPoint.getX() - graphDown.getX();
      double dy = graphPoint.getY() - graphDown.getY();
      log.trace("dx, dy: {},{}", dx, dy);
      PickedState<N> ps = vv.getPickedNodeState();

      for (N v : ps.getPicked()) {
        Point vp = layoutModel.apply(v);
        vp = Point.of(vp.x + dx, vp.y + dy);
        layoutModel.set(v, vp);
      }
      deltaDown = out;
    }

    down = null;
    node = null;
    edge = null;
    viewRectangle.setFrame(0, 0, 0, 0);
    layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);
    vv.removePostRenderPaintable(lensPaintable);
    vv.repaint();
  }

  /**
   * If the mouse is over a picked node, drag all picked nodes with the mouse. If the mouse is not
   * over a Node, draw the rectangle to select multiple Nodes
   */
  @SuppressWarnings("unchecked")
  public void mouseDragged(MouseEvent e) {
    log.trace("mouseDragged");
    VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
    vv.getNodeSpatial().setActive(false);
    vv.getEdgeSpatial().setActive(false);
    if (locked == false) {

      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();
      Point2D p = e.getPoint();
      log.trace("view p for drag event is {}", p);
      log.trace("down is {}", down);
      if (node != null) {
        // dragging points and changing their layout locations
        Point2D graphPoint = multiLayerTransformer.inverseTransform(p);
        log.trace("p in graph coords is {}", graphPoint);
        Point2D graphDown = multiLayerTransformer.inverseTransform(deltaDown);
        log.trace("graphDown (down in graph coords) is {}", graphDown);
        VisualizationModel<N, E> visualizationModel = vv.getModel();
        LayoutModel<N> layoutModel = visualizationModel.getLayoutModel();
        double dx = graphPoint.getX() - graphDown.getX();
        double dy = graphPoint.getY() - graphDown.getY();
        log.trace("dx, dy: {},{}", dx, dy);
        PickedState<N> ps = vv.getPickedNodeState();

        for (N v : ps.getPicked()) {
          Point vp = layoutModel.apply(v);
          vp = Point.of(vp.x + dx, vp.y + dy);
          layoutModel.set(v, vp);
        }
        deltaDown = p;

      } else {
        Point2D out = e.getPoint();
        if (e.getModifiers() == this.addToSelectionModifiers || e.getModifiers() == modifiers) {
          updatePickingTargets(vv, multiLayerTransformer, down, out);
        }
      }
      if (node != null) {
        e.consume();
      }
      vv.repaint();
    }
  }

  /**
   * rejects picking if the rectangle is too small, like if the user meant to select one node but
   * moved the mouse slightly
   *
   * @param p
   * @param q
   * @param min
   * @return
   */
  private boolean heyThatsTooClose(Point2D p, Point2D q, double min) {
    return Math.abs(p.getX() - q.getX()) < min && Math.abs(p.getY() - q.getY()) < min;
  }

  /**
   * override to consider Lens effects
   *
   * @param vv
   * @param p
   * @return
   */
  protected Point2D inverseTransform(VisualizationViewer<N, E> vv, Point2D p) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.inverseTransform(p);
  }

  /**
   * override to consider Lens effects
   *
   * @param vv
   * @param shape
   * @return
   */
  protected Shape transform(VisualizationViewer<N, E> vv, Shape shape) {
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();
    return multiLayerTransformer.transform(shape);
  }

  /**
   * override to consider Lens effects
   *
   * @param vv
   * @param multiLayerTransformer
   * @param down
   * @param out
   */
  protected void updatePickingTargets(
      VisualizationViewer vv,
      MultiLayerTransformer multiLayerTransformer,
      Point2D down,
      Point2D out) {
    log.trace("updatePickingTargets with {} to {}", down, out);
    viewRectangle.setFrameFromDiagonal(down, out);

    layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);

    if (log.isTraceEnabled()) {
      log.trace("viewRectangle {}", viewRectangle);
      log.trace("layoutTargetShape bounds {}", layoutTargetShape.getBounds());
    }
  }

  /**
   * pick the nodes inside the rectangle created from points 'down' and 'out' (two diagonally
   * opposed corners of the rectangle)
   *
   * @param vv the viewer containing the layout and picked state
   * @param pickTarget - the shape to pick nodes in (layout coordinate system)
   * @param clear whether to reset existing picked state
   */
  protected void pickContainedNodes(VisualizationViewer<N, E> vv, Shape pickTarget, boolean clear) {
    PickedState<N> pickedNodeState = vv.getPickedNodeState();

    if (pickedNodeState != null) {
      if (clear) {
        pickedNodeState.clear();
      }
      NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
      LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
      Collection<N> picked = pickSupport.getNodes(layoutModel, pickTarget);
      for (N v : picked) {
        pickedNodeState.pick(v, true);
      }
    }
  }

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(cursor);
  }

  public void mouseExited(MouseEvent e) {
    JComponent c = (JComponent) e.getSource();
    c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public void mouseMoved(MouseEvent e) {}

  /** @return Returns the locked. */
  public boolean isLocked() {
    return locked;
  }

  /** @param locked The locked to set. */
  public void setLocked(boolean locked) {
    this.locked = locked;
  }
}
