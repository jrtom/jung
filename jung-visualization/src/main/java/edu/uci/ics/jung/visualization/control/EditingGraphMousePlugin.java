package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.function.Supplier;
import javax.swing.JComponent;

/**
 * A plugin that can create vertices, undirected edges, and directed edges using mouse gestures.
 *
 * <p>vertexSupport and edgeSupport member classes are responsible for actually creating the new
 * graph elements, and for repainting the view when changes were made.
 *
 * @author Tom Nelson
 */
public class EditingGraphMousePlugin<V, E> extends AbstractGraphMousePlugin
    implements MouseListener, MouseMotionListener {

  protected VertexSupport<V, E> vertexSupport;
  protected EdgeSupport<V, E> edgeSupport;
  private Creating createMode = Creating.UNDETERMINED;

  private enum Creating {
    EDGE,
    VERTEX,
    UNDETERMINED
  }

  /**
   * Creates an instance and prepares shapes for visual effects, using the default modifiers of
   * BUTTON1_MASK.
   *
   * @param vertexFactory for creating vertices
   * @param edgeFactory for creating edges
   */
  public EditingGraphMousePlugin(Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
    this(MouseEvent.BUTTON1_MASK, vertexFactory, edgeFactory);
  }

  /**
   * Creates an instance and prepares shapes for visual effects.
   *
   * @param modifiers the mouse event modifiers to use
   * @param vertexFactory for creating vertices
   * @param edgeFactory for creating edges
   */
  public EditingGraphMousePlugin(
      int modifiers, Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
    super(modifiers);
    this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    this.vertexSupport = new SimpleVertexSupport<V, E>(vertexFactory);
    this.edgeSupport = new SimpleEdgeSupport<V, E>(edgeFactory);
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
   * If the mouse is pressed in an empty area, create a new vertex there. If the mouse is pressed on
   * an existing vertex, prepare to create an edge from that vertex to another
   */
  @SuppressWarnings("unchecked")
  public void mousePressed(MouseEvent e) {
    if (checkModifiers(e)) {
      final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      final Point2D p = e.getPoint();
      NetworkElementAccessor<V, E> pickSupport = vv.getPickSupport();
      if (pickSupport != null) {
        final V vertex = pickSupport.getNode(p.getX(), p.getY());
        if (vertex != null) { // get ready to make an edge
          this.createMode = Creating.EDGE;
          edgeSupport.startEdgeCreate(vv, vertex, e.getPoint());
        } else { // make a new vertex
          this.createMode = Creating.VERTEX;
          vertexSupport.startVertexCreate(vv, e.getPoint());
        }
      }
    }
  }

  /**
   * If startVertex is non-null, and the mouse is released over an existing vertex, create an edge
   * from startVertex to the vertex under the mouse pointer.
   */
  @SuppressWarnings("unchecked")
  public void mouseReleased(MouseEvent e) {
    if (checkModifiers(e)) {
      final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      final Point2D p = e.getPoint();
      if (createMode == Creating.EDGE) {
        NetworkElementAccessor<V, E> pickSupport = vv.getPickSupport();
        V vertex = null;
        // TODO: how does it make any sense for pickSupport to be null in this scenario?
        if (pickSupport != null) {
          vertex = pickSupport.getNode(p.getX(), p.getY());
        }
        if (vertex != null) {
          edgeSupport.endEdgeCreate(vv, vertex);
        }
      } else if (createMode == Creating.VERTEX) {
        vertexSupport.endVertexCreate(vv, e.getPoint());
      }
    }
    createMode = Creating.UNDETERMINED;
  }

  /**
   * If startVertex is non-null, stretch an edge shape between startVertex and the mouse pointer to
   * simulate edge creation
   */
  @SuppressWarnings("unchecked")
  public void mouseDragged(MouseEvent e) {
    if (checkModifiers(e)) {
      VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
      if (createMode == Creating.EDGE) {
        edgeSupport.midEdgeCreate(vv, e.getPoint());
      } else if (createMode == Creating.VERTEX) {
        vertexSupport.midVertexCreate(vv, e.getPoint());
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

  public VertexSupport<V, E> getVertexSupport() {
    return vertexSupport;
  }

  public void setVertexSupport(VertexSupport<V, E> vertexSupport) {
    this.vertexSupport = vertexSupport;
  }

  public EdgeSupport<V, E> edgesupport() {
    return edgeSupport;
  }

  public void setEdgeSupport(EdgeSupport<V, E> edgeSupport) {
    this.edgeSupport = edgeSupport;
  }
}
