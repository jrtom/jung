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
public class EditingPopupGraphMousePlugin extends AbstractPopupGraphMousePlugin {

  protected Supplier<Object> vertexFactory;
  protected Supplier<Object> edgeFactory;

  public EditingPopupGraphMousePlugin(
      Supplier<Object> vertexFactory, Supplier<Object> edgeFactory) {
    this.vertexFactory = vertexFactory;
    this.edgeFactory = edgeFactory;
  }

  @SuppressWarnings({"unchecked", "serial"})
  protected void handlePopup(MouseEvent e) {
    final VisualizationViewer vv = (VisualizationViewer) e.getSource();
    final MutableNetwork graph = (MutableNetwork) vv.getModel().getLayoutMediator().getNetwork();
    final Point2D p = e.getPoint();
    NetworkElementAccessor pickSupport = vv.getPickSupport();
    if (pickSupport != null) {

      final Object vertex = pickSupport.getNode(p.getX(), p.getY());
      final Object edge = pickSupport.getEdge(p.getX(), p.getY());
      final PickedState pickedVertexState = vv.getPickedVertexState();
      final PickedState pickedEdgeState = vv.getPickedEdgeState();

      JPopupMenu popup = new JPopupMenu();
      if (vertex != null) {
        Set<Object> picked = pickedVertexState.getPicked();
        if (picked.size() > 0) {
          JMenu menu =
              new JMenu("Create " + (graph.isDirected() ? "Directed" : "Undirected") + " Edge");
          popup.add(menu);
          for (final Object other : picked) {
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
                Object newVertex = vertexFactory.get();
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
