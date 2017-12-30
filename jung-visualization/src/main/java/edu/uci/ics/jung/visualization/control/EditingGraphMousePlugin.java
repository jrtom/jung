package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import javax.swing.JComponent;

/**
 * A plugin that can create nodes, undirected edges, and directed edges using mouse gestures.
 *
 * <p>nodeSupport and edgeSupport member classes are responsible for actually creating the new graph
 * elements, and for repainting the view when changes were made.
 *
 * @author Tom Nelson
 */
public class EditingGraphMousePlugin<N, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  protected NodeSupport<N, E> nodeSupport;
  protected EdgeSupport<N, E> edgeSupport;
  private Creating createMode = Creating.UNDETERMINED;

  private enum Creating {
    EDGE,
    NODE,
    UNDETERMINED
  }

  /**
   * Creates an instance and prepares shapes for visual effects, using the default modifiers of
   * BUTTON1_MASK.
   *
   * @param nodeFactory for creating nodes
   * @param edgeFactory for creating edges
   */
  public EditingGraphMousePlugin(Supplier<N> nodeFactory, Supplier<E> edgeFactory) {
    this(MouseEvent.BUTTON1_MASK, nodeFactory, edgeFactory);
  }

  /**
   * Creates an instance and prepares shapes for visual effects.
   *
   * @param modifiers the mouse event modifiers to use
   * @param nodeFactory for creating nodes
   * @param edgeFactory for creating edges
   */
  public EditingGraphMousePlugin(int modifiers, Supplier<N> nodeFactory, Supplier<E> edgeFactory) {
    super(modifiers);
    this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    this.nodeSupport = new SimpleNodeSupport<>(nodeFactory);
    this.edgeSupport = new SimpleEdgeSupport<>(edgeFactory);
  }

  /**
   * Overridden to be more flexible, and pass events with key combinations. The default responds to
   * both ButtonOne and ButtonOne+Shift
   */
  @Override
  public boolean checkModifiers(MouseEvent e) {
    return (e.getModifiers() & modifiers) != 0;
  }

  /**
   * If the mouse is pressed in an empty area, create a new node there. If the mouse is pressed on
   * an existing node, prepare to create an edge from that node to another
   */
  @SuppressWarnings("unchecked")
  public void mousePressed(MouseEvent e) {
    if (checkModifiers(e)) {
      final VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
      //      vv.getNodeSpatial().setActive(true);
      final LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
      final Point2D p = e.getPoint();
      NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
      if (pickSupport != null) {
        final N node = pickSupport.getNode(layoutModel, p.getX(), p.getY());
        if (node != null) { // get ready to make an edge
          this.createMode = Creating.EDGE;
          edgeSupport.startEdgeCreate(vv, node, e.getPoint());
        } else { // make a new node
          this.createMode = Creating.NODE;
          nodeSupport.startNodeCreate(vv, e.getPoint());
        }
      }
    }
  }

  /**
   * If startNode is non-null, and the mouse is released over an existing node, create an edge from
   * startNode to the node under the mouse pointer.
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    if (checkModifiers(e)) {
      final VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
      final LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();
      final Point2D p = e.getPoint();
      if (createMode == Creating.EDGE) {
        NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
        N node = null;
        // TODO: how does it make any sense for pickSupport to be null in this scenario?
        if (pickSupport != null) {
          node = pickSupport.getNode(layoutModel, p.getX(), p.getY());
        }
        if (node != null) {
          edgeSupport.endEdgeCreate(vv, node);
          vv.getEdgeSpatial().recalculate();

        } else {
          edgeSupport.abort(vv);
        }
      } else if (createMode == Creating.NODE) {
        nodeSupport.endNodeCreate(vv, e.getPoint());
        vv.getNodeSpatial().recalculate();
      }
    }
    createMode = Creating.UNDETERMINED;
  }

  /**
   * If startNode is non-null, stretch an edge shape between startNode and the mouse pointer to
   * simulate edge creation
   */
  @SuppressWarnings("unchecked")
  public void mouseDragged(MouseEvent e) {
    if (checkModifiers(e)) {
      VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
      if (createMode == Creating.EDGE) {
        edgeSupport.midEdgeCreate(vv, e.getPoint());
      } else if (createMode == Creating.NODE) {
        nodeSupport.midNodeCreate(vv, e.getPoint());
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

  public NodeSupport<N, E> getNodeSupport() {
    return nodeSupport;
  }

  public void setNodeSupport(NodeSupport<N, E> nodeSupport) {
    this.nodeSupport = nodeSupport;
  }

  public EdgeSupport<N, E> edgesupport() {
    return edgeSupport;
  }

  public void setEdgeSupport(EdgeSupport<N, E> edgeSupport) {
    this.edgeSupport = edgeSupport;
  }
}
