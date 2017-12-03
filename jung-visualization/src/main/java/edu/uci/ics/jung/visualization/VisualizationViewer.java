/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.visualization;

import com.google.common.graph.Network;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.MouseListenerTranslator;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.function.Function;
import javax.swing.ToolTipManager;

/**
 * Adds mouse behaviors and tooltips to the graph visualization base class
 *
 * @author Joshua O'Madadhain
 * @author Tom Nelson
 * @author Danyel Fisher
 */
@SuppressWarnings("serial")
public class VisualizationViewer<N, E> extends BasicVisualizationServer<N, E> {

  protected Function<? super N, String> vertexToolTipTransformer;
  protected Function<? super E, String> edgeToolTipTransformer;
  protected Function<MouseEvent, String> mouseEventToolTipTransformer;

  /** provides MouseListener, MouseMotionListener, and MouseWheelListener events to the graph */
  protected GraphMouse graphMouse;

  protected MouseListener requestFocusListener =
      new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          requestFocusInWindow();
        }
      };

  /**
   * @param network the network to render
   * @param size the size for the layout and for the view
   */
  public VisualizationViewer(Network<N, E> network, Dimension size) {
    this(network, size, size);
  }

  /**
   * @param network the network to visualize
   * @param layoutSize the size of the layout area
   * @param viewSize the size of the view area
   */
  public VisualizationViewer(Network<N, E> network, Dimension layoutSize, Dimension viewSize) {
    this(network, null, layoutSize, viewSize);
  }

  /**
   * @param network the network to visualize
   * @param layoutAlgorithm the algorithm to apply
   * @param layoutSize the size for the layout area
   * @param viewSize the size of the window to display the network
   */
  public VisualizationViewer(
      Network<N, E> network,
      LayoutAlgorithm<N, Point2D> layoutAlgorithm,
      Dimension layoutSize,
      Dimension viewSize) {
    this(new BaseVisualizationModel<N, E>(network, layoutAlgorithm, layoutSize), viewSize);
  }

  /**
   * @param network the network to render
   * @param layoutAlgorithm the algorithm to apply
   * @param preferredSize the size to use for both the layout and the screen display
   */
  public VisualizationViewer(
      Network<N, E> network, LayoutAlgorithm<N, Point2D> layoutAlgorithm, Dimension preferredSize) {
    this(new BaseVisualizationModel<N, E>(network, layoutAlgorithm, preferredSize), preferredSize);
  }

  /**
   * @param model the model for the view
   * @param preferredSize the initial size of the window to display the network
   */
  public VisualizationViewer(VisualizationModel<N, E, Point2D> model, Dimension preferredSize) {
    super(model, preferredSize);
    setFocusable(true);
    addMouseListener(requestFocusListener);
  }

  /**
   * a setter for the GraphMouse. This will remove any previous GraphMouse (including the one that
   * is added in the initMouseClicker method.
   *
   * @param graphMouse new value
   */
  public void setGraphMouse(GraphMouse graphMouse) {
    this.graphMouse = graphMouse;
    MouseListener[] ml = getMouseListeners();
    for (int i = 0; i < ml.length; i++) {
      if (ml[i] instanceof GraphMouse) {
        removeMouseListener(ml[i]);
      }
    }
    MouseMotionListener[] mml = getMouseMotionListeners();
    for (int i = 0; i < mml.length; i++) {
      if (mml[i] instanceof GraphMouse) {
        removeMouseMotionListener(mml[i]);
      }
    }
    MouseWheelListener[] mwl = getMouseWheelListeners();
    for (int i = 0; i < mwl.length; i++) {
      if (mwl[i] instanceof GraphMouse) {
        removeMouseWheelListener(mwl[i]);
      }
    }
    addMouseListener(graphMouse);
    addMouseMotionListener(graphMouse);
    addMouseWheelListener(graphMouse);
  }

  /** @return the current <code>GraphMouse</code> */
  public GraphMouse getGraphMouse() {
    return graphMouse;
  }

  /**
   * This is the interface for adding a mouse listener. The GEL will be called back with mouse
   * clicks on vertices.
   *
   * @param gel the mouse listener to add
   */
  public void addGraphMouseListener(GraphMouseListener<N> gel) {
    addMouseListener(new MouseListenerTranslator<N, E>(gel, this));
  }

  /**
   * Override to request focus on mouse enter, if a key listener is added
   *
   * @see java.awt.Component#addKeyListener(java.awt.event.KeyListener)
   */
  @Override
  public synchronized void addKeyListener(KeyListener l) {
    super.addKeyListener(l);
  }

  /** @param edgeToolTipTransformer the edgeToolTipTransformer to set */
  public void setEdgeToolTipTransformer(Function<? super E, String> edgeToolTipTransformer) {
    this.edgeToolTipTransformer = edgeToolTipTransformer;
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /** @param mouseEventToolTipTransformer the mouseEventToolTipTransformer to set */
  public void setMouseEventToolTipTransformer(
      Function<MouseEvent, String> mouseEventToolTipTransformer) {
    this.mouseEventToolTipTransformer = mouseEventToolTipTransformer;
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /** @param vertexToolTipTransformer the vertexToolTipTransformer to set */
  public void setVertexToolTipTransformer(Function<? super N, String> vertexToolTipTransformer) {
    this.vertexToolTipTransformer = vertexToolTipTransformer;
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /** called by the superclass to display tooltips */
  public String getToolTipText(MouseEvent event) {
    LayoutModel<N, Point2D> layoutModel = getModel().getLayoutModel();
    Point2D p = null;
    if (vertexToolTipTransformer != null) {
      p = event.getPoint();
      //renderContext.getBasicTransformer().inverseViewTransform(event.getPoint());
      N vertex = getPickSupport().getNode(layoutModel, p.getX(), p.getY());
      if (vertex != null) {
        return vertexToolTipTransformer.apply(vertex);
      }
    }
    if (edgeToolTipTransformer != null) {
      if (p == null) {
        p = renderContext.getMultiLayerTransformer().inverseTransform(Layer.VIEW, event.getPoint());
      }
      E edge = getPickSupport().getEdge(layoutModel, p.getX(), p.getY());
      if (edge != null) {
        return edgeToolTipTransformer.apply(edge);
      }
    }
    if (mouseEventToolTipTransformer != null) {
      return mouseEventToolTipTransformer.apply(event);
    }
    return super.getToolTipText(event);
  }

  /**
   * a convenience type to represent a class that processes all types of mouse events for the graph
   */
  public interface GraphMouse extends MouseListener, MouseMotionListener, MouseWheelListener {}
}
