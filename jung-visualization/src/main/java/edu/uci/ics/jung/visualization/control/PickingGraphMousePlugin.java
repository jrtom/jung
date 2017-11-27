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
 * picks a single vertex or edge, and MouseButtonTwo adds to the set of selected Vertices or
 * EdgeType. If a Vertex is selected and the mouse is dragged while on the selected Vertex, then
 * that Vertex will be repositioned to follow the mouse until the button is released.
 *
 * @author Tom Nelson
 */
public class PickingGraphMousePlugin<N, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  private static final Logger log = LoggerFactory.getLogger(PickingGraphMousePlugin.class);
  /** the picked Vertex, if any */
  protected N vertex;

  /** the picked Edge, if any */
  protected E edge;

  /** controls whether the Vertices may be moved with the mouse */
  protected boolean locked;

  /** additional modifiers for the action of adding to an existing selection */
  protected int addToSelectionModifiers;

  /** used to draw a rectangle to contain picked vertices */
  protected Rectangle2D viewRectangle = new Rectangle2D.Float();
  // viewRectangle projected onto the layout coordinate system
  protected Shape layoutTargetShape = viewRectangle;

  /** the Paintable for the lens picking rectangle */
  protected VisualizationServer.Paintable lensPaintable;

  /** color for the picking rectangle */
  protected Color lensColor = Color.cyan;

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
   * a Paintable to draw the rectangle used to pick multiple Vertices
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
   * For primary modifiers (default, MouseButton1): pick a single Vertex or Edge that is under the
   * mouse pointer. If no Vertex or edge is under the pointer, unselect all picked Vertices and
   * edges, and set up to draw a rectangle for multiple selection of contained Vertices. For
   * additional selection (default Shift+MouseButton1): Add to the selection, a single Vertex or
   * Edge that is under the mouse pointer. If a previously picked Vertex or Edge is under the
   * pointer, it is un-picked. If no vertex or Edge is under the pointer, set up to draw a multiple
   * selection rectangle (as above) but do not unpick previously picked elements.
   *
   * @param e the event
   */
  @SuppressWarnings("unchecked")
  public void mousePressed(MouseEvent e) {
    down = e.getPoint();
    VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
    LayoutModel<N, Point2D> layoutModel = vv.getModel().getLayoutModel();
    NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
    PickedState<N> pickedVertexState = vv.getPickedVertexState();
    PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
    if (pickSupport != null && pickedVertexState != null) {
      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();

      // subclass can override to account for view distortion effects
      updatePickingTargets(vv, multiLayerTransformer, down, down);

      // layoutPoint is the mouse event point projected on the layout coordinate system

      // subclass can override to account for view distortion effects
      Point2D layoutPoint = inverseTransform(vv, down);

      if (e.getModifiers() == modifiers) {

        vertex = pickSupport.getNode(layoutModel, layoutPoint);
        if (vertex != null) {
          // picked a vertex
          if (pickedVertexState.isPicked(vertex) == false) {
            pickedVertexState.clear();
            pickedVertexState.pick(vertex, true);
          }

        } else if ((edge = pickSupport.getEdge(layoutModel, layoutPoint)) != null) {
          // picked an edge
          pickedEdgeState.clear();
          pickedEdgeState.pick(edge, true);
        } else {
          // prepare to draw a pick area and clear previous picks
          vv.addPostRenderPaintable(lensPaintable);
          pickedEdgeState.clear();
          pickedVertexState.clear();
        }

      } else if (e.getModifiers() == addToSelectionModifiers) {
        vv.addPostRenderPaintable(lensPaintable);

        vertex = pickSupport.getNode(layoutModel, layoutPoint);
        if (vertex != null) {
          boolean wasThere = pickedVertexState.pick(vertex, !pickedVertexState.isPicked(vertex));
          if (wasThere) {
            vertex = null;
          }
        } else if ((edge = pickSupport.getEdge(layoutModel, layoutPoint)) != null) {
          pickedEdgeState.pick(edge, !pickedEdgeState.isPicked(edge));
        }
      }
    }
    if (vertex != null) {
      e.consume();
    }
  }

  /**
   * If the mouse is dragging a rectangle, pick the Vertices contained in that rectangle
   *
   * <p>clean up settings from mousePressed
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
    MultiLayerTransformer multiLayerTransformer = vv.getRenderContext().getMultiLayerTransformer();

    if (e.getModifiers() == modifiers) {
      if (down != null) {
        Point2D out = e.getPoint();

        if (vertex == null && heyThatsTooClose(down, out, 5) == false) {
          pickContainedVertices(vv, layoutTargetShape, true);
        }
      }
    } else if (e.getModifiers() == this.addToSelectionModifiers) {
      if (down != null) {
        Point2D out = e.getPoint();

        if (vertex == null && heyThatsTooClose(down, out, 5) == false) {
          pickContainedVertices(vv, layoutTargetShape, false);
        }
      }
    }
    down = null;
    vertex = null;
    edge = null;
    viewRectangle.setFrame(0, 0, 0, 0);
    layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);
    vv.removePostRenderPaintable(lensPaintable);
    vv.repaint();
  }

  /**
   * If the mouse is over a picked vertex, drag all picked vertices with the mouse. If the mouse is
   * not over a Vertex, draw the rectangle to select multiple Vertices
   */
  @SuppressWarnings("unchecked")
  public void mouseDragged(MouseEvent e) {
    if (locked == false) {
      VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
      MultiLayerTransformer multiLayerTransformer =
          vv.getRenderContext().getMultiLayerTransformer();

      if (vertex != null) {
        // dragging points and changing their layout locations
        Point p = e.getPoint();
        Point2D graphPoint = multiLayerTransformer.inverseTransform(p);
        Point2D graphDown = multiLayerTransformer.inverseTransform(down);
        VisualizationModel<N, E, Point2D> visualizationModel = vv.getModel();
        LayoutModel<N, Point2D> layoutModel = visualizationModel.getLayoutModel();
        double dx = graphPoint.getX() - graphDown.getX();
        double dy = graphPoint.getY() - graphDown.getY();
        PickedState<N> ps = vv.getPickedVertexState();

        for (N v : ps.getPicked()) {
          Point2D vp = layoutModel.apply(v);
          vp.setLocation(vp.getX() + dx, vp.getY() + dy);
          layoutModel.set(v, vp);
        }
        down = p;

      } else {
        Point2D out = e.getPoint();
        if (e.getModifiers() == this.addToSelectionModifiers || e.getModifiers() == modifiers) {
          updatePickingTargets(vv, multiLayerTransformer, down, out);
        }
      }
      if (vertex != null) {
        e.consume();
      }
      vv.repaint();
    }
  }

  /**
   * rejects picking if the rectangle is too small, like if the user meant to select one vertex but
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
    return multiLayerTransformer.inverseTransform(shape);
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
    viewRectangle.setFrameFromDiagonal(down, out);

    layoutTargetShape = multiLayerTransformer.inverseTransform(viewRectangle);

    if (log.isTraceEnabled()) {
      log.trace("viewRectangle {}", viewRectangle);
      log.trace("layoutTargetShape bounds {}", layoutTargetShape.getBounds());
    }
  }

  /**
   * pick the vertices inside the rectangle created from points 'down' and 'out' (two diagonally
   * opposed corners of the rectangle)
   *
   * @param vv the viewer containing the layout and picked state
   * @param pickTarget - the shape to pick vertices in (layout coordinate system)
   * @param clear whether to reset existing picked state
   */
  protected void pickContainedVertices(
      VisualizationViewer<N, E> vv, Shape pickTarget, boolean clear) {
    PickedState<N> pickedVertexState = vv.getPickedVertexState();

    if (pickedVertexState != null) {
      if (clear) {
        pickedVertexState.clear();
      }
      NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
      LayoutModel<N, Point2D> layoutModel = vv.getModel().getLayoutModel();
      Collection<N> picked = pickSupport.getNodes(layoutModel, pickTarget);
      for (N v : picked) {
        pickedVertexState.pick(v, true);
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
