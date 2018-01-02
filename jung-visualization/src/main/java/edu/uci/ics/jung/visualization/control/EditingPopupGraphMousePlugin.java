package edu.uci.ics.jung.visualization.control;

import com.google.common.graph.MutableNetwork;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
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
 * a plugin that uses popup menus to create nodes, undirected edges, and directed edges.
 *
 * @author Tom Nelson
 */
public class EditingPopupGraphMousePlugin<N, E> extends AbstractPopupGraphMousePlugin {

  protected Supplier<N> nodeFactory;
  protected Supplier<E> edgeFactory;

  public EditingPopupGraphMousePlugin(Supplier<N> nodeFactory, Supplier<E> edgeFactory) {
    this.nodeFactory = nodeFactory;
    this.edgeFactory = edgeFactory;
  }

  @SuppressWarnings({"unchecked", "serial"})
  protected void handlePopup(MouseEvent e) {
    final VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
    final LayoutModel<N> layoutModel = vv.getModel().getLayoutModel();

    final MutableNetwork<N, E> graph = (MutableNetwork<N, E>) vv.getModel().getNetwork();
    final Point2D p = e.getPoint();
    NetworkElementAccessor<N, E> pickSupport = vv.getPickSupport();
    if (pickSupport != null) {

      final N node = pickSupport.getNode(layoutModel, p.getX(), p.getY());
      final E edge = pickSupport.getEdge(layoutModel, p.getX(), p.getY());
      final PickedState<N> pickedNodeState = vv.getPickedNodeState();
      final PickedState<E> pickedEdgeState = vv.getPickedEdgeState();

      JPopupMenu popup = new JPopupMenu();
      if (node != null) {
        Set<N> picked = pickedNodeState.getPicked();
        if (picked.size() > 0) {
          JMenu menu =
              new JMenu("Create " + (graph.isDirected() ? "Directed" : "Undirected") + " Edge");
          popup.add(menu);
          for (final N other : picked) {
            menu.add(
                new AbstractAction("[" + other + "," + node + "]") {
                  public void actionPerformed(ActionEvent e) {
                    graph.addEdge(other, node, edgeFactory.get());
                    vv.repaint();
                  }
                });
          }
        }
        popup.add(
            new AbstractAction("Delete Node") {
              public void actionPerformed(ActionEvent e) {
                pickedNodeState.pick(node, false);
                graph.removeNode(node);
                vv.getNodeSpatial().recalculate();
                vv.repaint();
              }
            });
      } else if (edge != null) {
        popup.add(
            new AbstractAction("Delete Edge") {
              public void actionPerformed(ActionEvent e) {
                pickedEdgeState.pick(edge, false);
                graph.removeEdge(edge);
                vv.getEdgeSpatial().recalculate();
                vv.repaint();
              }
            });
      } else {
        popup.add(
            new AbstractAction("Create Node") {
              public void actionPerformed(ActionEvent e) {
                N newNode = nodeFactory.get();
                graph.addNode(newNode);
                Point2D p2d = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
                vv.getModel().getLayoutModel().set(newNode, p2d.getX(), p2d.getY());
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
