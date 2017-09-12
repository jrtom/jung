package edu.uci.ics.jung.visualization.control;

import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.algorithms.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

/**
 * a plugin that uses popup menus to create vertices, undirected edges, and directed edges.
 *
 * @author Tom Nelson
 */
public class EditingPopupGraphMousePlugin<V, E> extends AbstractPopupGraphMousePlugin {

  protected Supplier<V> vertexFactory;
  protected Supplier<E> edgeFactory;

  public EditingPopupGraphMousePlugin(Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
    this.vertexFactory = vertexFactory;
    this.edgeFactory = edgeFactory;
  }

  @SuppressWarnings({"unchecked", "serial"})
  protected void handlePopup(MouseEvent e) {
    final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
    final MutableNetwork<V, E> graph = (MutableNetwork<V, E>) vv.getModel().getNetwork();
    final Point2D p = e.getPoint();
    NetworkElementAccessor<V, E> pickSupport = vv.getPickSupport();
    if (pickSupport != null) {

      final V vertex = pickSupport.getNode(p.getX(), p.getY());
      final E edge = pickSupport.getEdge(p.getX(), p.getY());
      final PickedState<V> pickedVertexState = vv.getPickedVertexState();
      final PickedState<E> pickedEdgeState = vv.getPickedEdgeState();

      JPopupMenu popup = new JPopupMenu();
      if (vertex != null) {
        Set<V> picked = pickedVertexState.getPicked();
        if (picked.size() > 0) {
          JMenu menu =
              new JMenu("Create " + (graph.isDirected() ? "Directed" : "Undirected") + " Edge");
          popup.add(menu);
          for (final V other : picked) {
            menu.add(
                new AbstractAction("[" + other + "," + vertex + "]") {
                  public void actionPerformed(ActionEvent e) {
                    graph.addEdge(other, vertex, edgeFactory.get());
                    vv.repaint();
                  }
                });
          }
        }
        popup.add(
            new AbstractAction("Delete Vertex") {
              public void actionPerformed(ActionEvent e) {
                pickedVertexState.pick(vertex, false);
                graph.removeNode(vertex);
                vv.repaint();
              }
            });
      } else if (edge != null) {
        popup.add(
            new AbstractAction("Delete Edge") {
              public void actionPerformed(ActionEvent e) {
                pickedEdgeState.pick(edge, false);
                graph.removeEdge(edge);
                vv.repaint();
              }
            });
      } else {
        popup.add(
            new AbstractAction("Create Vertex") {
              public void actionPerformed(ActionEvent e) {
                V newVertex = vertexFactory.get();
                graph.addNode(newVertex);
                vv.getGraphLayout()
                    .setLocation(
                        newVertex,
                        vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p));
                vv.repaint();
              }
            });
      }
      if (popup.getComponentCount() > 0) {
        popup.show(vv, e.getX(), e.getY());
      }
    }
  }
}
