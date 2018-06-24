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

import edu.uci.ics.jung.visualization.control.TransformSupport;
import edu.uci.ics.jung.visualization.layout.NetworkElementAccessor;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.spatial.Spatial;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.geom.Point2D;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * @author Tom Nelson
 * @param <N> the node type
 * @param <E> the edge type
 */
public interface VisualizationServer<N, E> {

  /**
   * Specify whether this class uses its offscreen image or not.
   *
   * @param doubleBuffered if true, then doubleBuffering in the superclass is set to 'false'
   */
  void setDoubleBuffered(boolean doubleBuffered);

  /**
   * Returns whether this class uses double buffering. The superclass will be the opposite state.
   *
   * @return the double buffered state
   */
  boolean isDoubleBuffered();

  Shape viewOnLayout();

  Spatial<N> getNodeSpatial();

  void setNodeSpatial(Spatial<N> spatial);

  Spatial<E> getEdgeSpatial();

  void setEdgeSpatial(Spatial<E> spatial);

  TransformSupport<N, E> getTransformSupport();

  /** @return the model. */
  VisualizationModel<N, E> getModel();

  /** @param model the model for this class to use */
  void setModel(VisualizationModel<N, E> model);

  /**
   * In response to changes from the model, repaint the view, then fire an event to any listeners.
   * Examples of listeners are the GraphZoomScrollPane and the BirdsEyeVisualizationViewer
   *
   * @param e the change event
   */
  void stateChanged(ChangeEvent e);

  /**
   * Sets the showing Renderer to be the input Renderer. Also tells the Renderer to refer to this
   * instance as a PickedKey. (Because Renderers maintain a small amount of state, such as the
   * PickedKey, it is important to create a separate instance for each VV instance.)
   *
   * @param r the renderer to use
   */
  void setRenderer(Renderer<N, E> r);

  /** @return the renderer used by this instance. */
  Renderer<N, E> getRenderer();

  /**
   * Makes the component visible if {@code aFlag} is true, or invisible if false.
   *
   * @param aFlag true iff the component should be visible
   * @see javax.swing.JComponent#setVisible(boolean)
   */
  void setVisible(boolean aFlag);

  /** @return the renderingHints */
  Map<Key, Object> getRenderingHints();

  /** @param renderingHints The renderingHints to set. */
  void setRenderingHints(Map<Key, Object> renderingHints);

  /** @param paintable The paintable to add. */
  void addPreRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to remove. */
  void removePreRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to add. */
  void addPostRenderPaintable(Paintable paintable);

  /** @param paintable The paintable to remove. */
  void removePostRenderPaintable(Paintable paintable);

  /**
   * Adds a <code>ChangeListener</code>.
   *
   * @param l the listener to be added
   */
  void addChangeListener(ChangeListener l);

  /**
   * Removes a ChangeListener.
   *
   * @param l the listener to be removed
   */
  void removeChangeListener(ChangeListener l);

  /**
   * Returns an array of all the <code>ChangeListener</code>s added with addChangeListener().
   *
   * @return all of the <code>ChangeListener</code>s added or an empty array if no listeners have
   *     been added
   */
  ChangeListener[] getChangeListeners();

  /**
   * Notifies all listeners that have registered interest for notification on this event type. The
   * event instance is lazily created.
   *
   * @see EventListenerList
   */
  void fireStateChanged();

  /** @return the node PickedState instance */
  PickedState<N> getPickedNodeState();

  /** @return the edge PickedState instance */
  PickedState<E> getPickedEdgeState();

  void setPickedNodeState(PickedState<N> pickedNodeState);

  void setPickedEdgeState(PickedState<E> pickedEdgeState);

  /** @return the NetworkElementAccessor */
  NetworkElementAccessor<N, E> getPickSupport();

  /** @param pickSupport The pickSupport to set. */
  void setPickSupport(NetworkElementAccessor<N, E> pickSupport);

  Point2D getCenter();

  RenderContext<N, E> getRenderContext();

  void setRenderContext(RenderContext<N, E> renderContext);

  void repaint();

  /** an interface for the preRender and postRender */
  interface Paintable {
    public void paint(Graphics g);

    public boolean useTransform();
  }
}
