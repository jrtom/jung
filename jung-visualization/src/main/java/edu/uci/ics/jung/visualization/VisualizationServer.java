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

import java.awt.Graphics;
import java.awt.RenderingHints.Key;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * @author tom
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public interface VisualizationServer<V, E> {

    /**
     * Specify whether this class uses its offscreen image or not. 
     * 
     * @param doubleBuffered if true, then doubleBuffering in the superclass is set to 'false'
     */
    void setDoubleBuffered(boolean doubleBuffered);

    /**
     * Returns whether this class uses double buffering. The superclass
     * will be the opposite state.
     * @return the double buffered state 
     */
    boolean isDoubleBuffered();

    /**
     * @return the model.
     */
    VisualizationModel<V, E> getModel();

    /**
     * @param model the model for this class to use
     */
    void setModel(VisualizationModel<V, E> model);

    /**
     * In response to changes from the model, repaint the
     * view, then fire an event to any listeners.
     * Examples of listeners are the GraphZoomScrollPane and
     * the BirdsEyeVisualizationViewer
     * @param e the change event
     */
    void stateChanged(ChangeEvent e);

    /**
     * Sets the showing Renderer to be the input Renderer. Also
     * tells the Renderer to refer to this instance
     * as a PickedKey. (Because Renderers maintain a small
     * amount of state, such as the PickedKey, it is important
     * to create a separate instance for each VV instance.)
     * @param r the renderer to use
     */
    void setRenderer(Renderer<V, E> r);

    /**
     * @return the renderer used by this instance.
     */
    Renderer<V, E> getRenderer();

    /**
     * Replaces the current graph layout with {@code layout}.
     * @param layout the new layout to set
     */
    void setGraphLayout(Layout<V, E> layout);

    /**
     * @return the current graph layout.
     */
    Layout<V, E> getGraphLayout();

    /** 
     * Makes the component visible if {@code aFlag} is true, or invisible if false.
     * @param aFlag true iff the component should be visible
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    void setVisible(boolean aFlag);

    /**
     * @return the renderingHints
     */
    Map<Key, Object> getRenderingHints();

    /**
     * @param renderingHints The renderingHints to set.
     */
    void setRenderingHints(Map<Key, Object> renderingHints);

    /**
     * @param paintable The paintable to add.
     */
    void addPreRenderPaintable(Paintable paintable);

    /**
     * @param paintable The paintable to remove.
     */
    void removePreRenderPaintable(Paintable paintable);

    /**
     * @param paintable The paintable to add.
     */
    void addPostRenderPaintable(Paintable paintable);

    /**
     * @param paintable The paintable to remove.
     */
    void removePostRenderPaintable(Paintable paintable);

    /**
     * Adds a <code>ChangeListener</code>.
     * @param l the listener to be added
     */
    void addChangeListener(ChangeListener l);

    /**
     * Removes a ChangeListener.
     * @param l the listener to be removed
     */
    void removeChangeListener(ChangeListener l);

    /**
     * Returns an array of all the <code>ChangeListener</code>s added
     * with addChangeListener().
     *
     * @return all of the <code>ChangeListener</code>s added or an empty
     *         array if no listeners have been added
     */
    ChangeListener[] getChangeListeners();

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created.
     * @see EventListenerList
     */
    void fireStateChanged();

    /**
     * @return the vertex PickedState instance
     */
    PickedState<V> getPickedVertexState();

    /**
     * @return the edge PickedState instance
     */
    PickedState<E> getPickedEdgeState();

    void setPickedVertexState(PickedState<V> pickedVertexState);

    void setPickedEdgeState(PickedState<E> pickedEdgeState);

    /**
     * @return the GraphElementAccessor
     */
    GraphElementAccessor<V, E> getPickSupport();

    /**
     * @param pickSupport The pickSupport to set.
     */
    void setPickSupport(GraphElementAccessor<V, E> pickSupport);

    Point2D getCenter();

    RenderContext<V, E> getRenderContext();

    void setRenderContext(RenderContext<V, E> renderContext);
    
    void repaint();
    
    /**
     * an interface for the preRender and postRender
     */
    interface Paintable {
        public void paint(Graphics g);
        public boolean useTransform();
    }
}